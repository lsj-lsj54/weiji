import { request } from './http'
import type { PointAccount, PointLedger, RewardItem, TimeBill, UserBadge, UserItem } from './types'

export function fetchAccount() {
  return request<PointAccount>({ url: '/api/point/account' })
}

export function fetchLedgers(limit = 12) {
  return request<PointLedger[]>({ url: '/api/point/ledgers', params: { limit } })
}

export function fetchTimeBill(range: 'day' | 'week' | 'month' = 'day') {
  return request<TimeBill>({ url: '/api/point/time-bill', params: { range } })
}

export function fetchFocusBoard(range: 'day' | 'week' = 'day') {
  return request<TimeBill>({ url: '/api/point/focus-board', params: { range } })
}

export function fetchEveningSummary() {
  return request<TimeBill>({ url: '/api/point/evening-summary' })
}

export function listRewards() {
  return request<RewardItem[]>({ url: '/api/point/rewards' })
}

export function createReward(payload: Partial<RewardItem>) {
  return request<RewardItem>({ method: 'POST', url: '/api/point/rewards', data: payload })
}

export function redeemReward(id: number) {
  return request<unknown>({ method: 'POST', url: `/api/point/rewards/${id}/redeem` })
}

export function donatePoints(amount: number) {
  return request<void>({ method: 'POST', url: '/api/point/donate', data: { amount } })
}

export function listItems() {
  return request<UserItem[]>({ url: '/api/point/items' })
}

export function listBadges() {
  return request<UserBadge[]>({ url: '/api/point/badges' })
}
