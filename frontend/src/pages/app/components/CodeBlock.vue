<template>
  <div class="code-block" :class="{ 'is-streaming': streaming }">
    <div class="head">
      <button
        class="toggle"
        type="button"
        :aria-expanded="open"
        :aria-label="`${open ? '折叠' : '展开'} ${file.name}`"
        @click="toggle"
      >
        <el-icon class="chevron" :class="{ 'is-open': open }" aria-hidden="true"><ArrowDown /></el-icon>
        <el-icon class="file-icon" aria-hidden="true"><Document /></el-icon>
        <span class="file-name">{{ file.name }}</span>
        <span class="meta">{{ lineCount }} 行</span>
        <span v-if="streaming" class="writing"><span class="dot" aria-hidden="true" />编写中</span>
        <el-icon v-else class="ok" aria-label="已完成"><CircleCheckFilled /></el-icon>
      </button>
      <button class="copy" type="button" :aria-label="`复制 ${file.name}`" @click="copy">
        <el-icon aria-hidden="true"><CopyDocument /></el-icon>
      </button>
    </div>
    <div v-show="open" ref="body" class="body" @scroll.passive="onScroll">
      <pre class="yy-code"><code class="hljs" v-html="html" /><span v-if="streaming" class="caret" aria-hidden="true" /></pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowDown, CircleCheckFilled, CopyDocument, Document } from '@element-plus/icons-vue'
import { ElIcon, ElMessage } from 'element-plus'
import { computed, nextTick, shallowRef, useTemplateRef, watch } from 'vue'
import { useHighlightedCode } from '../composables/useHighlightedCode'
import type { GenFile } from '../composables/useAppChat'

const props = defineProps<{
  file: GenFile
}>()

const open = shallowRef(!props.file.done)
const userToggled = shallowRef(false)
const body = useTemplateRef<HTMLElement>('body')

let stickToBottom = true

const lineCount = computed(() => (props.file.content ? props.file.content.split('\n').length : 0))
const streaming = computed(() => !props.file.done)

const { html } = useHighlightedCode(
  () => props.file,
  () => {
    if (streaming.value && stickToBottom) {
      void nextTick(() => {
        if (body.value) {
          body.value.scrollTop = body.value.scrollHeight
        }
      })
    }
  },
)

// 写完后自动折叠,保持对话区整洁;用户手动展开或折叠过就不再干预
watch(
  () => props.file.done,
  (done) => {
    if (done && !userToggled.value) {
      open.value = false
    }
  },
)

function toggle() {
  userToggled.value = true
  open.value = !open.value
}

function onScroll() {
  const el = body.value
  if (el) {
    stickToBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 24
  }
}

async function copy() {
  try {
    await navigator.clipboard.writeText(props.file.content)
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.code-block {
  border: 1px solid #2d2852;
  border-radius: var(--yy-radius-sm);
  background: #1b1736;
  overflow: hidden;
  box-shadow: var(--yy-shadow-sm);
}

.code-block.is-streaming {
  border-color: rgba(139, 92, 246, 0.6);
  box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.12);
}

.head {
  display: flex;
  align-items: center;
  background: #231e44;
}

.toggle {
  display: flex;
  flex: 1;
  align-items: center;
  gap: 8px;
  min-width: 0;
  padding: 9px 12px;
  border: 0;
  color: #e7e3ff;
  font: inherit;
  font-size: 13px;
  text-align: left;
  background: transparent;
  cursor: pointer;
}

.toggle:focus-visible,
.copy:focus-visible {
  outline: 2px solid #c4b5fd;
  outline-offset: -2px;
}

.chevron {
  color: #a59fd0;
  font-size: 12px;
  transform: rotate(-90deg);
  transition: transform 0.2s ease;
}

.chevron.is-open {
  transform: none;
}

.file-icon {
  color: #c4b5fd;
}

.file-name {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-weight: 600;
}

.meta {
  color: #a59fd0;
  font-size: 12px;
}

.writing {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
  color: #fcd34d;
  font-size: 12px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fcd34d;
  animation: pulse 1.2s ease-in-out infinite;
}

.ok {
  margin-left: auto;
  color: #86efac;
}

.copy {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  align-self: stretch;
  border: 0;
  color: #a59fd0;
  background: transparent;
  cursor: pointer;
  transition: color 0.2s ease;
}

.copy:hover {
  color: #fff;
}

.body {
  max-height: 340px;
  overflow: auto;
  border-top: 1px solid #2d2852;
}

pre {
  margin: 0;
  padding: 12px 14px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 12.5px;
  line-height: 1.65;
  tab-size: 2;
  white-space: pre-wrap;
  word-break: break-all;
}

code.hljs {
  display: inline;
  padding: 0;
  color: #e7e3ff;
  background: transparent;
}

.caret {
  display: inline-block;
  width: 7px;
  height: 14px;
  margin-left: 1px;
  vertical-align: text-bottom;
  background: #c4b5fd;
  animation: blink 1s steps(2, start) infinite;
}

@keyframes pulse {
  50% {
    opacity: 0.35;
  }
}

@keyframes blink {
  to {
    visibility: hidden;
  }
}

@media (prefers-reduced-motion: reduce) {
  .dot,
  .caret {
    animation: none;
  }

  .chevron {
    transition: none;
  }
}
</style>
