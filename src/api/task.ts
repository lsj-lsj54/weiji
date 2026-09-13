import { request } from './http'
import type { DelayCurve, TaskItem, TaskTemplate } from './types'

export function listTasks(status?: number) {
  return request<TaskItem[]>({ url: '/api/task', params: status === undefined ? {} : { status } })
}

export function createTask(payload: Partial<TaskItem>) {
  return request<TaskItem>({ method: 'POST', url: '/api/task', data: payload })
}

export function updateTask(payload: Partial<TaskItem>) {
  return request<TaskItem>({ method: 'PUT', url: '/api/task', data: payload })
}

export function importTasks(tasks: Partial<TaskItem>[]) {
  return request<TaskItem[]>({ method: 'POST', url: '/api/task/import', data: tasks })
}

export function preloadTasks(payload: { idleMinutes: number; sceneCode?: string }) {
  return request<TaskItem[]>({ method: 'POST', url: '/api/task/preload', data: payload })
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

export function expireTasks(ids: number[]) {
  return request<void>({ method: 'POST', url: '/api/task/expire', data: { ids } })
}

export function listTemplates() {
  return request<TaskTemplate[]>({ url: '/api/task/templates' })
}

export function saveTemplate(payload: Partial<TaskTemplate>) {
  return request<TaskTemplate>({ method: 'POST', url: '/api/task/templates', data: payload })
}

export function applyTemplate(id: number) {
  return request<TaskItem>({ method: 'POST', url: `/api/task/templates/${id}/apply` })
}

export function delayCurve() {
  return request<DelayCurve>({ url: '/api/task/delay-curve' })
}
