<template>
  <!-- 加载态 -->
  <div v-if="loading" class="shop-page">
    <el-skeleton :rows="1" animated class="shop-header-skeleton" />
    <el-skeleton :rows="4" animated />
  </div>

  <!-- 错误态 -->
  <el-result
    v-else-if="error"
    icon="error"
    title="加载失败"
    sub-title="无法获取店铺信息，请重试"
  >
    <template #extra>
      <el-button type="primary" @click="fetchData">重新加载</el-button>
    </template>
  </el-result>

  <!-- 正常态 -->
  <div v-else class="shop-page">
    <div class="shop-header">
      <el-image
        fit="cover"
        class="seller-avatar"
        :src="seller.avatarUrl"
        :preview-src-list="[seller.avatarUrl]"
        :preview-teleported="true"
      >
        <template #error>
          <el-icon :size="32"><UserFilled /></el-icon>
        </template>
      </el-image>
      <div class="shop-header-info">
        <h2>{{ seller.nickname }} 的店铺</h2>
        <p class="product-count">共 {{ total }} 件在售商品</p>
      </div>
      <el-button
        v-if="canMessage"
        type="primary"
        round
        @click="goMessage"
      >
        <el-icon><ChatDotRound /></el-icon>
        私信
      </el-button>
    </div>

    <div v-if="products.length > 0" class="product-grid">
      <ProductCard v-for="p in products" :key="p.id" :product="p" />
    </div>
    <el-empty v-else description="该卖家暂无在售商品" />

    <div v-if="total > size" class="pagination-wrap">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { UserFilled, ChatDotRound } from '@element-plus/icons-vue'
import { productApi } from '@/api/modules/product'
import { userApi } from '@/api/modules/user'
import { chatApi } from '@/api/modules/chat'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import ProductCard from '@/components/product/ProductCard.vue'
import type { Product } from '@/types'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const sellerId = computed(() => Number(route.params.sellerId))

// 当前用户不是卖家本人、且已登录时才能私信
const canMessage = computed(() => {
  return auth.isLoggedIn() && auth.user?.id !== sellerId.value
})

const seller = reactive({ nickname: '', avatarUrl: '' })
const products = ref<Product[]>([])
const loading = ref(false)
const error = ref(false)
const currentPage = ref(1)
const total = ref(0)
const size = 12

const fetchData = async () => {
  loading.value = true
  error.value = false
  try {
    const [info, pageData] = await Promise.all([
      userApi.getPublicInfo(sellerId.value),
      productApi.list({ sellerId: sellerId.value, status: 'ACTIVE', page: 1, size }),
    ])
    seller.nickname = info.nickname
    seller.avatarUrl = info.avatarUrl
    products.value = pageData.records
    total.value = pageData.total
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const loadProducts = async (page: number) => {
  currentPage.value = page
  try {
    const data = await productApi.list({ sellerId: sellerId.value, status: 'ACTIVE', page, size })
    products.value = data.records
    total.value = data.total
  } catch { /* ignore */ }
}

const goMessage = async () => {
  try {
    const conv = await chatApi.getOrCreateConversation(sellerId.value, 0)
    router.push(`/chat?conversation=${conv.id}`)
  } catch {
    ElMessage.error('发起会话失败')
  }
}

watch(sellerId, () => {
  currentPage.value = 1
  fetchData()
})

onMounted(fetchData)
</script>

<style scoped>
.shop-page {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 24px 0;
}

.shop-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding: 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
}
.seller-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}
.seller-avatar :deep(img) {
  width: 72px;
  height: 72px;
  object-fit: cover;
}
.seller-avatar :deep(.el-image__error) {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--el-color-info-light-8);
  display: flex;
  align-items: center;
  justify-content: center;
}
.shop-header-info {
  flex: 1;
}
.shop-header-info h2 {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
}
.product-count {
  margin: 0;
  color: var(--text-muted);
  font-size: var(--text-sm);
}

.shop-header-skeleton {
  padding: 24px;
  background: var(--bg-card);
  border-radius: var(--radius-lg);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
