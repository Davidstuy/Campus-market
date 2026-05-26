<template>
  <div class="payment">
    <h2>确认支付</h2>

    <!-- 加载态 -->
    <el-skeleton v-if="loading" :rows="4" animated />

    <!-- 错误态 -->
    <el-result v-else-if="error" icon="error" title="加载失败" sub-title="无法获取订单信息">
      <template #extra>
        <el-button type="primary" @click="fetchData">重新加载</el-button>
      </template>
    </el-result>

    <!-- 订单不存在 -->
    <el-empty v-else-if="!order" description="订单不存在" />

    <!-- 正常态 -->
    <div v-else class="payment-content">
      <div class="order-info">
        <div class="label">订单号</div>
        <div class="value">{{ order.orderNo }}</div>
        <div class="label">商品</div>
        <div class="value">{{ order.productTitle }}</div>
        <div class="label">金额</div>
        <div class="price">¥{{ order.productPrice }}</div>
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

      <!-- 支付中 -->
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

  // 模拟跳转支付网关的加载动画
  await new Promise(resolve => setTimeout(resolve, 1500))

  try {
    await orderApi.pay(order.value.id)
    // 跳转到订单详情
    router.replace(`/orders/${order.value.id}`)
  } catch {
    paying.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.payment { max-width: 480px; margin: 0 auto; }
.order-info {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
}
.label { color: #909399; font-size: 13px; margin-bottom: 2px; }
.label + .label { margin-top: 12px; }
.value { font-size: 14px; }
.price { color: #f56c6c; font-size: 24px; font-weight: bold; margin-top: 4px; }
.pay-methods { display: flex; gap: 16px; margin-bottom: 24px; }
.pay-method {
  flex: 1;
  padding: 16px;
  text-align: center;
  border: 2px solid #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color .3s;
}
.pay-method.active { border-color: #409eff; color: #409eff; }
.pay-btn { width: 100%; }
.paying-overlay {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.spinner { text-align: center; }
.spinner p { margin-top: 16px; color: #909399; }
</style>
