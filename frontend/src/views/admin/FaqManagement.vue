<template>
  <div class="faq-management">
    <div class="page-header">
      <h2>FAQ 管理</h2>
      <el-button type="primary" @click="showAddDialog">新增 FAQ</el-button>
    </div>

    <!-- 加载态 -->
    <div v-if="loading" class="state-box">
      <el-skeleton :rows="4" animated />
    </div>

    <!-- 错误态 -->
    <div v-else-if="error" class="state-box">
      <p>{{ error }}</p>
      <el-button type="primary" @click="loadFaqs">重新加载</el-button>
    </div>

    <!-- 数据态 -->
    <template v-else>
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="question" label="问题" min-width="250" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="80" align="center" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-popconfirm
              title="确定删除该 FAQ？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑 FAQ' : '新增 FAQ'"
      width="600px"
      destroy-on-close
    >
      <el-form :model="form" label-width="80px">
        <el-form-item label="问题">
          <el-input v-model="form.question" placeholder="请输入常见问题" />
        </el-form-item>
        <el-form-item label="回答">
          <el-input
            v-model="form.answer"
            type="textarea"
            :rows="5"
            placeholder="请输入回答内容"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :disabled="!form.question.trim() || !form.answer.trim()"
          @click="handleSave"
        >
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
import type { Faq } from '@/types'

const loading = ref(true)
const error = ref('')
const list = ref<Faq[]>([])

const dialogVisible = ref(false)
const editing = ref<Faq | null>(null)
const form = reactive({ question: '', answer: '', sortOrder: 0 })

const loadFaqs = async () => {
  loading.value = true
  error.value = ''
  try {
    list.value = await adminApi.getFaqs()
  } catch {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  editing.value = null
  form.question = ''
  form.answer = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

const showEditDialog = (faq: Faq) => {
  editing.value = faq
  form.question = faq.question
  form.answer = faq.answer
  form.sortOrder = faq.sortOrder
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (editing.value) {
      await adminApi.updateFaq(editing.value.id, { ...form })
      ElMessage.success('更新成功')
    } else {
      await adminApi.createFaq({ ...form })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadFaqs()
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await adminApi.deleteFaq(id)
    ElMessage.success('删除成功')
    await loadFaqs()
  } catch {
    ElMessage.error('删除失败')
  }
}

onMounted(loadFaqs)
</script>

<style scoped>
.faq-management {
  display: flex;
  flex-direction: column;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.state-box {
  padding: 60px 0;
  text-align: center;
}
.state-box p {
  color: var(--text-muted);
  margin-bottom: 12px;
}
</style>
