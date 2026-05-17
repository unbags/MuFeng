const CART_ID_KEY = 'mufeng_cart_id'

let cachedCartId = null

export function getCartId() {
  if (cachedCartId) return cachedCartId

  cachedCartId = readCartId()
  if (!cachedCartId) {
    cachedCartId = createCartId()
    writeCartId(cachedCartId)
  }

  return cachedCartId
}

export function cartHeaders() {
  return { 'X-Cart-Id': getCartId() }
}

function readCartId() {
  try {
    return sessionStorage.getItem(CART_ID_KEY)
  } catch {
    return null
  }
}

function writeCartId(cartId) {
  try {
    sessionStorage.setItem(CART_ID_KEY, cartId)
  } catch {
    // In restricted browser storage modes the in-memory id is enough for this tab.
  }
}

function createCartId() {
  return 'cart_' + Date.now().toString(36) + '_' + Math.random().toString(36).slice(2, 10)
}
