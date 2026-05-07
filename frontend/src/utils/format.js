export function formatPrice(value) {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 0,
  }).format(Number(value || 0))
}

export function formatOrderType(type) {
  return type === 'delivery' ? '外带' : '堂食'
}

const ORDER_STATUS_MAP = {
  PENDING: '待确认',
  CONFIRMED: '已确认',
  PREPARING: '制作中',
  READY: '待取餐',
  DELIVERED: '已取餐',
  CANCELLED: '已取消',
}

export function formatOrderStatus(status) {
  return ORDER_STATUS_MAP[status] || status || '--'
}

export function formatDateShort(value) {
  if (!value) return '--'
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
  }).format(new Date(value))
}

export function formatDateTime(value) {
  if (!value) return '--'
  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value))
}
