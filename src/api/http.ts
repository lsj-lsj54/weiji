import axios, { type AxiosRequestConfig } from 'axios'
import type { ApiResult, TokenPayload } from './types'

const TOKEN_KEY = 'weiji.access'
const REFRESH_KEY = 'weiji.refresh'

export function getAccessToken() {
  return localStorage.getItem(TOKEN_KEY) ?? ''
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_KEY) ?? ''
}

export function setTokens(payload: TokenPayload) {
  localStorage.setItem(TOKEN_KEY, payload.accessToken)
  localStorage.setItem(REFRESH_KEY, payload.refreshToken)
}

export function clearTokens() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
}

export class ApiError extends Error {
  code: number
  constructor(code: number, message: string) {
    super(message)
    this.code = code
  }
}

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '',
  timeout: 20000,
})

http.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let refreshing: Promise<boolean> | null = null

async function refreshAccess(): Promise<boolean> {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return false
  try {
    const { data } = await axios.post<ApiResult<TokenPayload>>(
      `${import.meta.env.VITE_API_BASE || ''}/api/auth/refresh`,
      { refreshToken },
    )
    if (data.code !== 0 || !data.data) return false
    setTokens(data.data)
    return true
  } catch {
    return false
  }
}

http.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResult<unknown>
    if (body && typeof body.code === 'number' && body.code !== 0) {
      return Promise.reject(new ApiError(body.code, body.message || '请求失败'))
    }
    return response
  },
  async (error) => {
    const status = error.response?.status
    const original = error.config as AxiosRequestConfig & { _retry?: boolean }
    if (status === 401 && original && !original._retry && !String(original.url || '').includes('/api/auth/')) {
      original._retry = true
      if (!refreshing) {
        refreshing = refreshAccess().finally(() => {
          refreshing = null
        })
      }
      const ok = await refreshing
      if (ok) {
        return http(original)
      }
      clearTokens()
      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign('/login')
      }
    }
    const body = error.response?.data as ApiResult<unknown> | undefined
    if (body?.message) {
      return Promise.reject(new ApiError(body.code ?? status ?? 0, body.message))
    }
    return Promise.reject(new ApiError(status ?? 0, '网络不通，确认后端是否已启动'))
  },
)

export async function request<T>(config: AxiosRequestConfig): Promise<T> {
  const { data } = await http.request<ApiResult<T>>(config)
  return data.data
}

export default http
