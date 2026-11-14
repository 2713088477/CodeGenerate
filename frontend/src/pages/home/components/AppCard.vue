<script setup lang="ts">
import { Clock } from '@element-plus/icons-vue'
import { ElIcon } from 'element-plus'
import { computed, shallowRef, watch } from 'vue'
import type { AppVo } from '@/api'
import { formatTime } from '../composables/useMyApps'

const COVER_GRADIENTS = [
  ['#5b6cf5', '#8b5cf6'],
  ['#8b5cf6', '#f472b6'],
  ['#22d3ee', '#5b6cf5'],
  ['#7c3aed', '#ec4899'],
  ['#38bdf8', '#a78bfa'],
  ['#f472b6', '#fb923c'],
]

const props = defineProps<{
  app: AppVo
}>()

const emit = defineEmits<{
  open: [id: string]
}>()

const coverFailed = shallowRef(false)
watch(
  () => props.app.cover,
  () => {
    coverFailed.value = false
  },
)

const name = computed(() => props.app.appName?.trim() || '未命名应用')
const showImage = computed(() => !!props.app.cover && !coverFailed.value)
const initial = computed(() => Array.from(name.value)[0] ?? '?')
const createdAt = computed(() => formatTime(props.app.createdAt))

// 按 id 做稳定哈希,同一个 app 每次渲染的占位封面颜色一致
const placeholderStyle = computed(() => {
  const key = props.app.id ?? name.value
  let hash = 0
  for (const ch of key) {
    hash = (hash * 31 + ch.charCodeAt(0)) >>> 0
  }
  hash = Math.imul(hash ^ (hash >>> 15), 2246822507) >>> 0
  const [from, to] = COVER_GRADIENTS[hash % COVER_GRADIENTS.length]!
  const angle = 120 + (hash % 5) * 15
  return { background: `linear-gradient(${angle}deg, ${from}, ${to})` }
})

function onOpen() {
  if (props.app.id) {
    emit('open', props.app.id)
  }
}
</script>

<template>
  <article class="app-card">
    <button class="card-button" type="button" :aria-label="`打开应用 ${name}`" @click="onOpen">
      <div class="cover">
        <img
          v-if="showImage"
          class="cover-img"
          :src="app.cover"
          alt=""
          loading="lazy"
          @error="coverFailed = true"
        />
        <div v-else class="cover-placeholder" :style="placeholderStyle" aria-hidden="true">
          <span class="cover-glow" />
          <span class="cover-initial">{{ initial }}</span>
        </div>
      </div>
      <div class="info">
        <h3 class="name" :title="name">{{ name }}</h3>
        <p class="time">
          <el-icon aria-hidden="true"><Clock /></el-icon>
          <time :datetime="app.createdAt">{{ createdAt }}</time>
        </p>
      </div>
    </button>
  </article>
</template>

<style scoped>
.app-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: var(--yy-radius-md);
  background: var(--el-bg-color);
  box-shadow: var(--yy-shadow-sm);
  overflow: hidden;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.app-card:hover,
.app-card:focus-within {
  border-color: var(--yy-line-strong);
  box-shadow: var(--yy-shadow-lg);
  transform: translateY(-4px);
}

.card-button {
  display: block;
  width: 100%;
  padding: 0;
  border: 0;
  color: inherit;
  font: inherit;
  text-align: left;
  background: transparent;
  cursor: pointer;
}

.card-button:focus-visible {
  outline: 2px solid var(--yy-focus);
  outline-offset: -2px;
  border-radius: 16px;
}

.cover {
  aspect-ratio: 16 / 10;
  overflow: hidden;
  background: var(--el-fill-color-light);
}

.cover-img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.cover-glow {
  position: absolute;
  top: -30%;
  right: -10%;
  width: 70%;
  aspect-ratio: 1;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.35), transparent 70%);
}

.cover-initial {
  position: relative;
  color: rgba(255, 255, 255, 0.95);
  font-size: 56px;
  font-weight: 700;
  line-height: 1;
  text-transform: uppercase;
  text-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
}

.info {
  padding: 14px 16px 16px;
}

.name {
  margin: 0;
  overflow: hidden;
  color: var(--el-text-color-primary);
  font-size: 16px;
  font-weight: 600;
  line-height: 1.4;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 6px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

@media (prefers-reduced-motion: reduce) {
  .app-card {
    transition: none;
  }

  .app-card:hover,
  .app-card:focus-within {
    transform: none;
  }
}
</style>
