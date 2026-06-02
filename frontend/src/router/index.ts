import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: () => import('@/layouts/DefaultLayout.vue'),
      children: [
        {
          path: '',
          name: 'Home',
          component: () => import('@/views/Home.vue'),
        },
        {
          path: 'products',
          name: 'ProductList',
          component: () => import('@/views/ProductList.vue'),
        },
        {
          path: 'products/:id',
          name: 'ProductDetail',
          component: () => import('@/views/ProductDetail.vue'),
        },
        {
          path: 'publish',
          name: 'PublishProduct',
          component: () => import('@/views/PublishProduct.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'publish/:id',
          name: 'EditProduct',
          component: () => import('@/views/PublishProduct.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'profile',
          name: 'UserProfile',
          component: () => import('@/views/UserProfile.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'profile/listings',
          name: 'MyListings',
          component: () => import('@/views/MyListings.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'profile/favorites',
          name: 'Favorites',
          component: () => import('@/views/Favorites.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'checkout/:productId',
          name: 'Checkout',
          component: () => import('@/views/Checkout.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'payment/:orderId',
          name: 'Payment',
          component: () => import('@/views/Payment.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'orders',
          name: 'MyOrders',
          component: () => import('@/views/MyOrders.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'orders/:id',
          name: 'OrderDetail',
          component: () => import('@/views/OrderDetail.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'profile/sales',
          name: 'SellerOrders',
          component: () => import('@/views/SellerOrders.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'notifications',
          name: 'Notifications',
          component: () => import('@/views/Notifications.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'chat',
          name: 'Chat',
          component: () => import('@/views/Chat.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'faq',
          name: 'FAQ',
          component: () => import('@/views/Faq.vue'),
        },
        {
          path: 'guide',
          name: 'Guide',
          component: () => import('@/views/Guide.vue'),
        },
        {
          path: 'shop/:sellerId',
          name: 'Shop',
          component: () => import('@/views/Shop.vue'),
        },
        {
          path: 'community',
          name: 'Community',
          component: () => import('@/views/community/Community.vue'),
        },
        {
          path: 'community/create',
          name: 'CreatePost',
          component: () => import('@/views/community/CreatePost.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'community/:id',
          name: 'PostDetail',
          component: () => import('@/views/community/PostDetail.vue'),
        },
      ],
    },
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAdmin: true },
      children: [
        {
          path: '',
          redirect: { name: 'AdminDashboard' },
        },
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/Dashboard.vue'),
        },
        {
          path: 'review',
          name: 'AdminReview',
          component: () => import('@/views/admin/ReviewQueue.vue'),
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('@/views/admin/UserManagement.vue'),
        },
        {
          path: 'categories',
          name: 'AdminCategories',
          component: () => import('@/views/admin/CategoryManagement.vue'),
        },
        {
          path: 'faqs',
          name: 'AdminFaqs',
          component: () => import('@/views/admin/FaqManagement.vue'),
        },
      ],
    },
    {
      path: '/',
      component: () => import('@/layouts/AuthLayout.vue'),
      meta: { guest: true },
      children: [
        {
          path: 'login',
          name: 'Login',
          component: () => import('@/views/Login.vue'),
        },
        {
          path: 'register',
          name: 'Register',
          component: () => import('@/views/Register.vue'),
        },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.guest && token) {
    next({ name: 'Home' })
    return
  }

  if (to.meta.requiresAdmin) {
    if (!token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }
    try {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        const user = JSON.parse(userStr)
        if (user.role !== 'ADMIN') {
          next({ name: 'Home' })
          return
        }
      } else {
        next({ name: 'Home' })
        return
      }
    } catch {
      next({ name: 'Home' })
      return
    }
  }

  next()
})

export default router
