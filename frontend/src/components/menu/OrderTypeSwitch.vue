<script setup>
import { uiState } from '../../composables/state.js'
import { useCart } from '../../composables/useCart.js'

const cart = useCart()
</script>

<template>
  <div class="switch-group compact">
    <button
      class="switch-btn dine-in"
      :class="{ active: uiState.orderType === 'dine_in' }"
      @click="cart.setOrderType('dine_in')"
    >堂食</button>
    <button
      class="switch-btn takeout"
      :class="{ active: uiState.orderType === 'delivery' }"
      @click="cart.setOrderType('delivery')"
    >外带</button>
  </div>
</template>
