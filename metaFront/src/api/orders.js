import { request } from './index.js'

export function createOrder(payload) {
  return request('/orders', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function fetchOrder(orderNo) {
  return request(`/orders/${encodeURIComponent(orderNo)}`)
}
