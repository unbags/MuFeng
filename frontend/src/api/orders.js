import { request } from './index.js'

export function createOrder(payload) {
  return request('/orders', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function fetchAdminOrders() {
  return request('/admin/orders').then(normalizeAdminOrders)
}

export function fetchAdminOrderDetail(orderNo) {
  return request(`/admin/orders/${encodeURIComponent(orderNo)}`)
}

function normalizeAdminOrders(response) {
  if (Array.isArray(response)) return response
  if (response && Array.isArray(response.items)) return response.items
  return []
}
