<script setup lang="ts">
import { Promotion, VideoPause } from '@element-plus/icons-vue'
import { ElIcon, ElInput } from 'element-plus'
import { computed, shallowRef } from 'vue'

const MAX_LENGTH = 10000

const props = defineProps<{
  generating: boolean
  disabled?: boolean
}>()

const emit = defineEmits<{
  submit: [text: string]
  stop: []
}>()

const text = shallowRef('')
const canSend = computed(() => text.value.trim().length > 0 && !props.generating && !props.disabled)

function submit() {
  if (!canSend.value) {
    return
  }
  emit('submit', text.value.trim())
  text.value = ''
}

function onEnter(event: Event | KeyboardEvent) {
  const e = event as KeyboardEvent
  // Shift + Enter 换行;输入法选字时的回车不发送
  if (e.shiftKey || e.isComposing) {
    return
  }
  e.preventDefault()
  submit()
}
</script>

<template>
  <form class="chat-input" @submit.prevent="submit">
    <el-input
      v-model="text"
      class="field"
      type="textarea"
      :autosize="{ minRows: 2, maxRows: 6 }"
      :maxlength="MAX_LENGTH"
      resize="none"
      placeholder="描述你想要的页面或想调整的地方"
      aria-label="发送给艺言的消息"
      :disabled="disabled"
      @keydown.enter="onEnter"
    />
    <div class="footer">
      <span class="hint">Enter 发送,Shift + Enter 换行</span>
      <button v-if="generating" class="action stop" type="button" aria-label="停止生成" @click="emit('stop')">
        <el-icon aria-hidden="true"><VideoPause /></el-icon>
        停止
      </button>
      <button v-else class="action send" type="submit" :disabled="!canSend" aria-label="发送">
        <el-icon aria-hidden="true"><Promotion /></el-icon>
        发送
      </button>
    </div>
  </form>
</template>

<style scoped>
.chat-input {
  padding: 10px 12px 10px;
  border: 1px solid var(--yy-line);
  border-radius: var(--yy-radius-md);
  background: #fff;
  box-shadow: var(--yy-inner-light), var(--yy-shadow-sm);
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.chat-input:focus-within {
  border-color: var(--yy-line-strong);
  box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.12);
}

.field :deep(.el-textarea__inner) {
  padding: 2px 4px;
  border: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
  font-size: 14px;
  line-height: 1.65;
}

.footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: 6px;
}

.hint {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}

.action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-width: 76px;
  height: 34px;
  padding: 0 14px;
  border: 0;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition:
    filter 0.2s ease,
    opacity 0.2s ease;
}

.action:focus-visible {
  outline: 2px solid var(--yy-focus);
  outline-offset: 2px;
}

.send {
  color: #fff;
  background: var(--yy-gradient);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.35),
    0 4px 12px rgba(124, 58, 237, 0.3);
}

.send:hover:not(:disabled) {
  filter: brightness(1.06);
}

.send:disabled {
  opacity: 0.45;
  box-shadow: none;
  cursor: not-allowed;
}

.stop {
  color: var(--yy-violet);
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--yy-line-strong);
}

.stop:hover {
  background: var(--el-color-primary-light-8);
}

@media (prefers-reduced-motion: reduce) {
  .chat-input,
  .action {
    transition: none;
  }
}
</style>
