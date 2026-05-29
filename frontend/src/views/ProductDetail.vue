<template>
  <!-- 加载态 -->
  <div v-if="loading" class="product-detail">
    <el-skeleton :rows="8" animated />
  </div>

  <!-- 错误态 -->
  <el-result
    v-else-if="error"
    icon="error"
    title="加载失败"
    sub-title="无法获取商品详情，请重试"
  >
    <template #extra>
      <el-button type="primary" @click="fetchData">重新加载</el-button>
    </template>
  </el-result>

  <!-- 商品不存在 -->
  <el-empty v-else-if="!product" description="商品不存在" />

  <!-- 正常态 -->
  <div v-else class="product-detail">
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span class="sep">/</span>
      <router-link to="/products">商品</router-link>
      <span class="sep">/</span>
      <span>{{ product.title }}</span>
    </div>

    <div class="detail-main">
      <div class="images-section">
        <el-image
          v-if="images.length === 0"
          :src="thumbUrl(product.coverImage)"
          fit="cover"
          class="main-image"
        />
        <el-carousel v-else class="main-image" trigger="click">
          <el-carousel-item v-for="img in images" :key="img.id">
            <el-image :src="thumbUrl(img.url)" fit="cover" style="width: 100%; height: 100%" />
          </el-carousel-item>
        </el-carousel>
      </div>

      <div class="info-section">
        <h1 class="title">{{ product.title }}</h1>
        <div class="price">¥{{ product.price }}</div>
        <div class="meta">
          <span>分类：{{ product.category?.name }}</span>
          <span>状态：{{ statusMap[product.status] }}</span>
          <span>发布时间：{{ product.createdAt?.slice(0, 10) }}</span>
        </div>
        <div class="seller">
          <el-avatar :size="40" :src="product.seller?.avatarUrl" />
          <span>{{ product.seller?.nickname || product.seller?.username }}</span>
        </div>
        <div v-if="product.contactWechat" class="contact">微信：{{ product.contactWechat }}</div>
        <div v-if="product.contactQq" class="contact">QQ：{{ product.contactQq }}</div>

        <div class="actions">
          <el-button
            :type="product.isFavorited ? 'warning' : 'default'"
            @click="requireLogin(toggleFavorite)"
          >
            {{ product.isFavorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button
            v-if="canBuy"
            type="danger"
            @click="requireLogin(() => router.push(`/checkout/${product!.id}`))"
          >
            立即购买
          </el-button>
          <el-button
            v-if="canContact"
            type="primary"
            @click="requireLogin(contactSeller)"
            :loading="contactLoading"
          >
            联系卖家
          </el-button>
        </div>

        <el-descriptions :column="1" border class="desc-section">
          <el-descriptions-item label="商品描述">
            {{ product.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/modules/product'
import { favoriteApi } from '@/api/modules/favorite'
import { chatApi } from '@/api/modules/chat'
import type { Product, ProductImage } from '@/types'
import { PRODUCT_STATUS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref(false)
const product = ref<Product | null>(null)
const images = ref<ProductImage[]>([])
const statusMap = PRODUCT_STATUS

const currentUserId = computed(() => {
  const raw = localStorage.getItem('user')
  if (!raw) return 0
  return JSON.parse(raw).id as number
})
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const canBuy = computed(() =>
  product.value &&
  product.value.status === 'ACTIVE' &&
  product.value.sellerId !== currentUserId.value
)
const canContact = computed(() =>
  product.value &&
  product.value.status !== 'DELISTED' &&
  product.value.sellerId !== currentUserId.value
)
const contactLoading = ref(false)

const requireLogin = (action: () => void) => {
  if (!isLoggedIn.value) {
    router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
    return
  }
  action()
}

const thumbUrl = (url: string) => {
  if (!url || url === '/placeholder.svg') return url
  if (url.includes('aliyuncs.com')) {
    return url + '?x-oss-process=image/resize,m_lfit,w_400'
  }
  return url.replace('/v1/files/', '/v1/files/thumb/')
}

const fetchData = async () => {
  loading.value = true
  error.value = false
  try {
    const id = Number(route.params.id)
    product.value = await productApi.detail(id)
    images.value = (product.value as unknown as { images?: ProductImage[] }).images || []
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const toggleFavorite = async () => {
  if (!product.value) return
  try {
    if (product.value.isFavorited) {
      await favoriteApi.remove(product.value.id)
    } else {
      await favoriteApi.add(product.value.id)
    }
    product.value.isFavorited = !product.value.isFavorited
  } catch { /* ignore */ }
}

const contactSeller = async () => {
  if (!product.value) return
  contactLoading.value = true
  try {
    const conv = await chatApi.getOrCreateConversation(product.value.sellerId, product.value.id)
    router.push(`/chat?conversation=${conv.id}`)
  } catch {
    /* ignore */
  } finally {
    contactLoading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.product-detail { min-height: 300px; }

.breadcrumb {
  font-size: var(--text-sm);
  color: var(--text-muted);
  margin-bottom: 24px;
  display: flex;
  gap: 8px;
  align-items: center;
}
.breadcrumb a {
  color: var(--text-secondary);
  transition: color var(--transition-fast);
}
.breadcrumb a:hover {
  color: var(--el-color-primary);
}
.sep { color: var(--border-color); }

.detail-main {
  display: flex;
  gap: 40px;
  flex-wrap: wrap;
}

.images-section {
  width: 480px;
  max-width: 100%;
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}
.main-image {
  width: 100%;
  height: 420px;
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.info-section {
  flex: 1;
  min-width: 300px;
}
.title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.3;
}
.price {
  color: var(--el-color-danger);
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 20px;
  letter-spacing: -0.5px;
}
.meta {
  display: flex;
  gap: 16px;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.seller {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: var(--el-color-primary-light-9);
  border-radius: var(--radius-md);
}
.seller span {
  font-weight: 500;
  color: var(--text-primary);
}
.contact {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: 4px;
}
.actions {
  margin: 24px 0;
  display: flex;
  gap: 12px;
}
.actions :deep(.el-button) {
  border-radius: var(--radius-md);
  padding: 10px 24px;
}
.desc-section {
  margin-top: 24px;
}
.desc-section :deep(.el-descriptions__body) {
  border-radius: var(--radius-md);
}

@media (max-width: 768px) {
  .images-section {
    width: 100%;
  }
  .main-image {
    height: 280px;
  }
  .info-section {
    min-width: 0;
  }
}
</style>
