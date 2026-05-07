import { proxyRefs } from 'vue'
import { menuState, adminState, uiState } from './state.js'
import { showToast } from './useToast.js'
import { useMenu } from './useMenu.js'
import { useCart } from './useCart.js'
import { useAdmin } from './useAdmin.js'
import { useOrders } from './useOrders.js'
import { useDashboard } from './useDashboard.js'
import { formatPrice, formatOrderType, formatDateShort, formatDateTime } from '../utils/format.js'
import { normalizeId } from '../utils/normalize.js'
import { fetchMenu } from '../api/menu.js'
import { fetchAdminOrders } from '../api/orders.js'
import { fetchAdminDishes } from '../api/dishes.js'
import { fetchAdminCategories } from '../api/categories.js'
import { fetchAdminDashboard } from '../api/dashboard.js'

const menu = useMenu()
const cart = useCart()
const admin = useAdmin()
const orders = useOrders()
const dashboard = useDashboard()

async function fetchAllData() {
  const [menuResponse, adminCategories, adminDishes, adminOrders, adminDashboard] =
    await Promise.all([
      fetchMenu(),
      fetchAdminCategories(),
      fetchAdminDishes(),
      fetchAdminOrders(),
      fetchAdminDashboard(),
    ])

  return { menuResponse, adminCategories, adminDishes, adminOrders, adminDashboard }
}

function applyLoadedData({ menuResponse, adminCategories, adminDishes, adminOrders, adminDashboard }) {
  menuState.categories = menuResponse.categories
  menuState.dishes = (menuResponse.dishes || []).map((d) => {
    d.id = normalizeId(d.id)
    return d
  })
  adminState.categories = adminCategories
  adminState.dishes = (adminDishes || []).map((d) => {
    d.id = normalizeId(d.id)
    return d
  })
  adminState.orders = adminOrders
  adminState.dashboard = adminDashboard
}

async function loadAllData() {
  try {
    uiState.loading = true
    uiState.errorMessage = ''
    applyLoadedData(await fetchAllData())
    menu.clampPages()
    admin.clampAdminPages()
    dashboard.loadProductSales(uiState.dashboardTimeRange || 'week')
  } catch (error) {
    uiState.errorMessage = error.message || '数据加载失败'
  } finally {
    uiState.loading = false
  }
}

async function refreshAllData() {
  try {
    applyLoadedData(await fetchAllData())
    menu.clampPages()
    admin.clampAdminPages()
  } catch (error) {
    showToast(error.message || '数据刷新失败')
  }
}

let initialized = false

export function useOrderingStore() {
  if (!initialized) {
    initialized = true
  }

  return proxyRefs({
    // State
    menu: menuState,
    admin: adminState,
    state: uiState,

    // Menu
    safeCategories: menu.safeCategories,
    safeMenuDishes: menu.safeMenuDishes,
    filteredDishes: menu.filteredDishes,
    customerTotalPages: menu.customerTotalPages,
    customerPagedDishes: menu.customerPagedDishes,
    goCustomerPage: menu.goCustomerPage,
    customerPageList: menu.customerPageList,
    clampPages: menu.clampPages,

    // Cart
    cartItems: cart.cartItems,
    cartCount: cart.cartCount,
    subtotal: cart.subtotal,
    packageFee: cart.packageFee,
    deliveryFee: cart.deliveryFee,
    total: cart.total,
    getQuantity: cart.getQuantity,
    addToCart: cart.addToCart,
    decreaseQuantity: cart.decreaseQuantity,
    clearCart: cart.clearCart,
    openCheckout: cart.openCheckout,
    closeCheckout: cart.closeCheckout,
    submitOrder: cart.submitOrder,

    // Admin
    safeAdminDishes: admin.safeAdminDishes,
    safeAdminCategories: admin.safeAdminCategories,
    manageableCategories: admin.manageableCategories,
    categoryDishCounts: admin.categoryDishCounts,
    filteredAdminDishes: admin.filteredAdminDishes,
    adminTotalPages: admin.adminTotalPages,
    adminPagedDishes: admin.adminPagedDishes,
    goAdminPage: admin.goAdminPage,
    adminPageList: admin.adminPageList,
    setAdminCategoryFilter: admin.setAdminCategoryFilter,
    canSaveDish: admin.canSaveDish,
    canSaveCategory: admin.canSaveCategory,
    resetDishForm: admin.resetDishForm,
    openCreateDish: admin.openCreateDish,
    openEditDish: admin.openEditDish,
    closeDishForm: admin.closeDishForm,
    openDeleteDish: admin.openDeleteDish,
    closeDeleteDish: admin.closeDeleteDish,
    confirmDeleteDish: admin.confirmDeleteDish,
    resetCategoryForm: admin.resetCategoryForm,
    openCreateCategory: admin.openCreateCategory,
    openEditCategory: admin.openEditCategory,
    closeCategoryForm: admin.closeCategoryForm,
    openDeleteCategory: admin.openDeleteCategory,
    closeDeleteCategory: admin.closeDeleteCategory,
    saveCategory: admin.saveCategory,
    confirmDeleteCategory: admin.confirmDeleteCategory,
    saveDish: admin.saveDish,
    uploadDishImage: admin.handleDishImageUpload,
    handleToggleDish: admin.handleToggleDish,
    clampAdminPages: admin.clampAdminPages,
    getDishImage: admin.getDishImage,

    // Orders
    recentOrders: orders.recentOrders,
    openOrderDetail: orders.openOrderDetail,
    closeOrderDetail: orders.closeOrderDetail,

    // Dashboard
    revenueSeries: dashboard.revenueSeries,
    productSalesSeries: dashboard.productSalesSeries,
    orderTypeMix: dashboard.orderTypeMix,
    loadProductSales: dashboard.loadProductSales,

    // Data loading
    loadAllData,
    refreshAllData,

    // Utilities
    formatPrice,
    formatOrderType,
    formatDateShort,
    formatDateTime,
    showToast,
  })
}
