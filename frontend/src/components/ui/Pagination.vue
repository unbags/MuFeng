<script setup>
defineProps({
  currentPage: { type: Number, required: true },
  totalPages: { type: Number, required: true },
})
defineEmits(['prev', 'next', 'go'])
</script>

<template>
  <div class="pagination-bar compact" v-if="totalPages > 0">
    <button class="ghost-btn" :disabled="currentPage <= 1" @click="$emit('prev')">上一页</button>
    <button
      v-for="page in totalPages"
      :key="page"
      class="page-chip"
      :class="{ active: currentPage === page }"
      @click="$emit('go', page)"
    >{{ page }}</button>
    <button class="ghost-btn" :disabled="currentPage >= totalPages" @click="$emit('next')">下一页</button>
  </div>
</template>
