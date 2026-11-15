import { ElMessage } from 'element-plus'
import { createRouter, createWebHistory } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'
import { hasAdminPermission } from '@/router/permission'
import { useUserStore } from '@/stores/user'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAdmin?: boolean
  }
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/pages/auth/index.vue'),
    },
    {
      // 应用工作台自带顶栏,占满整个窗口,不放在 BasicLayout 里
      path: '/app/:id',
      name: 'appDetail',
      component: () => import('@/pages/app/index.vue'),
    },
    {
      path: '/',
      component: BasicLayout,
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/pages/home/index.vue'),
        },
        {
          path: 'user',
          name: 'userManage',
          meta: { requiresAdmin: true },
          component: () => import('@/pages/user/index.vue'),
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  if (!to.matched.some((record) => record.meta.requiresAdmin)) {
    return
  }

  const userStore = useUserStore()
  if (!userStore.profile) {
    return {
      name: 'login',
      query: { redirect: to.fullPath },
    }
  }
  if (!hasAdminPermission(userStore.profile.role)) {
    ElMessage.warning('没有权限访问')
    return { name: 'home' }
  }
})

export default router
