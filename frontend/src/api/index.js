import { HTTP_ERROR_MESSAGES } from '../config/constants.js'

let _router = null

export function setRouter(router) {
  _router = router
}

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

function friendlyMessage(error) {
  if (!error || !error.message) return '操作失败'
  const msg = error.message
  if (msg.includes('NetworkError') || msg.includes('Failed to fetch')) return '网络连接失败，请检查网络'
  if (msg.includes('timeout')) return '请求超时，请稍后重试'
  for (const [code, text] of Object.entries(HTTP_ERROR_MESSAGES)) {
    if (msg.includes(`(${code})`) || msg.includes(code)) return text
  }
  return msg
}

export async function request(path, options = {}) {
  const token = localStorage.getItem('token')

  const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData

  const headers = isFormData
    ? { ...(options.headers ?? {}) }
    : {
        'Content-Type': 'application/json',
        ...(options.headers ?? {}),
      }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const response = await fetch(`${API_BASE}${path}`, {
    headers,
    ...options,
  })

  // Handle 401 Unauthorized
  if (response.status === 401) {
    let serverMsg = '未授权，请先登录'
    try {
      const errorBody = await response.json()
      serverMsg = errorBody.message || serverMsg
    } catch {}
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    const isAuthPage = _router && ['/login', '/register'].includes(_router.currentRoute?.value?.path)
    if (_router && !isAuthPage) {
      _router.push(`/login?redirect=${encodeURIComponent(_router.currentRoute.value?.path || '/dashboard')}`)
    }
    throw new Error(serverMsg)
  }

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
