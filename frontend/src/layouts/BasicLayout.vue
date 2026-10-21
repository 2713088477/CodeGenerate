<script setup lang="ts">
import { ElContainer, ElHeader, ElMain } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
</script>

<template>
  <el-container class="app-layout" direction="vertical">
    <el-header class="app-header" height="64px">
      <AppHeader />
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<style scoped>
.app-layout {
  height: 100%;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.app-main {
  background: var(--el-bg-color-page);
}
</style>
