<script setup>
import { useCart } from '../../composables/useCart.js'

const props = defineProps({
  product: { type: Object, required: true },
  selected: { type: Boolean, default: false },
})

const emit = defineEmits(['select'])

const { addToCart } = useCart()

function onCardClick() {
  emit('select', props.product)
}
</script>

<template>
  <article class="product-card" :class="{ selected }" role="button" tabindex="0" @click="onCardClick" @keydown.enter="onCardClick" @keydown.space.prevent="onCardClick">
    <img :src="product.image" :alt="product.title" loading="lazy" />
    <div class="product-card-body">
      <span>{{ product.categoryLabel }}</span>
      <h3>{{ product.title }}</h3>
      <p>{{ product.flavor }}</p>
      <strong>¥{{ product.price.toLocaleString('zh-CN') }}</strong>
      <button class="add-btn" type="button" @click.stop="addToCart(product)">加入点餐车</button>
    </div>
  </article>
</template>

<style scoped>
.product-card {
  overflow: hidden;
  padding: 0;
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.84), rgba(255, 255, 255, 0.36));
  backdrop-filter: blur(26px) saturate(1.25);
  box-shadow: 0 24px 72px rgba(55, 82, 99, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.88);
  cursor: pointer;
  transition: transform 520ms var(--spring), box-shadow 520ms var(--spring), border-color 180ms ease;
}

.product-card:hover {
  transform: translateY(-5px) scale(1.012);
}

.product-card.selected {
  border-color: rgba(175, 123, 56, 0.48);
  box-shadow: 0 28px 86px rgba(175, 123, 56, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.product-card img {
  aspect-ratio: 4 / 5;
  object-fit: cover;
  width: 100%;
  display: block;
}

.product-card-body {
  padding: 18px;
}

.product-card-body span {
  color: var(--soft);
  font-size: 12px;
  letter-spacing: 0.12em;
}

.product-card-body h3 {
  margin: 10px 0 8px;
  font-size: 18px;
  font-weight: 700;
}

.product-card-body p {
  margin: 0 0 12px;
  color: var(--muted);
}

.product-card-body strong {
  font-size: 17px;
  display: block;
  margin-bottom: 12px;
}

.add-btn {
  width: 100%;
  min-height: 36px;
  border: 1px solid var(--ink);
  background: transparent;
  color: var(--ink);
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: background 180ms ease, color 180ms ease;
}

.add-btn:hover {
  background: var(--ink);
  color: #fff;
}
</style>
