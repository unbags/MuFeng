import { request, API_BASE } from './index.js'
import { cartHeaders } from '../utils/cartSession.js'

export function queryChat(payload) {
  return request('/chat/query', {
    method: 'POST',
    headers: cartHeaders(),
    body: JSON.stringify(payload),
  })
}

export function streamChat(payload, onChunk, onDone, onError, onAction) {
  const controller = new AbortController()

  fetch(`${API_BASE}/chat/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...cartHeaders() },
    credentials: 'include',
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
        const events = buffer.split('\n\n')
        buffer = events.pop() || ''
        for (const rawEvent of events) {
          const event = parseSseEvent(rawEvent)
          if (event.type === 'message' && event.data) {
            receivedChunk = true
            onChunk(event.data)
          } else if (event.type === 'actions' && event.data && onAction) {
            onAction(JSON.parse(event.data))
          } else if (event.type === 'done') {
            receivedChunk = true
          } else if (!event.type && event.data) {
            receivedChunk = true
            onChunk(event.data)
          }
        }
      }

      const finalEvent = parseSseEvent(buffer)
      if (finalEvent.type === 'message' && finalEvent.data) {
        receivedChunk = true
        onChunk(finalEvent.data)
      } else if (finalEvent.type === 'actions' && finalEvent.data && onAction) {
        onAction(JSON.parse(finalEvent.data))
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

function parseSseEvent(rawEvent) {
  const event = { type: '', data: '' }
  if (!rawEvent) return event
  for (const line of rawEvent.split('\n')) {
    if (line.startsWith('event:')) {
      event.type = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      const value = line.slice(5).trimStart()
      if (value && value !== '[DONE]') {
        event.data += event.data ? `\n${value}` : value
      }
    }
  }
  return event
}
