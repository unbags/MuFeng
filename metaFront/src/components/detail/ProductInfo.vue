<script setup>
import { useCart } from '../../composables/useCart.js'
import { useProducts } from '../../composables/useProducts.js'
import { computed, onMounted } from 'vue'

const props = defineProps({
  dishId: { type: [Number, String], required: true },
})

const { getProductById, loadProducts, isLoading } = useProducts()
const dish = computed(() => getProductById(props.dishId))

const { addToCart } = useCart()

onMounted(() => {
  loadProducts().catch(() => {})
})
</script>

<template>
  <section v-if="isLoading && !dish" class="not-found">
    <h2>菜品加载中</h2>
  </section>

  <section v-else-if="dish" class="product-detail glass-panel" aria-label="菜品详情">
    <div class="detail-media">
      <img :src="dish.image" :alt="dish.title" />
    </div>
    <div class="detail-copy">
      <p class="section-kicker">{{ dish.category }}</p>
      <h2>{{ dish.title }}</h2>
      <p>{{ dish.desc }}</p>
      <dl>
        <div>
          <dt>食材</dt>
          <dd>{{ dish.ingredients }}</dd>
        </div>
        <div>
          <dt>口味</dt>
          <dd>{{ dish.flavor }}</dd>
        </div>
        <div>
          <dt>价格</dt>
          <dd>¥{{ dish.price.toLocaleString('zh-CN') }}</dd>
        </div>
      </dl>
      <button class="primary-button" type="button" @click="addToCart(dish)">加入点餐车</button>
      <router-link class="back-link" to="/menu">← 返回菜单</router-link>
    </div>
  </section>

  <section v-else class="not-found">
    <h2>菜品未找到</h2>
    <router-link to="/menu">返回菜单</router-link>
  </section>
</template>

<style scoped>
.product-detail {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(340px, 0.7fr);
  gap: clamp(24px, 5vw, 68px);
  align-items: center;
  margin: 38px 0 104px;
  padding: clamp(20px, 4vw, 52px);
}

.glass-panel {
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.84), rgba(255, 255, 255, 0.36));
  backdrop-filter: blur(26px) saturate(1.25);
  box-shadow: 0 24px 72px rgba(55, 82, 99, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.88);
}

.detail-media img {
  aspect-ratio: 5 / 4;
  object-fit: cover;
  width: 100%;
  display: block;
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
  margin: 0 0 18px;
  font-family: Georgia, "Times New Roman", serif;
  font-size: clamp(34px, 4.2vw, 66px);
  font-weight: 400;
  line-height: 1.05;
}

.detail-copy p {
  max-width: 620px;
  color: var(--muted);
  font-size: 17px;
  line-height: 1.9;
  margin: 0 0 24px;
}

dl {
  display: grid;
  gap: 0;
  margin: 0 0 30px;
  border-top: 1px solid var(--line);
}

dl div {
  display: grid;
  grid-template-columns: 86px 1fr;
  gap: 20px;
  padding: 16px 0;
  border-bottom: 1px solid var(--line);
}

dt {
  color: var(--soft);
}

dd {
  margin: 0;
}

.primary-button {
  display: inline-flex;
  min-height: 48px;
  align-items: center;
  justify-content: center;
  padding: 0 24px;
  border: 1px solid var(--ink);
  background: var(--ink);
  color: #fff;
  font-weight: 800;
  cursor: pointer;
  font-family: inherit;
  transition: transform 520ms var(--spring);
}

.primary-button:hover {
  transform: translateY(-5px) scale(1.012);
}

.back-link {
  display: inline-block;
  margin-top: 16px;
  color: var(--muted);
  text-decoration: none;
  font-size: 14px;
  transition: color 180ms ease;
}

.back-link:hover {
  color: var(--ink);
}

.not-found {
  text-align: center;
  padding: 80px 20px;
}

.not-found h2 {
  margin-bottom: 16px;
}

.not-found a {
  color: var(--accent);
  text-decoration: none;
}

@media (max-width: 980px) {
  .product-detail {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 560px) {
  dl div {
    grid-template-columns: 1fr;
  }
}
</style>
