import { request } from './http'
import type { KnowledgeNote, KnowledgeReview } from './types'

export function listNotes(params?: { tag?: string; mastery?: string; starred?: boolean }) {
  return request<KnowledgeNote[]>({ url: '/api/knowledge', params })
}

export function getNote(id: number) {
  return request<KnowledgeNote>({ url: `/api/knowledge/${id}` })
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

export function updateNote(note: KnowledgeNote, tags?: string[]) {
  return request<KnowledgeNote>({ method: 'PUT', url: '/api/knowledge', data: { note, tags } })
}

export function mergeNotes(fromId: number, toId: number) {
  return request<KnowledgeNote>({ method: 'POST', url: '/api/knowledge/merge', data: { fromId, toId } })
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
