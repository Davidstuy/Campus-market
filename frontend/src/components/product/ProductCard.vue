<template>
  <div class="product-card" @click="$router.push(`/products/${product.id}`)">
    <el-image
      :src="thumbUrl(product.coverImage)"
      fit="cover"
      class="cover"
    >
      <template #error>
        <div class="image-placeholder">
          <el-icon :size="40"><Picture /></el-icon>
        </div>
      </template>
    </el-image>

    <!-- 收藏按钮（在收藏页和列表页可显示） -->
    <el-button
      v-if="showFavorite"
      class="fav-btn"
      :type="product.isFavorited ? 'warning' : 'default'"
      :icon="product.isFavorited ? StarFilled : Star"
      circle
      size="small"
      @click.stop="toggleFav"
    />

    <div class="info">
      <h3 class="title">{{ product.title }}</h3>
      <div class="price">¥{{ product.price }}</div>
      <div class="meta">
        <span>{{ product.seller?.nickname || '匿名' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Picture, Star, StarFilled } from '@element-plus/icons-vue'
import { favoriteApi } from '@/api/modules/favorite'
import type { Product } from '@/types'

const props = withDefaults(defineProps<{
  product: Product
  showFavorite?: boolean
}>(), { showFavorite: false })

const thumbUrl = (url: string) => {
  if (!url || url === '/placeholder.svg') return url
  // OSS URL: 使用阿里云图片处理服务缩略
  if (url.includes('aliyuncs.com')) {
    return url + '?x-oss-process=image/resize,m_lfit,w_400'
  }
  // 本地路径：使用本地缩略图端点
  return url.replace('/v1/files/', '/v1/files/thumb/')
}

const emit = defineEmits<{
  unfavorited: [productId: number]
}>()

const toggleFav = async () => {
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
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  border: 1px solid #e2e8f0;
  position: relative;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
.product-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}
.cover {
  width: 100%;
  height: 200px;
  background: #f1f5f9;
}
.image-placeholder {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  color: #94a3b8;
}
.fav-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 2;
}
.info {
  padding: 14px;
}
.title {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 8px;
  color: #1e293b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.price {
  color: #ef4444;
  font-size: 18px;
  font-weight: 700;
}
.meta {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}
</style>
