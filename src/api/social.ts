import { request } from './http'
import type {
  DeskSession,
  FriendRequest,
  FriendRow,
  PlazaPost,
  RankRow,
  TeamMember,
  TeamRow,
} from './types'

export function weekRank() {
  return request<RankRow[]>({ url: '/api/social/rank/week' })
}

export function listFriends() {
  return request<FriendRow[]>({ url: '/api/social/friends' })
}

export function sendFriendRequest(userId: number) {
  return request<FriendRequest>({ method: 'POST', url: '/api/social/friends/requests', data: { userId } })
}

export function handleFriendRequest(id: number, accept: boolean) {
  return request<void>({
    method: 'POST',
    url: `/api/social/friends/requests/${id}/handle`,
    data: { accept },
  })
}

export function listPlaza() {
  return request<PlazaPost[]>({ url: '/api/social/plaza' })
}

export function postPlaza(title: string, content: string) {
  return request<PlazaPost>({
    method: 'POST',
    url: '/api/social/plaza',
    data: { title, content, anonymous: 1 },
  })
}

export function favoritePlaza(id: number) {
  return request<void>({ method: 'POST', url: `/api/social/plaza/${id}/favorite` })
}

export function createTeam(name: string, goalDesc: string, memberId?: number) {
  return request<TeamRow>({
    method: 'POST',
    url: '/api/social/teams',
    data: { name, goalDesc, memberId },
  })
}

export function teamCheckin(id: number) {
  return request<TeamMember>({ method: 'POST', url: `/api/social/teams/${id}/checkin` })
}

export function teamProgress(id: number) {
  return request<TeamMember[]>({ url: `/api/social/teams/${id}/progress` })
}

export function createDesk(userId: number) {
  return request<DeskSession>({ method: 'POST', url: '/api/social/desk', data: { userId } })
}

export function startDesk(id: number) {
  return request<DeskSession>({ method: 'POST', url: `/api/social/desk/${id}/start` })
}

export function reportDesk(id: number, done: boolean, report: string) {
  return request<DeskSession>({
    method: 'POST',
    url: `/api/social/desk/${id}/report`,
    data: { done, report },
  })
}

export function getDesk(id: number) {
  return request<DeskSession>({ url: `/api/social/desk/${id}` })
}
