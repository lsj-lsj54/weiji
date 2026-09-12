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

export interface UserGoal {
  id: number
  title: string
  description?: string
  targetMinutes?: number
  finishedMinutes?: number
  status?: number
}

export interface TaskTemplate {
  id: number
  userId?: number
  name: string
  content?: string
  category?: string
  durationMinutes?: number
  sceneCode?: string
}

export interface DelayCurve {
  points: number[]
  completionRate: number
}

export interface RewardItem {
  id: number
  name: string
  level?: string
  pointCost?: number
  lockMode?: number
  cooldownHours?: number
  itemCode?: string
}

export interface UserItem {
  id: number
  itemCode: string
  quantity: number
}

export interface UserBadge {
  id: number
  badgeId: number
  code?: string
  name?: string
  description?: string
}

export interface RankRow {
  rank: number
  userId: number
  nickname: string
  minutes: number
}

export interface FriendRow {
  id: number
  userId: number
  friendId: number
  nickname?: string
  phone?: string
}

export interface PlazaPost {
  id: number
  title: string
  content?: string
  anonymous?: number
  templateId?: number
}

export interface TeamRow {
  id: number
  name: string
  goalDesc?: string
  ownerId?: number
}

export interface TeamMember {
  id: number
  teamId: number
  userId: number
  finishedCount?: number
}

export interface DeskSession {
  id: number
  ownerId: number
  peerId: number
  status: string
}

export interface FriendRequest {
  id: number
  fromUserId: number
  toUserId: number
  status: string
  nickname?: string
  phone?: string
}

export interface TeamCard {
  id: number
  name: string
  goalDesc?: string
  ownerId?: number
  finishedCount?: number
  members?: TeamMember[]
}

export const WEEKDAYS = [
  { v: 0, l: '日' },
  { v: 1, l: '一' },
  { v: 2, l: '二' },
  { v: 3, l: '三' },
  { v: 4, l: '四' },
  { v: 5, l: '五' },
  { v: 6, l: '六' },
] as const

export const REWARD_LEVELS = ['NORMAL', 'MID', 'HIGH', 'MILESTONE'] as const

export const ITEM_LABEL: Record<string, string> = {
  SKIP_CARD: '跳过卡',
  DAY_EXEMPT: '当日豁免',
  CHARITY: '公益凭证',
  CUSTOM: '自定义',
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
