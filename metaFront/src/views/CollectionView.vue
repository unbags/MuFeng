<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import CategoryTabs from '../components/collection/CategoryTabs.vue'
import SearchBox from '../components/collection/SearchBox.vue'
import ProductGrid from '../components/collection/ProductGrid.vue'
import { useProducts } from '../composables/useProducts.js'

const selectedId = ref(null)
const router = useRouter()
const { loadProducts } = useProducts()

onMounted(() => {
  loadProducts().catch(() => {})
})

function onSelect(product) {
  selectedId.value = product.id
  router.push(`/menu/${product.id}`)
}
</script>

<template>
  <section class="collection-section">
    <div class="section-heading">
      <p class="section-kicker">沐枫菜单</p>
      <h2>精选时令食材，匠心烹饪，打造属于你的美味时光。</h2>
    </div>

    <div class="shop-tools glass-panel" aria-label="商品筛选">
      <CategoryTabs />
      <SearchBox />
    </div>

    <ProductGrid :selected-id="selectedId" @select="onSelect" />
  </section>
</template>

<style scoped>
.collection-section {
  padding: 106px 0 62px;
  border-top: 1px solid rgba(21, 21, 21, 0.09);
}

.section-heading {
  display: grid;
  grid-template-columns: 0.28fr 1fr;
  gap: 38px;
  align-items: start;
  margin-bottom: 38px;
}

.section-kicker {
  margin: 0 0 18px;
  color: var(--accent);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

h2 {
  max-width: 860px;
  margin: 0;
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(34px, 4.2vw, 66px);
  font-weight: 400;
  line-height: 1.05;
}

.shop-tools {
  display: grid;
  grid-template-columns: 1fr minmax(260px, 360px);
  gap: 20px;
  align-items: center;
  margin-bottom: 22px;
  padding: 16px;
}

.glass-panel {
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.84), rgba(255, 255, 255, 0.36));
  backdrop-filter: blur(26px) saturate(1.25);
  box-shadow: 0 24px 72px rgba(55, 82, 99, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.88);
}

@media (max-width: 980px) {
  .section-heading,
  .shop-tools {
    grid-template-columns: 1fr;
  }
}
</style>
