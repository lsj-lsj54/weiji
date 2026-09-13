import { request } from './http'
import type { FocusSession } from './types'

export function startFocus(payload: Partial<FocusSession>) {
  return request<FocusSession>({ method: 'POST', url: '/api/focus/start', data: payload })
}

export function pauseFocus(id: number) {
  return request<FocusSession>({ method: 'POST', url: `/api/focus/${id}/pause` })
}

export function resumeFocus(id: number) {
  return request<FocusSession>({ method: 'POST', url: `/api/focus/${id}/resume` })
}

export function finishFocus(id: number, clientEndTs: number, remark?: string) {
  return request<FocusSession>({
    method: 'POST',
    url: `/api/focus/${id}/finish`,
    data: { clientEndTs, remark },
  })
}

export function abandonFocus(id: number) {
  return request<void>({ method: 'POST', url: `/api/focus/${id}/abandon` })
}

export function liveFocus() {
  return request<FocusSession | null>({ url: '/api/focus/live' })
}
