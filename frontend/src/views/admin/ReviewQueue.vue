<template>
  <div class="review-queue">
    <h2>商品审核</h2>

    <!-- loading -->
    <div v-if="loading" class="loading-state">
      <div v-for="i in 3" :key="i" class="review-card skeleton">
        <el-skeleton :rows="3" animated />
      </div>
    </div>

    <!-- error -->
    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <el-button type="primary" @click="loadProducts">重新加载</el-button>
    </div>

    <!-- empty -->
    <div v-else-if="list.length === 0" class="empty-state">
      <el-empty description="暂无待审核商品" />
    </div>

    <!-- list -->
    <div v-else>
      <div v-for="item in list" :key="item.id" class="review-card">
        <div class="review-header">
          <div class="product-info">
            <el-image
              v-if="item.coverImage"
              :src="item.coverImage"
              class="product-cover"
              fit="cover"
            />
            <div>
              <h4>{{ item.title }}</h4>
              <p class="price">¥{{ item.price }}</p>
              <p class="seller">卖家：{{ item.seller?.nickname || '未知' }}</p>
              <p class="category">分类：{{ item.category?.name || '-' }}</p>
            </div>
          </div>
          <el-tag type="warning" size="small">高风险</el-tag>
        </div>

        <p class="description">{{ item.description }}</p>

        <div class="review-actions">
          <el-button type="success" @click="handleApprove(item.id)">
            通过
          </el-button>
          <el-popconfirm
            title="确定驳回该商品吗？"
            @confirm="showRejectDialog(item.id)"
          >
            <template #reference>
              <el-button type="danger">驳回</el-button>
            </template>
          </el-popconfirm>
        </div>
      </div>

      <!-- pagination -->
      <div v-if="total > size" class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadProducts"
        />
      </div>
    </div>

    <!-- reject dialog -->
    <el-dialog v-model="dialogVisible" title="驳回原因" width="450px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="3"
        placeholder="请填写驳回原因，用户将看到此信息"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" :disabled="!rejectReason.trim()" @click="handleReject">
          确认驳回
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/modules/admin'
import type { Product } from '@/types'

const loading = ref(true)
const error = ref('')
const list = ref<Product[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const rejectReason = ref('')
const rejectTargetId = ref(0)

const loadProducts = async () => {
  loading.value = true
  error.value = ''
  try {
    const data = await adminApi.getPendingProducts(page.value, size.value)
    list.value = data.records
    total.value = data.total
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

const handleApprove = async (id: number) => {
  try {
    await adminApi.approveProduct(id)
    ElMessage.success('已通过审核')
    loadProducts()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

const showRejectDialog = (id: number) => {
  rejectTargetId.value = id
  rejectReason.value = ''
  dialogVisible.value = true
}

const handleReject = async () => {
  if (!rejectReason.value.trim()) return
  try {
    await adminApi.rejectProduct(rejectTargetId.value, rejectReason.value.trim())
    ElMessage.success('已驳回')
    dialogVisible.value = false
    loadProducts()
  } catch (e: any) {
    ElMessage.error(e?.message || '驳回失败')
  }
}

onMounted(loadProducts)
</script>

<style scoped>
.review-queue h2 {
  margin: 0 0 24px;
  font-size: 20px;
}

.review-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.product-info {
  display: flex;
  gap: 12px;
}

.product-cover {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  flex-shrink: 0;
}

.product-info h4 {
  margin: 0 0 4px;
  font-size: 16px;
}

.price {
  color: #e6a23c;
  font-size: 18px;
  font-weight: 600;
  margin: 4px 0;
}

.seller,
.category {
  color: #909399;
  font-size: 13px;
  margin: 2px 0;
}

.description {
  margin: 12px 0;
  color: #606266;
  line-height: 1.6;
}

.review-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.loading-state,
.error-state,
.empty-state {
  padding: 60px 0;
  text-align: center;
}

.error-state p {
  color: #f56c6c;
  margin-bottom: 12px;
}

.skeleton {
  min-height: 120px;
}
</style>
