<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { ApiError } from '@/api/http'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const phone = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(phone.value.trim(), password.value)
    await router.replace('/now')
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="screen screen--auth">
    <div class="auth-bar">
      <ThemeToggle />
    </div>
    <p class="kicker" style="margin-top: 32px">把漏掉的分钟攒回来</p>
    <h1 class="brand">微积</h1>
    <p class="hint" style="margin: 16px 0 36px">通勤、等候、下班后。先告诉我你有几分钟。</p>
    <form class="stack" @submit.prevent="submit">
      <input v-model="phone" class="field" inputmode="numeric" autocomplete="username" placeholder="手机号" />
      <input v-model="password" class="field" type="password" autocomplete="current-password" placeholder="密码" />
      <p v-if="error" class="toast">{{ error }}</p>
      <button class="btn btn--primary" type="submit" :disabled="loading">进入</button>
    </form>
    <p class="hint" style="margin-top: 24px">
      还没有账号？
      <router-link class="linkish" to="/register">注册</router-link>
    </p>
  </main>
</template>
