<template>
  <aside class="sidebar">
    <!-- 热搜 -->
    <div v-if="showHot" class="sidebar-card">
      <h4 class="card-title">
        <el-icon><TrendCharts /></el-icon>
        热搜话题
        <span class="refresh-hint">实时</span>
      </h4>
      <div v-if="hotLoading" class="hot-skeleton">
        <el-skeleton :rows="4" animated />
      </div>
      <div v-else-if="hotList.length === 0" class="hot-empty">
        暂无数据
      </div>
      <ul v-else class="hot-list">
        <li
          v-for="item in hotList"
          :key="item.topicId"
          class="hot-item"
          @click="$router.push(`/community?topic=${item.topicId}`)"
        >
          <span class="hot-rank" :class="'rank-' + item.rank">{{ item.rank }}</span>
          <span class="hot-name">{{ item.name }}</span>
          <span class="hot-count">{{ item.count }} 帖</span>
        </li>
      </ul>
    </div>

    <!-- 广告位 1 -->
    <div class="sidebar-card ad-card">
      <div class="ad-placeholder">
        <el-icon :size="28"><Present /></el-icon>
        <span>广告位招租</span>
      </div>
    </div>

    <!-- 广告位 2 -->
    <div class="sidebar-card ad-card">
      <div class="ad-placeholder">
        <el-icon :size="28"><ShoppingBag /></el-icon>
        <span>广告位招租</span>
      </div>
    </div>

    <!-- 广告位 3 (小) -->
    <div class="sidebar-card ad-card ad-small">
      <div class="ad-placeholder">
        <span>推广位</span>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { communityApi } from '@/api/modules/community'
import { TrendCharts, Present, ShoppingBag } from '@element-plus/icons-vue'

defineProps<{
  showHot?: boolean
}>()

interface HotItem {
  rank: number
  topicId: number
  name: string
  count: number
}

const hotList = ref<HotItem[]>([])
const hotLoading = ref(false)
let pollTimer: ReturnType<typeof setInterval> | null = null

const fetchHot = async () => {
  try {
    hotList.value = await communityApi.getHotTopics()
  } catch { /* silent */ }
}

onMounted(async () => {
  hotLoading.value = true
  await fetchHot()
  hotLoading.value = false
  // 每小时刷新热搜
  pollTimer = setInterval(fetchHot, 3600000)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<style scoped>
.sidebar {
  width: 280px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sidebar-card {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  border: 1px solid #f3f4f6;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
}

.refresh-hint {
  font-size: 10px;
  color: #4f6ef7;
  background: #eef0ff;
  padding: 1px 6px;
  border-radius: 4px;
  font-weight: 400;
}

/* 热搜列表 */
.hot-list { list-style: none; margin: 0; padding: 0; }
.hot-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 0;
  cursor: pointer;
  border-bottom: 1px solid #f9fafb;
  transition: color 0.15s;
}
.hot-item:last-child { border-bottom: none; }
.hot-item:hover .hot-name { color: #4f6ef7; }

.hot-rank {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #9ca3af;
  background: #f3f4f6;
  flex-shrink: 0;
}
.hot-rank.rank-1 { background: #ef4444; color: #fff; }
.hot-rank.rank-2 { background: #f97316; color: #fff; }
.hot-rank.rank-3 { background: #eab308; color: #fff; }

.hot-name {
  flex: 1;
  font-size: 13px;
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-count {
  font-size: 11px;
  color: #9ca3af;
  flex-shrink: 0;
}

/* 广告位 */
.ad-card {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100px;
}

.ad-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  color: #c4c4c4;
  font-size: 13px;
}

.ad-small {
  min-height: 60px;
}

.hot-skeleton, .hot-empty {
  padding: 8px 0;
  color: #9ca3af;
  font-size: 13px;
  text-align: center;
}
</style>
