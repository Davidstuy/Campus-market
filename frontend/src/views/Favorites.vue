<template>
  <div class="favorites-page">
    <h2>我的收藏</h2>

    <!-- 加载态 -->
    <el-skeleton v-if="loading && products.length === 0" :rows="4" animated />

    <!-- 错误态 -->
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

    <!-- 空状态 -->
    <el-empty
      v-else-if="!loading && products.length === 0"
      description="还没有收藏商品，去逛逛吧"
    >
      <el-button type="primary" @click="$router.push('/products')">浏览商品</el-button>
    </el-empty>

    <!-- 正常态：商品卡片网格 -->
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
    // API 返回的 records 已经是 ProductVO，直接使用
    products.value = data.records as unknown as Product[]
    total.value = data.total
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

/** 取消收藏后从列表中移除 */
const handleUnfavorited = (productId: number) => {
  products.value = products.value.filter(p => p.id !== productId)
  total.value = Math.max(0, total.value - 1)
}

onMounted(fetchData)
</script>

<style scoped>
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
