import { request } from './index.js'
import { cartHeaders } from '../utils/cartSession.js'

export function fetchCart() {
  return request('/cart', {
    headers: cartHeaders(),
  })
}

export function addCartItem(payload) {
  return request('/cart/items', {
    method: 'POST',
    headers: cartHeaders(),
    body: JSON.stringify(payload),
  })
}

export function updateCartItem(dishId, payload) {
  return request(`/cart/items/${encodeURIComponent(dishId)}`, {
    method: 'PUT',
    headers: cartHeaders(),
    body: JSON.stringify(payload),
  })
}

export function removeCartItem(dishId, operationId) {
  const query = operationId ? `?operationId=${encodeURIComponent(operationId)}` : ''
  return request(`/cart/items/${encodeURIComponent(dishId)}${query}`, {
    method: 'DELETE',
    headers: cartHeaders(),
  })
}

export function clearBackendCart() {
  return request('/cart', {
    method: 'DELETE',
    headers: cartHeaders(),
  })
}
