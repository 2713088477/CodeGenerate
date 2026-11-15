<script setup lang="ts">
import { ArrowDown } from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'
import { computed, nextTick, onMounted, shallowRef, useTemplateRef, watch } from 'vue'
import type { ChatMessage } from '../composables/useAppChat'
import AssistantMessage from './AssistantMessage.vue'
import ChatInput from './ChatInput.vue'

const props = defineProps<{
  messages: ChatMessage[]
  generating: boolean
}>()

const emit = defineEmits<{
  send: [text: string]
  stop: []
  retry: []
}>()

const scroller = useTemplateRef<HTMLElement>('scroller')
const stickToBottom = shallowRef(true)

const lastId = computed(() => props.messages[props.messages.length - 1]?.id)

// 流式内容变化的信号:最后一条回复的总长度和状态。不直接深度监听 messages,避免每个分片都触发大量比较
const contentSignal = computed(() => {
  const last = props.messages[props.messages.length - 1]
  if (!last) {
    return ''
  }
  const size = last.files.reduce((sum, file) => sum + file.content.length, 0) + last.description.length
  return `${last.id}:${last.status}:${last.files.length}:${size}`
})

function scrollToBottom(smooth = false) {
  void nextTick(() => {
    scroller.value?.scrollTo({ top: scroller.value.scrollHeight, behavior: smooth ? 'smooth' : 'auto' })
  })
}

function onScroll() {
  const el = scroller.value
  if (el) {
    stickToBottom.value = el.scrollHeight - el.scrollTop - el.clientHeight < 80
  }
}

// 用户自己发了新消息时,不管之前滚到哪里都回到底部
watch(lastId, () => {
  stickToBottom.value = true
  scrollToBottom()
})

watch(contentSignal, () => {
  if (stickToBottom.value) {
    scrollToBottom()
  }
})

onMounted(() => scrollToBottom())

function isLastFailed(message: ChatMessage) {
  return message.id === lastId.value && message.status === 'error'
}
</script>

<template>
  <section class="chat-panel" aria-label="对话">
    <div ref="scroller" class="messages" @scroll.passive="onScroll">
      <div class="messages-inner">
        <template v-for="message in messages" :key="message.id">
          <div v-if="message.role === 'user'" class="user-row">
            <p class="user-bubble">{{ message.content }}</p>
          </div>
          <AssistantMessage v-else :message="message" :can-retry="isLastFailed(message)" @retry="emit('retry')" />
        </template>
      </div>
    </div>

    <button
      v-show="!stickToBottom && generating"
      class="jump"
      type="button"
      aria-label="回到最新内容"
      @click="((stickToBottom = true), scrollToBottom(true))"
    >
      <el-icon aria-hidden="true"><ArrowDown /></el-icon>
      回到最新
    </button>

    <div class="composer">
      <p class="notice">继续对话会根据你的新描述重新生成整个页面,并覆盖当前版本。</p>
      <ChatInput :generating="generating" @submit="emit('send', $event)" @stop="emit('stop')" />
    </div>
  </section>
</template>

<style scoped>
.chat-panel {
  position: relative;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  height: 100%;
  background: linear-gradient(180deg, #fbfaff, #f6f3ff);
}

.messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.messages-inner {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 20px 16px 12px;
}

.user-row {
  display: flex;
  justify-content: flex-end;
}

.user-bubble {
  max-width: 85%;
  margin: 0;
  padding: 10px 14px;
  border-radius: 16px 16px 4px 16px;
  color: #fff;
  font-size: 14px;
  line-height: 1.65;
  background: var(--yy-gradient);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.3),
    0 6px 16px rgba(124, 58, 237, 0.25);
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.jump {
  position: absolute;
  bottom: 150px;
  left: 50%;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid var(--yy-line-strong);
  border-radius: 999px;
  color: var(--yy-violet);
  font-size: 12px;
  font-weight: 500;
  background: #fff;
  box-shadow: var(--yy-shadow-md);
  cursor: pointer;
  transform: translateX(-50%);
}

.jump:focus-visible {
  outline: 2px solid var(--yy-focus);
  outline-offset: 2px;
}

.composer {
  flex-shrink: 0;
  padding: 0 16px 14px;
}

.notice {
  margin: 0 0 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
</style>
