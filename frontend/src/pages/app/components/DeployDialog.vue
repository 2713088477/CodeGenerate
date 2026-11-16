<template>
  <el-dialog v-model="visible" title="部署成功" width="460px" align-center append-to-body>
    <div class="result">
      <span class="badge" aria-hidden="true">
        <el-icon :size="28"><CircleCheckFilled /></el-icon>
      </span>
      <p class="lead">你的应用已经部署,可以通过下面的链接访问。</p>
    </div>

    <el-input :model-value="url" readonly aria-label="应用访问链接" @focus="($event.target as HTMLInputElement).select()">
      <template #append>
        <el-button aria-label="复制链接" @click="copy">
          <el-icon aria-hidden="true"><CopyDocument /></el-icon>
        </el-button>
      </template>
    </el-input>

    <p class="tip">链接依赖 Nginx 托管部署目录,服务器没有配置 Nginx 时可能打不开。重新部署会保留同一个链接。</p>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" @click="open">
        <el-icon aria-hidden="true"><TopRight /></el-icon>
        打开应用
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { CircleCheckFilled, CopyDocument, TopRight } from '@element-plus/icons-vue'
import { ElButton, ElDialog, ElIcon, ElInput, ElMessage } from 'element-plus'

const props = defineProps<{
  url: string
}>()

const visible = defineModel<boolean>({ required: true })

async function copy() {
  try {
    await navigator.clipboard.writeText(props.url)
    ElMessage.success('链接已复制')
  } catch {
    ElMessage.error('复制失败,请手动选择链接复制')
  }
}

function open() {
  window.open(props.url, '_blank', 'noopener')
}
</script>

<style scoped>
.result {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.badge {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: var(--yy-radius-md);
  color: #16a34a;
  background: #dcfce7;
}

.lead {
  margin: 0;
  color: var(--el-text-color-regular);
  font-size: 14px;
  line-height: 1.6;
}

.tip {
  margin: 12px 0 0;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.6;
}
</style>
