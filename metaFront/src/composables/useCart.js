import { computed, ref, watch } from 'vue'
import { createOrder } from '../api/orders.js'

const CART_STORAGE_KEY = 'mufeng_customer_cart'
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
  isCartOpen.value = true
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
  if (cartCount.value === 0) return
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
    addToCart,
    removeFromCart,
    decreaseQuantity,
    openCart,
    closeCart,
    setOrderType,
    submitOrder,
    clearCart,
    closeReceipt,
  }
}
