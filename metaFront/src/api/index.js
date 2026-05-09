const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

function friendlyMessage(error) {
  if (!error || !error.message) return '操作失败'
  const msg = error.message
  if (msg.includes('NetworkError') || msg.includes('Failed to fetch')) return '网络连接失败，请检查网络'
  if (msg.includes('timeout')) return '请求超时，请稍后重试'
  return msg
}

export async function request(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers ?? {}),
  }

  const response = await fetch(`${API_BASE}${path}`, {
    headers,
    ...options,
  })

  if (!response.ok) {
    let message = '请求失败'
    try {
      const errorBody = await response.json()
      message = errorBody.message || message
    } catch {
      message = `${message} (${response.status})`
    }
    throw new Error(message)
  }

  if (response.status === 204) return null

  const text = await response.text()
  if (!text) return null

  const body = JSON.parse(text)

  if (body && typeof body === 'object' && 'code' in body && 'data' in body) {
    if (body.code !== 200) {
      throw new Error(body.message || '请求失败')
    }
    return body.data
  }

  return body
}

export function friendlyError(error) {
  return friendlyMessage(error)
}
