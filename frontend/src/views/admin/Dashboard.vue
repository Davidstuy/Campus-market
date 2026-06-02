<template>
  <div class="dashboard">
    <h2>数据看板</h2>

    <div v-if="loading" class="stats-grid">
      <div v-for="i in 4" :key="i" class="stat-card skeleton">
        <el-skeleton :rows="2" animated />
      </div>
    </div>

    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <el-button type="primary" @click="loadDashboard">重新加载</el-button>
    </div>

    <div v-else class="stats-grid">
      <div class="stat-card">
        <div class="stat-value">{{ stats.totalUsers }}</div>
        <div class="stat-label">总用户数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.totalProducts }}</div>
        <div class="stat-label">总商品数</div>
      </div>
      <div class="stat-card warning">
        <div class="stat-value">{{ stats.pendingReview }}</div>
        <div class="stat-label">待审核</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.activeProducts }}</div>
        <div class="stat-label">在售商品</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminApi, type DashboardStats } from '@/api/modules/admin'

const loading = ref(true)
const error = ref('')
const stats = ref<DashboardStats>({
  totalUsers: 0,
  totalProducts: 0,
  activeProducts: 0,
  pendingReview: 0,
  totalCategories: 0,
})

const loadDashboard = async () => {
  loading.value = true
  error.value = ''
  try {
    stats.value = await adminApi.getDashboard()
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<style scoped>
.dashboard h2 {
  margin: 0 0 24px;
  font-size: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
}

.stat-card.warning {
  background: #fef0f0;
  border: 1px solid #fde2e2;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
}

.stat-card.warning .stat-value {
  color: #e6a23c;
}

.stat-label {
  margin-top: 8px;
  font-size: 14px;
  color: #909399;
}

.error-state {
  text-align: center;
  padding: 60px 0;
  color: #909399;
}

.skeleton {
  min-height: 100px;
}
</style>
