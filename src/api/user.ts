import { request } from './http'
import type { TaskItem, UserGoal, UserPreference, UserProfile } from './types'

export function fetchMe() {
  return request<UserProfile>({ url: '/api/user/me' })
}

export function searchUser(phone: string) {
  return request<UserProfile>({ url: '/api/user/search', params: { phone } })
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

export function listGoals() {
  return request<UserGoal[]>({ url: '/api/user/goals' })
}

export function createGoal(payload: Partial<UserGoal>) {
  return request<UserGoal>({ method: 'POST', url: '/api/user/goals', data: payload })
}

export function decomposeGoal(id: number, chunkMinutes = 15) {
  return request<TaskItem[]>({
    method: 'POST',
    url: `/api/user/goals/${id}/decompose`,
    data: { chunkMinutes },
  })
}
