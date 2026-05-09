import { request } from './index.js'

export function createOrder(payload) {
  return request('/orders', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function fetchAdminOrders(params = {}) {
  const query = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '' && value !== 'all') {
      query.set(key, value)
    }
  })
  const suffix = query.toString() ? `?${query.toString()}` : ''
  return request(`/admin/orders${suffix}`).then(normalizeAdminOrders)
}

export function fetchAdminOrderDetail(orderNo) {
  return request(`/admin/orders/${encodeURIComponent(orderNo)}`)
}

export function updateAdminOrderStatus(orderNo, status, reason = '') {
  return request(`/admin/orders/${encodeURIComponent(orderNo)}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status, reason, operator: 'staff' }),
  })
}

function normalizeAdminOrders(response) {
  if (Array.isArray(response)) return response
  if (response && Array.isArray(response.items)) return response.items
  return []
}
