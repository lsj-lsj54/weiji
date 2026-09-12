import { createRouter, createWebHistory } from 'vue-router'
import { getAccessToken } from '@/api/http'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
    { path: '/register', component: () => import('@/views/RegisterView.vue'), meta: { public: true } },
    { path: '/', redirect: '/now' },
    { path: '/now', component: () => import('@/views/NowView.vue') },
    { path: '/tasks', component: () => import('@/views/TasksView.vue') },
    { path: '/points', component: () => import('@/views/PointsView.vue') },
    { path: '/knowledge', component: () => import('@/views/KnowledgeView.vue') },
    { path: '/social', component: () => import('@/views/SocialView.vue') },
    { path: '/me', component: () => import('@/views/MeView.vue') },
  ],
})

router.beforeEach((to) => {
  if (to.meta.public) return true
  if (!getAccessToken()) return '/login'
  return true
})

export default router
