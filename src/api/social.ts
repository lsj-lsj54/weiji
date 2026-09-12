import { request } from './http'
import type { RankRow } from './types'

export function weekRank() {
  return request<RankRow[]>({ url: '/api/social/rank/week' })
}
