<template>
  <div class="order-detail">
    <el-skeleton v-if="loading" :rows="6" animated />

    <el-result v-else-if="error" icon="error" title="加载失败">
      <template #extra>
        <el-button type="primary" @click="fetchData">重新加载</el-button>
      </template>
    </el-result>

    <el-empty v-else-if="!order" description="订单不存在" />

    <div v-else class="order-detail-card">
      <div class="order-header">
        <h2 class="page-title">订单详情</h2>
        <OrderStatusTag :status="order.status" />
      </div>

      <div class="steps-wrapper">
        <el-steps :active="activeStep" align-center class="steps">
          <el-step title="提交订单" :description="order.createdAt?.slice(0, 16)" />
          <el-step title="付款" :description="order.paidAt?.slice(0, 16) || '待付款'" />
          <el-step title="卖家发货" :description="order.shippedAt?.slice(0, 16) || '待发货'" />
          <el-step title="确认收货" :description="order.completedAt?.slice(0, 16) || '待收货'" />
        </el-steps>
      </div>

      <div class="info-table-wrapper">
        <el-descriptions :column="2" border class="info-table">
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <OrderStatusTag :status="order.status" />
          </el-descriptions-item>
          <el-descriptions-item label="商品">{{ order.productTitle }}</el-descriptions-item>
          <el-descriptions-item label="金额">
            <span class="price">¥{{ order.productPrice }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="买家">
            {{ order.buyer?.nickname || order.buyer?.username }}
          </el-descriptions-item>
          <el-descriptions-item label="卖家">
            {{ order.seller?.nickname || order.seller?.username }}
          </el-descriptions-item>
          <el-descriptions-item v-if="order.buyerRemark" label="买家备注" :span="2">
            {{ order.buyerRemark }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="actions">
        <template v-if="isBuyer">
          <el-button
            v-if="order.status === 'PENDING'"
            type="danger"
            @click="handlePay"
          >
            去支付
          </el-button>
          <el-button
            v-if="order.status === 'PENDING'"
            @click="handleCancel"
          >
            取消订单
          </el-button>
          <el-button
            v-if="order.status === 'SHIPPED'"
            type="primary"
            @click="handleComplete"
          >
            确认收货
          </el-button>
        </template>

        <template v-if="isSeller">
          <el-button
            v-if="order.status === 'PAID'"
            type="primary"
            @click="handleShip"
          >
            标记发货
          </el-button>
        </template>

        <el-button @click="$router.push(isBuyer ? '/orders' : '/profile/sales')">返回</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { orderApi } from '@/api/modules/order'
import OrderStatusTag from '@/components/order/OrderStatusTag.vue'
import type { OrderVO } from '@/types'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref(false)
const order = ref<OrderVO | null>(null)

const currentUserId = computed(() => {
  const raw = localStorage.getItem('user')
  if (!raw) return 0
  return JSON.parse(raw).id as number
})
const isBuyer = computed(() => order.value?.buyerId === currentUserId.value)
const isSeller = computed(() => order.value?.sellerId === currentUserId.value)

const activeStep = computed(() => {
  if (!order.value) return 0
  const s = order.value.status
  if (s === 'PENDING' || s === 'CANCELLED') return 0
  if (s === 'PAID') return 1
  if (s === 'SHIPPED') return 2
  if (s === 'COMPLETED') return 3
  return 0
})

const fetchData = async () => {
  loading.value = true
  error.value = false
  try {
    const id = Number(route.params.id)
    order.value = await orderApi.detail(id)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const handlePay = () => {
  if (order.value) router.push(`/payment/${order.value.id}`)
}

const handleCancel = async () => {
  if (!order.value) return
  try {
    await orderApi.cancel(order.value.id)
    fetchData()
  } catch { /* ignore */ }
}

const handleShip = async () => {
  if (!order.value) return
  try {
    await orderApi.ship(order.value.id)
    fetchData()
  } catch { /* ignore */ }
}

const handleComplete = async () => {
  if (!order.value) return
  try {
    await orderApi.complete(order.value.id)
    fetchData()
  } catch { /* ignore */ }
}

onMounted(fetchData)
</script>

<style scoped>
.order-detail { max-width: 760px; margin: 0 auto; }

.order-detail-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  padding: 32px;
  box-shadow: var(--shadow-sm);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}
.page-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  letter-spacing: -0.3px;
  margin: 0;
}

.steps-wrapper {
  background: var(--el-color-primary-light-9);
  border-radius: var(--radius-md);
  padding: 24px 16px;
  margin-bottom: 28px;
}
.steps :deep(.el-step__title) {
  font-size: var(--text-sm);
  font-weight: 500;
}
.steps :deep(.el-step__description) {
  font-size: var(--text-xs);
}

.info-table-wrapper {
  margin-bottom: 24px;
}
.info-table-wrapper :deep(.el-descriptions__body) {
  border-radius: var(--radius-md);
}
.info-table-wrapper :deep(.el-descriptions__cell) {
  padding: 12px 16px;
}

.price {
  color: var(--el-color-danger);
  font-weight: 700;
  font-size: var(--text-lg);
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 28px;
}
.actions :deep(.el-button--primary) {
  font-weight: 600;
}

@media (max-width: 768px) {
  .order-detail-card {
    padding: 16px;
  }
}
</style>
