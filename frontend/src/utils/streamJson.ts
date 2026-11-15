/**
 * 增量解析流式 JSON 里的顶层字符串字段。
 *
 * 后端把模型输出的 JSON 文本分片推过来,例如:
 *   {"htmlCode":"<!DOCTYPE html>...","cssCode":"...","jsCode":"...","description":"..."}
 * 完整文本到最后才是合法 JSON,但界面需要边收边展示每个字段的当前内容。
 * 这里逐字符推进一个小状态机,只关心深度为 1 的 "key": "string value",
 * 能处理转义序列(\n \" \uXXXX 等),也能处理转义被切在两个分片之间的情况。
 */

export interface StreamJsonSnapshot {
  /** 已经收到的各字段内容(正在写的字段是不完整的) */
  fields: Record<string, string>
  /** 字段首次出现的顺序 */
  order: string[]
  /** 已经写完(收到结束引号)的字段 */
  closed: Set<string>
  /** 正在写入的字段,没有则为 null */
  active: string | null
}

type Phase = 'key' | 'colon' | 'value'
type StringRole = 'key' | 'value' | 'skip'

const SIMPLE_ESCAPES: Record<string, string> = {
  n: '\n',
  t: '\t',
  r: '\r',
  b: '\b',
  f: '\f',
  '/': '/',
  '\\': '\\',
  '"': '"',
}

export class StreamJsonParser {
  private depth = 0
  private phase: Phase = 'key'
  private inString = false
  private role: StringRole = 'skip'
  private escaping = false
  private unicode: string | null = null
  private keyBuffer = ''
  private currentKey = ''
  private fields: Record<string, string> = {}
  private order: string[] = []
  private closed = new Set<string>()
  private active: string | null = null

  /** 喂入一个新分片,返回当前快照 */
  push(chunk: string): StreamJsonSnapshot {
    // 分片内攒在局部变量里,最后一次性拼进字段,避免逐字符拼接大字符串
    let pending = ''
    const flush = () => {
      if (pending && this.role === 'value') {
        this.fields[this.currentKey] += pending
      }
      pending = ''
    }

    for (const c of chunk) {
      if (this.inString) {
        if (this.unicode !== null) {
          this.unicode += c
          if (this.unicode.length === 4) {
            const code = Number.parseInt(this.unicode, 16)
            this.unicode = null
            if (!Number.isNaN(code)) {
              this.append(String.fromCharCode(code), (s) => (pending += s))
            }
          }
          continue
        }
        if (this.escaping) {
          this.escaping = false
          if (c === 'u') {
            this.unicode = ''
          } else {
            this.append(SIMPLE_ESCAPES[c] ?? c, (s) => (pending += s))
          }
          continue
        }
        if (c === '\\') {
          this.escaping = true
          continue
        }
        if (c === '"') {
          flush()
          this.inString = false
          if (this.role === 'key') {
            this.currentKey = this.keyBuffer
            this.phase = 'colon'
          } else if (this.role === 'value') {
            this.closed.add(this.currentKey)
            this.active = null
            this.phase = 'key'
          }
          continue
        }
        this.append(c, (s) => (pending += s))
        continue
      }

      switch (c) {
        case '"':
          this.inString = true
          if (this.depth === 1 && this.phase === 'key') {
            this.role = 'key'
            this.keyBuffer = ''
          } else if (this.depth === 1 && this.phase === 'value') {
            this.role = 'value'
            this.active = this.currentKey
            if (!Object.hasOwn(this.fields, this.currentKey)) {
              this.fields[this.currentKey] = ''
              this.order.push(this.currentKey)
            }
          } else {
            this.role = 'skip'
          }
          break
        case '{':
        case '[':
          this.depth++
          break
        case '}':
        case ']':
          this.depth--
          break
        case ':':
          if (this.depth === 1 && this.phase === 'colon') {
            this.phase = 'value'
          }
          break
        case ',':
          if (this.depth === 1) {
            this.phase = 'key'
          }
          break
        default:
          break
      }
    }
    flush()
    return this.snapshot()
  }

  snapshot(): StreamJsonSnapshot {
    return {
      fields: { ...this.fields },
      order: [...this.order],
      closed: new Set(this.closed),
      active: this.active,
    }
  }

  private append(text: string, sink: (s: string) => void) {
    if (this.role === 'key') {
      this.keyBuffer += text
    } else if (this.role === 'value') {
      sink(text)
    }
  }
}
