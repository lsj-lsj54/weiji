import { request } from './http'
import type { TokenPayload } from './types'

export function login(phone: string, password: string) {
  return request<TokenPayload>({ method: 'POST', url: '/api/auth/login', data: { phone, password } })
}

export function register(phone: string, password: string, nickname?: string) {
  return request<TokenPayload>({
    method: 'POST',
    url: '/api/auth/register',
    data: { phone, password, nickname },
  })
}

export function logout() {
  return request<void>({ method: 'POST', url: '/api/auth/logout' })
}
