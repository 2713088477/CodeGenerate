import { onBeforeUnmount, shallowRef, watch } from 'vue'
import { highlightCode } from '@/utils/highlight'
import type { GenFile } from './useAppChat'

// 流式过程中全量高亮的开销随代码变长而增大,所以限制刷新频率;写完后再做一次完整高亮
const HIGHLIGHT_INTERVAL = 120

/**
 * 把正在流式写入的文件转成高亮后的 HTML。
 * @param file 当前文件,可能为空
 * @param onRendered 每次刷新高亮后调用,调用方可以借机滚动到底部
 */
export function useHighlightedCode(file: () => GenFile | undefined, onRendered?: () => void) {
  const html = shallowRef('')
  let timer = 0
  let lastRender = 0
  let lastKey: string | undefined

  function render() {
    timer = 0
    lastRender = performance.now()
    const current = file()
    html.value = current ? highlightCode(current.content, current.language) : ''
    onRendered?.()
  }

  watch(
    () => {
      const current = file()
      return [current?.key, current?.content, current?.done] as const
    },
    ([key, , done]) => {
      // 换了文件、写完了、或者是第一次:立即渲染
      if (key !== lastKey || done || lastRender === 0) {
        lastKey = key
        window.clearTimeout(timer)
        render()
        return
      }
      if (!timer) {
        timer = window.setTimeout(render, Math.max(0, HIGHLIGHT_INTERVAL - (performance.now() - lastRender)))
      }
    },
    { immediate: true },
  )

  onBeforeUnmount(() => window.clearTimeout(timer))

  return { html }
}
