import { computed, ref } from 'vue'
import { fetchMenu } from '../api/menu.js'

const dishes = ref([])
const categories = ref([{ id: 'all', label: '全部餐品' }])
const activeCategory = ref('all')
const searchQuery = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

function normalizeDish(dish, categoryLabels) {
  return {
    id: dish.id,
    title: dish.name,
    category: dish.category,
    categoryLabel: dish.categoryLabel || categoryLabels[dish.category] || dish.category,
    price: Number(dish.price) || 0,
    rating: dish.rating,
    calories: dish.calories,
    ingredients: dish.description || '',
    flavor: dish.highlight || dish.description || '店内精选',
    desc: dish.description || '',
    image: dish.imageUrl || 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=900&q=85',
  }
}

function normalizeCategories(list) {
  const mapped = (Array.isArray(list) ? list : []).map((category) => ({
    id: category.id,
    label: category.label,
  }))
  return mapped.length ? mapped : [{ id: 'all', label: '全部餐品' }]
}

let menuPromise = null

async function loadProducts() {
  if (menuPromise) return menuPromise
  isLoading.value = true
  errorMessage.value = ''
  menuPromise = fetchMenu()
    .then((menu) => {
      categories.value = normalizeCategories(menu.categories)
      const categoryLabels = categories.value.reduce((acc, category) => {
        acc[category.id] = category.label
        return acc
      }, {})
      dishes.value = (menu.dishes || []).map((dish) => normalizeDish(dish, categoryLabels))
      if (!categories.value.some((item) => item.id === activeCategory.value)) {
        activeCategory.value = 'all'
      }
    })
    .catch((error) => {
      errorMessage.value = error.message || '菜单加载失败'
      throw error
    })
    .finally(() => {
      isLoading.value = false
      menuPromise = null
    })
  return menuPromise
}

export function useProducts() {
  const filteredProducts = computed(() => {
    const keyword = searchQuery.value.trim().toLowerCase()
    return dishes.value.filter((dish) => {
      const matchesCategory = activeCategory.value === 'all' || dish.category === activeCategory.value
      const matchesSearch =
        !keyword ||
        [dish.title, dish.category, dish.ingredients, dish.flavor].some((item) =>
          item.toLowerCase().includes(keyword),
        )
      return matchesCategory && matchesSearch
    })
  })

  const featuredProducts = computed(() => filteredProducts.value.slice(0, 3))

  function setCategory(category) {
    activeCategory.value = category
  }

  function getProductById(id) {
    return dishes.value.find((dish) => dish.id === Number(id))
  }

  return {
    dishes,
    categories,
    activeCategory,
    searchQuery,
    filteredProducts,
    featuredProducts,
    isLoading,
    errorMessage,
    loadProducts,
    setCategory,
    getProductById,
  }
}
