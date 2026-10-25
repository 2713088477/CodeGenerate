import { login as loginApi, logout as logoutApi, register as registerApi } from '@/api'
import type { LoginRequest, RegisterRequest, UserProfileVo } from '@/api'
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

const STORAGE_KEY = 'yigou-profile'

type ApiEnvelope = {
  code?: number
  message?: string
  data?: unknown
}

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

function readEnvelope(result: unknown): ApiEnvelope | undefined {
  if (!result || typeof result !== 'object') {
    return undefined
  }
  const record = result as { data?: ApiEnvelope; response?: { data?: ApiEnvelope } }
  if (record.data && typeof record.data === 'object' && 'code' in record.data) {
    return record.data
  }
  return record.response?.data
}

function readProfileData(result: unknown): UserProfileVo {
  const body = readEnvelope(result)
  if (body?.code === 0 && body.data && typeof body.data === 'object') {
    return body.data as UserProfileVo
  }
  throw new Error(body?.message || '请求失败')
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
    profile.value = readProfileData(await loginApi({ body }))
  }

  async function register(body: RegisterRequest) {
    profile.value = readProfileData(await registerApi({ body }))
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
