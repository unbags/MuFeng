<script setup>
defineProps({ mix: { type: Array, required: true } })
</script>

<template>
  <div class="summary-stack">
    <article v-for="item in mix" :key="item.label" class="mix-row">
      <div class="mix-head">
        <strong>{{ item.label }}</strong>
        <span>{{ item.value }} 单</span>
      </div>
      <div class="mix-track">
        <span class="mix-fill" :style="{ width: `${item.percent}%` }" />
      </div>
      <small>{{ item.percent }}%</small>
    </article>
  </div>
</template>
