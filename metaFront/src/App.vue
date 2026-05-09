<script setup>
import AppHeader from './components/layout/AppHeader.vue'
import AppCartDrawer from './components/layout/AppCartDrawer.vue'
import AppChatWidget from './components/layout/AppChatWidget.vue'
</script>

<template>
  <div class="site-shell">
    <AppHeader />

    <main>
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <AppCartDrawer />
    <AppChatWidget />
  </div>
</template>

<style>
@import './styles/tokens.css';

.page-enter-active,
.page-leave-active {
  transition: opacity 280ms ease, transform 280ms ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-12px);
}
</style>
