<template>
  <div class="payment">
    <h2 class="page-title">确认支付</h2>

    <el-skeleton v-if="loading" :rows="4" animated />

    <el-result v-else-if="error" icon="error" title="加载失败" sub-title="无法获取订单信息">
      <template #extra>
        <el-button type="primary" @click="fetchData">重新加载</el-button>
      </template>
    </el-result>

    <el-empty v-else-if="!order" description="订单不存在" />

    <div v-else class="payment-card">
      <div class="order-info">
        <div class="info-row">
          <span class="label">订单号</span>
          <span class="value">{{ order.orderNo }}</span>
        </div>
        <div class="info-row">
          <span class="label">商品</span>
          <span class="value">{{ order.productTitle }}</span>
        </div>
        <div class="info-row">
          <span class="label">金额</span>
          <span class="price">¥{{ order.productPrice }}</span>
        </div>
      </div>

      <el-divider />

      <div class="pay-methods">
        <div
          class="pay-method"
          :class="{ active: payMethod === 'wechat' }"
          @click="payMethod = 'wechat'"
        >
          微信支付
        </div>
        <div
          class="pay-method"
          :class="{ active: payMethod === 'alipay' }"
          @click="payMethod = 'alipay'"
        >
          支付宝
        </div>
      </div>

      <div v-if="paying" class="paying-overlay">
        <div class="spinner">
          <el-icon class="is-loading" :size="48"><Loading /></el-icon>
          <p>正在跳转支付网关...</p>
        </div>
      </div>

      <el-button
        v-else
        type="danger"
        size="large"
        :loading="false"
        class="pay-btn"
        @click="handlePay"
      >
        确认支付 ¥{{ order.productPrice }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '@/api/modules/order'
import { Loading } from '@element-plus/icons-vue'
import type { OrderVO } from '@/types'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref(false)
const paying = ref(false)
const order = ref<OrderVO | null>(null)
const payMethod = ref('wechat')

const fetchData = async () => {
  loading.value = true
  error.value = false
  try {
    const id = Number(route.params.orderId)
    order.value = await orderApi.detail(id)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const handlePay = async () => {
  if (!order.value) return
  paying.value = true
  await new Promise(resolve => setTimeout(resolve, 1500))
  try {
    await orderApi.pay(order.value.id)
    router.replace(`/orders/${order.value.id}`)
  } catch {
    paying.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.payment { max-width: 480px; margin: 0 auto; }

.page-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  margin-bottom: 24px;
  letter-spacing: -0.3px;
}

.payment-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  padding: 32px;
  box-shadow: var(--shadow-sm);
}

.order-info {
  background: var(--el-color-primary-light-9);
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}
.info-row + .info-row {
  border-top: 1px solid var(--border-color);
}
.label {
  color: var(--text-secondary);
  font-size: var(--text-sm);
}
.value {
  font-size: var(--text-base);
  color: var(--text-primary);
  font-weight: 500;
}
.price {
  color: var(--el-color-danger);
  font-size: var(--text-3xl);
  font-weight: 700;
  letter-spacing: -0.5px;
}

.pay-methods {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}
.pay-method {
  flex: 1;
  padding: 16px;
  text-align: center;
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  font-weight: 500;
  font-size: var(--text-base);
  color: var(--text-secondary);
  transition: all var(--transition-base);
}
.pay-method:hover {
  border-color: var(--el-color-primary-light-5);
  color: var(--el-color-primary);
}
.pay-method.active {
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.pay-btn {
  width: 100%;
  height: 48px;
  border-radius: var(--radius-md);
  font-size: var(--text-lg);
  font-weight: 600;
}

.paying-overlay {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.spinner { text-align: center; }
.spinner p { margin-top: 16px; color: var(--text-muted); font-size: var(--text-sm); }
</style>
