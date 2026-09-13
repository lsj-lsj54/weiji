<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { ApiError, getAccessToken, getApiBase, setApiBase } from '@/api/http'
import { theme } from '@/composables/useTheme'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const phone = ref('')
const password = ref('')
const server = ref('')
const error = ref('')
const loading = ref(false)
const pageClass = computed(() => `page page--auth theme-${theme.value}`)

onShow(() => {
  server.value = getApiBase()
  if (getAccessToken()) {
    uni.switchTab({ url: '/pages/now/now' })
  }
})

async function submit() {
  if (loading.value) return
  error.value = ''
  loading.value = true
  try {
    setApiBase(server.value)
    await auth.login(phone.value.trim(), password.value)
    uni.switchTab({ url: '/pages/now/now' })
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <view :class="pageClass">
    <view class="auth-bar">
      <ThemeToggle />
    </view>
    <view class="kicker" style="margin-top: 32px">把漏掉的分钟攒回来</view>
    <view class="brand">微积</view>
    <view class="hint" style="margin: 16px 0 36px">通勤、等候、下班后。先告诉我你有几分钟。</view>
    <view class="stack">
      <input v-model="phone" class="field" type="number" placeholder="手机号" />
      <input v-model="password" class="field" password placeholder="密码" />
      <input v-model="server" class="field" placeholder="服务器，App 必填。例 http://192.168.1.8:8080" />
      <view v-if="error" class="toast">{{ error }}</view>
      <view class="btn btn--primary" :class="{ 'is-disabled': loading }" @click="submit">进入</view>
    </view>
    <view class="hint" style="margin-top: 24px">
      还没有账号？
      <text class="linkish" @click="uni.navigateTo({ url: '/pages/register/register' })">注册</text>
    </view>
  </view>
</template>
