import type { BaseResponseVoid } from '@/api'

function isEnvelope(value: unknown): value is BaseResponseVoid {
  return !!value && typeof value === 'object' && 'code' in value
}

function readEnvelope(result: unknown): BaseResponseVoid | undefined {
  if (!result || typeof result !== 'object') {
    return undefined
  }
  const record = result as { data?: unknown; response?: { data?: unknown } }
  if (isEnvelope(record.data)) {
    return record.data
  }
  if (isEnvelope(record.response?.data)) {
    return record.response.data
  }
  return undefined
}

export function errorMessage(error: unknown) {
  const body = readEnvelope(error)
  if (body?.message) {
    return body.message
  }
  if (error instanceof Error && error.message) {
    return error.message
  }
  return '请求失败'
}

export function ensureSuccess(result: unknown) {
  const body = readEnvelope(result)
  if (body?.code === 0) {
    return
  }
  throw new Error(body?.message || '请求失败')
}

export function readData<T>(result: unknown): T {
  const body = readEnvelope(result)
  if (body?.code === 0 && body.data != null) {
    return body.data as T
  }
  throw new Error(body?.message || '请求失败')
}
