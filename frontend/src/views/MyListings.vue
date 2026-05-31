<template>
  <div class="listings-page">
    <div class="page-header">
      <h2 class="page-title">我的发布</h2>
      <el-button type="primary" @click="$router.push('/publish')">发布新商品</el-button>
    </div>

    <el-skeleton v-if="loading && products.length === 0" :rows="5" animated />

    <el-result
      v-else-if="error"
      status="error"
      title="加载失败"
      sub-title="无法获取发布列表，请重试"
    >
      <template #extra>
        <el-button type="primary" @click="fetchData">重新加载</el-button>
      </template>
    </el-result>

    <el-empty
      v-else-if="!loading && products.length === 0"
      description="还没有发布商品"
    >
      <el-button type="primary" @click="$router.push('/publish')">发布第一个商品</el-button>
    </el-empty>

    <template v-else>
      <div class="table-wrapper">
        <el-table :data="products" border stripe v-loading="loading">
          <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
          <el-table-column label="封面" width="80">
            <template #default="{ row }">
              <el-image
                v-if="row.coverImage"
                :src="row.coverImage"
                fit="cover"
                style="width: 50px; height: 50px; border-radius: 4px"
              />
              <span v-else style="color: #ccc; font-size: 12px">无图</span>
            </template>
          </el-table-column>
          <el-table-column label="价格" width="100">
            <template #default="{ row }">¥{{ row.price }}</template>
          </el-table-column>
          <el-table-column label="分类" width="80">
            <template #default="{ row }">{{ row.category?.name }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusType[row.status]" size="small">{{ statusMap[row.status] }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发布时间" width="110">
            <template #default="{ row }">{{ row.createdAt?.slice(0, 10) }}</template>
          </el-table-column>
          <el-table-column label="操作" min-width="230" fixed="right">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click="$router.push(`/products/${row.id}`)">
                查看
              </el-button>
              <el-button size="small" text type="primary" :disabled="row.status !== 'ACTIVE'" @click="$router.push(`/publish/${row.id}`)">
                编辑
              </el-button>
              <template v-if="row.status === 'ACTIVE'">
                <el-button size="small" text type="warning" @click="updateStatus(row.id, 'SOLD')">
                  标记已售
                </el-button>
                <el-button size="small" text type="info" @click="updateStatus(row.id, 'DELISTED')">
                  下架
                </el-button>
              </template>
              <template v-else-if="row.status === 'SOLD'">
                <el-button size="small" text type="success" @click="updateStatus(row.id, 'ACTIVE')">
                  重新上架
                </el-button>
              </template>
              <template v-else-if="row.status === 'DELISTED'">
                <el-button size="small" text type="success" @click="updateStatus(row.id, 'ACTIVE')">
                  重新上架
                </el-button>
              </template>
              <el-button size="small" text type="danger" @click="handleDelete(row.id)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-if="total > size" class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchData"
        />
      </div>
    </template>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { productApi } from '@/api/modules/product'
import type { Product } from '@/types'
import { PRODUCT_STATUS } from '@/utils/constants'

const loading = ref(false)
const error = ref(false)
const products = ref<Product[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const statusMap = PRODUCT_STATUS
const statusType: Record<string, string> = { ACTIVE: 'success', SOLD: 'info', DELISTED: 'danger' }

const fetchData = async () => {
  loading.value = true
  error.value = false
  try {
    const data = await productApi.mine({ page: page.value, size: size.value })
    products.value = data.records
    total.value = data.total
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const updateStatus = async (id: number, status: string) => {
  let label = ''
  if (status === 'SOLD') label = '标记已售'
  else if (status === 'ACTIVE') label = '重新上架'
  else label = '下架'
  try {
    await ElMessageBox.confirm(`确定要${label}该商品吗？`, '提示', { type: 'warning' })
    await productApi.updateStatus(id, status)
    ElMessage.success('状态更新成功')
    fetchData()
  } catch { /* cancelled */ }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('删除后无法恢复，确定要删除吗？', '确认删除', { type: 'warning' })
    await productApi.delete(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled */ }
}

onMounted(fetchData)
</script>

<style scoped>
.page-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  letter-spacing: -0.3px;
  margin: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.table-wrapper {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.table-wrapper :deep(.el-table th.el-table__cell) {
  background: var(--el-color-primary-light-9);
  font-weight: 600;
  color: var(--text-primary);
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .listings-page :deep(.el-table) {
    overflow-x: auto;
  }
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
