<script setup>
import { computed, onMounted } from 'vue'
import { RouterView, useRoute } from 'vue-router'
import { useOrderingStore } from './composables/useOrderingStore'
import AppSidebar from './components/layout/AppSidebar.vue'
import AppToast from './components/layout/AppToast.vue'
import AppLoading from './components/layout/AppLoading.vue'

const store = useOrderingStore()
const route = useRoute()

const isAuthRoute = computed(() =>
  route.path === '/login' || route.path === '/register',
)

const isWorkbenchRoute = computed(() =>
  route.path.startsWith('/workbench') || route.path.startsWith('/customer'),
)

const sidebarOverview = computed(() => [
  { label: '可售商品', value: `${store.safeMenuDishes.length} 道` },
  { label: '今日订单', value: `${store.admin.dashboard.todayOrderCount} 单` },
  { label: '分类数量', value: `${store.manageableCategories.length} 类` },
])

onMounted(() => {
  const token = localStorage.getItem('token')
  if (!isAuthRoute.value && token) {
    store.loadAllData()
  }
})
</script>

<template>
  <div v-if="isAuthRoute" class="auth-shell">
    <RouterView />
  </div>

  <div v-else class="app-shell" :class="{ 'customer-shell': isWorkbenchRoute }">
    <AppSidebar :overview="sidebarOverview" />

    <main class="workspace">
      <AppToast />

      <AppLoading :visible="store.state.loading" />

      <RouterView v-if="!store.state.loading" v-slot="{ Component, route: viewRoute }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" :key="viewRoute.path" />
        </transition>
      </RouterView>
    </main>
  </div>
</template>
