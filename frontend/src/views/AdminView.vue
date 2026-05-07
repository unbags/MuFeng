<script setup>
import { computed } from 'vue'
import { useOrderingStore } from '../composables/useOrderingStore'
import { useAdmin } from '../composables/useAdmin'
import { uiState } from '../composables/state.js'
import AdminDishCard from '../components/admin/AdminDishCard.vue'
import AdminCategoryCard from '../components/admin/AdminCategoryCard.vue'
import DishFormModal from '../components/admin/DishFormModal.vue'
import CategoryFormModal from '../components/admin/CategoryFormModal.vue'
import ConfirmDeleteModal from '../components/admin/ConfirmDeleteModal.vue'
import MetricCard from '../components/ui/MetricCard.vue'
import Pagination from '../components/ui/Pagination.vue'
import EmptyState from '../components/ui/EmptyState.vue'

const store = useOrderingStore()
const admin = useAdmin()

const adminOverview = computed(() => [
  { label: '商品总数', value: `${store.safeAdminDishes.length} 道` },
  { label: '分类总数', value: `${store.manageableCategories.length} 类` },
  { label: '上架商品', value: `${store.admin.dashboard.availableDishCount} 道` },
])

const activeFilterLabel = computed(() => {
  if (uiState.adminCategoryFilter === 'all') return ''
  const cat = store.safeAdminCategories.find((c) => c.id === uiState.adminCategoryFilter)
  return cat ? cat.label : ''
})

function onCategorySelect(categoryId) {
  admin.setAdminCategoryFilter(categoryId)
}
</script>

<template>
  <section class="screen-page admin-page">
    <section class="page-body admin-body compact-admin-body">
      <article class="panel admin-products-panel compact-admin-panel">
        <div class="admin-toolbar">
          <div class="admin-overview-row">
            <MetricCard v-for="item in adminOverview" :key="item.label" :label="item.label" :value="item.value" />
          </div>
          <div class="admin-action-row">
            <div>
              <span class="section-label">商品管理</span>
              <h3>
                商品创建与维护
                <span v-if="activeFilterLabel" class="filter-badge">
                  {{ activeFilterLabel }}
                  <button class="filter-clear" @click="onCategorySelect('all')">&times;</button>
                </span>
              </h3>
            </div>
            <button class="primary-btn" @click.stop="store.openCreateDish()">新增商品</button>
          </div>
        </div>

        <div class="admin-grid-scroll">
          <div v-if="admin.filteredAdminDishes.value.length" class="admin-dish-grid">
            <AdminDishCard v-for="dish in store.adminPagedDishes" :key="dish.id" :dish="dish" />
          </div>
          <EmptyState v-else title="该分类下暂无商品" description="点击「新增商品」为此分类添加商品，或选择其他分类。" />
        </div>

        <Pagination
          :current-page="store.state.adminPage"
          :total-pages="store.adminTotalPages"
          @prev="store.goAdminPage(-1)"
          @next="store.goAdminPage(1)"
          @go="admin.goToPage($event)"
        />
      </article>

      <aside class="panel admin-side compact-admin-side category-admin-side">
        <div class="panel-head compact">
          <div>
            <span class="section-label">分类管理</span>
            <h3>商品分类维护</h3>
          </div>
          <button class="primary-btn compact-action" @click="store.openCreateCategory">新增分类</button>
        </div>

        <div class="admin-side-scroll category-side-scroll">
          <section class="category-side-list">
            <AdminCategoryCard v-for="category in store.safeAdminCategories" :key="category.id" :category="category" @select="onCategorySelect" />
            <EmptyState v-if="!store.safeAdminCategories.length" title="暂无分类" description="点击新增分类后，可以在创建商品时选择使用。" />
          </section>
        </div>
      </aside>
    </section>

    <DishFormModal />
    <CategoryFormModal />

    <ConfirmDeleteModal
      v-if="store.state.pendingDeleteDish"
      title="删除确认"
      :message="`确认删除这个商品吗？`"
      detail="删除后会从前台菜单和后台列表移除，但历史订单记录仍会保留。"
      :item-name="store.state.pendingDeleteDish.name"
      :item-meta="`${store.state.pendingDeleteDish.categoryLabel} · ${store.formatPrice(store.state.pendingDeleteDish.price)}`"
      @cancel="store.closeDeleteDish"
      @confirm="store.confirmDeleteDish"
    />

    <ConfirmDeleteModal
      v-if="store.state.pendingDeleteCategory"
      title="删除分类"
      :message="`确认删除该分类吗？`"
      detail="如果该分类下仍有商品，系统会阻止删除。请先移动或删除相关商品。"
      :item-name="store.state.pendingDeleteCategory.label"
      :item-meta="`当前商品数：${store.categoryDishCounts[store.state.pendingDeleteCategory.id] || 0} 道`"
      @cancel="store.closeDeleteCategory"
      @confirm="store.confirmDeleteCategory"
    />
  </section>
</template>
