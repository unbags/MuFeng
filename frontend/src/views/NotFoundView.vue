<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <section class="screen-page not-found-page">
    <div class="not-found-content">
      <div class="not-found-icon">404</div>
      <h2>页面不存在</h2>
      <p>您访问的页面不存在或已被移除，请返回工作台。</p>
      <RouterLink to="/workbench" class="primary-btn">返回工作台</RouterLink>
    </div>
  </section>
</template>
