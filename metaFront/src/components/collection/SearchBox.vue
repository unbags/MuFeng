<script setup>
import { useProducts } from '../../composables/useProducts.js'

const { searchQuery } = useProducts()
</script>

<template>
  <label class="search-box">
    <span>搜索</span>
    <input v-model="searchQuery" type="search" placeholder="红烧肉、水饺、柠檬茶" />
  </label>
</template>

<style scoped>
.search-box {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 12px;
  align-items: center;
  min-height: 44px;
  padding: 0 14px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.5);
}

.search-box span {
  color: var(--soft);
  font-size: 13px;
}

.search-box input {
  min-width: 0;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--ink);
  font-family: inherit;
}
</style>
