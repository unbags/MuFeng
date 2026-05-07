<script setup>
import { ref } from 'vue'

defineProps({
  src: { type: String, default: '' },
  alt: { type: String, default: '' },
  fallbackText: { type: String, default: '暂无图片' },
})

const loadFailed = ref(false)

function onImageError() {
  loadFailed.value = true
}
</script>

<template>
  <img v-if="src && !loadFailed" :src="src" :alt="alt" class="dish-image" @error="onImageError" />
  <div v-else class="dish-placeholder">
    <strong>{{ alt || fallbackText }}</strong>
    <span>{{ fallbackText }}</span>
  </div>
</template>
