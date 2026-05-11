<script setup>
import { uiState } from '../../composables/state.js'
import { useCart } from '../../composables/useCart.js'
import { useAdmin } from '../../composables/useAdmin.js'
import { formatPrice, formatOrderType } from '../../utils/format.js'
import QuantityControl from '../ui/QuantityControl.vue'
import { NOTE_MAX_LENGTH } from '../../config/constants.js'

const cart = useCart()
const admin = useAdmin()
</script>

<template>
  <transition name="mask-fade">
    <div v-if="uiState.showCheckout" class="modal-mask" @click="cart.closeCheckout">
      <section class="modal-card checkout-modal" @click.stop>
        <div class="modal-head checkout-head">
          <div>
            <span class="section-label">订单结算</span>
            <h3>确认本次点餐</h3>
          </div>
          <button class="close-btn" @click="cart.closeCheckout">&times;</button>
        </div>

        <div class="checkout-layout">
          <section class="checkout-items-panel">
            <div class="checkout-section-head">
              <strong>商品列表</strong>
              <small>{{ cart.cartCount.value }} 件商品</small>
            </div>
            <div class="checkout-item-scroll">
              <article v-for="item in cart.cartItems.value" :key="item.id" class="checkout-item-row">
                <div class="checkout-item-media">
                  <img v-if="admin.getDishImage(item)" :src="admin.getDishImage(item)" :alt="item.name" />
                  <span v-else>{{ item.name.slice(0, 1) }}</span>
                </div>
                <div class="checkout-item-copy">
                  <strong>{{ item.name }}</strong>
                  <small>{{ formatPrice(item.price) }} &times; {{ item.quantity }}</small>
                </div>
                <QuantityControl
                  :model-value="item.quantity"
                  @update:model-value="(v) => v > item.quantity ? cart.addToCart(item.id) : cart.decreaseQuantity(item.id)"
                />
                <strong>{{ formatPrice(Number(item.price) * item.quantity) }}</strong>
              </article>
            </div>
          </section>

          <aside class="checkout-summary-panel">
            <div class="checkout-section-head">
              <strong>结算信息</strong>
              <small>{{ formatOrderType(uiState.orderType) }}</small>
            </div>

            <label class="note-box checkout-note">
              <div class="note-head">
                <span>订单备注</span>
                <small>{{ uiState.note.length }}/{{ NOTE_MAX_LENGTH }}</small>
              </div>
              <textarea v-model="uiState.note" rows="3" :maxlength="NOTE_MAX_LENGTH" placeholder="例如：少冰、不加香菜、先上饮品" />
            </label>

            <div class="checkout-bill">
              <div class="bill-row total checkout-total"><span>合计</span><strong>{{ formatPrice(cart.total.value) }}</strong></div>
            </div>

            <div class="checkout-actions">
              <button class="ghost-btn" @click="cart.closeCheckout">加菜</button>
              <button class="primary-btn" :disabled="uiState.actionLoading || !cart.cartCount.value" @click="cart.submitOrder">
                {{ uiState.actionLoading ? '结算中...' : '确认结算' }}
              </button>
            </div>
          </aside>
        </div>
      </section>
    </div>
  </transition>
</template>
