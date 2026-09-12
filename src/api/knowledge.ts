import { request } from './http'
import type { KnowledgeNote, KnowledgeReview } from './types'

export function listNotes() {
  return request<KnowledgeNote[]>({ url: '/api/knowledge' })
}

export function createNote(title: string, content: string, tags: string[]) {
  return request<KnowledgeNote>({
    method: 'POST',
    url: '/api/knowledge',
    data: {
      note: { title, content, mediaType: 'TEXT', mastery: 'UNKNOWN', starred: 0 },
      tags,
    },
  })
}

export function deleteNote(id: number) {
  return request<void>({ method: 'DELETE', url: `/api/knowledge/${id}` })
}

export function dueReview() {
  return request<KnowledgeReview | null>({ url: '/api/knowledge/review/due' })
}

export function markReview(id: number, remembered: boolean) {
  return request<KnowledgeReview>({
    method: 'POST',
    url: `/api/knowledge/review/${id}/mark`,
    data: { remembered },
  })
}

export function weeklySummary() {
  return request<{ count: number; byTag: Record<string, string[]> }>({ url: '/api/knowledge/weekly-summary' })
}

export function exportNotes() {
  return request<string>({ url: '/api/knowledge/export' })
}
