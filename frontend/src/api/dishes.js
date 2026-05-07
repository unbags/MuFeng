import { request } from './index.js'

export function fetchAdminDishes() {
  return request('/admin/dishes')
}

export function createAdminDish(payload) {
  return request('/admin/dishes', {
    method: 'POST',
    body: JSON.stringify(payload),
  })
}

export function updateAdminDish(dishId, payload) {
  return request(`/admin/dishes/${dishId}`, {
    method: 'PUT',
    body: JSON.stringify(payload),
  })
}

export function uploadAdminDishImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request('/admin/dishes/upload', {
    method: 'POST',
    body: formData,
  })
}

export function toggleDishAvailability(dishId, available) {
  return request(`/admin/dishes/${dishId}/availability?available=${available}`, {
    method: 'PATCH',
  })
}

export function deleteAdminDish(dishId) {
  return request(`/admin/dishes/${dishId}`, {
    method: 'DELETE',
  })
}
