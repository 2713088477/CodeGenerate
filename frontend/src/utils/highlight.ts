import hljs from 'highlight.js/lib/core'
import css from 'highlight.js/lib/languages/css'
import javascript from 'highlight.js/lib/languages/javascript'
import xml from 'highlight.js/lib/languages/xml'

// 只注册生成结果会用到的三种语言,避免把整个 highlight.js 打进包里
hljs.registerLanguage('xml', xml)
hljs.registerLanguage('css', css)
hljs.registerLanguage('javascript', javascript)

export function highlightCode(code: string, language: 'xml' | 'css' | 'javascript') {
  return hljs.highlight(code, { language, ignoreIllegals: true }).value
}
