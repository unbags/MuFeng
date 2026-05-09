import { request } from './index.js'

export function fetchMenu() {
  return request('/menu')
}
