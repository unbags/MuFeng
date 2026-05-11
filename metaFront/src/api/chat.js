import { request } from './index.js'

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'

export function queryChat(payload) {
  return request('/chat/query', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function streamChat(payload, onChunk, onDone, onError) {
  const controller = new AbortController()

  fetch(`${API_BASE}/chat/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
    signal: controller.signal,
  })
    .then(async (response) => {
      if (!response.ok) {
        const errorBody = await response.json().catch(() => ({}))
        throw new Error(errorBody.message || `请求失败 (${response.status})`)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let receivedChunk = false

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })
        const lines = buffer.split('\n')
        buffer = lines.pop() || ''
        for (const line of lines) {
          const chunk = parseSseDataLine(line)
          if (chunk) {
            receivedChunk = true
            onChunk(chunk)
          }
        }
      }

      const finalChunk = parseSseDataLine(buffer)
      if (finalChunk) {
        receivedChunk = true
        onChunk(finalChunk)
      }

      if (!receivedChunk) {
        throw new Error('智能助手暂无回复')
      }

      onDone()
    })
    .catch((error) => {
      if (error.name !== 'AbortError') {
        onError(error)
      }
    })

  return controller
}

function parseSseDataLine(line) {
  if (!line.startsWith('data:')) return ''
  const value = line.slice(5).trimStart()
  if (!value || value === '[DONE]') return ''
  return value
}
