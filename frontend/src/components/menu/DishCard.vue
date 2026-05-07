<script setup>
import { useCart } from '../../composables/useCart.js'
import { useAdmin } from '../../composables/useAdmin.js'
import { formatPrice } from '../../utils/format.js'
import ImageWithFallback from '../ui/ImageWithFallback.vue'
import QuantityControl from '../ui/QuantityControl.vue'

const props = defineProps({ dish: { type: Object, required: true } })
const cart = useCart()
const admin = useAdmin()
</script>

<template>
  <article class="dish-card">
    <div class="dish-cover">
      <ImageWithFallback :src="admin.getDishImage(dish)" :alt="dish.name" />
    </div>
    <div class="dish-copy compact">
      <h4>{{ dish.name }}</h4>
    </div>
    <div class="dish-footer">
      <strong>{{ formatPrice(dish.price) }}</strong>
      <QuantityControl
        :model-value="cart.getQuantity(dish.id)"
        :min="0"
        @update:model-value="(v) => v > cart.getQuantity(dish.id) ? cart.addToCart(dish.id) : cart.decreaseQuantity(dish.id)"
      />
    </div>
  </article>
</template>
