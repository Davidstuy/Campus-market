<template>
  <div class="my-orders">
    <h2 class="page-title">我的订单</h2>

    <el-skeleton v-if="loading" :rows="5" animated />

    <el-result v-else-if="error" icon="error" title="加载失败">
      <template #extra>
        <el-button type="primary" @click="fetchData">重新加载</el-button>
      </template>
    </el-result>

    <el-empty v-else-if="orders.length === 0" description="暂无订单">
      <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
    </el-empty>

    <div v-else class="order-list">
      <div
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        @click="$router.push(`/orders/${order.id}`)"
      >
        <div class="order-header">
          <span class="order-no">{{ order.orderNo }}</span>
          <OrderStatusTag :status="order.status" />
        </div>
        <div class="order-body">
          <el-image :src="order.productCover || '/placeholder.svg'" fit="cover" class="cover" />
          <div class="info">
            <div class="title">{{ order.productTitle }}</div>
            <div class="price">¥{{ order.productPrice }}</div>
          </div>
        </div>
        <div class="order-footer">
          <span>卖家：{{ order.seller?.nickname || '未知' }}</span>
          <span>{{ order.createdAt?.slice(0, 10) }}</span>
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api/modules/order'
import OrderStatusTag from '@/components/order/OrderStatusTag.vue'
import type { OrderVO } from '@/types'

const loading = ref(false)
const error = ref(false)
const orders = ref<OrderVO[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = 10

const fetchData = async () => {
  loading.value = true
  error.value = false
  try {
    const data = await orderApi.listBuy(page.value, pageSize)
    orders.value = data.records
    total.value = data.total
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.my-orders { max-width: 800px; margin: 0 auto; }
.page-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  margin-bottom: 24px;
  letter-spacing: -0.3px;
}

.order-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all var(--transition-base);
  box-shadow: var(--shadow-xs);
}
.order-card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order-no { font-size: var(--text-sm); color: var(--text-muted); }

.order-body { display: flex; gap: 12px; }
.cover {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-md);
  flex-shrink: 0;
  background: #f1f5f9;
}
.title {
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}
.price {
  color: var(--el-color-danger);
  font-weight: 700;
  font-size: var(--text-lg);
}

.order-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
