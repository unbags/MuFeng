import { createRouter, createWebHashHistory } from 'vue-router'
import AdminView from '../views/AdminView.vue'
import CustomerView from '../views/CustomerView.vue'
import DashboardView from '../views/DashboardView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import NotFoundView from '../views/NotFoundView.vue'

const AUTH_ROUTES = ['/login', '/register']

function getToken() {
  return localStorage.getItem('token')
}

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login',
    },
    {
      path: '/workbench',
      name: 'customer',
      component: CustomerView,
      meta: { requiresAuth: true },
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true },
    },
    {
      path: '/products',
      name: 'admin',
      component: AdminView,
      meta: { requiresAuth: true },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView,
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFoundView,
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const hasToken = !!getToken()
  const path = to.path

  // Logged-in users should not see auth pages
  if (hasToken && AUTH_ROUTES.includes(path)) {
    return '/dashboard'
  }

  // Unauthenticated users are redirected to login for all non-auth routes
  if (!hasToken && !AUTH_ROUTES.includes(path)) {
    return `/login?redirect=${encodeURIComponent(path)}`
  }
})

export default router
