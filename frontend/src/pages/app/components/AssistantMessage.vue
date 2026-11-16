<template>
  <div class="assistant">
    <img class="avatar" src="/logo.png" alt="" />
    <div class="content">
      <p v-if="working" class="status" role="status" aria-live="polite">
        <span class="spinner" aria-hidden="true" />
        {{ statusText }}
      </p>

      <p v-if="message.restored" class="note">该应用的代码已生成,可以在预览区查看。想调整的话,直接告诉我新的需求。</p>

      <div v-if="message.files.length > 0" class="files">
        <CodeBlock v-for="file in message.files" :key="file.key" :file="file" />
      </div>

      <!-- eslint-disable-next-line vue/no-v-html -->
      <div v-if="descriptionHtml" class="markdown" v-html="descriptionHtml" />

      <p v-if="message.status === 'done' && !message.restored" class="result ok">
        <el-icon aria-hidden="true"><CircleCheckFilled /></el-icon>
        已生成并保存,可以在预览区查看
      </p>
      <p v-else-if="message.status === 'stopped'" class="result muted">
        <el-icon aria-hidden="true"><VideoPause /></el-icon>
        已停止生成,这次的内容不会保存
      </p>
      <div v-else-if="message.status === 'error'" class="result fail" role="alert">
        <span class="fail-text">
          <el-icon aria-hidden="true"><CircleCloseFilled /></el-icon>
          {{ message.error }}
        </span>
        <el-button v-if="canRetry" size="small" round @click="$emit('retry')">
          <el-icon aria-hidden="true"><RefreshRight /></el-icon>
          重试
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { CircleCheckFilled, CircleCloseFilled, RefreshRight, VideoPause } from '@element-plus/icons-vue'
import { ElButton, ElIcon } from 'element-plus'
import { computed } from 'vue'
import { renderMarkdown } from '@/utils/markdown'
import type { ChatMessage } from '../composables/useAppChat'
import CodeBlock from './CodeBlock.vue'

const props = defineProps<{
  message: ChatMessage
  /** 只有最后一条失败的回复才显示重试 */
  canRetry: boolean
}>()

defineEmits<{
  retry: []
}>()

const working = computed(() => props.message.status === 'connecting' || props.message.status === 'streaming')

const statusText = computed(() => {
  const { status, files } = props.message
  if (status === 'connecting') {
    return '正在连接…'
  }
  if (status === 'streaming') {
    const writing = files.find((file) => !file.done)
    if (writing) {
      return `正在编写 ${writing.name}`
    }
    return files.length > 0 ? '正在整理说明…' : '正在构思页面…'
  }
  return ''
})

const descriptionHtml = computed(() => (props.message.description ? renderMarkdown(props.message.description) : ''))
</script>

<style scoped>
.assistant {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.content {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  align-self: flex-start;
  margin: 0;
  padding: 6px 12px;
  border-radius: 999px;
  color: var(--yy-violet);
  font-size: 13px;
  font-weight: 500;
  background: var(--el-color-primary-light-9);
}

.spinner {
  width: 12px;
  height: 12px;
  border: 2px solid rgba(124, 58, 237, 0.25);
  border-top-color: var(--yy-violet);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.note {
  margin: 0;
  padding: 10px 14px;
  border: 1px solid var(--yy-line);
  border-radius: var(--yy-radius-sm);
  color: var(--el-text-color-regular);
  font-size: 14px;
  line-height: 1.7;
  background: #fff;
}

.files {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.markdown {
  padding: 10px 14px;
  border: 1px solid var(--yy-line);
  border-radius: var(--yy-radius-sm);
  color: var(--el-text-color-primary);
  font-size: 14px;
  line-height: 1.75;
  background: #fff;
  overflow-wrap: anywhere;
}

.markdown :deep(> :first-child) {
  margin-top: 0;
}

.markdown :deep(> :last-child) {
  margin-bottom: 0;
}

.markdown :deep(p),
.markdown :deep(ul),
.markdown :deep(ol) {
  margin: 0 0 8px;
}

.markdown :deep(ul),
.markdown :deep(ol) {
  padding-left: 1.4em;
}

.markdown :deep(code) {
  padding: 1px 6px;
  border-radius: 6px;
  color: var(--yy-violet);
  font-size: 0.9em;
  background: var(--el-color-primary-light-9);
}

.markdown :deep(a) {
  color: var(--yy-violet);
}

.result {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0;
  font-size: 13px;
}

.result.ok {
  color: #15803d;
}

.result.muted {
  color: var(--el-text-color-secondary);
}

.result.fail {
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border: 1px solid #fecaca;
  border-radius: var(--yy-radius-sm);
  color: #b91c1c;
  background: #fef2f2;
}

.fail-text {
  display: inline-flex;
  align-items: flex-start;
  gap: 6px;
  min-width: 0;
  overflow-wrap: anywhere;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .spinner {
    animation-duration: 2s;
  }
}
</style>
