<script setup>
import { useProducts } from '../../composables/useProducts.js'
import ProductCard from './ProductCard.vue'

const { filteredProducts, isLoading, errorMessage } = useProducts()

defineProps({
  selectedId: { type: Number, default: null },
})

const emit = defineEmits(['select'])
</script>

<template>
  <div v-if="isLoading" class="grid-state">菜单加载中...</div>
  <div v-else-if="errorMessage" class="grid-state error">{{ errorMessage }}</div>
  <div v-else-if="!filteredProducts.length" class="grid-state">没有找到符合条件的菜品。</div>
  <div v-else class="product-grid">
    <ProductCard
      v-for="product in filteredProducts"
      :key="product.id"
      :product="product"
      :selected="product.id === selectedId"
      @select="emit('select', $event)"
    />
  </div>
</template>

<style scoped>
.grid-state {
  padding: 42px 18px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.58);
  color: var(--muted);
  text-align: center;
}

.grid-state.error {
  color: #9b2c2c;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}

@media (max-width: 980px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 560px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
