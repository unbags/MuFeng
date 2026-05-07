<script setup>
import { computed } from 'vue'
import { useAdmin } from '../../composables/useAdmin.js'
import { useOrderingStore } from '../../composables/useOrderingStore.js'
import { uiState } from '../../composables/state.js'

const props = defineProps({ category: { type: Object, required: true } })
const emit = defineEmits(['select'])
const admin = useAdmin()
const store = useOrderingStore()

const isSelected = computed(() => uiState.adminCategoryFilter === props.category.id)

const dishCount = computed(() => {
  if (props.category.id === 'all') return store.safeAdminDishes.length
  return (admin.categoryDishCounts.value || {})[props.category.id] || 0
})
</script>

<template>
  <article
    class="category-card compact-category-card"
    :class="{ locked: category.id === 'all', selected: isSelected }"
    @click="emit('select', category.id)"
  >
    <div class="category-card-main compact-category-main">
      <div>
        <span class="category-type-label">{{ category.id === 'all' ? '系统分类' : '自定义分类' }}</span>
        <h3>{{ category.label }}</h3>
      </div>
      <strong>{{ dishCount }}</strong>
    </div>

    <div class="category-card-actions compact-category-actions">
      <button class="ghost-btn" :disabled="category.id === 'all'" @click.stop="admin.openEditCategory(category)">编辑</button>
      <button class="danger-btn" :disabled="category.id === 'all'" @click.stop="admin.openDeleteCategory(category)">删除</button>
    </div>
  </article>
</template>
