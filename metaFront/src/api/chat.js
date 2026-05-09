import { request } from './index.js'

export function queryChat(payload) {
  return request('/chat/query', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}
