<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { ApiError, getApiBase, setApiBase } from '@/api/http'
import { theme } from '@/composables/useTheme'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const phone = ref('')
const password = ref('')
const nickname = ref('')
const server = ref('')
const error = ref('')
const loading = ref(false)
const pageClass = computed(() => `page page--auth theme-${theme.value}`)

onShow(() => {
  server.value = getApiBase()
})

async function submit() {
  if (loading.value) return
  error.value = ''
  loading.value = true
  try {
    setApiBase(server.value)
    await auth.register(phone.value.trim(), password.value, nickname.value.trim() || undefined)
    uni.switchTab({ url: '/pages/now/now' })
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : '注册失败'
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
    <view class="kicker" style="margin-top: 32px">从第一条碎片开始</view>
    <view class="brand">微积</view>
    <view class="stack" style="margin-top: 32px">
      <input v-model="phone" class="field" type="number" placeholder="手机号" />
      <input v-model="nickname" class="field" placeholder="怎么称呼你（可选）" />
      <input v-model="password" class="field" password placeholder="密码，6 到 32 位" />
      <input v-model="server" class="field" placeholder="服务器，App 必填。例 http://192.168.1.8:8080" />
      <view v-if="error" class="toast">{{ error }}</view>
      <view class="btn btn--primary" :class="{ 'is-disabled': loading }" @click="submit">注册并进入</view>
    </view>
    <view class="hint" style="margin-top: 24px">
      已有账号？
      <text class="linkish" @click="uni.navigateBack()">登录</text>
    </view>
  </view>
</template>
