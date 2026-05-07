<script setup>
defineProps({ visible: { type: Boolean, default: false } })
</script>

<template>
  <div v-if="visible" class="loading-panel skeleton-screen">
    <div class="skeleton-head"></div>
    <div class="skeleton-grid">
      <span v-for="i in 9" :key="i"></span>
    </div>
  </div>
</template>
