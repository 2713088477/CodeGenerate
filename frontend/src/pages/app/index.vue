<script setup lang="ts">
import { ArrowLeft, MagicStick } from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const appId = computed(() => String(route.params.id ?? ''))
</script>

<template>
  <section class="app-page">
    <div class="app-card">
      <span class="app-icon" aria-hidden="true">
        <el-icon :size="28"><MagicStick /></el-icon>
      </span>
      <h1 class="app-title">应用已创建</h1>
      <p class="app-id">应用 ID:{{ appId }}</p>
      <p class="app-tip">对话生成功能开发中,敬请期待。</p>
      <router-link class="back-link" to="/">
        <el-icon aria-hidden="true"><ArrowLeft /></el-icon>
        返回首页
      </router-link>
    </div>
  </section>
</template>

<style scoped>
.app-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100%;
}

.app-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: min(100%, 420px);
  padding: 40px 32px;
  border-radius: 16px;
  background: var(--el-bg-color);
  box-shadow: var(--yy-shadow-md);
  text-align: center;
}

.app-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  color: #fff;
  background: var(--yy-gradient);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.35),
    0 6px 16px rgba(124, 58, 237, 0.3);
}

.app-title {
  margin: 20px 0 8px;
  font-size: 22px;
  font-weight: 700;
}

.app-id {
  margin: 0;
  color: var(--el-text-color-regular);
  font-size: 14px;
  word-break: break-all;
}

.app-tip {
  margin: 8px 0 24px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border-radius: 8px;
  color: var(--el-color-primary);
  font-size: 14px;
  text-decoration: none;
}

.back-link:hover {
  background: var(--el-color-primary-light-9);
}

.back-link:focus-visible {
  outline: 2px solid var(--el-color-primary);
  outline-offset: 2px;
}
</style>
