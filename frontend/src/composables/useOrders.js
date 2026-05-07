import { computed } from 'vue'
import { adminState, uiState } from './state.js'
import { showToast } from './useToast.js'
import { fetchAdminOrders, fetchAdminOrderDetail } from '../api/orders.js'

const recentOrders = computed(() =>
  (Array.isArray(adminState.orders) ? adminState.orders : []).slice(0, 8),
)

async function openOrderDetail(orderNo) {
  if (!orderNo) return

  try {
    uiState.showOrderDetail = true
    uiState.orderDetailLoading = true
    uiState.orderDetailError = ''
    uiState.selectedOrderDetail = null
    uiState.selectedOrderDetail = await fetchAdminOrderDetail(orderNo)
  } catch (error) {
    uiState.orderDetailError = error.message || '订单详情加载失败'
    showToast(uiState.orderDetailError)
  } finally {
    uiState.orderDetailLoading = false
  }
}

function closeOrderDetail() {
  uiState.showOrderDetail = false
  uiState.selectedOrderDetail = null
  uiState.orderDetailLoading = false
  uiState.orderDetailError = ''
}

export function useOrders() {
  return {
    recentOrders,
    openOrderDetail,
    closeOrderDetail,
  }
}
