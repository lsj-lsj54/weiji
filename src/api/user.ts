import { request } from './http'
import type { UserPreference, UserProfile } from './types'

export function fetchMe() {
  return request<UserProfile>({ url: '/api/user/me' })
}

export function updateProfile(payload: { nickname?: string; avatar?: string }) {
  return request<UserProfile>({ method: 'PUT', url: '/api/user/profile', data: payload })
}

export function fetchPreference() {
  return request<UserPreference>({ url: '/api/user/preference' })
}

export function updatePreference(payload: Partial<UserPreference>) {
  return request<UserPreference>({ method: 'PUT', url: '/api/user/preference', data: payload })
}
