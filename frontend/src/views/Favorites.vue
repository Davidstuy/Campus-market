<template>
  <div class="favorites-page">
    <h2 class="page-title">我的收藏</h2>

    <el-skeleton v-if="loading && products.length === 0" :rows="4" animated />

    <el-result
      v-else-if="error"
      status="error"
      title="加载失败"
      sub-title="无法获取收藏列表，请重试"
    >
      <template #extra>
        <el-button type="primary" @click="fetchData">重新加载</el-button>
      </template>
    </el-result>

    <el-empty
      v-else-if="!loading && products.length === 0"
      description="还没有收藏商品，去逛逛吧"
    >
      <el-button type="primary" @click="$router.push('/products')">浏览商品</el-button>
    </el-empty>

    <template v-else>
      <div class="product-grid">
        <ProductCard
          v-for="p in products"
          :key="p.id"
          :product="p"
          :show-favorite="true"
          @unfavorited="handleUnfavorited(p.id)"
        />
      </div>

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
import { ref, onMounted } from 'vue'
import { favoriteApi } from '@/api/modules/favorite'
import type { Product } from '@/types'
import ProductCard from '@/components/product/ProductCard.vue'

const loading = ref(false)
const error = ref(false)
const products = ref<Product[]>([])
const page = ref(1)
const size = ref(12)
const total = ref(0)

const fetchData = async () => {
  loading.value = true
  error.value = false
  try {
    const data = await favoriteApi.list({ page: page.value, size: size.value })
    products.value = data.records as unknown as Product[]
    total.value = data.total
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const handleUnfavorited = (productId: number) => {
  products.value = products.value.filter(p => p.id !== productId)
  total.value = Math.max(0, total.value - 1)
}

onMounted(fetchData)
</script>

<style scoped>
.page-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  margin-bottom: 24px;
  letter-spacing: -0.3px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.pagination {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

@media (max-width: 640px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
