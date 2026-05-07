<script setup>
defineProps({
  label: { type: String, required: true },
  value: { type: [String, Number], required: true },
})
</script>

<template>
  <article class="mini-metric-card">
    <span>{{ label }}</span>
    <strong>{{ value }}</strong>
  </article>
</template>
