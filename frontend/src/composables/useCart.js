import { computed, watch } from 'vue'
import { menuState, uiState, adminState } from './state.js'
import { showToast } from './useToast.js'
import { normalizeId, normalizeCollection } from '../utils/normalize.js'
import { formatPrice } from '../utils/format.js'
import { DELIVERY_FEE } from '../config/constants.js'
import { fetchAdminDashboard } from '../api/dashboard.js'
import { createOrder, fetchAdminOrders } from '../api/orders.js'

const CART_STORAGE_KEY = 'ordering_cart'
const CART_VERSION = 1

function loadCart() {
  try {
    const raw = localStorage.getItem(CART_STORAGE_KEY)
    if (!raw) return
    const data = JSON.parse(raw)
    if (data.version !== CART_VERSION) {
      localStorage.removeItem(CART_STORAGE_KEY)
      return
    }
    uiState.cart = data.cart || []
    uiState.note = data.note || ''
    uiState.orderType = data.orderType || 'dine_in'
  } catch {
    localStorage.removeItem(CART_STORAGE_KEY)
  }
}

function saveCart() {
  const validIds = new Set(safeMenuDishes.value.map(d => normalizeId(d.id)))
  const cleanedCart = uiState.cart.filter(item => validIds.has(normalizeId(item.id)))
  if (cleanedCart.length !== uiState.cart.length) {
    uiState.cart = cleanedCart
  }
  try {
    localStorage.setItem(CART_STORAGE_KEY, JSON.stringify({
      version: CART_VERSION,
      cart: uiState.cart,
      note: uiState.note,
      orderType: uiState.orderType,
    }))
  } catch { /* storage full, silently ignore */ }
}

const safeMenuDishes = computed(() => normalizeCollection(menuState.dishes))

const cartItems = computed(() =>
  uiState.cart
    .map((entry) => {
      const dish = safeMenuDishes.value.find((item) => normalizeId(item.id) === normalizeId(entry.id))
      return dish ? { ...dish, quantity: entry.quantity } : null
    })
    .filter(Boolean),
)

const cartCount = computed(() =>
  uiState.cart.reduce((total, item) => total + item.quantity, 0),
)

const subtotal = computed(() =>
  cartItems.value.reduce((total, item) => total + Number(item.price) * item.quantity, 0),
)

const deliveryFee = computed(() => (uiState.orderType === 'delivery' ? DELIVERY_FEE : 0))
const total = computed(() => subtotal.value + deliveryFee.value)

function getQuantity(dishId) {
  return uiState.cart.find((item) => normalizeId(item.id) === normalizeId(dishId))?.quantity ?? 0
}

function addToCart(dishId) {
  const normalizedDishId = normalizeId(dishId)
  const target = uiState.cart.find((item) => normalizeId(item.id) === normalizedDishId)
  const dish = safeMenuDishes.value.find((item) => normalizeId(item.id) === normalizedDishId)

  if (target) {
    target.quantity += 1
  } else {
    uiState.cart.push({ id: normalizedDishId, quantity: 1 })
  }

  if (dish) showToast(`已加入 ${dish.name}`)
  saveCart()
}

function decreaseQuantity(dishId) {
  const normalizedDishId = normalizeId(dishId)
  const target = uiState.cart.find((item) => normalizeId(item.id) === normalizedDishId)
  const dish = safeMenuDishes.value.find((item) => normalizeId(item.id) === normalizedDishId)

  if (!target) return

  if (target.quantity === 1) {
    uiState.cart = uiState.cart.filter((item) => normalizeId(item.id) !== normalizedDishId)
    if (dish) showToast(`已移除 ${dish.name}`)
  } else {
    target.quantity -= 1
  }
  saveCart()
}

function setOrderType(type) {
  uiState.orderType = type
  saveCart()
}

function clearCart() {
  uiState.cart = []
  uiState.note = ''
  uiState.showCheckout = false
  try { localStorage.removeItem(CART_STORAGE_KEY) } catch { /* ignore */ }
  showToast('购物车已清空')
}

function openCheckout() {
  if (!cartItems.value.length) return
  uiState.showCheckout = true
}

function closeCheckout() {
  uiState.showCheckout = false
}

async function submitOrder() {
  if (!cartItems.value.length) return

  try {
    uiState.actionLoading = true
    const payload = {
      orderType: uiState.orderType,
      note: uiState.note,
      items: cartItems.value.map((item) => ({
        dishId: item.id,
        quantity: item.quantity,
      })),
    }

    const response = await createOrder(payload)
    uiState.lastOrderSnapshot = response
    uiState.showCheckout = false
    uiState.showReceipt = true
    uiState.cart = []
    uiState.note = ''
    try { localStorage.removeItem(CART_STORAGE_KEY) } catch { /* ignore */ }
    showToast('订单已提交')
    try {
      const [orders, dashboard] = await Promise.all([fetchAdminOrders(), fetchAdminDashboard()])
      adminState.orders = orders || []
      adminState.dashboard = dashboard
    } catch {
      showToast('订单已提交，但数据刷新失败')
    }
  } catch (error) {
    showToast(error.message || '订单提交失败')
  } finally {
    uiState.actionLoading = false
  }
}

let initialized = false

function init() {
  if (initialized) return
  initialized = true

  loadCart()
  watch(() => uiState.cart, () => saveCart(), { deep: true })
  watch(() => uiState.note, () => saveCart())
}

export function useCart() {
  init()
  return {
    cartItems,
    cartCount,
    subtotal,
    deliveryFee,
    total,
    setOrderType,
    getQuantity,
    addToCart,
    decreaseQuantity,
    clearCart,
    openCheckout,
    closeCheckout,
    submitOrder,
  }
}
