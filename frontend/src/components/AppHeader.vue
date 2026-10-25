<script setup lang="ts">
import { ArrowDown } from '@element-plus/icons-vue'
import { ElAvatar, ElButton, ElDropdown, ElDropdownItem, ElDropdownMenu, ElIcon, ElMessage } from 'element-plus'
import { storeToRefs } from 'pinia'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { hasAdminPermission } from '@/router/permission'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const { profile } = storeToRefs(userStore)

const displayName = computed(() => profile.value?.username || profile.value?.userAccount || '已登录')
const isAdmin = computed(() => hasAdminPermission(profile.value?.role))
const avatarText = computed(() => displayName.value.slice(0, 1))

function goLogin() {
  router.push({ name: 'login' })
}

async function onLogout() {
  await userStore.logout()
  ElMessage.success('已退出登录')
}

function onAccountCommand(command: string | number) {
  if (command === 'logout') {
    void onLogout()
  }
}
</script>

<template>
  <div class="header-bar">
    <div class="header-start">
      <router-link class="brand" to="/" aria-label="回到首页">
        <img src="/logo.png" alt="" />
        <span class="brand-name">艺构</span>
      </router-link>

      <nav class="nav" aria-label="页面导航">
        <router-link class="nav-link" to="/">首页</router-link>
        <router-link v-if="isAdmin" class="nav-link" :to="{ name: 'userManage' }">用户管理</router-link>
      </nav>
    </div>

    <div class="actions">
      <el-dropdown v-if="profile" trigger="click" @command="onAccountCommand">
        <button class="account" type="button" aria-label="账号菜单">
          <el-avatar class="account-avatar" :size="32" :src="profile.avatar || undefined" aria-hidden="true">
            {{ avatarText }}
          </el-avatar>
          <span class="account-name">{{ displayName }}</span>
          <el-icon class="account-arrow" aria-hidden="true"><ArrowDown /></el-icon>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-button v-else type="primary" @click="goLogin">登录</el-button>
    </div>
  </div>
</template>

<style scoped>
.header-bar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
}

.header-start {
  position: absolute;
  left: 20%;
  top: 0;
  display: flex;
  align-items: center;
  height: 100%;
  min-width: 0;
  gap: 16px;
}

.brand {
  display: inline-flex;
  align-items: center;
  flex-shrink: 0;
  gap: 8px;
  height: 40px;
  text-decoration: none;
}

.brand-name {
  color: var(--el-text-color-primary);
  font-size: 18px;
  font-weight: 600;
  line-height: 1;
  letter-spacing: 0.06em;
}

.brand img {
  display: block;
  height: 40px;
  width: auto;
}

.nav-link {
  color: var(--el-text-color-regular);
  font-size: 15px;
  font-weight: 500;
  line-height: 1;
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 6px;
}

.nav-link:hover {
  color: var(--el-color-primary);
}

.nav-link.router-link-exact-active {
  color: var(--el-color-primary);
}

.actions {
  position: absolute;
  right: 10%;
  top: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  height: 100%;
}

.account {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding: 4px 8px 4px 4px;
  border: 0;
  border-radius: 999px;
  color: inherit;
  background: transparent;
  cursor: pointer;
}

.account:hover {
  background: var(--el-fill-color-light);
}

.account:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}

.account-avatar {
  flex-shrink: 0;
}

.account-name {
  max-width: 160px;
  overflow: hidden;
  color: var(--el-text-color-primary);
  font-size: 14px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.account-arrow {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
