<script setup>
import { useCart } from '../../composables/useCart.js'
import { useChat } from '../../composables/useChat.js'

const { cartCount, openCart } = useCart()
const { toggleSupport } = useChat()
</script>

<template>
  <header class="app-header">
    <router-link class="brand" to="/menu" aria-label="返回菜单">沐枫餐饮</router-link>
    <nav class="nav-links" aria-label="主导航">
      <router-link to="/menu">菜单</router-link>
    </nav>
    <div class="header-actions">
      <button class="ai-button" type="button" @click="toggleSupport">
        <span>AI</span>
        点餐助手
      </button>
      <button class="cart-button" type="button" @click="openCart">
        点餐车
        <span>{{ cartCount }}</span>
      </button>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 16px;
  z-index: 20;
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 28px;
  margin: 16px 0 34px;
  padding: 14px 18px;
  border: 1px solid rgba(255, 255, 255, 0.72);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.84), rgba(255, 255, 255, 0.36));
  backdrop-filter: blur(26px) saturate(1.25);
  box-shadow: 0 24px 72px rgba(55, 82, 99, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.88);
}

.brand {
  font-family: Georgia, "Times New Roman", serif;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.18em;
  color: var(--ink);
  text-decoration: none;
}

.nav-links {
  display: flex;
  gap: clamp(18px, 4vw, 46px);
  color: var(--muted);
  font-size: 14px;
}

.nav-links a {
  color: var(--muted);
  text-decoration: none;
  transition: color 180ms ease;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: var(--ink);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-button {
  display: inline-flex;
  min-height: 42px;
  align-items: center;
  gap: 8px;
  padding: 0 16px;
  border: 1px solid var(--ink);
  background: var(--ink);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: transform 520ms var(--spring);
}

.ai-button:hover {
  transform: translateY(-5px) scale(1.012);
}

.ai-button span {
  display: grid;
  width: 22px;
  height: 22px;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.44);
  font-size: 11px;
  letter-spacing: 0.06em;
}

.cart-button {
  display: inline-flex;
  min-height: 42px;
  align-items: center;
  gap: 10px;
  padding: 0 16px;
  border: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.52);
  color: var(--ink);
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
  transition: transform 520ms var(--spring);
}

.cart-button:hover {
  transform: translateY(-5px) scale(1.012);
}

.cart-button span {
  display: grid;
  min-width: 22px;
  height: 22px;
  place-items: center;
  background: var(--ink);
  color: #fff;
  font-size: 12px;
}

@media (max-width: 980px) {
  .app-header {
    grid-template-columns: 1fr;
  }
  .nav-links {
    justify-content: flex-start;
    overflow-x: auto;
    padding-bottom: 4px;
  }
}
</style>
