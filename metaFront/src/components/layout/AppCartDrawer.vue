<script setup>
import { ref } from 'vue'
import { useCart } from '../../composables/useCart.js'
import { NOTE_MAX_LENGTH } from '../../config/constants.js'

const {
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
  closeCart,
  setOrderType,
  submitOrder,
  clearCart,
  closeReceipt,
  openOrderList,
  closeOrderList,
} = useCart()

const errorMessage = ref('')

function statusLabel(status) {
  const map = {
    PENDING: '待接单',
    PREPARING: '制作中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  return map[status] || status || '--'
}

async function onConfirm() {
  errorMessage.value = ''
  try {
    await submitOrder()
  } catch (err) {
    errorMessage.value = err.message || '订单提交失败'
  }
}

function onClose() {
  if (showReceipt.value) {
    closeReceipt()
  }
  closeOrderList()
  closeCart()
}
</script>

<template>
  <aside class="cart-drawer" :class="{ open: isCartOpen }" aria-label="点餐车" @click.self="onClose">
    <div class="cart-panel" role="dialog" aria-modal="true">
      <!-- Header -->
      <div class="cart-head">
        <h2>{{ showOrderList ? '我的订单' : '点餐车' }}</h2>
        <div class="cart-head-actions">
          <button v-if="showOrderList" class="clear-button" type="button" @click="closeOrderList()">返回购物车</button>
          <template v-else>
            <button class="clear-button" type="button" @click="openOrderList()">订单列表</button>
            <button v-if="cart.length" class="clear-button" type="button" @click="clearCart()">清空</button>
            <button type="button" @click="onClose">关闭</button>
          </template>
        </div>
      </div>

      <!-- Order list view -->
      <template v-if="showOrderList">
        <div class="cart-body">
          <p v-if="isLoadingOrders" class="empty-cart">订单加载中...</p>
          <p v-else-if="!orderHistory.length" class="empty-cart">暂无历史订单</p>
          <div v-else class="order-list">
            <article
              v-for="order in orderHistory"
              :key="order.orderNo"
              class="order-card-mini"
            >
              <div class="order-mini-head">
                <strong>{{ order.orderNo }}</strong>
                <span :class="['order-mini-status', (order.status || '').toLowerCase()]">{{ statusLabel(order.status) }}</span>
              </div>
              <div class="order-mini-body">
                <span>{{ order.orderType === 'dine_in' ? '堂食' : '外带' }}</span>
                <span v-if="order.tableNumber">桌号 {{ order.tableNumber }}</span>
                <span v-if="order.pickupNumber">取餐号 {{ order.pickupNumber }}</span>
                <strong>¥{{ Number(order.totalAmount || 0).toLocaleString('zh-CN') }}</strong>
              </div>
              <div v-if="order.items && order.items.length" class="order-mini-items">
                {{ order.items.map(i => `${i.name} x${i.quantity}`).join('、') }}
              </div>
              <router-link class="order-mini-link" :to="`/orders/${order.orderNo}`" @click="onClose">查看详情</router-link>
            </article>
          </div>
        </div>
      </template>

      <!-- Receipt view -->
      <template v-else-if="showReceipt && lastOrder">
        <div class="receipt glass-panel">
          <p class="receipt-icon">&#10003;</p>
          <h3>下单成功</h3>
          <dl>
            <div>
              <dt>订单号</dt>
              <dd>{{ lastOrder.orderNo || '--' }}</dd>
            </div>
            <div>
              <dt>类型</dt>
              <dd>{{ lastOrder.orderType === 'dine_in' ? '堂食' : '外带' }}</dd>
            </div>
            <div v-if="lastOrder.tableNumber">
              <dt>桌号</dt>
              <dd>{{ lastOrder.tableNumber }}</dd>
            </div>
            <div v-if="lastOrder.pickupNumber">
              <dt>取餐号</dt>
              <dd>{{ lastOrder.pickupNumber }}</dd>
            </div>
            <div>
              <dt>合计</dt>
              <dd>&yen;{{ Number(lastOrder.total || 0).toLocaleString('zh-CN') }}</dd>
            </div>
          </dl>
          <router-link class="primary-button" :to="`/orders/${lastOrder.orderNo}`" @click="clearCart">
            查看订单状态
          </router-link>
        </div>
      </template>

      <!-- Cart view -->
      <template v-else>
        <!-- Toolbar: type toggle + table/pickup field (fixed) -->
        <div v-if="cart.length" class="cart-toolbar">
          <div class="type-toggle">
            <button
              type="button"
              :class="{ active: orderType === 'dine_in' }"
              @click="setOrderType('dine_in')"
            >
              堂食
            </button>
            <button
              type="button"
              :class="{ active: orderType === 'delivery' }"
              @click="setOrderType('delivery')"
            >
              外带
            </button>
          </div>

          <div v-if="orderType === 'dine_in'" class="field-group">
            <input
              id="table-number"
              v-model="tableNumber"
              type="text"
              placeholder="桌号，如 A5"
            />
          </div>
        </div>

        <div class="cart-body">
          <!-- Cart items -->
          <div v-if="cart.length" class="cart-items">
            <article v-for="item in cart" :key="item.id" class="cart-item">
              <img :src="item.image" :alt="item.title" />
              <div class="cart-item-info">
                <h3>{{ item.title }}</h3>
                <p class="cart-item-flavor">{{ item.flavor }}</p>
                <strong>&yen;{{ (item.price * item.quantity).toLocaleString('zh-CN') }}</strong>
                <div class="qty-control">
                  <button type="button" @click="decreaseQuantity(item.id)">-</button>
                  <span>{{ item.quantity }}</span>
                  <button type="button" @click="addToCart(item)">+</button>
                </div>
              </div>
            </article>
          </div>

          <p v-else class="empty-cart">点餐车为空。选一道喜欢的菜品开始吧。</p>
        </div>

        <!-- Error -->
        <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>

        <!-- Note -->
        <div v-if="cart.length" class="cart-note">
          <input
            id="order-note"
            v-model="note"
            type="text"
            placeholder="口味要求等（选填）"
            :maxlength="NOTE_MAX_LENGTH"
          />
        </div>

        <!-- Footer -->
        <div v-if="cart.length" class="cart-foot">
          <div class="cart-summary">
            <div class="cart-total-line">
              <span>合计</span>
              <strong>&yen;{{ total.toLocaleString('zh-CN') }}</strong>
            </div>
          </div>
          <button
            class="primary-button"
            type="button"
            :disabled="isSubmitting"
            @click="onConfirm"
          >
            {{ isSubmitting ? '提交中...' : '确认下单' }}
          </button>
        </div>
      </template>
    </div>
  </aside>
</template>

<style scoped>
.cart-drawer {
  position: fixed;
  inset: 0;
  z-index: 40;
  display: flex;
  justify-content: flex-end;
  pointer-events: none;
  background: rgba(21, 21, 21, 0);
  transition: background 260ms ease;
}

.cart-drawer.open {
  pointer-events: auto;
  background: rgba(21, 21, 21, 0.18);
}

.cart-panel {
  display: flex;
  flex-direction: column;
  width: min(460px, 100%);
  height: 100vh;
  padding: 24px;
  transform: translateX(108%);
  transition: transform 560ms var(--spring);
  border-left: 1px solid rgba(255, 255, 255, 0.72);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.88), rgba(255, 255, 255, 0.44));
  backdrop-filter: blur(26px) saturate(1.25);
}

.cart-drawer.open .cart-panel {
  transform: translateX(0);
}

/* --- scrollable body --- */
.cart-body {
  flex: 1;
  overflow-y: auto;
  scrollbar-width: none;
}

.cart-body::-webkit-scrollbar {
  display: none;
}

/* --- header --- */
.cart-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.cart-head h2 {
  font-family: Georgia, "Times New Roman", serif;
  font-size: 24px;
  font-weight: 400;
  margin: 0;
}

.cart-head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cart-head button {
  border: 0;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  font-family: inherit;
  font-size: 13px;
}

/* --- toolbar (type toggle + fields, fixed) --- */
.cart-toolbar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--line);
}

.type-toggle {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
  border: 1px solid var(--line);
  overflow: hidden;
  flex-shrink: 0;
}

.type-toggle button {
  min-height: 32px;
  padding: 0 12px;
  border: 0;
  background: rgba(255, 255, 255, 0.4);
  color: var(--muted);
  font-weight: 700;
  font-size: 13px;
  cursor: pointer;
  font-family: inherit;
  transition: background 180ms ease, color 180ms ease;
}

.type-toggle button.active {
  background: var(--ink);
  color: #fff;
}

.cart-toolbar .field-group {
  flex: 1;
  min-width: 0;
  margin: 0;
}

.cart-toolbar .field-group input {
  width: 100%;
  min-height: 32px;
  padding: 0 10px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.5);
  color: var(--ink);
  outline: 0;
  font-size: 13px;
  font-family: inherit;
}

/* --- cart items --- */
.cart-items {
  display: grid;
  gap: 16px;
}

.cart-item {
  display: grid;
  grid-template-columns: 80px 1fr;
  gap: 14px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
}

.cart-item img {
  aspect-ratio: 1;
  object-fit: cover;
  width: 80px;
  display: block;
}

.cart-item-info h3 {
  margin: 0 0 4px;
  font-size: 16px;
}

.cart-item-flavor {
  margin: 0 0 6px;
  color: var(--muted);
  font-size: 13px;
}

.cart-item-info strong {
  display: block;
  font-size: 15px;
  margin-bottom: 8px;
}

/* --- quantity control --- */
.qty-control {
  display: inline-flex;
  align-items: center;
  gap: 0;
  border: 1px solid var(--line);
}

.qty-control button {
  width: 30px;
  height: 30px;
  border: 0;
  background: rgba(255, 255, 255, 0.54);
  color: var(--ink);
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: background 120ms ease;
}

.qty-control button:hover:not(:disabled) {
  background: var(--ink);
  color: #fff;
}

.qty-control button:disabled {
  opacity: 0.3;
  cursor: default;
}

.qty-control span {
  display: grid;
  width: 36px;
  height: 30px;
  place-items: center;
  font-size: 14px;
  font-weight: 700;
}

/* --- empty --- */
.empty-cart {
  color: var(--muted);
  line-height: 1.8;
  text-align: center;
  padding: 32px 0;
}

/* --- error --- */
.error-msg {
  flex-shrink: 0;
  margin: 0;
  padding: 10px 14px;
  border: 1px solid rgba(200, 60, 60, 0.3);
  background: rgba(200, 60, 60, 0.06);
  color: #a33;
  font-size: 14px;
}

/* --- note --- */
.cart-note {
  flex-shrink: 0;
  padding-top: 8px;
}

.cart-note input {
  width: 100%;
  min-height: 36px;
  padding: 0 10px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.5);
  color: var(--ink);
  outline: 0;
  font-size: 13px;
  font-family: inherit;
}

/* --- footer --- */
.cart-foot {
  flex-shrink: 0;
  display: grid;
  gap: 14px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--line);
}

.cart-summary {
  display: grid;
}

.cart-total-line {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  font-size: 16px;
  color: var(--ink);
}

.cart-total-line strong {
  font-size: 18px;
}

.primary-button {
  display: inline-flex;
  min-height: 48px;
  align-items: center;
  justify-content: center;
  padding: 0 24px;
  border: 1px solid var(--ink);
  background: var(--ink);
  color: #fff;
  font-weight: 800;
  text-decoration: none;
  cursor: pointer;
  font-family: inherit;
  transition: transform 520ms var(--spring), opacity 180ms ease;
}

.primary-button:disabled {
  opacity: 0.5;
  cursor: default;
  transform: none;
}

.primary-button:hover:not(:disabled) {
  transform: translateY(-3px);
}

.clear-button {
  border: 0;
  background: transparent;
  color: var(--muted);
  font-size: 14px;
  cursor: pointer;
  font-family: inherit;
  transition: color 180ms ease;
}

.clear-button:hover {
  color: #a33;
}

/* --- receipt --- */
.receipt {
  text-align: center;
  padding: 32px 24px;
  margin-top: 16px;
}

.glass-panel {
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.84), rgba(255, 255, 255, 0.36));
  backdrop-filter: blur(26px) saturate(1.25);
  box-shadow: 0 24px 72px rgba(55, 82, 99, 0.16);
}

.receipt-icon {
  display: grid;
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  place-items: center;
  border-radius: 50%;
  background: var(--accent);
  color: #fff;
  font-size: 28px;
  font-weight: 700;
}

.receipt h3 {
  margin: 0 0 20px;
  font-family: Georgia, "Times New Roman", serif;
  font-size: 28px;
  font-weight: 400;
}

.receipt dl {
  display: grid;
  gap: 0;
  margin: 0 0 24px;
  text-align: left;
}

.receipt dl > div {
  display: grid;
  grid-template-columns: 80px 1fr;
  gap: 16px;
  padding: 10px 0;
  border-bottom: 1px solid var(--line);
}

.receipt dt {
  color: var(--soft);
  font-size: 13px;
}

.receipt dd {
  margin: 0;
  font-weight: 700;
}

.receipt .primary-button {
  width: 100%;
}

/* --- order list --- */
.order-list {
  display: grid;
  gap: 12px;
}

.order-card-mini {
  padding: 14px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.52);
}

.order-mini-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.order-mini-head strong {
  font-size: 13px;
  letter-spacing: 0.04em;
}

.order-mini-status {
  font-size: 11px;
  font-weight: 800;
  padding: 2px 8px;
  border: 1px solid var(--line);
}

.order-mini-status.pending { color: var(--accent); border-color: var(--accent); }
.order-mini-status.preparing { color: #2563eb; border-color: #2563eb; }
.order-mini-status.completed { color: #16a34a; border-color: #16a34a; }
.order-mini-status.cancelled { color: #a33; border-color: rgba(200, 60, 60, 0.4); }

.order-mini-body {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 13px;
  color: var(--muted);
  margin-bottom: 6px;
}

.order-mini-body strong {
  margin-left: auto;
  color: var(--ink);
}

.order-mini-items {
  font-size: 12px;
  color: var(--soft);
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-mini-link {
  display: inline-block;
  font-size: 12px;
  font-weight: 700;
  color: var(--accent);
  text-decoration: none;
  transition: color 180ms ease;
}

.order-mini-link:hover {
  color: var(--ink);
}

/* --- responsive --- */
@media (max-width: 560px) {
  .cart-item {
    grid-template-columns: 1fr;
  }
  .cart-item img {
    width: 100%;
    aspect-ratio: 3 / 2;
  }
}
</style>
