<script setup lang="ts">
import { Promotion } from '@element-plus/icons-vue'
import { ElIcon, ElInput } from 'element-plus'
import { computed, shallowRef } from 'vue'

const MAX_LENGTH = 10000

const EXAMPLES = [
  '做一个个人博客首页,简洁大方,带文章列表',
  '一个待办事项清单,支持完成和删除任务',
  '设计一个咖啡店官网,包含菜单和联系方式',
  '制作一个番茄钟计时器,界面清爽',
]

defineProps<{
  loading: boolean
}>()

const emit = defineEmits<{
  submit: [prompt: string]
}>()

const prompt = shallowRef('')
const canSubmit = computed(() => prompt.value.trim().length > 0)

function onSubmit() {
  const value = prompt.value.trim()
  if (!value) {
    return
  }
  emit('submit', value)
}

function fillExample(text: string) {
  prompt.value = text
}

function clear() {
  prompt.value = ''
}

defineExpose({ clear })
</script>

<template>
  <div class="prompt-box">
    <form class="panel" @submit.prevent="onSubmit">
      <el-input
        v-model="prompt"
        class="prompt-input"
        type="textarea"
        :autosize="{ minRows: 3, maxRows: 7 }"
        :maxlength="MAX_LENGTH"
        resize="none"
        placeholder="描述你想要的应用,例如:做一个带搜索功能的图书收藏夹"
        aria-label="应用需求描述"
        :disabled="loading"
        @keydown.ctrl.enter.prevent="onSubmit"
        @keydown.meta.enter.prevent="onSubmit"
      />
      <div class="panel-footer">
        <span class="counter">{{ prompt.length }} / {{ MAX_LENGTH }}</span>
        <span class="shortcut">Ctrl + Enter 快速创建</span>
        <button class="submit" type="submit" :disabled="!canSubmit || loading" :aria-busy="loading">
          <span v-if="loading" class="spinner" aria-hidden="true" />
          <el-icon v-else aria-hidden="true"><Promotion /></el-icon>
          {{ loading ? '创建中…' : '创建应用' }}
        </button>
      </div>
    </form>

    <div class="examples" role="group" aria-label="示例提示词">
      <span class="examples-label">试试:</span>
      <button
        v-for="item in EXAMPLES"
        :key="item"
        class="chip"
        type="button"
        :disabled="loading"
        @click="fillExample(item)"
      >
        {{ item }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.prompt-box {
  width: min(100%, 760px);
  margin: 0 auto;
}

.panel {
  padding: 16px 16px 12px;
  border: 1px solid var(--yy-line);
  border-radius: var(--yy-radius-lg);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: var(--yy-inner-light), var(--yy-shadow-md);
  backdrop-filter: blur(8px);
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.panel:focus-within {
  border-color: var(--yy-line-strong);
  box-shadow:
    0 0 0 4px rgba(139, 92, 246, 0.14),
    var(--yy-shadow-lg);
}

.prompt-input :deep(.el-textarea__inner) {
  padding: 4px 6px;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  color: var(--el-text-color-primary);
  font-size: 16px;
  line-height: 1.7;
}

.panel-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.counter {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

.shortcut {
  margin-right: auto;
  color: var(--el-text-color-placeholder);
  font-size: 13px;
}

.submit {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-width: 124px;
  height: 44px;
  padding: 0 20px;
  border: 0;
  border-radius: 14px;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  background: var(--yy-gradient);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.35),
    0 6px 16px rgba(124, 58, 237, 0.35);
  cursor: pointer;
  transition:
    box-shadow 0.2s ease,
    opacity 0.2s ease,
    filter 0.2s ease;
}

.submit:hover:not(:disabled) {
  filter: brightness(1.06);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.35),
    0 8px 22px rgba(124, 58, 237, 0.45);
}

.submit:focus-visible {
  outline: 2px solid var(--yy-focus);
  outline-offset: 3px;
}

.submit:disabled {
  opacity: 0.45;
  box-shadow: none;
  cursor: not-allowed;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.examples {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
}

.examples-label {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.chip {
  max-width: 100%;
  padding: 6px 14px;
  border: 1px solid var(--yy-line);
  border-radius: 999px;
  color: #5b21b6;
  font-size: 13px;
  background: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition:
    background-color 0.2s ease,
    border-color 0.2s ease;
}

.chip:hover:not(:disabled) {
  border-color: var(--yy-line-strong);
  background: #fff;
  box-shadow: var(--yy-shadow-sm);
}

.chip:focus-visible {
  outline: 2px solid var(--yy-focus);
  outline-offset: 2px;
}

.chip:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .shortcut {
    display: none;
  }

  .counter {
    margin-right: auto;
  }
}

@media (prefers-reduced-motion: reduce) {
  .panel,
  .submit,
  .chip {
    transition: none;
  }

  .spinner {
    animation-duration: 2s;
  }
}
</style>
