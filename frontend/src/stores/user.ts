import { login as loginApi, logout as logoutApi, register as registerApi } from '@/api'
import type { LoginRequest, RegisterRequest, UserProfileVo } from '@/api'
import { readData } from '@/utils/response'
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

const STORAGE_KEY = 'yigou-profile'

function loadProfile(): UserProfileVo | null {
  const raw = sessionStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as UserProfileVo
  } catch {
    sessionStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const profile = ref<UserProfileVo | null>(loadProfile())

  watch(profile, (value) => {
    if (value) {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(value))
      return
    }
    sessionStorage.removeItem(STORAGE_KEY)
  })

  async function login(body: LoginRequest) {
    profile.value = readData<UserProfileVo>(await loginApi({ body }))
  }

  async function register(body: RegisterRequest) {
    profile.value = readData<UserProfileVo>(await registerApi({ body }))
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      profile.value = null
    }
  }

  return { profile, login, register, logout }
})
