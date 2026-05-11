<script setup>
import { uiState } from '../../composables/state.js'
import { useCart } from '../../composables/useCart.js'
import { formatPrice } from '../../utils/format.js'
import QuantityControl from '../ui/QuantityControl.vue'
import { NOTE_MAX_LENGTH } from '../../config/constants.js'

const cart = useCart()
</script>

<template>
  <aside class="panel cart-panel">
    <div class="panel-head compact">
      <div>
        <span class="section-label">订单汇总</span>
        <h3>购物车</h3>
      </div>
      <div class="cart-head-actions">
        <button class="ghost-btn tiny" :disabled="!cart.cartCount.value" @click="cart.clearCart">清空</button>
        <span class="cart-badge">{{ cart.cartCount.value }} 件</span>
      </div>
    </div>

    <div class="cart-scroll-area">
      <div v-if="cart.cartItems.value.length" class="cart-list">
        <article v-for="item in cart.cartItems.value" :key="item.id" class="cart-item">
          <div>
            <h4>{{ item.name }}</h4>
            <p>{{ formatPrice(item.price) }} / 件</p>
          </div>
          <div class="cart-actions">
            <QuantityControl
              :model-value="item.quantity"
              @update:model-value="(v) => v > item.quantity ? cart.addToCart(item.id) : cart.decreaseQuantity(item.id)"
            />
            <strong>{{ formatPrice(Number(item.price) * item.quantity) }}</strong>
          </div>
        </article>
      </div>
      <div v-else class="empty-card">
        <strong>购物车还是空的</strong>
        <p>选择商品后，这里会自动汇总数量和金额。</p>
      </div>
    </div>

    <div class="cart-footer compact">
      <label class="note-box compact">
        <div class="note-head">
          <span>订单备注</span>
          <small>{{ uiState.note.length }}/{{ NOTE_MAX_LENGTH }}</small>
        </div>
        <textarea v-model="uiState.note" rows="2" :maxlength="NOTE_MAX_LENGTH" placeholder="例如：少冰、不加香菜、先上饮品" />
      </label>

      <div class="bill-box compact">
        <div class="bill-grid">
          <div class="bill-row total">
            <span>合计</span>
            <strong>{{ formatPrice(cart.total.value) }}</strong>
          </div>
        </div>
      </div>

      <button class="primary-btn checkout-btn" :disabled="!cart.cartCount.value || uiState.actionLoading" @click="cart.openCheckout">
        去结算
      </button>
    </div>
  </aside>
</template>
