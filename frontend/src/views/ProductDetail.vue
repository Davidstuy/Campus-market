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
            @click="toggleFavorite"
          >
            {{ product.isFavorited ? '已收藏' : '收藏' }}
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
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { productApi } from '@/api/modules/product'
import { favoriteApi } from '@/api/modules/favorite'
import type { Product, ProductImage } from '@/types'
import { PRODUCT_STATUS } from '@/utils/constants'

const route = useRoute()
const loading = ref(false)
const error = ref(false)
const product = ref<Product | null>(null)
const images = ref<ProductImage[]>([])
const statusMap = PRODUCT_STATUS

const thumbUrl = (url: string) => {
  if (!url || url === '/placeholder.svg') return url
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

onMounted(fetchData)
</script>

<style scoped>
.product-detail { min-height: 300px; }
.detail-main {
  display: flex;
  gap: 30px;
  flex-wrap: wrap;
}
.images-section {
  width: 480px;
  max-width: 100%;
}
.main-image {
  width: 100%;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
}
.info-section {
  flex: 1;
  min-width: 300px;
}
.title { font-size: 22px; margin-bottom: 12px; }
.price {
  color: #f56c6c;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 16px;
}
.meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 16px;
}
.seller {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.contact { font-size: 14px; color: #606266; margin-bottom: 6px; }
.actions { margin: 20px 0; }
.desc-section { margin-top: 20px; }

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
  .meta {
    flex-wrap: wrap;
  }
}
</style>
