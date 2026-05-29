<template>
  <div class="checkout">
    <h2 class="page-title">确认订单</h2>

    <el-skeleton v-if="loading" :rows="5" animated />

    <el-result v-else-if="error" icon="error" title="加载失败" sub-title="无法获取商品信息">
      <template #extra>
        <el-button type="primary" @click="fetchData">重新加载</el-button>
      </template>
    </el-result>

    <el-empty v-else-if="!product" description="商品不存在" />

    <div v-else class="checkout-card">
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

.page-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  margin-bottom: 24px;
  letter-spacing: -0.3px;
}

.checkout-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  padding: 32px;
  box-shadow: var(--shadow-sm);
}

.product-summary {
  display: flex;
  gap: 16px;
  align-items: center;
}
.cover {
  width: 120px;
  height: 120px;
  border-radius: var(--radius-md);
  flex-shrink: 0;
  background: #f1f5f9;
}
.info h3 {
  margin: 0 0 8px;
  font-size: var(--text-lg);
  font-weight: 600;
}
.price {
  color: var(--el-color-danger);
  font-size: var(--text-2xl);
  font-weight: 700;
  letter-spacing: -0.3px;
}
.seller {
  color: var(--text-muted);
  font-size: var(--text-sm);
  margin-top: 6px;
}

.total {
  text-align: right;
  font-size: var(--text-lg);
  color: var(--text-primary);
  margin: 20px 0;
}
.final-price {
  color: var(--el-color-danger);
  font-size: var(--text-3xl);
  font-weight: 700;
  letter-spacing: -0.5px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--radius-md);
  font-size: var(--text-lg);
  font-weight: 600;
}
</style>
