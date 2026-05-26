<template>
  <div class="my-orders">
    <h2>我的订单</h2>

    <!-- 加载态 -->
    <el-skeleton v-if="loading" :rows="5" animated />

    <!-- 错误态 -->
    <el-result v-else-if="error" icon="error" title="加载失败">
      <template #extra>
        <el-button type="primary" @click="fetchData">重新加载</el-button>
      </template>
    </el-result>

    <!-- 空态 -->
    <el-empty v-else-if="orders.length === 0" description="暂无订单">
      <el-button type="primary" @click="$router.push('/products')">去逛逛</el-button>
    </el-empty>

    <!-- 正常态 -->
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
.order-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: box-shadow .3s;
}
.order-card:hover { box-shadow: 0 2px 12px rgba(0,0,0,.1); }
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.order-no { font-size: 13px; color: #909399; }
.order-body { display: flex; gap: 12px; }
.cover { width: 80px; height: 80px; border-radius: 6px; flex-shrink: 0; }
.title { font-size: 15px; margin-bottom: 6px; }
.price { color: #f56c6c; font-weight: bold; }
.order-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 12px;
  color: #c0c4cc;
}
.pagination { margin-top: 20px; display: flex; justify-content: center; }
</style>
