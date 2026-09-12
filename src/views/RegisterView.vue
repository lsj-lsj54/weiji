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
const nickname = ref('')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await auth.register(phone.value.trim(), password.value, nickname.value.trim() || undefined)
    await router.replace('/now')
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '注册失败'
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
    <p class="kicker" style="margin-top: 32px">从第一条碎片开始</p>
    <h1 class="brand">微积</h1>
    <form class="stack" style="margin-top: 32px" @submit.prevent="submit">
      <input v-model="phone" class="field" inputmode="numeric" autocomplete="username" placeholder="手机号" />
      <input v-model="nickname" class="field" autocomplete="nickname" placeholder="怎么称呼你（可选）" />
      <input
        v-model="password"
        class="field"
        type="password"
        autocomplete="new-password"
        placeholder="密码，6 到 32 位"
      />
      <p v-if="error" class="toast">{{ error }}</p>
      <button class="btn btn--primary" type="submit" :disabled="loading">注册并进入</button>
    </form>
    <p class="hint" style="margin-top: 24px">
      已有账号？
      <router-link class="linkish" to="/login">登录</router-link>
    </p>
  </main>
</template>
