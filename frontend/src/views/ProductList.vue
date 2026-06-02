<template>
  <div class="product-list-page">
    <h2 class="page-title">浏览商品</h2>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索关键词"
        clearable
        style="width: 240px"
        @keyup.enter="fetchData"
      />
      <el-select v-model="categoryId" placeholder="分类筛选" clearable style="width: 160px" @change="fetchData">
        <el-option
          v-for="cat in categories"
          :key="cat.id"
          :label="cat.name"
          :value="cat.id"
        />
      </el-select>
      <el-select v-model="sort" style="width: 160px" @change="fetchData">
        <el-option
          v-for="opt in SORT_OPTIONS"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
    </div>

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

    <template v-else>
      <div class="product-list-content">
        <AdSidebar />
        <div v-loading="loading" class="product-grid">
          <ProductCard
            v-for="p in products"
            :key="p.id"
            :product="p"
            :show-favorite="isLoggedIn"
          />
        </div>
        <AdSidebar />
      </div>
      <el-empty v-if="!loading && products.length === 0" description="没有找到商品" />

      <div v-if="total > size" class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { productApi } from '@/api/modules/product'
import { categoryApi } from '@/api/modules/category'
import { favoriteApi } from '@/api/modules/favorite'
import type { Product, Category } from '@/types'
import { SORT_OPTIONS } from '@/utils/constants'
import ProductCard from '@/components/product/ProductCard.vue'
import AdSidebar from '@/components/common/AdSidebar.vue'

const keyword = ref('')
const categoryId = ref<number | undefined>()
const sort = ref('latest')
const loading = ref(false)
const error = ref(false)
const products = ref<Product[]>([])
const categories = ref<Category[]>([])
const page = ref(1)
const size = ref(12)
const total = ref(0)

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const fetchData = async () => {
  loading.value = true
  error.value = false
  try {
    const data = await productApi.list({
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      categoryId: categoryId.value,
      sort: sort.value,
    })
    products.value = data.records
    total.value = data.total
    loading.value = false

    if (isLoggedIn.value && data.records.length > 0) {
      const ids = data.records.map(p => p.id)
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

onMounted(async () => {
  const [, catData] = await Promise.all([
    fetchData(),
    categoryApi.list(),
  ])
  categories.value = catData
})
</script>

<style scoped>
.product-list-content {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  width: 100vw;
  margin-left: calc(-50vw + 50%);
  padding: 0 1.5vw;
}

.product-list-content > .product-grid {
  flex: 1;
  min-width: 0;
}

.page-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  margin-bottom: 24px;
  letter-spacing: -0.3px;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xs);
  flex-wrap: wrap;
}
.toolbar :deep(.el-input__wrapper),
.toolbar :deep(.el-select__wrapper) {
  border-radius: var(--radius-md);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
  min-height: 200px;
}

.pagination {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

@media (max-width: 1200px) {
  .product-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 900px) {
  .product-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 640px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 400px) {
  .product-grid { grid-template-columns: 1fr; }
}
</style>
