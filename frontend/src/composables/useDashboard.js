import { computed, ref } from 'vue'
import { menuState, adminState, uiState } from './state.js'
import { fetchProductSales } from '../api/dashboard.js'

const safeMenuDishes = computed(() =>
  Array.isArray(menuState.dishes) ? menuState.dishes.filter((item) => item && item.id != null) : [],
)

const safeAdminCategories = computed(() => {
  const cats = adminState.categories.length ? adminState.categories : menuState.categories
  return Array.isArray(cats) ? cats.filter((item) => item && item.id != null) : []
})

const safeAdminOrders = computed(() =>
  Array.isArray(adminState.orders) ? adminState.orders.filter((order) => order && order.orderNo) : [],
)

/* ---------- time-range helpers ---------- */
const DAY_MS = 86400000

function rangeCutoff(range) {
  const now = Date.now()
  if (range === 'month') return now - 30 * DAY_MS
  if (range === 'year') return now - 365 * DAY_MS
  return now - 7 * DAY_MS
}

function buildPeriodKeys(range) {
  const now = new Date()
  const keys = []
  if (range === 'year') {
    for (let i = 11; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
      keys.push(`${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)
    }
  } else if (range === 'month') {
    for (let i = 5; i >= 0; i--) {
      const end = new Date(now.getFullYear(), now.getMonth(), now.getDate() - i * 5)
      const start = i === 5 ? new Date(now.getFullYear(), now.getMonth(), now.getDate() - 29) : new Date(end.getFullYear(), end.getMonth(), end.getDate() - 4)
      keys.push({
        key: `${start.getMonth() + 1}/${start.getDate()}-${end.getMonth() + 1}/${end.getDate()}`,
        start: start.getTime(),
        end: end.getTime(),
      })
    }
  } else {
    for (let i = 6; i >= 0; i--) {
      const d = new Date(now.getFullYear(), now.getMonth(), now.getDate() - i)
      keys.push(`${d.getMonth() + 1}/${d.getDate()}`)
    }
  }
  return keys
}

function dateLabel(dateStr, range) {
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  if (range === 'year') return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
  return `${d.getMonth() + 1}/${d.getDate()}`
}

/* ---------- revenue series ---------- */
const revenueSeries = computed(() => {
  const range = uiState.dashboardTimeRange || 'week'
  const cutoff = rangeCutoff(range)
  const filtered = safeAdminOrders.value.filter((o) => {
    const ts = new Date(o.createdAt).getTime()
    return !isNaN(ts) && ts >= cutoff
  })

  const periodKeys = buildPeriodKeys(range)

  if (range === 'month') {
    const buckets = periodKeys.map((pk) => ({ label: pk.key, value: 0, type: 'dine_in' }))
    let deliv = 0
    let dine = 0
    for (const order of filtered) {
      const ts = new Date(order.createdAt).getTime()
      for (let i = 0; i < periodKeys.length; i++) {
        if (ts >= periodKeys[i].start && ts <= periodKeys[i].end + DAY_MS - 1) {
          buckets[i].value += Number(order.totalAmount) || 0
          break
        }
      }
      if (order.orderType === 'delivery') deliv++
      else dine++
    }
    buckets.forEach((b) => { b.type = deliv >= dine ? 'delivery' : 'dine_in' })
    const max = Math.max(1, ...buckets.map((b) => b.value))
    return buckets.map((b) => ({
      label: b.label,
      value: b.value,
      height: Math.max(6, (b.value / max) * 100),
      type: b.type,
    }))
  }

  // Week / Year: simple key lookup
  const buckets = new Map()
  for (const key of periodKeys) {
    buckets.set(key, { label: key, value: 0, type: 'dine_in' })
  }

  let deliv = 0
  let dine = 0
  for (const order of filtered) {
    const key = dateLabel(order.createdAt, range)
    const bucket = buckets.get(key)
    if (bucket) bucket.value += Number(order.totalAmount) || 0
    if (order.orderType === 'delivery') deliv++
    else dine++
  }

  buckets.forEach((b) => { b.type = deliv >= dine ? 'delivery' : 'dine_in' })
  const bars = [...buckets.values()]
  const max = Math.max(1, ...bars.map((b) => b.value))

  return bars.map((bar) => ({
    label: bar.label,
    value: bar.value,
    height: Math.max(6, (bar.value / max) * 100),
    type: bar.type,
  }))
})

/* ---------- product sales (from API, time-range aware) ---------- */
const _productSales = ref([])
const _productSalesLoading = ref(false)

async function loadProductSales(range) {
  _productSalesLoading.value = true
  try {
    const data = await fetchProductSales(range || uiState.dashboardTimeRange || 'week')
    _productSales.value = Array.isArray(data) ? data : []
  } catch {
    _productSales.value = []
  } finally {
    _productSalesLoading.value = false
  }
}

const productSalesSeries = computed(() =>
  _productSales.value.map((item) => ({
    id: item.dishId,
    label: item.dishName,
    value: item.quantity || 0,
    revenue: item.revenue || 0,
  })),
)

/* ---------- order type mix ---------- */
const orderTypeMix = computed(() => {
  const dineIn = safeAdminOrders.value.filter((order) => order.orderType === 'dine_in').length
  const delivery = safeAdminOrders.value.filter((order) => order.orderType === 'delivery').length
  const totalOrders = Math.max(1, dineIn + delivery)

  return [
    { label: '堂食', value: dineIn, percent: Math.round((dineIn / totalOrders) * 100) },
    { label: '外带', value: delivery, percent: Math.round((delivery / totalOrders) * 100) },
  ]
})

function setTimeRange(range) {
  uiState.dashboardTimeRange = range
  loadProductSales(range)
}

export function useDashboard() {
  return { revenueSeries, productSalesSeries, orderTypeMix, setTimeRange, loadProductSales }
}
