<template>
  <div class="product-list-page">
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

    <template v-else>
      <div v-loading="loading" class="product-grid">
        <ProductCard
          v-for="p in products"
          :key="p.id"
          :product="p"
          :show-favorite="isLoggedIn"
        />
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

    if (isLoggedIn.value && data.records.length > 0) {
      const ids = data.records.map(p => p.id)
      const favMap = await favoriteApi.check(ids)
      products.value.forEach(p => {
        p.isFavorited = favMap[p.id] || false
      })
    }

    total.value = data.total
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  categories.value = await categoryApi.list()
  fetchData()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
  min-height: 200px;
}
.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>
