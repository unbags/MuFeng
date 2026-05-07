<script setup>
import { computed } from 'vue'
import { useAdmin } from '../../composables/useAdmin.js'
import { uiState } from '../../composables/state.js'
import { formatPrice } from '../../utils/format.js'
import ImageWithFallback from '../ui/ImageWithFallback.vue'

const props = defineProps({ dish: { type: Object, required: true } })
const admin = useAdmin()

const toggling = computed(() => uiState.loadingActions.has(props.dish.id))
</script>

<template>
  <article class="admin-dish-card compact" :class="{ toggling }">
    <div class="admin-dish-top">
      <div class="admin-thumb">
        <ImageWithFallback :src="admin.getDishImage(dish)" :alt="dish.name" fallback-text="无图" />
      </div>
      <div class="admin-dish-copy">
        <h4>{{ dish.name }}</h4>
        <p>{{ dish.categoryLabel }}</p>
      </div>
    </div>

    <div class="admin-dish-meta compact">
      <strong>{{ formatPrice(dish.price) }}</strong>
    </div>

    <div class="admin-dish-status">
      <span class="status-pill" :class="{ offline: !dish.available }">
        {{ dish.available ? '已上架' : '已下架' }}
      </span>
    </div>

    <div class="admin-dish-actions compact">
      <button class="ghost-btn" @click.stop="admin.openEditDish(dish)">编辑</button>
      <button
        class="ghost-btn toggle-btn"
        :class="{ toggling }"
        :disabled="toggling"
        @click="admin.handleToggleDish(dish)"
      >
        <span class="toggle-label">{{ toggling ? '处理中' : (dish.available ? '下架' : '上架') }}</span>
      </button>
      <button class="danger-btn" @click="admin.openDeleteDish(dish)">删除</button>
    </div>
  </article>
</template>
