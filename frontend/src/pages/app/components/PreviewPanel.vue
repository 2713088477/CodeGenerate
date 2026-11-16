<template>
  <section class="preview-panel" aria-label="应用预览">
    <div class="toolbar">
      <div class="modes" role="group" aria-label="视图切换">
        <button
          class="mode"
          type="button"
          :class="{ 'is-active': mode === 'preview' }"
          :aria-pressed="mode === 'preview'"
          @click="mode = 'preview'"
        >
          预览
        </button>
        <button
          class="mode"
          type="button"
          :class="{ 'is-active': mode === 'code' }"
          :aria-pressed="mode === 'code'"
          :disabled="!hasFiles"
          @click="mode = 'code'"
        >
          代码
        </button>
      </div>

      <div v-if="mode === 'preview' && hasPreview" class="tools">
        <div class="devices" role="group" aria-label="预览设备">
          <el-tooltip content="桌面" placement="bottom">
            <button
              class="icon-btn"
              type="button"
              :class="{ 'is-active': device === 'desktop' }"
              :aria-pressed="device === 'desktop'"
              aria-label="桌面宽度"
              @click="device = 'desktop'"
            >
              <el-icon aria-hidden="true"><Monitor /></el-icon>
            </button>
          </el-tooltip>
          <el-tooltip content="手机" placement="bottom">
            <button
              class="icon-btn"
              type="button"
              :class="{ 'is-active': device === 'mobile' }"
              :aria-pressed="device === 'mobile'"
              aria-label="手机宽度"
              @click="device = 'mobile'"
            >
              <el-icon aria-hidden="true"><Iphone /></el-icon>
            </button>
          </el-tooltip>
        </div>
        <el-tooltip content="刷新" placement="bottom">
          <button class="icon-btn" type="button" aria-label="刷新预览" @click="refresh">
            <el-icon aria-hidden="true"><Refresh /></el-icon>
          </button>
        </el-tooltip>
        <el-tooltip content="在新标签页打开" placement="bottom">
          <button class="icon-btn" type="button" aria-label="在新标签页打开" @click="openInNewTab">
            <el-icon aria-hidden="true"><TopRight /></el-icon>
          </button>
        </el-tooltip>
      </div>
    </div>

    <div class="stage">
      <!-- 代码视图:生成过程中实时展示 -->
      <div v-if="mode === 'code'" class="code-view">
        <div v-if="hasFiles" class="tabs" role="tablist" aria-label="文件">
          <button
            v-for="file in files"
            :key="file.key"
            class="tab"
            type="button"
            role="tab"
            :class="{ 'is-active': current?.key === file.key }"
            :aria-selected="current?.key === file.key"
            @click="picked = file.key"
          >
            {{ file.name }}
            <span v-if="!file.done" class="tab-dot" aria-hidden="true" />
          </button>
        </div>
        <div ref="codeBody" class="code-body">
          <pre class="yy-code"><code class="hljs" v-html="html" /></pre>
        </div>
      </div>

      <!-- 预览视图 -->
      <template v-else>
        <div v-if="hasPreview" class="frame-wrap">
          <div class="frame" :class="`is-${device}`">
            <iframe
              :key="frameSrc"
              class="iframe"
              :src="frameSrc"
              title="应用预览"
              sandbox="allow-scripts allow-same-origin allow-forms allow-popups allow-modals"
              @load="frameLoading = false"
            />
            <div v-if="frameLoading" class="frame-loading" role="status">
              <span class="spinner" aria-hidden="true" />
              正在加载预览…
            </div>
          </div>
        </div>
        <div v-else class="empty">
          <img class="empty-logo" src="/logo.png" alt="" />
          <p class="empty-title">{{ generating ? '正在生成中…' : '还没有预览' }}</p>
          <p class="empty-text">
            {{ generating ? '生成完成后,这里会自动展示页面效果' : '在左侧描述你想要的页面,生成后会在这里预览' }}
          </p>
        </div>
      </template>
    </div>
  </section>
</template>

<script setup lang="ts">
import { Iphone, Monitor, Refresh, TopRight } from '@element-plus/icons-vue'
import { ElIcon, ElTooltip } from 'element-plus'
import { computed, nextTick, shallowRef, useTemplateRef, watch } from 'vue'
import { useHighlightedCode } from '../composables/useHighlightedCode'
import type { GenFile } from '../composables/useAppChat'

type Mode = 'preview' | 'code'
type Device = 'desktop' | 'mobile'

const props = defineProps<{
  appId: string
  /** 后端是否已经保存过这个应用的代码 */
  hasPreview: boolean
  /** 每次生成完成加 1,用来让 iframe 重新加载 */
  version: number
  generating: boolean
  /** 最近一次生成的文件,用于"代码"视图 */
  files: GenFile[]
}>()

const mode = shallowRef<Mode>('preview')
const device = shallowRef<Device>('desktop')
const reloadKey = shallowRef(0)
const frameLoading = shallowRef(true)
const picked = shallowRef<string | null>(null)
const codeBody = useTemplateRef<HTMLElement>('codeBody')

const previewUrl = computed(() => `/api/app/preview/${props.appId}/`)
const frameSrc = computed(() => `${previewUrl.value}?t=${props.version}-${reloadKey.value}`)
const hasFiles = computed(() => props.files.length > 0)

// 选中的文件:用户点过就用用户选的,否则跟随正在写的(最后一个)文件
const current = computed(() => {
  const chosen = picked.value ? props.files.find((file) => file.key === picked.value) : undefined
  return chosen ?? props.files[props.files.length - 1]
})

const { html } = useHighlightedCode(
  () => current.value,
  () => {
    if (props.generating && picked.value === null) {
      void nextTick(() => {
        if (codeBody.value) {
          codeBody.value.scrollTop = codeBody.value.scrollHeight
        }
      })
    }
  },
)

// 开始生成就切到代码视图看进度,生成完成(version 变化)后切回预览
watch(
  () => props.generating,
  (value) => {
    if (value) {
      picked.value = null
      mode.value = 'code'
    }
  },
)
watch(
  () => props.version,
  () => {
    frameLoading.value = true
    mode.value = 'preview'
  },
)

function refresh() {
  frameLoading.value = true
  reloadKey.value++
}

function openInNewTab() {
  window.open(frameSrc.value, '_blank', 'noopener')
}
</script>

<style scoped>
.preview-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  height: 100%;
  background: var(--el-bg-color-page);
}

.toolbar {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  height: 48px;
  padding: 0 14px;
  border-bottom: 1px solid var(--yy-line);
  background: #fff;
}

.modes {
  display: inline-flex;
  padding: 3px;
  border-radius: 10px;
  background: var(--el-color-primary-light-9);
}

.mode {
  padding: 5px 16px;
  border: 0;
  border-radius: 8px;
  color: var(--el-text-color-regular);
  font-size: 13px;
  font-weight: 500;
  background: transparent;
  cursor: pointer;
  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

.mode.is-active {
  color: var(--yy-violet);
  font-weight: 600;
  background: #fff;
  box-shadow: var(--yy-shadow-sm);
}

.mode:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.mode:focus-visible,
.icon-btn:focus-visible,
.tab:focus-visible {
  outline: 2px solid var(--yy-focus);
  outline-offset: 2px;
}

.tools {
  display: flex;
  align-items: center;
  gap: 4px;
}

.devices {
  display: inline-flex;
  gap: 2px;
  margin-right: 6px;
  padding-right: 8px;
  border-right: 1px solid var(--yy-line);
}

.icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 8px;
  color: var(--el-text-color-regular);
  font-size: 16px;
  background: transparent;
  cursor: pointer;
  transition:
    color 0.2s ease,
    background-color 0.2s ease;
}

.icon-btn:hover {
  color: var(--yy-violet);
  background: var(--yy-tint);
}

.icon-btn.is-active {
  color: var(--yy-violet);
  background: var(--el-color-primary-light-8);
}

.stage {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.frame-wrap {
  display: flex;
  justify-content: center;
  height: 100%;
  padding: 0;
  overflow: auto;
}

.frame {
  position: relative;
  width: 100%;
  height: 100%;
  background: #fff;
  transition: width 0.25s ease;
}

.frame.is-mobile {
  width: 390px;
  max-width: 100%;
  height: calc(100% - 32px);
  margin: 16px 0;
  border: 1px solid var(--yy-line-strong);
  border-radius: 24px;
  box-shadow: var(--yy-shadow-lg);
  overflow: hidden;
}

.iframe {
  display: block;
  width: 100%;
  height: 100%;
  border: 0;
  background: #fff;
}

.frame-loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
  background: rgba(255, 255, 255, 0.85);
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(124, 58, 237, 0.25);
  border-top-color: var(--yy-violet);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  height: 100%;
  padding: 24px;
  text-align: center;
  background:
    radial-gradient(45% 40% at 30% 30%, rgba(34, 211, 238, 0.12), transparent 70%),
    radial-gradient(45% 40% at 70% 70%, rgba(244, 114, 182, 0.14), transparent 70%);
}

.empty-logo {
  width: 96px;
  height: 96px;
  object-fit: contain;
}

.empty-title {
  margin: 6px 0 0;
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 600;
}

.empty-text {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.code-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #1b1736;
}

.tabs {
  display: flex;
  flex-shrink: 0;
  gap: 2px;
  padding: 8px 10px 0;
  background: #231e44;
}

.tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: 0;
  border-radius: 8px 8px 0 0;
  color: #a59fd0;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
  background: transparent;
  cursor: pointer;
}

.tab:hover {
  color: #fff;
}

.tab.is-active {
  color: #fff;
  background: #1b1736;
}

.tab-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #fcd34d;
  animation: pulse 1.2s ease-in-out infinite;
}

.code-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.code-body pre {
  margin: 0;
  padding: 16px 18px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
  line-height: 1.7;
  tab-size: 2;
  white-space: pre-wrap;
  word-break: break-all;
}

.code-body code.hljs {
  display: inline;
  padding: 0;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  50% {
    opacity: 0.35;
  }
}

@media (prefers-reduced-motion: reduce) {
  .frame,
  .mode,
  .icon-btn {
    transition: none;
  }

  .spinner,
  .tab-dot {
    animation-duration: 2s;
  }
}
</style>
