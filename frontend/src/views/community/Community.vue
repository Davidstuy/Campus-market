<template>
  <div class="community-layout">
    <div class="community-page">
      <h2 class="page-title">社区</h2>

    <!-- 主题标签栏 -->
    <div class="topic-tabs">
      <button
        :class="['topic-tab', { active: activeTopic === 0 }]"
        @click="switchTopic(0)"
      >
        全部
      </button>
      <button
        v-for="t in topics"
        :key="t.id"
        :class="['topic-tab', { active: activeTopic === t.id }]"
        @click="switchTopic(t.id)"
      >
        <el-icon v-if="t.icon" class="tab-icon"><component :is="t.icon" /></el-icon>
        {{ t.name }}
        <span class="tab-count">{{ t.postCount }}</span>
      </button>
    </div>

    <!-- 错误状态 -->
    <el-result
      v-if="error"
      icon="error"
      title="加载失败"
      sub-title="无法获取帖子列表，请重试"
    >
      <template #extra>
        <el-button type="primary" @click="fetchPosts">重新加载</el-button>
      </template>
    </el-result>

    <!-- 加载中骨架屏 -->
    <div v-else-if="loading" class="post-list">
      <div v-for="i in 3" :key="i" class="post-card skeleton">
        <el-skeleton animated>
          <template #template>
            <div class="sk-header">
              <el-skeleton-item variant="circle" style="width: 36px; height: 36px" />
              <div>
                <el-skeleton-item variant="text" style="width: 120px" />
                <el-skeleton-item variant="text" style="width: 80px; margin-top: 4px" />
              </div>
            </div>
            <el-skeleton-item variant="text" style="width: 60%; height: 20px; margin-top: 12px" />
            <el-skeleton-item variant="text" style="width: 90%" />
            <el-skeleton-item variant="text" style="width: 70%" />
            <el-skeleton-item variant="rect" style="width: 100%; height: 120px; margin-top: 10px; border-radius: 8px" />
          </template>
        </el-skeleton>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else-if="posts.length === 0" description="还没有帖子，快来发布第一条吧">
      <el-button type="primary" @click="$router.push('/community/create')">发布帖子</el-button>
    </el-empty>

    <!-- 帖子列表 -->
    <div v-else class="post-list">
      <div
        v-for="post in posts"
        :key="post.id"
        class="post-card"
        @click="$router.push(`/community/${post.id}`)"
      >
        <div class="post-header">
          <el-avatar :src="post.user?.avatarUrl" :size="36" class="post-avatar" />
          <div class="post-meta">
            <span class="post-author">{{ post.user?.nickname || post.user?.username || '匿名' }}</span>
            <span class="post-time">{{ formatTime(post.createdAt) }}</span>
          </div>
          <el-tag v-if="post.topic" size="small" type="info" class="post-topic-tag">
            {{ post.topic.name }}
          </el-tag>
        </div>

        <h3 class="post-title">{{ post.title }}</h3>
        <p class="post-preview">{{ post.content }}</p>

        <!-- 媒体网格 -->
        <div v-if="post.media && post.media.length > 0" class="post-media-grid" :class="`grid-${Math.min(post.media.length, 3)}`">
          <div
            v-for="(m, idx) in post.media.slice(0, 9)"
            :key="idx"
            class="media-item"
          >
            <el-image
              v-if="m.mediaType === 'IMAGE'"
              :src="thumbUrl(m.url)"
              fit="cover"
              class="media-img"
            />
            <div v-else class="media-video">
              <video :src="m.url" class="media-video-el" />
              <span class="video-play-icon">▶</span>
            </div>
          </div>
          <div v-if="post.media.length > 9" class="media-more">
            +{{ post.media.length - 9 }}
          </div>
        </div>

        <div class="post-footer">
          <span class="post-comments">
            <el-icon><ChatDotRound /></el-icon>
            {{ post.commentCount }} 条评论
          </span>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > size" class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          @current-change="fetchPosts"
        />
      </div>
    </div>

    <!-- 发布按钮 (FAB) -->
    <el-button
      type="primary"
      circle
      size="large"
      class="fab"
      @click="$router.push('/community/create')"
    >
      <el-icon size="24"><Edit /></el-icon>
    </el-button>
    </div>
    <!-- 侧边栏 -->
    <Sidebar show-hot />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { communityApi } from '@/api/modules/community'
import type { Topic, Post } from '@/types'
import { ChatDotRound, Edit } from '@element-plus/icons-vue'
import Sidebar from '@/components/common/Sidebar.vue'

const topics = ref<Topic[]>([])
const posts = ref<Post[]>([])
const loading = ref(false)
const error = ref(false)
const activeTopic = ref(0)
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(async () => {
  await Promise.all([fetchTopics(), fetchPosts()])
})

async function fetchTopics() {
  try {
    topics.value = await communityApi.listTopics()
  } catch { /* topic load failure is non-fatal */ }
}

async function fetchPosts() {
  loading.value = true
  error.value = false
  try {
    const topicId = activeTopic.value > 0 ? activeTopic.value : undefined
    const result = await communityApi.listPosts(page.value, size.value, topicId)
    posts.value = result.records
    total.value = result.total
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

function switchTopic(topicId: number) {
  activeTopic.value = topicId
  page.value = 1
  fetchPosts()
}

function formatTime(dateStr: string) {
  const d = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  const min = Math.floor(diff / 60000)
  if (min < 1) return '刚刚'
  if (min < 60) return `${min} 分钟前`
  const hours = Math.floor(min / 60)
  if (hours < 24) return `${hours} 小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days} 天前`
  return d.toLocaleDateString('zh-CN')
}

function thumbUrl(url: string) {
  if (!url) return url
  if (url.includes('aliyuncs.com')) return url + '?x-oss-process=image/resize,m_lfit,w_400'
  return url.replace('/v1/files/', '/v1/files/thumb/')
}
</script>

<style scoped>
.community-layout {
  display: flex;
  gap: 20px;
  padding: 16px 1.5vw 80px;
}

.community-page {
  flex: 1;
  min-width: 0;
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 12px;
  color: var(--text-primary, #1a1a2e);
}

/* 主题标签栏 */
.topic-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding-bottom: 12px;
  margin-bottom: 16px;
  -webkit-overflow-scrolling: touch;
}

.topic-tabs::-webkit-scrollbar { display: none; }

.topic-tab {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  padding: 7px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  background: #fff;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.topic-tab:hover { border-color: var(--primary-color, #4f6ef7); color: var(--primary-color, #4f6ef7); }
.topic-tab.active {
  background: var(--primary-color, #4f6ef7);
  color: #fff;
  border-color: var(--primary-color, #4f6ef7);
}

.tab-icon { font-size: 14px; }
.tab-count {
  font-size: 12px;
  opacity: 0.7;
  margin-left: 2px;
}

/* 帖子列表 */
.post-list { display: flex; flex-direction: column; gap: 16px; }

.post-card {
  background: #fff;
  border-radius: 10px;
  padding: 14px 16px;
  cursor: pointer;
  transition: box-shadow 0.2s;
  border: 1px solid #f3f4f6;
}

.post-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.06); }

.post-card.skeleton { cursor: default; border: none; }

.sk-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.post-avatar { flex-shrink: 0; }

.post-meta {
  display: flex;
  flex-direction: column;
  flex: 1;
}

.post-author { font-size: 13px; font-weight: 600; color: #1f2937; }
.post-time { font-size: 11px; color: #9ca3af; }

.post-topic-tag { flex-shrink: 0; }

.post-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #1f2937;
  line-height: 1.4;
}

.post-preview {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: pre-wrap;
  word-break: break-word;
}

/* 媒体网格 */
.post-media-grid {
  display: grid;
  gap: 4px;
  margin-top: 8px;
  border-radius: 8px;
  overflow: hidden;
}

.grid-1 {
  grid-template-columns: 1fr;
  max-width: 240px;
}

.grid-2 { grid-template-columns: 1fr 1fr; }
.grid-3 { grid-template-columns: repeat(3, 1fr); }

.media-item {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 6px;
  background: #f3f4f6;
  max-height: 140px;
}

.media-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
}

.media-video {
  width: 100%;
  height: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1f2937;
}

.media-video-el {
  width: 100%;
  height: 100%;
  object-fit: contain;
  opacity: 0.6;
}

.video-play-icon {
  position: absolute;
  color: #fff;
  font-size: 28px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.3);
}

.media-more {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0,0,0,0.65);
  color: #fff;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
}

/* 页脚 */
.post-footer {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.post-comments {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: #9ca3af;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* FAB */
.fab {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 56px;
  height: 56px;
  box-shadow: 0 4px 16px rgba(79, 110, 247, 0.35);
  z-index: 100;
}

@media (max-width: 900px) {
  .community-layout {
    flex-direction: column;
    padding: 12px 8px 80px;
  }
  .community-layout > .sidebar { display: none; }
}
</style>
