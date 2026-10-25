<script setup lang="ts">
import { Lock, User } from '@element-plus/icons-vue'
import type { FormInstance, FormItemRule, FormRules } from 'element-plus'
import { ElButton, ElForm, ElFormItem, ElInput, ElMessage, ElTabPane, ElTabs } from 'element-plus'
import { computed, reactive, shallowRef, useTemplateRef } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { LoginRequest, RegisterRequest } from '@/api'
import { useUserStore } from '@/stores/user'
import { errorMessage } from '@/utils/response'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeTab = shallowRef<'login' | 'register'>('login')
const submitting = shallowRef(false)
const loginFormRef = useTemplateRef<FormInstance>('loginFormRef')
const registerFormRef = useTemplateRef<FormInstance>('registerFormRef')

const loginForm = reactive<LoginRequest>({
  userAccount: '',
  userPassword: '',
})

const registerForm = reactive<RegisterRequest>({
  userAccount: '',
  userPassword: '',
  confirmPassword: '',
})

const heading = computed(() => (activeTab.value === 'login' ? '登录艺构' : '注册艺构'))

const validateConfirmPassword: FormItemRule['validator'] = (_rule, value, callback) => {
  if (value === '') {
    callback(new Error('确认密码不能为空'))
  } else if (value !== registerForm.userPassword) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const accountRules: FormItemRule[] = [
  { required: true, whitespace: true, message: '账号不能为空', trigger: 'blur' },
  { max: 32, message: '账号长度不能超过 32', trigger: 'blur' },
]

const passwordRules: FormItemRule[] = [
  { required: true, message: '密码不能为空', trigger: 'blur' },
  { min: 6, max: 64, message: '密码长度为 6 到 64 位', trigger: 'blur' },
]

const loginRules = reactive<FormRules<LoginRequest>>({
  userAccount: accountRules,
  userPassword: passwordRules,
})

const registerRules = reactive<FormRules<RegisterRequest>>({
  userAccount: accountRules,
  userPassword: passwordRules,
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
})

function redirectAfterAuth() {
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect.startsWith('/') && !redirect.startsWith('//')) {
    return router.push(redirect)
  }
  return router.push({ name: 'home' })
}

async function submitLogin() {
  if (submitting.value || !loginFormRef.value) {
    return
  }
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await userStore.login({
      userAccount: loginForm.userAccount.trim(),
      userPassword: loginForm.userPassword,
    })
    ElMessage.success('登录成功')
    await redirectAfterAuth()
  } catch (error) {
    ElMessage.error(errorMessage(error))
  } finally {
    submitting.value = false
  }
}

async function submitRegister() {
  if (submitting.value || !registerFormRef.value) {
    return
  }
  try {
    await registerFormRef.value.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    await userStore.register({
      userAccount: registerForm.userAccount.trim(),
      userPassword: registerForm.userPassword,
      confirmPassword: registerForm.confirmPassword,
    })
    ElMessage.success('注册成功')
    await redirectAfterAuth()
  } catch (error) {
    ElMessage.error(errorMessage(error))
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <section class="panel">
      <div class="panel-card">
        <router-link class="card-brand" to="/" aria-label="回到首页">
          <img class="card-logo" src="/logo.png" alt="" />
          <span class="card-name">艺构</span>
        </router-link>
        <h2 class="panel-title">{{ heading }}</h2>
        <p class="panel-lead">{{ activeTab === 'login' ? '使用账号和密码继续' : '创建一个新账号' }}</p>

        <el-tabs v-model="activeTab" class="auth-tabs" stretch>
          <el-tab-pane label="登录" name="login">
            <el-form
              ref="loginFormRef"
              class="auth-form"
              :model="loginForm"
              :rules="loginRules"
              label-width="0"
              @submit.prevent="submitLogin"
            >
              <el-form-item prop="userAccount">
                <el-input
                  v-model="loginForm.userAccount"
                  size="large"
                  placeholder="请输入账号"
                  aria-label="账号"
                  autocomplete="username"
                  maxlength="32"
                  clearable
                  :prefix-icon="User"
                />
              </el-form-item>
              <el-form-item prop="userPassword">
                <el-input
                  v-model="loginForm.userPassword"
                  size="large"
                  type="password"
                  placeholder="请输入密码"
                  aria-label="密码"
                  autocomplete="current-password"
                  maxlength="64"
                  show-password
                  :prefix-icon="Lock"
                />
              </el-form-item>
              <el-form-item class="submit-item">
                <el-button class="submit" type="primary" size="large" native-type="submit" :loading="submitting">
                  登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form
              ref="registerFormRef"
              class="auth-form"
              :model="registerForm"
              :rules="registerRules"
              label-width="0"
              @submit.prevent="submitRegister"
            >
              <el-form-item prop="userAccount">
                <el-input
                  v-model="registerForm.userAccount"
                  size="large"
                  placeholder="请输入账号"
                  aria-label="账号"
                  autocomplete="username"
                  maxlength="32"
                  clearable
                  :prefix-icon="User"
                />
              </el-form-item>
              <el-form-item prop="userPassword">
                <el-input
                  v-model="registerForm.userPassword"
                  size="large"
                  type="password"
                  placeholder="请输入密码，6 到 64 位"
                  aria-label="密码"
                  autocomplete="new-password"
                  maxlength="64"
                  show-password
                  :prefix-icon="Lock"
                />
              </el-form-item>
              <el-form-item prop="confirmPassword">
                <el-input
                  v-model="registerForm.confirmPassword"
                  size="large"
                  type="password"
                  placeholder="请再次输入密码"
                  aria-label="确认密码"
                  autocomplete="new-password"
                  maxlength="64"
                  show-password
                  :prefix-icon="Lock"
                />
              </el-form-item>
              <el-form-item class="submit-item">
                <el-button class="submit" type="primary" size="large" native-type="submit" :loading="submitting">
                  注册
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </section>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100%;
  display: flex;
  align-items: safe center;
  justify-content: center;
  box-sizing: border-box;
  padding: 32px 16px;
  background: #efe2cc url('/login-backgroud.jpg') center / cover no-repeat;
}

.panel {
  width: min(100%, 420px);
}

.panel-card {
  padding: 28px 28px 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 18px 48px rgba(72, 42, 12, 0.22);
}

.card-brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  color: inherit;
  text-decoration: none;
}

.card-brand:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 4px;
  border-radius: 8px;
}

.card-logo {
  display: block;
  width: 36px;
  height: 36px;
}

.card-name {
  color: var(--el-text-color-primary);
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.panel-title {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 28px;
  font-weight: 700;
}

.panel-lead {
  margin: 8px 0 20px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.auth-form {
  padding-top: 8px;
}

.submit-item {
  margin-bottom: 0;
}

.submit {
  width: 100%;
}

@media (max-width: 640px) {
  .auth-page {
    align-items: flex-start;
    padding: 24px 16px 32px;
  }

  .panel-card {
    padding: 24px 20px 20px;
  }
}
</style>
