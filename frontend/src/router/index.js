import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'

const routes = [
  {
    path: '/',
    redirect: to => {
      const userStr = localStorage.getItem('user') || sessionStorage.getItem('user')
      if (userStr) {
        try {
          const user = JSON.parse(userStr)
          if (user.role === 'TENANT') return '/tenant'
          if (user.role === 'MERCHANT') return '/merchant'
          if (user.role === 'ADMIN') return '/admin'
        } catch (e) {}
      }
      return '/login'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/ForgotPassword.vue')
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('../views/UserProfile.vue')
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/Cart.vue')
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('../views/Favorites.vue')
  },
  // Placeholders for different roles
  {
    path: '/tenant',
    name: 'TenantHome',
    component: () => import('../views/TenantHome.vue')
  },
  {
    path: '/tenant/orders',
    name: 'TenantOrders',
    component: () => import('../views/TenantOrders.vue')
  },
  {
    path: '/generator/:id',
    name: 'GeneratorDetail',
    component: () => import('../views/GeneratorDetail.vue')
  },
  {
    path: '/booking/:id',
    name: 'Booking',
    component: () => import('../views/Booking.vue')
  },
  {
    path: '/payment',
    name: 'Payment',
    component: () => import('../views/Payment.vue')
  },
  {
    path: '/payment/success',
    name: 'PaymentSuccess',
    component: () => import('../views/PaymentSuccess.vue')
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('../views/OrderDetail.vue')
  },
  {
    path: '/merchant',
    name: 'MerchantHome',
    component: () => import('../views/MerchantHome.vue')
  },
  {
    path: '/merchant/customers',
    name: 'MerchantCustomers',
    component: () => import('../views/MerchantCustomers.vue')
  },
  {
    path: '/merchant/apply',
    name: 'MerchantApply',
    component: () => import('../views/MerchantApply.vue')
  },
  {
    path: '/admin',
    name: 'AdminHome',
    component: () => import('../views/AdminHome.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

router.beforeEach((to, from, next) => {
  const publicPages = ['/login', '/register', '/forgot-password'];
  const authRequired = !publicPages.includes(to.path);
  const loggedIn = localStorage.getItem('token') || sessionStorage.getItem('token');

  // Requirement 1: Force clear login state when returning to login page
  if (to.path === '/login') {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('user');
  }

  // Requirement 2 & Security: Redirect to login if trying to access protected page without token
  if (authRequired && !loggedIn) {
    return next('/login');
  }

  next();
})

export default router
