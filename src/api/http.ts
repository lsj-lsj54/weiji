import type { ApiResult, TokenPayload } from './types'

const TOKEN_KEY = 'weiji.access'
const REFRESH_KEY = 'weiji.refresh'

export function getAccessToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

export function getRefreshToken() {
  return uni.getStorageSync(REFRESH_KEY) || ''
}

export function setTokens(payload: TokenPayload) {
  uni.setStorageSync(TOKEN_KEY, payload.accessToken)
  uni.setStorageSync(REFRESH_KEY, payload.refreshToken)
}

export function clearTokens() {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(REFRESH_KEY)
}

export class ApiError extends Error {
  code: number
  constructor(code: number, message: string) {
    super(message)
    this.code = code
  }
}

export interface RequestOptions {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: unknown
  params?: Record<string, string | number | boolean | undefined>
  _retry?: boolean
}

function apiBase() {
  return import.meta.env.VITE_API_BASE || ''
}

function withQuery(url: string, params?: RequestOptions['params']) {
  if (!params) return url
  const parts: string[] = []
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === '') return
    parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
  })
  if (!parts.length) return url
  return `${url}${url.includes('?') ? '&' : '?'}${parts.join('&')}`
}

function goLogin() {
  const pages = getCurrentPages()
  const route = pages.length ? pages[pages.length - 1].route || '' : ''
  if (route.includes('pages/login/login') || route.includes('pages/register/register')) return
  uni.reLaunch({ url: '/pages/login/login' })
}

type HttpResult = { statusCode: number; data: unknown }

function rawRequest(opts: RequestOptions, token?: string): Promise<HttpResult> {
  const url = apiBase() + withQuery(opts.url, opts.params)
  const header: Record<string, string> = {
    'Content-Type': 'application/json',
  }
  const auth = token ?? getAccessToken()
  if (auth) header.Authorization = `Bearer ${auth}`
  return new Promise((resolve, reject) => {
    uni.request({
      url,
      method: opts.method || 'GET',
      data: opts.data as Record<string, unknown> | undefined,
      header,
      timeout: 20000,
      success: (res) => {
        resolve({ statusCode: res.statusCode || 0, data: res.data })
      },
      fail: () => {
        reject(new ApiError(0, '网络不通，确认后端是否已启动'))
      },
    })
  })
}

let refreshing: Promise<boolean> | null = null

async function refreshAccess(): Promise<boolean> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return false
  try {
    const res = await rawRequest(
      { url: '/api/auth/refresh', method: 'POST', data: { refreshToken } },
      '',
    )
    const body = res.data as ApiResult<TokenPayload>
    if (res.statusCode >= 400 || body.code !== 0 || !body.data) return false
    setTokens(body.data)
    return true
  } catch {
    return false
  }
}

export async function request<T>(opts: RequestOptions): Promise<T> {
  const res = await rawRequest(opts)
  if (res.statusCode === 401 && !opts._retry && !opts.url.includes('/api/auth/')) {
    if (!refreshing) {
      refreshing = refreshAccess().finally(() => {
        refreshing = null
      })
    }
    const ok = await refreshing
    if (ok) {
      return request<T>({ ...opts, _retry: true })
    }
    clearTokens()
    goLogin()
    throw new ApiError(401, '请重新登录')
  }
  const body = res.data as ApiResult<T>
  if (body && typeof body.code === 'number' && body.code !== 0) {
    throw new ApiError(body.code, body.message || '请求失败')
  }
  if (res.statusCode >= 400) {
    throw new ApiError(res.statusCode, body?.message || '请求失败')
  }
  return body?.data as T
}

export function errMsg(e: unknown, fallback: string) {
  return e instanceof ApiError ? e.message : fallback
}
