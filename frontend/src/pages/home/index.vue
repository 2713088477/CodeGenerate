<script setup lang="ts">
import { FolderOpened, Lock } from '@element-plus/icons-vue'
import { ElButton, ElIcon, ElMessage, ElPagination, ElSkeleton, ElSkeletonItem } from 'element-plus'
import { storeToRefs } from 'pinia'
import { computed, onMounted, shallowRef, useTemplateRef, watch } from 'vue'
import { useRouter } from 'vue-router'
import { addApp } from '@/api'
import { useUserStore } from '@/stores/user'
import { errorMessage, readData } from '@/utils/response'
import AppCard from './components/AppCard.vue'
import PromptBox from './components/PromptBox.vue'
import { useMyApps } from './composables/useMyApps'

const router = useRouter()
const { profile } = storeToRefs(useUserStore())
const { apps, loading, loaded, pager, loadApps, changePage, reset } = useMyApps()

const promptBox = useTemplateRef<InstanceType<typeof PromptBox>>('promptBox')
const listSection = useTemplateRef<HTMLElement>('listSection')
const creating = shallowRef(false)

const showSkeleton = computed(() => loading.value && !loaded.value)
const showEmpty = computed(() => loaded.value && !loading.value && apps.value.length === 0)

function goLogin() {
  void router.push({ name: 'login', query: { redirect: '/' } })
}

async function onCreate(prompt: string) {
  if (!profile.value) {
    ElMessage.warning('请先登录')
    goLogin()
    return
  }
  if (creating.value) {
    return
  }
  creating.value = true
  try {
    // 后端 Long 序列化成字符串,这里统一转成 string 作为路由参数
    const id = String(readData<number | string>(await addApp({ body: { initPrompt: prompt } })))
    promptBox.value?.clear()
    ElMessage.success('应用已创建')
    await router.push({ name: 'appDetail', params: { id } })
  } catch (error) {
    ElMessage.error(errorMessage(error))
  } finally {
    creating.value = false
  }
}

async function onPageChange(pageNum: number) {
  await changePage(pageNum)
  listSection.value?.scrollIntoView({ block: 'start' })
}

function openApp(id: string) {
  void router.push({ name: 'appDetail', params: { id } })
}

watch(profile, (value) => {
  if (value) {
    void loadApps()
  } else {
    reset()
  }
})

onMounted(() => {
  if (profile.value) {
    void loadApps()
  }
})
</script>

<template>
  <section class="home-page">
    <div class="home-inner">
      <header class="hero">
        <h1 class="hero-title">
          一句话,生成你的<span class="hero-accent">应用</span>
        </h1>
        <p class="hero-sub">描述你的想法,AI 帮你写好页面代码,随时预览和部署。</p>
      </header>

      <PromptBox ref="promptBox" :loading="creating" @submit="onCreate" />

      <section ref="listSection" class="apps" aria-labelledby="apps-heading">
        <div class="apps-header">
          <h2 id="apps-heading" class="apps-title">我的应用</h2>
          <span v-if="profile && pager.total > 0" class="apps-count">共 {{ pager.total }} 个</span>
        </div>

        <div v-if="!profile" class="state">
          <span class="state-icon" aria-hidden="true"><el-icon :size="26"><Lock /></el-icon></span>
          <p class="state-text">登录后即可创建并查看你的应用</p>
          <el-button type="primary" @click="goLogin">去登录</el-button>
        </div>

        <ul v-else-if="showSkeleton" class="grid" aria-busy="true" aria-label="加载中">
          <li v-for="n in 8" :key="n" class="skeleton-card">
            <el-skeleton animated>
              <template #template>
                <el-skeleton-item variant="image" class="skeleton-cover" />
                <div class="skeleton-info">
                  <el-skeleton-item variant="h3" style="width: 60%" />
                  <el-skeleton-item variant="text" style="width: 40%" />
                </div>
              </template>
            </el-skeleton>
          </li>
        </ul>

        <div v-else-if="showEmpty" class="state">
          <span class="state-icon" aria-hidden="true"><el-icon :size="26"><FolderOpened /></el-icon></span>
          <p class="state-text">还没有应用,从上面的输入框开始创建吧</p>
        </div>

        <template v-else>
          <ul class="grid" :class="{ 'is-loading': loading }">
            <li v-for="item in apps" :key="item.id">
              <AppCard :app="item" @open="openApp" />
            </li>
          </ul>

          <div v-if="pager.total > pager.pageSize" class="pager">
            <el-pagination
              :current-page="pager.pageNum"
              :page-size="pager.pageSize"
              :total="pager.total"
              layout="prev, pager, next"
              background
              hide-on-single-page
              @current-change="onPageChange"
            />
          </div>
        </template>
      </section>
    </div>
  </section>
</template>

<style scoped>
.home-page {
  /* 仅本页的 Element 组件(分页、骨架屏等)使用更圆润的圆角 */
  --el-border-radius-base: 8px;
  /* 抵消 el-main 的 20px 内边距,让背景铺满主区域 */
  box-sizing: border-box;
  min-height: calc(100% + 40px);
  margin: -20px;
  padding: 56px 20px 64px;
  /* 光晕取自 logo:青蓝环、粉紫环、紫罗兰主体,右下角一点金色 */
  background:
    radial-gradient(55% 45% at 12% 0%, rgba(34, 211, 238, 0.16), transparent 70%),
    radial-gradient(50% 45% at 88% 4%, rgba(244, 114, 182, 0.18), transparent 70%),
    radial-gradient(55% 40% at 50% 18%, rgba(139, 92, 246, 0.14), transparent 70%),
    radial-gradient(40% 30% at 100% 100%, rgba(251, 191, 36, 0.1), transparent 70%),
    var(--el-bg-color-page);
}

.home-inner {
  max-width: 1200px;
  margin: 0 auto;
}

.hero {
  margin-bottom: 32px;
  text-align: center;
}

.hero-title {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: clamp(30px, 5vw, 46px);
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: 0.02em;
  text-wrap: balance;
}

.hero-accent {
  background: var(--yy-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-sub {
  margin: 14px 0 0;
  color: var(--el-text-color-regular);
  font-size: 16px;
}

.apps {
  margin-top: 56px;
  scroll-margin-top: 80px;
}

.apps-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 20px;
}

.apps-title {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  font-size: 22px;
  font-weight: 700;
}

.apps-title::before {
  content: '';
  width: 6px;
  height: 22px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--yy-cyan), var(--yy-violet-soft) 55%, var(--yy-pink-soft));
}

.apps-count {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  margin: 0;
  padding: 0;
  list-style: none;
  transition: opacity 0.2s ease;
}

.grid.is-loading {
  opacity: 0.6;
  pointer-events: none;
}

.skeleton-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: var(--yy-radius-md);
  background: var(--el-bg-color);
  overflow: hidden;
}

.skeleton-cover {
  width: 100%;
  height: auto;
  aspect-ratio: 16 / 10;
}

.skeleton-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 16px 16px;
}

.state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 16px;
  border: 1.5px dashed var(--yy-line-strong);
  border-radius: var(--yy-radius-md);
  background: rgba(255, 255, 255, 0.7);
}

.state-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: var(--yy-radius-md);
  color: var(--yy-violet);
  background: var(--el-color-primary-light-9);
  box-shadow: var(--yy-inner-light), var(--yy-shadow-sm);
}

.state-text {
  margin: 0;
  color: var(--el-text-color-regular);
  font-size: 15px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

@media (max-width: 640px) {
  .home-page {
    padding-top: 32px;
  }

  .apps {
    margin-top: 40px;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}

@media (prefers-reduced-motion: reduce) {
  .grid {
    transition: none;
  }
}
</style>
