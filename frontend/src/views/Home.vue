<template>
  <div class="home-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品..."
        size="large"
        clearable
        @keyup.enter="search"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" size="large" @click="search">搜索</el-button>
    </div>

    <!-- 分类导航 -->
    <div class="category-nav">
      <el-button
        v-for="cat in categories"
        :key="cat.id"
        :type="activeCategory === cat.id ? 'primary' : 'default'"
        @click="activeCategory = activeCategory === cat.id ? 0 : cat.id"
      >
        {{ cat.name }}
      </el-button>
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

    <!-- 商品列表 -->
    <div v-else v-loading="loading" class="product-grid">
      <template v-if="!loading && products.length === 0">
        <el-empty description="暂无商品，快去发布第一个吧" />
      </template>
      <ProductCard
        v-for="product in products"
        :key="product.id"
        :product="product"
        :show-favorite="isLoggedIn"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { productApi } from '@/api/modules/product'
import { categoryApi } from '@/api/modules/category'
import { favoriteApi } from '@/api/modules/favorite'
import type { Product, Category } from '@/types'
import ProductCard from '@/components/product/ProductCard.vue'

const keyword = ref('')
const activeCategory = ref(0)
const loading = ref(false)
const error = ref(false)
const products = ref<Product[]>([])
const categories = ref<Category[]>([])

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

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

    if (isLoggedIn.value && productData.records.length > 0) {
      const ids = productData.records.map(p => p.id)
      const favMap = await favoriteApi.check(ids)
      products.value.forEach(p => {
        p.isFavorited = favMap[p.id] || false
      })
    }
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

watch(activeCategory, fetchData)
fetchData()

const search = () => fetchData()
</script>

<style scoped>
.home-page { padding: 20px 0; }
.search-bar {
  display: flex;
  gap: 12px;
  max-width: 600px;
  margin: 0 auto 30px;
}
.category-nav {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 30px;
  justify-content: center;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  min-height: 200px;
}
</style>
