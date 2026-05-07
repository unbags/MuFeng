<script setup>
import { computed } from 'vue'

const props = defineProps({
  series: { type: Array, required: true },
})

const maxValue = computed(() => Math.max(1, ...props.series.map((s) => s.value)))
</script>

<template>
  <div class="category-list">
    <article v-for="item in series" :key="item.id" class="category-meter">
      <div>
        <strong>{{ item.label }}</strong>
        <small>{{ item.value }} 份</small>
      </div>
      <div class="meter-track">
        <span :style="{ width: `${Math.round((item.value / maxValue) * 100)}%` }" />
      </div>
    </article>
  </div>
</template>
