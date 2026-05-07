<script setup>
import { useOrderingStore } from '../composables/useOrderingStore'
import { useMenu } from '../composables/useMenu'
import DishCard from '../components/menu/DishCard.vue'
import OrderTypeSwitch from '../components/menu/OrderTypeSwitch.vue'
import CartPanel from '../components/cart/CartPanel.vue'
import CheckoutModal from '../components/cart/CheckoutModal.vue'
import ReceiptModal from '../components/cart/ReceiptModal.vue'

const store = useOrderingStore()
const menu = useMenu()
</script>

<template>
  <section class="screen-page customer-page" :class="store.state.orderType === 'delivery' ? 'order-takeout' : 'order-dine-in'">
    <section class="page-body customer-body">
      <article class="panel menu-panel">
        <div class="menu-toolbar">
          <div class="chip-row">
            <button
              v-for="category in store.safeCategories"
              :key="category.id"
              class="filter-chip"
              :class="{ active: store.state.activeCategory === category.id }"
              @click="menu.selectCategory(category.id)"
            >{{ category.label }}</button>
          </div>
          <OrderTypeSwitch />
        </div>

        <div class="dish-grid-scroll">
          <div class="dish-grid">
            <DishCard v-for="dish in store.customerPagedDishes" :key="dish.id" :dish="dish" />
          </div>
        </div>

        <div class="pagination-bar compact">
          <button class="ghost-btn" :disabled="store.state.customerPage === 1" @click="store.goCustomerPage(-1)">上一页</button>
          <button
            v-for="page in store.customerPageList()" :key="page"
            class="page-chip" :class="{ active: store.state.customerPage === page }"
            @click="store.state.customerPage = page"
          >{{ page }}</button>
          <button class="ghost-btn" :disabled="store.state.customerPage === store.customerTotalPages" @click="store.goCustomerPage(1)">下一页</button>
        </div>
      </article>

      <CartPanel />
    </section>

    <CheckoutModal />
    <ReceiptModal />
  </section>
</template>
