import { request } from './index.js'

export function login(username, password) {
  return request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

export function register(username, password, displayName) {
  return request('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, password, displayName }),
  })
}

export function fetchAuthStatus() {
  return request('/auth/status')
}
