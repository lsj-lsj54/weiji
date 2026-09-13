import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { login as loginApi, logout as logoutApi, register as registerApi } from '@/api/auth'
import { clearTokens, getAccessToken, setTokens } from '@/api/http'
import { fetchMe, fetchPreference } from '@/api/user'
import type { UserPreference, UserProfile } from '@/api/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserProfile | null>(null)
  const preference = ref<UserPreference | null>(null)

  const isAuthed = computed(() => Boolean(getAccessToken()))

  async function bootstrap() {
    if (!getAccessToken()) return
    user.value = await fetchMe()
    preference.value = await fetchPreference()
  }

  async function login(phone: string, password: string) {
    setTokens(await loginApi(phone, password))
    await bootstrap()
  }

  async function register(phone: string, password: string, nickname?: string) {
    setTokens(await registerApi(phone, password, nickname))
    await bootstrap()
  }

  async function logout() {
    try {
      await logoutApi()
    } catch {
      /* still clear locally */
    }
    clearTokens()
    user.value = null
    preference.value = null
  }

  function setPreference(next: UserPreference) {
    preference.value = next
  }

  return { user, preference, isAuthed, bootstrap, login, register, logout, setPreference }
})
