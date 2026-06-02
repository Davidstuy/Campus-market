<template>
  <div class="user-management">
    <h2>用户管理</h2>

    <!-- search -->
    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名或昵称"
        clearable
        style="width: 300px"
        @keyup.enter="search"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="search">搜索</el-button>
    </div>

    <!-- loading -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- error -->
    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <el-button type="primary" @click="loadUsers">重新加载</el-button>
    </div>

    <!-- empty -->
    <div v-else-if="list.length === 0" class="empty-state">
      <el-empty description="暂无用户" />
    </div>

    <!-- table -->
    <template v-else>
      <el-table :data="list" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small">
              {{ row.role === 'ADMIN' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'BANNED' ? 'danger' : 'success'" size="small">
              {{ row.status === 'BANNED' ? '已封禁' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              @click="$router.push(`/shop/${row.id}`)"
            >
              店铺
            </el-button>
            <template v-if="row.role !== 'ADMIN'">
              <el-button
                v-if="row.status !== 'BANNED'"
                type="danger"
                size="small"
                @click="handleBan(row.id)"
              >
                封禁
              </el-button>
              <el-button
                v-else
                type="success"
                size="small"
                @click="handleUnban(row.id)"
              >
                解封
              </el-button>
            </template>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="total > size" class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadUsers"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/modules/admin'
import type { User } from '@/types'

const loading = ref(true)
const error = ref('')
const list = ref<User[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')

const loadUsers = async () => {
  loading.value = true
  error.value = ''
  try {
    const data = await adminApi.getUsers(page.value, size.value, keyword.value || undefined)
    list.value = data.records
    total.value = data.total
  } catch (e: any) {
    error.value = e?.message || '加载失败'
  } finally {
    loading.value = false
  }
}

const search = () => {
  page.value = 1
  loadUsers()
}

const handleBan = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '封禁后该用户将无法登录，且所有在售商品将被下架。确定封禁？',
      '确认封禁',
      {
        type: 'warning',
        confirmButtonText: '确认封禁',
        cancelButtonText: '取消',
      }
    )
    await adminApi.banUser(id)
    ElMessage.success('已封禁')
    loadUsers()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '操作失败')
  }
}

const handleUnban = async (id: number) => {
  try {
    await adminApi.unbanUser(id)
    ElMessage.success('已解封')
    loadUsers()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

onMounted(loadUsers)
</script>

<style scoped>
.user-management h2 {
  margin: 0 0 20px;
  font-size: 20px;
}

.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
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

.text-muted {
  color: #c0c4cc;
}
</style>
