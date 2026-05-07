import { computed, watch } from 'vue'
import { menuState, adminState, uiState } from './state.js'
import { showToast } from './useToast.js'
import { normalizeCollection, clampPage } from '../utils/normalize.js'
import { formatPrice } from '../utils/format.js'
import { PAGE_SIZE } from '../config/constants.js'
import { fetchMenu } from '../api/menu.js'
import { fetchAdminDashboard } from '../api/dashboard.js'
import {
  fetchAdminDishes, createAdminDish, updateAdminDish,
  uploadAdminDishImage, toggleDishAvailability, deleteAdminDish,
} from '../api/dishes.js'
import {
  fetchAdminCategories, createAdminCategory,
  updateAdminCategory, deleteAdminCategory,
} from '../api/categories.js'

const safeAdminDishes = computed(() => normalizeCollection(adminState.dishes))
const safeAdminCategories = computed(() =>
  normalizeCollection(adminState.categories.length ? adminState.categories : menuState.categories),
)
const manageableCategories = computed(() =>
  safeAdminCategories.value.filter((item) => item.id !== 'all'),
)
const categoryDishCounts = computed(() =>
  safeAdminCategories.value.reduce((acc, category) => {
    acc[category.id] = safeAdminDishes.value.filter(
      (dish) => dish.categoryId === category.id,
    ).length
    return acc
  }, {}),
)

const filteredAdminDishes = computed(() => {
  if (uiState.adminCategoryFilter === 'all') return safeAdminDishes.value
  return safeAdminDishes.value.filter((d) => d.categoryId === uiState.adminCategoryFilter)
})

const adminTotalPages = computed(() =>
  Math.max(1, Math.ceil(filteredAdminDishes.value.length / PAGE_SIZE)),
)

const adminPagedDishes = computed(() =>
  filteredAdminDishes.value.slice(
    (uiState.adminPage - 1) * PAGE_SIZE,
    uiState.adminPage * PAGE_SIZE,
  ),
)

function setAdminCategoryFilter(categoryId) {
  uiState.adminCategoryFilter = categoryId
  uiState.adminPage = 1
}

function goAdminPage(delta) {
  uiState.adminPage = clampPage(uiState.adminPage + delta, adminTotalPages.value)
}

function goToPage(page) {
  uiState.adminPage = clampPage(page, adminTotalPages.value)
}

function adminPageList() {
  return Array.from({ length: adminTotalPages.value }, (_, i) => i + 1)
}

const canSaveDish = computed(() =>
  Boolean(
    uiState.dishForm.name.trim() &&
    uiState.dishForm.categoryId &&
    Number.isFinite(Number(uiState.dishForm.price)) &&
    Number(uiState.dishForm.price) > 0 &&
    uiState.dishForm.description.trim(),
  ),
)

const canSaveCategory = computed(() =>
  Boolean(
    uiState.categoryForm.label.trim() &&
    Number.isFinite(Number(uiState.categoryForm.sortOrder)),
  ),
)

function resetDishForm() {
  uiState.editingDishId = null
  uiState.dishForm.name = ''
  uiState.dishForm.categoryId = manageableCategories.value[0]?.id ?? 'signature'
  uiState.dishForm.price = ''
  uiState.dishForm.rating = '4.8'
  uiState.dishForm.calories = ''
  uiState.dishForm.description = ''
  uiState.dishForm.highlight = ''
  uiState.dishForm.imageUrl = ''
  uiState.dishForm.available = true
  uiState.dishFormErrors = {}
}

function openCreateDish() {
  resetDishForm()
  uiState.showDishForm = true
}

function openEditDish(dish) {
  uiState.editingDishId = dish.id
  uiState.dishForm.name = dish.name
  uiState.dishForm.categoryId = dish.categoryId
  uiState.dishForm.price = String(dish.price)
  uiState.dishForm.rating = String(dish.rating ?? '4.8')
  uiState.dishForm.calories = String(dish.calories ?? '')
  uiState.dishForm.description = dish.description ?? ''
  uiState.dishForm.highlight = dish.highlight ?? ''
  uiState.dishForm.imageUrl = dish.imageUrl ?? ''
  uiState.dishForm.available = dish.available
  uiState.dishFormErrors = {}
  uiState.showDishForm = true
}

function closeDishForm() {
  uiState.showDishForm = false
  resetDishForm()
}

function resetCategoryForm() {
  uiState.editingCategoryId = null
  uiState.categoryForm.label = ''
  uiState.categoryForm.sortOrder =
    Math.max(10, ...safeAdminCategories.value.map((item) => Number(item.sortOrder) || 0)) + 1
  uiState.categoryFormErrors = {}
}

function openCreateCategory() {
  resetCategoryForm()
  uiState.showCategoryForm = true
}

function openEditCategory(category) {
  uiState.editingCategoryId = category.id
  uiState.categoryForm.label = category.label
  uiState.categoryForm.sortOrder = Number(category.sortOrder) || 10
  uiState.categoryFormErrors = {}
  uiState.showCategoryForm = true
}

function closeCategoryForm() {
  uiState.showCategoryForm = false
  resetCategoryForm()
}

function openDeleteCategory(category) {
  uiState.pendingDeleteCategory = category
}

function closeDeleteCategory() {
  uiState.pendingDeleteCategory = null
}

function openDeleteDish(dish) {
  uiState.pendingDeleteDish = dish
}

function closeDeleteDish() {
  uiState.pendingDeleteDish = null
}

function validateDishForm() {
  const errors = {}
  if (!uiState.dishForm.name.trim()) errors.name = '请输入商品名称'
  if (!uiState.dishForm.categoryId) errors.categoryId = '请选择分类'
  const price = Number(uiState.dishForm.price)
  if (!Number.isFinite(price) || price <= 0) errors.price = '价格必须大于 0'
  if (!uiState.dishForm.description.trim()) errors.description = '请输入商品介绍'
  uiState.dishFormErrors = errors
  return Object.keys(errors).length === 0
}

function validateCategoryForm() {
  const errors = {}
  if (!uiState.categoryForm.label.trim()) errors.label = '请输入分类名称'
  if (!Number.isFinite(Number(uiState.categoryForm.sortOrder))) errors.sortOrder = '排序必须为数字'
  uiState.categoryFormErrors = errors
  return Object.keys(errors).length === 0
}

function buildDishPayload() {
  const introduction = uiState.dishForm.description.trim()
  return {
    name: uiState.dishForm.name.trim(),
    categoryId: uiState.dishForm.categoryId,
    price: Number(uiState.dishForm.price) || 0,
    rating: Number(uiState.dishForm.rating) || 4.8,
    calories: Number(uiState.dishForm.calories) || 0,
    description: introduction,
    highlight: uiState.dishForm.highlight || introduction,
    imageUrl: uiState.dishForm.imageUrl,
    available: uiState.dishForm.available,
  }
}

async function refreshAdminCatalog() {
  const [menuResponse, categories, dishes, dashboard] = await Promise.all([
    fetchMenu(),
    fetchAdminCategories(),
    fetchAdminDishes(),
    fetchAdminDashboard(),
  ])

  menuState.categories = menuResponse.categories || []
  menuState.dishes = normalizeCollection(menuResponse.dishes)
  adminState.categories = categories || []
  adminState.dishes = normalizeCollection(dishes)
  adminState.dashboard = dashboard
  clampAdminPages()
}

async function saveDish() {
  if (!validateDishForm()) return

  try {
    uiState.actionLoading = true
    const payload = buildDishPayload()

    if (uiState.editingDishId) {
      await updateAdminDish(uiState.editingDishId, payload)
      showToast('商品已更新')
    } else {
      await createAdminDish(payload)
      showToast('商品已创建')
    }

    closeDishForm()
    await refreshAdminCatalog()
  } catch (error) {
    showToast(error.message || '商品保存失败')
  } finally {
    uiState.actionLoading = false
  }
}

async function saveCategory() {
  if (!validateCategoryForm()) return

  try {
    uiState.actionLoading = true
    const payload = {
      label: uiState.categoryForm.label.trim(),
      sortOrder: Number(uiState.categoryForm.sortOrder),
    }

    if (uiState.editingCategoryId) {
      await updateAdminCategory(uiState.editingCategoryId, payload)
      showToast('分类已更新')
    } else {
      await createAdminCategory(payload)
      showToast('分类已创建')
    }

    closeCategoryForm()
    await refreshAdminCatalog()
  } catch (error) {
    showToast(error.message || '分类保存失败')
  } finally {
    uiState.actionLoading = false
  }
}

async function confirmDeleteDish() {
  if (!uiState.pendingDeleteDish) return

  const dish = uiState.pendingDeleteDish
  closeDeleteDish()

  try {
    uiState.actionLoading = true
    await deleteAdminDish(dish.id)
    showToast(`已删除 ${dish.name}`)
    await refreshAdminCatalog()
  } catch (error) {
    showToast(error.message || '商品删除失败')
  } finally {
    uiState.actionLoading = false
  }
}

async function confirmDeleteCategory() {
  if (!uiState.pendingDeleteCategory) return

  const category = uiState.pendingDeleteCategory
  closeDeleteCategory()

  try {
    uiState.actionLoading = true
    await deleteAdminCategory(category.id)
    showToast('分类已删除')
    await refreshAdminCatalog()
  } catch (error) {
    showToast(error.message || '分类删除失败，请先移动或删除该分类下商品')
  } finally {
    uiState.actionLoading = false
  }
}

async function handleDishImageUpload(file) {
  if (!file) return

  try {
    uiState.imageUploading = true
    const response = await uploadAdminDishImage(file)
    uiState.dishForm.imageUrl = response.url ?? ''
    showToast('图片上传成功')
  } catch (error) {
    showToast(error.message || '图片上传失败')
  } finally {
    uiState.imageUploading = false
  }
}

async function handleToggleDish(dish) {
  const dishId = dish.id
  if (uiState.loadingActions.has(dishId)) return

  const newAvailable = !dish.available
  const prev = dish.available

  // Optimistic local update
  dish.available = newAvailable
  adminState.dashboard.availableDishCount += newAvailable ? 1 : -1
  adminState.dashboard.unavailableDishCount += newAvailable ? -1 : 1

  uiState.loadingActions.add(dishId)

  try {
    await toggleDishAvailability(dish.id, newAvailable)
    showToast(newAvailable ? `已上架 ${dish.name}` : `已下架 ${dish.name}`)
    await refreshAdminCatalog()
  } catch (error) {
    // Revert on failure
    dish.available = prev
    adminState.dashboard.availableDishCount += prev ? 1 : -1
    adminState.dashboard.unavailableDishCount += prev ? -1 : 1
    showToast(error.message || '状态更新失败')
  } finally {
    uiState.loadingActions.delete(dishId)
  }
}

function clampAdminPages() {
  uiState.adminPage = clampPage(uiState.adminPage, adminTotalPages.value)
}

let initialized = false

function init() {
  if (initialized) return
  initialized = true

  watch(
    () => adminState.dishes.length,
    () => {
      uiState.adminPage = clampPage(uiState.adminPage, adminTotalPages.value)
    },
  )
}

function getDishImage(dish) {
  return dish?.imageUrl || ''
}

export function useAdmin() {
  init()
  return {
    safeAdminDishes,
    safeAdminCategories,
    manageableCategories,
    categoryDishCounts,
    filteredAdminDishes,
    adminTotalPages,
    adminPagedDishes,
    goAdminPage,
    goToPage,
    adminPageList,
    setAdminCategoryFilter,
    canSaveDish,
    canSaveCategory,
    resetDishForm,
    openCreateDish,
    openEditDish,
    closeDishForm,
    resetCategoryForm,
    openCreateCategory,
    openEditCategory,
    closeCategoryForm,
    openDeleteCategory,
    closeDeleteCategory,
    openDeleteDish,
    closeDeleteDish,
    validateDishForm,
    validateCategoryForm,
    saveDish,
    saveCategory,
    confirmDeleteDish,
    confirmDeleteCategory,
    handleDishImageUpload,
    handleToggleDish,
    clampAdminPages,
    getDishImage,
  }
}
