<script setup>
import { formatPrice, formatOrderType, formatDateTime, formatOrderStatus } from '../../utils/format.js'

defineProps({ order: { type: Object, required: true } })
defineEmits(['view-detail'])
</script>

<template>
  <article class="recent-order-card compact history">
    <div class="recent-order-head history">
      <div class="order-id-block">
        <strong>{{ order.orderNo }}</strong>
        <p>{{ formatOrderType(order.orderType) }} &middot; {{ formatDateTime(order.createdAt) }}</p>
      </div>
      <span class="status-pill order-status">{{ formatOrderStatus(order.status) }}</span>
    </div>
    <div class="recent-order-foot history">
      <span>{{ order.itemCount }} 件商品</span>
      <strong>{{ formatPrice(order.totalAmount) }}</strong>
    </div>
    <button class="ghost-btn order-detail-trigger" @click="$emit('view-detail', order.orderNo)">查看</button>
  </article>
</template>
