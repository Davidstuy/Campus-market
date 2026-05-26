<template>
  <div class="order-detail">
    <!-- 加载态 -->
    <el-skeleton v-if="loading" :rows="6" animated />

    <!-- 错误态 -->
    <el-result v-else-if="error" icon="error" title="加载失败">
      <template #extra>
        <el-button type="primary" @click="fetchData">重新加载</el-button>
      </template>
    </el-result>

    <!-- 订单不存在 -->
    <el-empty v-else-if="!order" description="订单不存在" />

    <!-- 正常态 -->
    <div v-else>
      <div class="order-header">
        <h3>订单详情</h3>
        <OrderStatusTag :status="order.status" />
      </div>

      <!-- 进度条 -->
      <el-steps :active="activeStep" align-center class="steps">
        <el-step title="提交订单" :description="order.createdAt?.slice(0, 16)" />
        <el-step title="付款" :description="order.paidAt?.slice(0, 16) || '待付款'" />
        <el-step title="卖家发货" :description="order.shippedAt?.slice(0, 16) || '待发货'" />
        <el-step title="确认收货" :description="order.completedAt?.slice(0, 16) || '待收货'" />
      </el-steps>

      <!-- 订单信息 -->
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

      <!-- 操作按钮 -->
      <div class="actions">
        <!-- 买家操作 -->
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
            type="default"
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

        <!-- 卖家操作 -->
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
.order-detail { max-width: 720px; margin: 0 auto; }
.order-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.steps { margin-bottom: 30px; }
.info-table { margin-bottom: 20px; }
.price { color: #f56c6c; font-weight: bold; }
.actions { display: flex; gap: 12px; justify-content: center; margin-top: 24px; }
</style>
