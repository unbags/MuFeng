<script setup>
import { uiState } from '../../composables/state.js'
import { formatPrice, formatOrderType } from '../../utils/format.js'
</script>

<template>
  <transition name="mask-fade">
    <div v-if="uiState.showReceipt && uiState.lastOrderSnapshot" class="modal-mask" @click="uiState.showReceipt = false">
      <section class="modal-card receipt-modal order-success-modal" @click.stop>
        <div class="success-modal-head">
          <div class="success-icon">&#10003;</div>
          <button class="close-btn" @click="uiState.showReceipt = false">&times;</button>
        </div>

        <div class="success-title-block">
          <span class="section-label success-label">结算完成</span>
          <h3>订单已提交</h3>
          <p>{{ formatOrderType(uiState.lastOrderSnapshot.type) }}订单已进入制作队列。</p>
        </div>

        <div class="success-total-card">
          <span>应收金额</span>
          <strong>{{ formatPrice(uiState.lastOrderSnapshot.total) }}</strong>
          <small>{{ uiState.lastOrderSnapshot.items.length }} 项商品 &middot; {{ formatOrderType(uiState.lastOrderSnapshot.type) }}</small>
        </div>

        <div class="receipt-panel compact-success">
          <div class="receipt-panel-head">
            <span>订单明细</span>
            <small>{{ uiState.lastOrderSnapshot.items.length }} 项商品</small>
          </div>
          <div class="receipt-list modern">
            <div v-for="item in uiState.lastOrderSnapshot.items" :key="item.name" class="receipt-row modern">
              <div class="receipt-item-copy">
                <strong>{{ item.name }}</strong>
                <span>&times; {{ item.quantity }}</span>
              </div>
              <strong>{{ formatPrice(item.total) }}</strong>
            </div>
          </div>
          <div v-if="uiState.lastOrderSnapshot.note" class="receipt-note modern">
            <span>订单备注</span>
            <p>{{ uiState.lastOrderSnapshot.note }}</p>
          </div>
        </div>

        <div class="modal-footer success-footer">
          <button class="primary-btn success-btn" @click="uiState.showReceipt = false">完成</button>
        </div>
      </section>
    </div>
  </transition>
</template>
