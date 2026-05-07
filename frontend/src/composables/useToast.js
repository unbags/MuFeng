import { uiState } from './state.js'
import { TOAST_DURATION_MS } from '../config/constants.js'

let toastTimer = null

export function showToast(message) {
  uiState.toastMessage = message
  if (toastTimer) clearTimeout(toastTimer)
  toastTimer = setTimeout(() => {
    uiState.toastMessage = ''
  }, TOAST_DURATION_MS)
}

export function useToast() {
  return { showToast }
}
