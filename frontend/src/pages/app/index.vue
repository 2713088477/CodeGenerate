<template>
  <div class="workspace">
    <AppTopBar :app="app" :deploying="deploying" :can-deploy="canDeploy" @back="goHome" @rename="onRename" @deploy="onDeploy" />

    <div class="tabs" role="tablist" aria-label="工作区">
      <button class="tab" type="button" role="tab" :aria-selected="tab === 'chat'" :class="{ 'is-active': tab === 'chat' }" @click="tab = 'chat'">
        对话
      </button>
      <button class="tab" type="button" role="tab" :aria-selected="tab === 'preview'" :class="{ 'is-active': tab === 'preview' }" @click="tab = 'preview'">
        预览
      </button>
    </div>

    <main class="body" :data-tab="tab">
      <div class="col chat-col">
        <ChatPanel :messages="messages" :generating="generating" @send="onSend" @stop="stop" @retry="retry" />
      </div>
      <div class="col preview-col">
        <PreviewPanel
          :app-id="appId"
          :has-preview="hasPreview"
          :version="version"
          :generating="generating"
          :files="previewFiles"
        />
      </div>
    </main>

    <DeployDialog v-model="deployVisible" :url="deployUrl" />
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus'
import { storeToRefs } from 'pinia'
import { computed, onMounted, shallowRef } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { deployApp, getMyApp, updateMyApp } from '@/api'
import type { AppVo } from '@/api'
import { useUserStore } from '@/stores/user'
import { ensureSuccess, errorMessage, readData } from '@/utils/response'
import AppTopBar from './components/AppTopBar.vue'
import ChatPanel from './components/ChatPanel.vue'
import DeployDialog from './components/DeployDialog.vue'
import PreviewPanel from './components/PreviewPanel.vue'
import { useAppChat } from './composables/useAppChat'

type Tab = 'chat' | 'preview'

const route = useRoute()
const router = useRouter()
const { profile } = storeToRefs(useUserStore())

const appId = computed(() => String(route.params.id ?? ''))
const app = shallowRef<AppVo | null>(null)
const ready = shallowRef(false)
const hasPreview = shallowRef(false)
const version = shallowRef(0)
const tab = shallowRef<Tab>('chat')

const deploying = shallowRef(false)
const deployUrl = shallowRef('')
const deployVisible = shallowRef(false)

const { messages, generating, latestAssistant, seedRestored, send, stop, retry } = useAppChat({
  appId: () => appId.value,
  onDone: () => {
    hasPreview.value = true
    version.value++
  },
})

const previewFiles = computed(() => latestAssistant.value?.files ?? [])
const canDeploy = computed(() => ready.value && hasPreview.value && !generating.value)

function goHome() {
  void router.push({ name: 'home' })
}

async function loadApp() {
  app.value = readData<AppVo>(await getMyApp({ path: { id: appId.value } }))
}

/** 后端只有生成成功才会落盘,所以预览入口能访问就说明已经生成过 */
async function probePreview() {
  try {
    const response = await fetch(`/api/app/preview/${appId.value}/`, { method: 'HEAD', cache: 'no-store' })
    return response.ok
  } catch {
    return false
  }
}

async function init() {
  if (!profile.value) {
    void router.replace({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await loadApp()
  } catch (error) {
    ElMessage.error(errorMessage(error))
    void router.replace({ name: 'home' })
    return
  }

  const prompt = app.value?.initPrompt?.trim() ?? ''
  hasPreview.value = await probePreview()
  ready.value = true

  if (hasPreview.value) {
    // 对话历史没有落库,只能恢复到"提问 + 已生成"这一步
    seedRestored(prompt)
    version.value++
  } else if (prompt) {
    // 刚从首页创建、还没生成过:自动用初始提示词发起第一轮生成
    await send(prompt)
  }
}

async function onRename() {
  try {
    const { value } = await ElMessageBox.prompt('', '重命名应用', {
      inputValue: app.value?.appName ?? '',
      inputPlaceholder: '请输入应用名称',
      inputValidator: (input: string) => (input.trim() ? true : '应用名称不能为空'),
      confirmButtonText: '保存',
      cancelButtonText: '取消',
    })
    const appName = value.trim()
    ensureSuccess(await updateMyApp({ path: { id: appId.value }, body: { appName } }))
    if (app.value) {
      app.value = { ...app.value, appName }
    }
    ElMessage.success('已重命名')
  } catch (error) {
    // 点取消会 reject 成字符串 'cancel',不用提示
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(errorMessage(error))
    }
  }
}

async function onDeploy() {
  if (!canDeploy.value || deploying.value) {
    return
  }
  deploying.value = true
  try {
    deployUrl.value = readData<string>(await deployApp({ path: { id: appId.value } }))
    deployVisible.value = true
    // 首次部署会生成 deployKey,刷新一下应用信息让按钮变成"重新部署"
    await loadApp().catch(() => undefined)
  } catch (error) {
    ElMessage.error(errorMessage(error))
  } finally {
    deploying.value = false
  }
}

async function onSend(text: string) {
  tab.value = 'chat'
  await send(text)
}

onBeforeRouteLeave(async () => {
  if (!generating.value) {
    return true
  }
  try {
    await ElMessageBox.confirm('正在生成中,离开会中断这次生成,内容不会保存。确定离开吗?', '离开页面', {
      type: 'warning',
      confirmButtonText: '离开',
      cancelButtonText: '留下',
    })
    return true
  } catch {
    return false
  }
})

onMounted(init)
</script>

<style scoped>
.workspace {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: var(--el-bg-color-page);
}

.body {
  display: grid;
  flex: 1;
  grid-template-columns: minmax(360px, 40%) 1fr;
  min-height: 0;
}

.col {
  min-width: 0;
  min-height: 0;
}

.chat-col {
  border-right: 1px solid var(--yy-line);
}

.tabs {
  display: none;
}

@media (max-width: 900px) {
  .tabs {
    display: flex;
    flex-shrink: 0;
    gap: 4px;
    padding: 8px 12px;
    border-bottom: 1px solid var(--yy-line);
    background: #fff;
  }

  .tab {
    flex: 1;
    padding: 8px 0;
    border: 0;
    border-radius: 10px;
    color: var(--el-text-color-regular);
    font-size: 14px;
    font-weight: 500;
    background: transparent;
    cursor: pointer;
  }

  .tab.is-active {
    color: var(--yy-violet);
    font-weight: 600;
    background: var(--el-color-primary-light-9);
  }

  .tab:focus-visible {
    outline: 2px solid var(--yy-focus);
    outline-offset: 2px;
  }

  .body {
    grid-template-columns: 1fr;
  }

  .chat-col {
    border-right: 0;
  }

  .body[data-tab='chat'] .preview-col,
  .body[data-tab='preview'] .chat-col {
    display: none;
  }
}
</style>
