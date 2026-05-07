import { computed, watch, nextTick } from 'vue'
import { menuState, uiState } from './state.js'
import { normalizeCollection, clampPage } from '../utils/normalize.js'
import { PAGE_SIZE } from '../config/constants.js'

const safeCategories = computed(() => normalizeCollection(menuState.categories))
const safeMenuDishes = computed(() => normalizeCollection(menuState.dishes))

const filteredDishes = computed(() => {
  if (uiState.activeCategory === 'all') return safeMenuDishes.value
  return safeMenuDishes.value.filter((dish) => dish.category === uiState.activeCategory)
})

const customerTotalPages = computed(() =>
  Math.max(1, Math.ceil(filteredDishes.value.length / PAGE_SIZE)),
)

const customerPagedDishes = computed(() => {
  const start = (uiState.customerPage - 1) * PAGE_SIZE
  const end = uiState.customerPage * PAGE_SIZE
  return normalizeCollection(filteredDishes.value.slice(start, end))
})

function selectCategory(categoryId) {
  uiState.activeCategory = categoryId
}

function goCustomerPage(delta) {
  uiState.customerPage = clampPage(uiState.customerPage + delta, customerTotalPages.value)
}

function customerPageList() {
  return Array.from({ length: customerTotalPages.value }, (_, i) => i + 1)
}

let categoryJustChanged = false

function clampPages() {
  uiState.customerPage = clampPage(uiState.customerPage, customerTotalPages.value)
}

let initialized = false

function init() {
  if (initialized) return
  initialized = true

  watch(
    () => uiState.activeCategory,
    () => {
      categoryJustChanged = true
      uiState.customerPage = 1
      nextTick(() => { categoryJustChanged = false })
    },
  )

  watch(
    () => filteredDishes.value.length,
    () => {
      if (categoryJustChanged) return
      uiState.customerPage = clampPage(uiState.customerPage, customerTotalPages.value)
    },
  )
}

export function useMenu() {
  init()
  return {
    safeCategories,
    safeMenuDishes,
    filteredDishes,
    customerTotalPages,
    customerPagedDishes,
    selectCategory,
    goCustomerPage,
    customerPageList,
    clampPages,
  }
}
