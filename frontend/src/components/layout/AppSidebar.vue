<script setup>
import { RouterLink, useRouter } from 'vue-router'
import { useAuth } from '../../composables/useAuth.js'

defineProps({
  overview: { type: Array, default: () => [] },
})

const router = useRouter()
const { isLoggedIn, logout } = useAuth()

function handleLogout() {
  logout()
  router.push('/login')
}
</script>

<template>
  <aside class="sidebar">
    <div class="sidebar-brand">
      <span class="eyebrow">店员工作台</span>
      <h1>沐枫餐饮</h1>
    </div>

    <nav class="sidebar-nav">
      <RouterLink class="nav-item" to="/workbench" active-class="active"><span>点餐工作台</span></RouterLink>
      <RouterLink class="nav-item" to="/dashboard" active-class="active"><span>数据看板</span></RouterLink>
      <RouterLink class="nav-item" to="/products" active-class="active"><span>商品管理</span></RouterLink>
    </nav>

    <section class="sidebar-overview">
      <div class="sidebar-section-head"><h2>实时概览</h2></div>
      <div class="overview-list">
        <article v-for="item in overview" :key="item.label" class="overview-item">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </article>
      </div>
    </section>

    <div v-if="isLoggedIn()" class="sidebar-footer">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>
  </aside>
</template>
