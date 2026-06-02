<template>
  <div class="category-management">
    <div class="header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="showAddDialog">新增分类</el-button>
    </div>

    <!-- loading -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="4" animated />
    </div>

    <!-- error -->
    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <el-button type="primary" @click="loadCategories">重新加载</el-button>
    </div>

    <!-- table -->
    <template v-else>
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="分类名称" min-width="150" />
        <el-table-column prop="icon" label="图标" width="120">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
            <span>{{ row.icon }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-popconfirm title="确定删除该分类？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑分类' : '新增分类'"
      width="400px"
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" placeholder="分类名称" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名，如 Goods" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="99" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!form.name.trim()" @click="handleSave">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/modules/admin'
import { categoryApi } from '@/api/modules/category'
import type { Category } from '@/types'

const loading = ref(true)
const error = ref('')
const list = ref<Category[]>([])

const dialogVisible = ref(false)
const editing = ref<Category | null>(null)
const form = reactive({ name: '', icon: '', sortOrder: 0 })

const loadCategories = async () => {
  loading.value = true
  error.value = ''
  try {
    list.value = await categoryApi.list() as Category[]
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  editing.value = null
  form.name = ''
  form.icon = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

const showEditDialog = (cat: Category) => {
  editing.value = cat
  form.name = cat.name
  form.icon = cat.icon
  form.sortOrder = cat.sortOrder
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (editing.value) {
      await adminApi.updateCategory(editing.value.id, { ...form })
      ElMessage.success('已更新')
    } else {
      await adminApi.createCategory({ ...form })
      ElMessage.success('已新增')
    }
    dialogVisible.value = false
    loadCategories()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await adminApi.deleteCategory(id)
    ElMessage.success('已删除')
    loadCategories()
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

onMounted(loadCategories)
</script>

<style scoped>
.category-management .header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.category-management h2 {
  margin: 0;
  font-size: 20px;
}

.loading-state,
.error-state {
  padding: 60px 0;
  text-align: center;
}

.error-state p {
  color: #f56c6c;
  margin-bottom: 12px;
}
</style>
