<template>
  <div class="home-page">
    <!-- Hero 区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">校园二手交易</h1>
        <p class="hero-subtitle">安全便捷的校园闲置物品交易平台</p>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-section">
      <div class="search-bar">
        <el-icon class="search-icon"><Search /></el-icon>
        <input
          v-model="keyword"
          placeholder="搜索商品名称、描述..."
          class="search-input"
          @keyup.enter="search"
        />
        <el-button type="primary" class="search-btn" @click="search">搜索</el-button>
      </div>
    </div>

    <!-- 分类导航 -->
    <div class="category-nav">
      <button
        v-for="cat in categories"
        :key="cat.id"
        class="category-pill"
        :class="{ active: activeCategory === cat.id }"
        @click="activeCategory = activeCategory === cat.id ? 0 : cat.id"
      >
        {{ cat.name }}
      </button>
    </div>

    <!-- 错误态 -->
    <el-result
      v-if="error"
      icon="error"
      title="加载失败"
      sub-title="无法获取商品列表，请重试"
    >
      <template #extra>
        <el-button type="primary" @click="fetchData">重新加载</el-button>
      </template>
    </el-result>

    <!-- 区域标题 + 商品列表 -->
    <template v-else>
      <div class="section-header">
        <h2 class="section-title">最新商品</h2>
        <router-link to="/products" class="section-more">
          查看全部 <el-icon><ArrowRight /></el-icon>
        </router-link>
      </div>

      <div v-loading="loading" class="product-grid">
        <template v-if="!loading && products.length === 0">
          <el-empty class="empty-state" description="暂无商品，快去发布第一个吧" />
        </template>
        <ProductCard
          v-for="product in products"
          :key="product.id"
          :product="product"
          :show-favorite="auth.isLoggedIn()"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Search, ArrowRight } from '@element-plus/icons-vue'
import { productApi } from '@/api/modules/product'
import { categoryApi } from '@/api/modules/category'
import { favoriteApi } from '@/api/modules/favorite'
import { useAuthStore } from '@/stores/auth'
import type { Product, Category } from '@/types'
import ProductCard from '@/components/product/ProductCard.vue'

const auth = useAuthStore()
const keyword = ref('')
const activeCategory = ref(0)
const loading = ref(false)
const error = ref(false)
const products = ref<Product[]>([])
const categories = ref<Category[]>([])

const fetchData = async () => {
  loading.value = true
  error.value = false
  try {
    const [productData, categoryData] = await Promise.all([
      productApi.list({ page: 1, size: 20, categoryId: activeCategory.value || undefined, keyword: keyword.value || undefined }),
      categoryApi.list(),
    ])
    products.value = productData.records
    categories.value = categoryData
    loading.value = false

    if (auth.isLoggedIn() && productData.records.length > 0) {
      const ids = productData.records.map(p => p.id)
      const favMap = await favoriteApi.check(ids)
      products.value.forEach(p => {
        p.isFavorited = favMap[p.id] || false
      })
    }
  } catch {
    error.value = true
    loading.value = false
  }
}

watch(activeCategory, fetchData)
fetchData()

const search = () => fetchData()
</script>

<style scoped>
.home-page { padding: 20px 0; }

/* Hero */
.hero-section {
  background: var(--gradient-hero);
  border-radius: var(--radius-xl);
  padding: 48px 40px;
  margin-bottom: 32px;
  text-align: center;
  position: relative;
  overflow: hidden;
}
.hero-section::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(79, 110, 247, 0.08) 0%, transparent 60%);
  transform: translate(-50%, -50%);
  pointer-events: none;
}
.hero-content {
  position: relative;
  z-index: 1;
}
.hero-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}
.hero-subtitle {
  font-size: var(--text-lg);
  color: var(--text-secondary);
}

/* 搜索 */
.search-section {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}
.search-bar {
  display: flex;
  align-items: center;
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: 4px 4px 4px 16px;
  width: 100%;
  max-width: 560px;
  box-shadow: var(--shadow-sm);
  transition: box-shadow var(--transition-base), border-color var(--transition-base);
}
.search-bar:focus-within {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 3px rgba(79, 110, 247, 0.12), var(--shadow-md);
}
.search-icon {
  color: var(--text-muted);
  font-size: 18px;
  margin-right: 8px;
  flex-shrink: 0;
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: var(--text-primary);
  background: transparent;
  height: 40px;
  font-family: var(--font-sans);
}
.search-input::placeholder {
  color: var(--text-muted);
}
.search-btn {
  border-radius: var(--radius-lg);
  padding: 8px 24px;
  flex-shrink: 0;
}

/* 分类 */
.category-nav {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 32px;
  justify-content: center;
}
.category-pill {
  padding: 6px 18px;
  border-radius: var(--radius-full);
  border: 1px solid var(--border-color);
  background: var(--bg-white);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition-fast);
  font-family: var(--font-sans);
  outline: none;
}
.category-pill:hover {
  border-color: var(--el-color-primary-light-5);
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}
.category-pill.active {
  background: var(--gradient-primary);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 2px 8px rgba(79, 110, 247, 0.25);
}

/* 区域标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.section-title {
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--text-primary);
}
.section-more {
  font-size: var(--text-sm);
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
  gap: 4px;
  transition: gap var(--transition-fast);
  text-decoration: none;
}
.section-more:hover {
  gap: 8px;
  color: var(--el-color-primary-dark-2);
}

/* 商品网格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  min-height: 200px;
}
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .hero-section {
    padding: 32px 20px;
    border-radius: var(--radius-lg);
  }
  .hero-title {
    font-size: 24px;
  }
}
@media (max-width: 640px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
