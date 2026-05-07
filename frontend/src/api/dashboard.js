import { request } from './index.js'

export function fetchAdminDashboard() {
  return request('/admin/dashboard')
}

export function fetchProductSales(range = 'week') {
  return request(`/admin/dashboard/product-sales?range=${encodeURIComponent(range)}`)
}
