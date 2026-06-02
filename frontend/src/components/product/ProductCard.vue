<template>
  <div class="product-card" @click="$router.push(`/products/${product.id}`)">
    <div class="cover-wrapper">
      <el-image
        :src="thumbUrl(product.coverImage)"
        fit="contain"
        class="cover"
        :preview-src-list="[product.coverImage]"
        :preview-teleported="true"
        @click.stop
      >
        <template #error>
          <div class="image-placeholder">
            <el-icon :size="32"><Picture /></el-icon>
          </div>
        </template>
      </el-image>
      <div class="cover-overlay"></div>
      <el-button
        class="fav-btn"
        :type="product.isFavorited ? 'warning' : 'default'"
        :icon="product.isFavorited ? StarFilled : Star"
        circle
        size="small"
        @click.stop="toggleFav"
      />
    </div>
    <div class="info">
      <h3 class="title">{{ product.title }}</h3>
      <div class="price-section">
        <span class="price">¥{{ product.price }}</span>
      </div>
      <div class="meta">
        <span class="seller-name" @click.stop="goToShop">{{ product.seller?.nickname || '匿名' }}</span>
        <span class="meta-divider">·</span>
        <span class="date">{{ formatRelativeTime(product.createdAt) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Picture, Star, StarFilled } from '@element-plus/icons-vue'
import { favoriteApi } from '@/api/modules/favorite'
import type { Product } from '@/types'

const router = useRouter()

const props = withDefaults(defineProps<{
  product: Product
  showFavorite?: boolean
}>(), { showFavorite: false })

const thumbUrl = (url: string) => {
  if (!url || url === '/placeholder.svg') return url
  if (url.includes('aliyuncs.com')) {
    return url + '?x-oss-process=image/resize,m_lfit,w_400'
  }
  return url.replace('/v1/files/', '/v1/files/thumb/')
}

const formatRelativeTime = (dateStr: string): string => {
  if (!dateStr) return ''
  const now = Date.now()
  const date = new Date(dateStr).getTime()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return dateStr.slice(0, 10)
}

const emit = defineEmits<{
  unfavorited: [productId: number]
}>()

const goToShop = () => {
  if (props.product.sellerId) {
    router.push(`/shop/${props.product.sellerId}`)
  }
}

const toggleFav = async () => {
  if (!localStorage.getItem('token')) {
    router.push(`/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`)
    return
  }
  if (props.product.isFavorited) {
    await favoriteApi.remove(props.product.id)
    props.product.isFavorited = false
    emit('unfavorited', props.product.id)
  } else {
    await favoriteApi.add(props.product.id)
    props.product.isFavorited = true
  }
}
</script>

<style scoped>
.product-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: all var(--transition-base);
  border: 1px solid var(--border-color);
  position: relative;
  box-shadow: var(--shadow-sm);
}
.product-card:hover {
  box-shadow: var(--shadow-card-hover);
  transform: translateY(-4px);
  border-color: transparent;
}
.product-card:active {
  transform: scale(0.98);
}

.cover-wrapper {
  position: relative;
  overflow: hidden;
  aspect-ratio: 1 / 1;
  background: #f9fafb;
}
.cover {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  transition: transform var(--transition-slow);
  object-fit: contain !important;
}
.product-card:hover .cover {
  transform: scale(1.05);
}
.cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, transparent 60%, rgba(0, 0, 0, 0.04) 100%);
  opacity: 0;
  transition: opacity var(--transition-base);
}
.product-card:hover .cover-overlay {
  opacity: 1;
}
.image-placeholder {
  width: 100%;
  aspect-ratio: 4 / 3;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  color: var(--text-muted);
}

.fav-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 2;
  opacity: 0;
  transform: scale(0.8);
  transition: all var(--transition-fast);
}
.product-card:hover .fav-btn,
.fav-btn:has(.el-icon-StarFilled) {
  opacity: 1;
  transform: scale(1);
}

.info {
  padding: 10px;
}
.title {
  font-size: var(--text-xs);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}
.price-section {
  margin-bottom: 6px;
}
.price {
  color: var(--el-color-danger);
  font-size: var(--text-base);
  font-weight: 700;
  letter-spacing: -0.3px;
}
.meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: var(--text-xs);
  color: var(--text-muted);
}
.seller-name {
  cursor: pointer;
  color: var(--el-color-primary);
  transition: opacity var(--transition-fast);
}
.seller-name:hover {
  opacity: 0.7;
}
.meta-divider {
  color: var(--border-color);
}
</style>
