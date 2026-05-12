import { computed, ref, watch } from 'vue'
import { createOrder, fetchOrder } from '../api/orders.js'

const CART_STORAGE_KEY = 'mufeng_customer_cart'
const ORDER_HISTORY_KEY = 'mufeng_order_history'
const CART_VERSION = 1

// --- state ---
const cart = ref([])
const isCartOpen = ref(false)
const orderType = ref('dine_in')
const tableNumber = ref('')
const pickupNumber = ref('')
const note = ref('')
const isSubmitting = ref(false)
const lastOrder = ref(null)
const showReceipt = ref(false)
const showOrderList = ref(false)
const orderHistory = ref([])
const isLoadingOrders = ref(false)

// --- localStorage ---
function loadCart() {
  try {
    const raw = localStorage.getItem(CART_STORAGE_KEY)
    if (!raw) return
    const data = JSON.parse(raw)
    if (data.version !== CART_VERSION) {
      localStorage.removeItem(CART_STORAGE_KEY)
      return
    }
    cart.value = data.cart || []
    orderType.value = data.orderType || 'dine_in'
    tableNumber.value = data.tableNumber || ''
    pickupNumber.value = data.pickupNumber || ''
    note.value = data.note || ''
  } catch {
    localStorage.removeItem(CART_STORAGE_KEY)
  }
}

function saveCart() {
  try {
    localStorage.setItem(CART_STORAGE_KEY, JSON.stringify({
      version: CART_VERSION,
      cart: cart.value,
      orderType: orderType.value,
      tableNumber: tableNumber.value,
      pickupNumber: pickupNumber.value,
      note: note.value,
    }))
  } catch { /* storage full */ }
}

// --- computed ---
const cartCount = computed(() => cart.value.reduce((sum, item) => sum + item.quantity, 0))
const cartTotal = computed(() => cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0))
const total = computed(() => cartTotal.value)

// --- cart operations ---
function addToCart(dish) {
  const existing = cart.value.find((item) => item.id === dish.id)
  if (existing) {
    existing.quantity += 1
  } else {
    cart.value.push({ ...dish, quantity: 1 })
  }
  if (!isCartOpen.value) {
    isCartOpen.value = true
  }
  saveCart()
}

function removeFromCart(dishId) {
  cart.value = cart.value.filter((item) => item.id !== dishId)
  saveCart()
}

function decreaseQuantity(dishId) {
  const target = cart.value.find((item) => item.id === dishId)
  if (!target) return
  if (target.quantity === 1) {
    cart.value = cart.value.filter((item) => item.id !== dishId)
  } else {
    target.quantity -= 1
  }
  saveCart()
}

function openCart() {
  isCartOpen.value = true
}

function closeCart() {
  isCartOpen.value = false
}

// --- order type ---
function setOrderType(type) {
  orderType.value = type
  if (type === 'delivery') {
    tableNumber.value = ''
  } else {
    pickupNumber.value = ''
  }
  saveCart()
}

// --- submit ---
async function submitOrder() {
  if (!cart.value.length) return
  if (isSubmitting.value) return
  if (orderType.value === 'dine_in' && !tableNumber.value.trim()) {
    throw new Error('请先输入桌号')
  }

  try {
    isSubmitting.value = true

    const payload = {
      orderType: orderType.value,
      note: note.value,
      items: cart.value.map((item) => ({
        dishId: item.id,
        quantity: item.quantity,
      })),
    }

    if (orderType.value === 'dine_in') {
      payload.tableNumber = tableNumber.value.trim()
    }

    const response = await createOrder(payload)
    lastOrder.value = response
    pickupNumber.value = response.pickupNumber || ''
    showReceipt.value = true
    if (response.orderNo) saveOrderToHistory(response.orderNo)
    isCartOpen.value = false

    clearCartSilent()
    return response
  } finally {
    isSubmitting.value = false
  }
}

function clearCartSilent() {
  cart.value = []
  note.value = ''
  try { localStorage.removeItem(CART_STORAGE_KEY) } catch { /* ignore */ }
}

function clearCart() {
  clearCartSilent()
  isCartOpen.value = false
  showReceipt.value = false
}

function closeReceipt() {
  showReceipt.value = false
}

// --- order history ---
function loadOrderHistory() {
  try {
    const raw = localStorage.getItem(ORDER_HISTORY_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveOrderToHistory(orderNo) {
  try {
    const list = loadOrderHistory()
    if (!list.includes(orderNo)) {
      list.unshift(orderNo)
      if (list.length > 20) list.length = 20
      localStorage.setItem(ORDER_HISTORY_KEY, JSON.stringify(list))
    }
  } catch { /* ignore */ }
}

async function fetchOrderHistory() {
  const orderNos = loadOrderHistory()
  if (!orderNos.length) {
    orderHistory.value = []
    return
  }

  isLoadingOrders.value = true
  try {
    const results = await Promise.allSettled(
      orderNos.map((no) => fetchOrder(no))
    )
    orderHistory.value = results
      .map((result, index) => {
        if (result.status === 'fulfilled') {
          return result.value
        }
        return { orderNo: orderNos[index], status: null, _error: true }
      })
      .filter((o) => o !== null)
  } catch {
    orderHistory.value = []
  } finally {
    isLoadingOrders.value = false
  }
}

function openOrderList() {
  showOrderList.value = true
  isCartOpen.value = true
  fetchOrderHistory()
}

function closeOrderList() {
  showOrderList.value = false
}

// --- init ---
let initialized = false

function init() {
  if (initialized) return
  initialized = true

  loadCart()
  watch(cart, () => saveCart(), { deep: true })
  watch(orderType, () => saveCart())
  watch(tableNumber, () => saveCart())
  watch(pickupNumber, () => saveCart())
  watch(note, () => saveCart())
}

export function useCart() {
  init()
  return {
    cart,
    cartCount,
    cartTotal,
    total,
    isCartOpen,
    orderType,
    tableNumber,
    pickupNumber,
    note,
    isSubmitting,
    lastOrder,
    showReceipt,
    showOrderList,
    orderHistory,
    isLoadingOrders,
    addToCart,
    removeFromCart,
    decreaseQuantity,
    openCart,
    closeCart,
    setOrderType,
    submitOrder,
    clearCart,
    closeReceipt,
    openOrderList,
    closeOrderList,
  }
}
