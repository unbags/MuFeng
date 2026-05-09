<script setup>
import { useProducts } from '../../composables/useProducts.js'

const { categories, activeCategory, setCategory } = useProducts()
</script>

<template>
  <div class="category-tabs">
    <button
      v-for="category in categories"
      :key="category.id"
      type="button"
      :class="{ active: activeCategory === category.id }"
      @click="setCategory(category.id)"
    >
      {{ category.label }}
    </button>
  </div>
</template>

<style scoped>
.category-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-tabs button {
  min-height: 40px;
  padding: 0 16px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.46);
  color: var(--muted);
  cursor: pointer;
  font-family: inherit;
  transition: transform 520ms var(--spring), color 180ms ease, background 180ms ease;
}

.category-tabs button.active {
  border-color: var(--ink);
  background: var(--ink);
  color: #fff;
}

.category-tabs button:hover {
  transform: translateY(-3px);
}
</style>
