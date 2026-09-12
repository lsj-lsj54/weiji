export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface TokenPayload {
  accessToken: string
  refreshToken: string
  expiresIn: number
  tokenType: string
}

export interface UserProfile {
  id: number
  phone: string
  nickname: string
  avatar: string
  status: number
}

export interface UserPreference {
  id?: number
  userId?: number
  energyStatus: string
  restDays: string
  dndPeriods: string
  dailyFocusMinutes: number
  weeklyFocusMinutes: number
}

export interface TaskItem {
  id: number
  userId?: number
  templateId?: number
  goalId?: number
  title: string
  content?: string
  category?: string
  priority?: number
  durationMinutes?: number
  sceneCode?: string
  sceneTags?: string
  status?: number
  buffered?: number
  deferredTo?: string
  sourceType?: string
}

export interface FocusSession {
  id: number
  userId?: number
  taskId?: number
  goalId?: number
  title: string
  category?: string
  clientStartTs?: number
  clientEndTs?: number
  serverStartTs?: number
  serverEndTs?: number
  durationSeconds?: number
  pausedSeconds?: number
  lastPauseTs?: number
  sourceType?: string
  status: number
  remark?: string
  pointGranted?: number
}

export interface PointAccount {
  balance: number
  totalEarned: number
  streakDays: number
}

export interface TimeBill {
  from?: string
  to?: string
  totalMinutes: number
  manualMinutes?: number
  matchMinutes?: number
  categoryMinutes?: Record<string, number>
  eveningsEquivalent?: number
  text?: string
  targetMinutes?: number
  progress?: number
}

export interface PointLedger {
  id: number
  changeAmount: number
  bizType: string
  remark?: string
  createdAt?: string
}

export interface KnowledgeNote {
  id: number
  title: string
  content?: string
  mastery?: string
  starred?: number
}

export interface KnowledgeReview {
  id: number
  noteId: number
  intervalDays?: number
  nextReviewAt?: string
}

export interface RankRow {
  rank: number
  userId: number
  nickname: string
  minutes: number
}

export const TASK_STATUS: Record<number, string> = {
  0: '待做',
  1: '进行中',
  2: '已完成',
  3: '今日缓冲',
  4: '已过期',
}

export const SCENES = [
  { code: 'HOME', label: '家' },
  { code: 'COMMUTE', label: '通勤' },
  { code: 'COMPANY', label: '公司' },
  { code: 'LIBRARY', label: '图书馆' },
] as const

export const ENERGIES = [
  { code: 'ENERGETIC', label: '还行' },
  { code: 'TIRED', label: '有点累' },
  { code: 'SLACKING', label: '提不起劲' },
] as const

export const CATEGORIES = ['学习', '生活', '自我提升'] as const
