<template>
  <div class="checkout">
    <h2>确认订单</h2>

    <!-- 加载态 -->
    <el-skeleton v-if="loading" :rows="5" animated />

    <!-- 错误态 -->
    <el-result v-else-if="error" icon="error" title="加载失败" sub-title="无法获取商品信息">
      <template #extra>
        <el-button type="primary" @click="fetchData">重新加载</el-button>
      </template>
    </el-result>

    <!-- 商品不存在 -->
    <el-empty v-else-if="!product" description="商品不存在" />

    <!-- 正常态 -->
    <div v-else class="checkout-content">
      <div class="product-summary">
        <el-image :src="product.coverImage || '/placeholder.svg'" fit="cover" class="cover" />
        <div class="info">
          <h3>{{ product.title }}</h3>
          <div class="price">¥{{ product.price }}</div>
          <div class="seller">卖家：{{ product.seller?.nickname || product.seller?.username }}</div>
        </div>
      </div>

      <el-divider />

      <el-form label-width="80px">
        <el-form-item label="给卖家留言">
          <el-input
            v-model="remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="选填，如：什么时候方便取货？"
          />
        </el-form-item>
      </el-form>

      <div class="total">
        实付金额：<span class="final-price">¥{{ product.price }}</span>
      </div>

      <el-button
        type="danger"
        size="large"
        :loading="submitting"
        class="submit-btn"
        @click="handleSubmit"
      >
        提交订单
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/modules/product'
import { orderApi } from '@/api/modules/order'
import type { Product } from '@/types'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref(false)
const submitting = ref(false)
const product = ref<Product | null>(null)
const remark = ref('')

const fetchData = async () => {
  loading.value = true
  error.value = false
  try {
    const productId = Number(route.params.productId)
    product.value = await productApi.detail(productId)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!product.value) return
  submitting.value = true
  try {
    const order = await orderApi.create({
      productId: product.value.id,
      buyerRemark: remark.value,
    }) as import('@/types').OrderVO
    router.push(`/payment/${order.id}`)
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.checkout {
  max-width: 640px;
  margin: 0 auto;
}
.product-summary {
  display: flex;
  gap: 16px;
  align-items: center;
}
.cover {
  width: 120px;
  height: 120px;
  border-radius: 8px;
  flex-shrink: 0;
}
.info h3 { margin: 0 0 8px; }
.price { color: #f56c6c; font-size: 20px; font-weight: bold; }
.seller { color: #909399; font-size: 13px; margin-top: 6px; }
.total { text-align: right; font-size: 16px; margin: 20px 0; }
.final-price { color: #f56c6c; font-size: 24px; font-weight: bold; }
.submit-btn { width: 100%; }
</style>
