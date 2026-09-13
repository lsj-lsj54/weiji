import { onShow } from '@dcloudio/uni-app'
import { getAccessToken } from '@/api/http'

export function useAuthGuard() {
  onShow(() => {
    if (!getAccessToken()) {
      uni.reLaunch({ url: '/pages/login/login' })
    }
  })
}
