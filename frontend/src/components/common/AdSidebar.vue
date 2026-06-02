<template>
  <aside class="ad-sidebar">
    <div
      v-for="i in 3"
      :key="i"
      class="ad-card"
      :class="{ 'ad-contact': i === 2 }"
      @click="goSupport"
    >
      <div class="ad-placeholder">
        <el-icon :size="24"><Present /></el-icon>
        <span>广告位</span>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Present } from '@element-plus/icons-vue'
import { chatApi } from '@/api/modules/chat'
import { ElMessage } from 'element-plus'

const router = useRouter()

const goSupport = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    router.push('/login')
    return
  }
  try {
    const conv = await chatApi.getOrCreateSupportConversation()
    router.push(`/chat?conversation=${conv.id}`)
  } catch {
    ElMessage.error('无法连接客服')
  }
}
</script>

<style scoped>
.ad-sidebar {
  width: 220px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ad-card {
  background: #fff;
  border-radius: 10px;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #e5e7eb;
  transition: all 0.2s;
  cursor: pointer;
}

.ad-card:hover {
  border-color: var(--el-color-primary, #4f6ef7);
  box-shadow: 0 2px 8px rgba(79, 110, 247, 0.1);
}

.ad-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #d1d5db;
  font-size: 13px;
  transition: color 0.2s;
}

.ad-card:hover .ad-placeholder {
  color: var(--el-color-primary);
}

/* 中间卡片微调：hover 时更明显 */
.ad-contact:hover {
  background: var(--el-color-primary-light-9);
}

@media (max-width: 1024px) {
  .ad-sidebar { display: none; }
}
</style>
