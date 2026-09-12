import { request } from './http'
import type { KnowledgeNote, KnowledgeReview } from './types'

export function listNotes() {
  return request<KnowledgeNote[]>({ url: '/api/knowledge' })
}

export function dueReview() {
  return request<KnowledgeReview | null>({ url: '/api/knowledge/review/due' })
}
