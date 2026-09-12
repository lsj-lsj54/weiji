import { request } from './http'
import type { TaskItem } from './types'

export function listTasks(status?: number) {
  return request<TaskItem[]>({ url: '/api/task', params: { status } })
}

export function createTask(payload: Partial<TaskItem>) {
  return request<TaskItem>({ method: 'POST', url: '/api/task', data: payload })
}

export function matchTasks(payload: {
  idleMinutes: number
  sceneCode?: string
  category?: string
  energyStatus?: string
}) {
  return request<TaskItem[]>({ method: 'POST', url: '/api/task/match', data: payload })
}

export function bufferTask(id: number) {
  return request<void>({ method: 'POST', url: `/api/task/${id}/buffer` })
}

export function completeTask(id: number, startDelaySeconds?: number) {
  return request<TaskItem>({
    method: 'POST',
    url: `/api/task/${id}/complete`,
    data: { startDelaySeconds },
  })
}

export function skipTask(id: number) {
  return request<TaskItem>({ method: 'POST', url: `/api/task/${id}/skip` })
}
