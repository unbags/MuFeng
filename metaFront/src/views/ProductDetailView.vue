<script setup>
import { useRoute } from 'vue-router'
import ProductInfo from '../components/detail/ProductInfo.vue'

const route = useRoute()
</script>

<template>
  <div class="detail-page">
    <ProductInfo :dish-id="route.params.id" />
  </div>
</template>

<style scoped>
.detail-page {
  padding: 40px 0;
}
</style>
