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
  } else if (to.meta.guest && token) {
    next({ name: 'Home' })
  } else {
    next()
  }
})

export default router
