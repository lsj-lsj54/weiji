import { request } from './http'
import type { PointAccount, PointLedger, TimeBill } from './types'

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
