<script setup lang="ts">
import { ArrowDown, ArrowLeft, Upload } from '@element-plus/icons-vue'
import { ElButton, ElDropdown, ElDropdownItem, ElDropdownMenu, ElIcon } from 'element-plus'
import { computed } from 'vue'
import type { AppVo } from '@/api'

const props = defineProps<{
  app: AppVo | null
  deploying: boolean
  canDeploy: boolean
}>()

const emit = defineEmits<{
  back: []
  rename: []
  deploy: []
}>()

const name = computed(() => props.app?.appName?.trim() || '未命名应用')
const deployText = computed(() => (props.app?.deployKey ? '重新部署' : '部署'))

function onCommand(command: string | number | object) {
  if (command === 'rename') {
    emit('rename')
  } else if (command === 'home') {
    emit('back')
  }
}
</script>

<template>
  <header class="top-bar">
    <div class="left">
      <button class="back" type="button" aria-label="返回首页" @click="emit('back')">
        <el-icon aria-hidden="true"><ArrowLeft /></el-icon>
      </button>
      <img class="logo" src="/logo.png" alt="" />
      <el-dropdown trigger="click" @command="onCommand">
        <button class="name-btn" type="button" aria-label="应用菜单">
          <span class="name" :title="name">{{ name }}</span>
          <el-icon class="arrow" aria-hidden="true"><ArrowDown /></el-icon>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="rename">重命名</el-dropdown-item>
            <el-dropdown-item command="home">返回首页</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <el-button class="deploy" type="primary" :loading="deploying" :disabled="!canDeploy" @click="emit('deploy')">
      <el-icon v-if="!deploying" aria-hidden="true"><Upload /></el-icon>
      {{ deployText }}
    </el-button>
  </header>
</template>

<style scoped>
.top-bar {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid var(--yy-line);
  background: #fff;
}

.left {
  display: flex;
  flex: 1;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

/* el-dropdown 外层是 inline 元素,需要它也能收缩,长名字才会显示省略号 */
.left :deep(.el-dropdown) {
  min-width: 0;
}

.back {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 8px;
  color: var(--el-text-color-regular);
  background: transparent;
  cursor: pointer;
  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

.back:hover {
  color: var(--yy-violet);
  background: var(--yy-tint);
}

.back:focus-visible,
.name-btn:focus-visible {
  outline: 2px solid var(--yy-focus);
  outline-offset: 2px;
}

.logo {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.name-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  padding: 6px 10px;
  border: 0;
  border-radius: 10px;
  color: var(--el-text-color-primary);
  background: transparent;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.name-btn:hover {
  background: var(--yy-tint);
}

.name {
  min-width: 0;
  max-width: 320px;
  overflow: hidden;
  font-size: 15px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow {
  flex-shrink: 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.deploy {
  flex-shrink: 0;
  height: 36px;
  padding: 0 18px;
  border: 0;
  border-radius: 10px;
  font-weight: 600;
  background: var(--yy-gradient);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.35),
    0 4px 12px rgba(124, 58, 237, 0.28);
}

.deploy:hover:not(:disabled),
.deploy:focus-visible:not(:disabled) {
  filter: brightness(1.06);
}

.deploy.is-disabled,
.deploy:disabled {
  opacity: 0.5;
  box-shadow: none;
}

@media (prefers-reduced-motion: reduce) {
  .back,
  .name-btn {
    transition: none;
  }
}
</style>
