import { computed, onScopeDispose, ref } from 'vue'
import { chatToGenCode } from '@/api'
import { errorMessage } from '@/utils/response'
import { StreamJsonParser, type StreamJsonSnapshot } from '@/utils/streamJson'

export type MessageStatus = 'connecting' | 'streaming' | 'done' | 'error' | 'stopped'
export type CodeLanguage = 'xml' | 'css' | 'javascript'

export interface GenFile {
  key: string
  name: string
  language: CodeLanguage
  content: string
  /** 这个文件已经写完(收到了字段的结束引号) */
  done: boolean
}

export interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  /** 用户消息的文本 */
  content: string
  status: MessageStatus
  files: GenFile[]
  description: string
  descriptionDone: boolean
  error: string
  /** 重新进入页面时补出来的静态回复,没有流式内容 */
  restored: boolean
}

/** 模型 JSON 里的字段 -> 对应的文件 */
const FILE_FIELDS: Record<string, { name: string; language: CodeLanguage }> = {
  htmlCode: { name: 'index.html', language: 'xml' },
  cssCode: { name: 'style.css', language: 'css' },
  jsCode: { name: 'script.js', language: 'javascript' },
}

let nextId = 1

function createMessage(role: ChatMessage['role'], content = ''): ChatMessage {
  return {
    id: nextId++,
    role,
    content,
    status: role === 'assistant' ? 'connecting' : 'done',
    files: [],
    description: '',
    descriptionDone: false,
    error: '',
    restored: false,
  }
}

function lastByRole(list: ChatMessage[], role: ChatMessage['role']) {
  for (let i = list.length - 1; i >= 0; i--) {
    if (list[i]!.role === role) {
      return list[i]
    }
  }
  return undefined
}

function applySnapshot(message: ChatMessage, snapshot: StreamJsonSnapshot) {
  message.files = snapshot.order
    .filter((key) => key in FILE_FIELDS)
    .map((key) => ({
      key,
      name: FILE_FIELDS[key]!.name,
      language: FILE_FIELDS[key]!.language,
      content: snapshot.fields[key] ?? '',
      done: snapshot.closed.has(key),
    }))
  message.description = snapshot.fields.description ?? ''
  message.descriptionDone = snapshot.closed.has('description')
}

/**
 * 对话生成的状态机:发送消息 -> 消费 SSE -> 把流式 JSON 拆成文件和说明。
 * 事件约定见后端 AppServiceImpl#chatToGenCode:ready -> 若干 {"v":"片段"} -> done / error。
 */
export function useAppChat(options: { appId: () => string; onDone?: () => void }) {
  const messages = ref<ChatMessage[]>([])
  let controller: AbortController | null = null

  const latestAssistant = computed(() => lastByRole(messages.value, 'assistant'))
  const generating = computed(() => {
    const status = latestAssistant.value?.status
    return status === 'connecting' || status === 'streaming'
  })

  function seedRestored(prompt: string) {
    const user = createMessage('user', prompt)
    const assistant = createMessage('assistant')
    assistant.status = 'done'
    assistant.restored = true
    messages.value = [user, assistant]
  }

  async function run(text: string) {
    messages.value.push(createMessage('user', text), createMessage('assistant'))
    const assistant = messages.value[messages.value.length - 1]!

    const parser = new StreamJsonParser()
    const abort = new AbortController()
    controller = abort

    let snapshot: StreamJsonSnapshot | null = null
    let frame = 0
    let sawDone = false
    let serverError = ''
    let streamError = ''

    // 分片很密,按动画帧合并后再写入响应式状态,避免每个分片都触发一次渲染
    const flush = () => {
      frame = 0
      if (snapshot) {
        applySnapshot(assistant, snapshot)
      }
    }
    const schedule = () => {
      if (!frame) {
        frame = requestAnimationFrame(flush)
      }
    }

    try {
      const { stream } = await chatToGenCode({
        body: { appId: options.appId(), message: text },
        // 生成的 axios 客户端没有默认的请求体序列化器,SSE 走 fetch,不指定的话 body 会变成 "[object Object]"
        bodySerializer: (body) => JSON.stringify(body),
        signal: abort.signal,
        // 默认会在出错后无限重试,而重试会重新触发一次完整的生成,必须关掉
        sseMaxRetryAttempts: 1,
        onSseError: (error) => {
          streamError = errorMessage(error)
        },
        onSseEvent: (event) => {
          if (event.event === 'ready') {
            assistant.status = 'streaming'
          } else if (event.event === 'done') {
            sawDone = true
          } else if (event.event === 'error') {
            serverError = (event.data as { message?: string } | undefined)?.message || '代码生成失败,请稍后重试'
          } else if (event.event === undefined) {
            const piece = (event.data as { v?: unknown } | undefined)?.v
            if (typeof piece === 'string') {
              snapshot = parser.push(piece)
              schedule()
            }
          }
        },
      })
      // 数据已经在 onSseEvent 里处理,这里只需要把流读完
      for await (const _ of stream) {
        void _
      }
    } catch (error) {
      streamError = errorMessage(error)
    }

    if (frame) {
      cancelAnimationFrame(frame)
    }
    flush()
    controller = null

    const finished = assistant.files.map((file) => ({ ...file, done: true }))
    if (abort.signal.aborted) {
      assistant.status = 'stopped'
      assistant.files = finished
      assistant.descriptionDone = true
    } else if (sawDone) {
      assistant.status = 'done'
      assistant.files = finished
      assistant.descriptionDone = true
      options.onDone?.()
    } else {
      // 没收到 done 就结束了:要么服务端报了错,要么连接被中途断开
      assistant.status = 'error'
      assistant.error = serverError || streamError || '连接中断,请重试'
      assistant.files = finished
    }
  }

  async function send(text: string) {
    const value = text.trim()
    if (!value || generating.value) {
      return
    }
    await run(value)
  }

  function stop() {
    controller?.abort()
  }

  async function retry() {
    if (generating.value) {
      return
    }
    const list = messages.value
    const lastUser = lastByRole(list, 'user')
    if (!lastUser) {
      return
    }
    // 去掉最后一轮(失败的回复和对应的提问),再重新发一遍
    const index = list.lastIndexOf(lastUser)
    list.splice(index)
    await run(lastUser.content)
  }

  onScopeDispose(stop)

  return { messages, generating, latestAssistant, seedRestored, send, stop, retry }
}
