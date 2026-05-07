import { reactive } from 'vue'

export const menuState = reactive({
  categories: [],
  dishes: [],
})

export const adminState = reactive({
  categories: [],
  dishes: [],
  orders: [],
  dashboard: {
    totalRevenue: 0,
    todayRevenue: 0,
    todayOrderCount: 0,
    availableDishCount: 0,
    unavailableDishCount: 0,
  },
})

export const uiState = reactive({
  loading: true,
  actionLoading: false,
  errorMessage: '',
  toastMessage: '',
  orderType: 'dine_in',
  activeCategory: 'all',
  adminCategoryFilter: 'all',
  customerPage: 1,
  adminPage: 1,
  note: '',
  showCheckout: false,
  showReceipt: false,
  lastOrderSnapshot: null,
  cart: [],
  showDishForm: false,
  pendingDeleteDish: null,
  editingDishId: null,
  showCategoryForm: false,
  pendingDeleteCategory: null,
  editingCategoryId: null,
  showOrderDetail: false,
  selectedOrderDetail: null,
  orderDetailLoading: false,
  orderDetailError: '',
  dashboardTimeRange: 'week',
  categoryForm: {
    label: '',
    sortOrder: 10,
  },
  dishForm: {
    name: '',
    categoryId: 'signature',
    price: '',
    rating: '4.8',
    calories: '',
    description: '',
    highlight: '',
    imageUrl: '',
    available: true,
  },
  imageUploading: false,
  loadingActions: new Set(),
  dishFormErrors: {},
  categoryFormErrors: {},
})
