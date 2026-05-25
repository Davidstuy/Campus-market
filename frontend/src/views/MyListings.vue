<template>
  <div class="listings-page">
    <div class="page-header">
      <h2>我的发布</h2>
      <el-button type="primary" @click="$router.push('/publish')">发布新商品</el-button>
    </div>

    <!-- 加载态 -->
    <el-skeleton v-if="loading && products.length === 0" :rows="5" animated />

    <!-- 错误态 -->
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

    <!-- 空状态 -->
    <el-empty
      v-else-if="!loading && products.length === 0"
      description="还没有发布商品"
    >
      <el-button type="primary" @click="$router.push('/publish')">发布第一个商品</el-button>
    </el-empty>

    <!-- 正常态：表格 -->
    <template v-else>
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
            <el-button size="small" text type="primary" :disabled="row.status !== 'ACTIVE'" @click="openEdit(row)">
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

            <!-- 4. 如果是下架状态，也可以考虑显示“重新上架” -->
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

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑商品" width="600px" :close-on-click-modal="false">
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入商品标题" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="editForm.categoryId" placeholder="请选择分类">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="editForm.price" :min="0" :precision="2" :step="10" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="微信">
          <el-input v-model="editForm.contactWechat" placeholder="选填" />
        </el-form-item>
        <el-form-item label="QQ">
          <el-input v-model="editForm.contactQq" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editSubmitting" @click="handleEditSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { productApi } from '@/api/modules/product'
import { categoryApi } from '@/api/modules/category'
import type { Product, Category } from '@/types'
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

// ====== 状态更新 ======
const updateStatus = async (id: number, status: string) => {
   // 优化提示文字逻辑
  let label = ''
  if (status === 'SOLD') label = '标记已售'
  else if (status === 'ACTIVE') label = '重新上架'
  else label = '下架'
  try {
    await ElMessageBox.confirm(`确定要${label}该商品吗？`, '提示', { type: 'warning' })
    await productApi.updateStatus(id, status)
    ElMessage.success('状态更新成功')
    fetchData()
  } catch { /* cancelled by user */ }
}

// ====== 删除 ======
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('删除后无法恢复，确定要删除吗？', '确认删除', { type: 'warning' })
    await productApi.delete(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled */ }
}

// ====== 编辑 ======
const editVisible = ref(false)
const editSubmitting = ref(false)
const editFormRef = ref<FormInstance>()
const editingId = ref<number | null>(null)
const categories = ref<Category[]>([])

const editForm = reactive({
  title: '',
  categoryId: null as number | null,
  price: 0,
  description: '',
  coverImage: '',
  images: [] as string[],
  contactWechat: '',
  contactQq: '',
})

const editRules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
}

const openEdit = async (product: Product) => {
  // 如果还没加载分类，先加载
  if (categories.value.length === 0) {
    try {
      categories.value = await categoryApi.list()
    } catch {
      ElMessage.error('加载分类失败')
      return
    }
  }
  editingId.value = product.id
  editForm.title = product.title
  editForm.categoryId = product.categoryId
  editForm.price = product.price
  editForm.description = product.description || ''
  editForm.coverImage = product.coverImage || ''
  editForm.images = product.images?.map(img => img.url) || []
  editForm.contactWechat = product.contactWechat || ''
  editForm.contactQq = product.contactQq || ''
  editVisible.value = true
}

const handleEditSubmit = async () => {
  const valid = await editFormRef.value?.validate().catch(() => false)
  if (!valid || editingId.value === null) return

  editSubmitting.value = true
  try {
    await productApi.update(editingId.value, {
      title: editForm.title,
      price: editForm.price,
      categoryId: editForm.categoryId!,
      description: editForm.description,
      coverImage: editForm.coverImage,
      images: editForm.images,
      contactWechat: editForm.contactWechat,
      contactQq: editForm.contactQq,
    })
    ElMessage.success('保存成功')
    editVisible.value = false
    fetchData()
  } catch { /* error handled in interceptor */ }
  finally {
    editSubmitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
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
