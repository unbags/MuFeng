import { computed } from 'vue'
import { adminState, uiState } from './state.js'
import { showToast } from './useToast.js'
import { fetchAdminOrders, fetchAdminOrderDetail, updateAdminOrderStatus } from '../api/orders.js'

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

const nextOrderActions = computed(() => {
  const status = uiState.selectedOrderDetail?.status
  const actions = {
    PENDING: [
      { status: 'PREPARING', label: '开始制作' },
      { status: 'CANCELLED', label: '取消订单', reason: '店员取消' },
    ],
    PREPARING: [{ status: 'COMPLETED', label: '已完成' }],
  }
  return actions[status] || []
})

async function changeSelectedOrderStatus(status, reason = '') {
  const orderNo = uiState.selectedOrderDetail?.orderNo
  if (!orderNo || uiState.actionLoading) return

  try {
    uiState.actionLoading = true
    uiState.selectedOrderDetail = await updateAdminOrderStatus(orderNo, status, reason)
    adminState.orders = await fetchAdminOrders()
    showToast('订单状态已更新')
  } catch (error) {
    showToast(error.message || '订单状态更新失败')
  } finally {
    uiState.actionLoading = false
  }
}

export function useOrders() {
  return {
    recentOrders,
    nextOrderActions,
    openOrderDetail,
    closeOrderDetail,
    changeSelectedOrderStatus,
  }
}
