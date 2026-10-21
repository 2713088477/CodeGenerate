<script setup lang="ts">
import { ElButton } from 'element-plus'
</script>

<template>
  <div class="header-bar">
    <div class="header-start">
      <router-link class="brand" to="/" aria-label="回到首页">
        <img src="/logo.png" alt="零代码生成器" />
      </router-link>

      <nav class="nav" aria-label="页面导航">
        <router-link class="nav-link" to="/">首页</router-link>
      </nav>
    </div>

    <div class="actions">
      <el-button type="primary">登录</el-button>
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
  height: 40px;
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
  height: 100%;
}
</style>
