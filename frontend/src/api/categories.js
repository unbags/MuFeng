import { request } from './index.js'

export function fetchAdminCategories() {
  return request('/admin/categories')
}

export function createAdminCategory(payload) {
  return request('/admin/categories', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateAdminCategory(categoryId, payload) {
  return request(`/admin/categories/${encodeURIComponent(categoryId)}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function deleteAdminCategory(categoryId) {
  return request(`/admin/categories/${encodeURIComponent(categoryId)}`, {
    method: 'DELETE',
  })
}
