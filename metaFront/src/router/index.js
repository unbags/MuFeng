import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/menu',
  },
  {
    path: '/menu',
    name: 'menu',
    component: () => import('../views/CollectionView.vue'),
  },
  {
    path: '/menu/:id',
    name: 'dish-detail',
    component: () => import('../views/ProductDetailView.vue'),
  },
  {
    path: '/orders/:orderNo',
    name: 'order-status',
    component: () => import('../views/OrderStatusView.vue'),
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('../views/NotFoundView.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0, behavior: 'smooth' }
  },
})

export default router
