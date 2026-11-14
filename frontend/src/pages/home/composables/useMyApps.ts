import { pageMyApps } from '@/api'
import type { AppVo, PageResultAppVo } from '@/api'
import { errorMessage, readData } from '@/utils/response'
import { ElMessage } from 'element-plus'
import { reactive, shallowRef } from 'vue'

export const APP_PAGE_SIZE = 12

/** 当前用户的 app 分页列表。 */
export function useMyApps() {
  const apps = shallowRef<AppVo[]>([])
  const loading = shallowRef(false)
  const loaded = shallowRef(false)
  const pager = reactive({ pageNum: 1, pageSize: APP_PAGE_SIZE, total: 0 })

  async function loadApps() {
    loading.value = true
    try {
      const result = await pageMyApps({
        query: { query: { pageNum: pager.pageNum, pageSize: pager.pageSize } },
        // 生成类型把条件包在 query 对象里，Spring 按字段名绑定，所以展开成顶层参数
        querySerializer: {
          object: { explode: true, style: 'form' },
        },
      })
      const data = readData<PageResultAppVo>(result)
      apps.value = data.records ?? []
      pager.total = data.total ?? 0
    } catch (error) {
      apps.value = []
      pager.total = 0
      ElMessage.error(errorMessage(error))
    } finally {
      loading.value = false
      loaded.value = true
    }
  }

  function changePage(pageNum: number) {
    pager.pageNum = pageNum
    return loadApps()
  }

  function reset() {
    apps.value = []
    pager.pageNum = 1
    pager.total = 0
    loaded.value = false
  }

  return { apps, loading, loaded, pager, loadApps, changePage, reset }
}

/** 2026-11-14T16:15:00 -> 2026-11-14 16:15 */
export function formatTime(value?: string) {
  if (!value) {
    return ''
  }
  return value.replace('T', ' ').slice(0, 16)
}
