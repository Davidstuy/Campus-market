<template>
  <div class="post-layout">
    <div class="post-detail-page">
    <!-- 错误状态 -->
    <el-result
      v-if="error"
      icon="error"
      title="加载失败"
      sub-title="无法获取帖子详情"
    >
      <template #extra>
        <el-button type="primary" @click="fetchPost">重新加载</el-button>
      </template>
    </el-result>

    <!-- 加载中 -->
    <div v-else-if="loading" class="detail-skeleton">
      <el-skeleton animated>
        <template #template>
          <div class="sk-header">
            <el-skeleton-item variant="circle" style="width: 40px; height: 40px" />
            <div>
              <el-skeleton-item variant="text" style="width: 120px" />
              <el-skeleton-item variant="text" style="width: 80px; margin-top: 4px" />
            </div>
          </div>
          <el-skeleton-item variant="text" style="width: 60%; height: 28px; margin-top: 16px" />
          <el-skeleton-item variant="text" style="width: 90%" />
          <el-skeleton-item variant="text" style="width: 80%" />
          <el-skeleton-item variant="text" style="width: 70%" />
        </template>
      </el-skeleton>
    </div>

    <!-- 帖子内容 -->
    <template v-else-if="post">
      <!-- 返回按钮 -->
      <el-button text @click="$router.push('/community')" class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回社区
      </el-button>

      <article class="post-article">
        <!-- 头部 -->
        <div class="post-header">
          <el-avatar :src="post.user?.avatarUrl" :size="44" />
          <div class="post-meta">
            <span class="post-author">{{ post.user?.nickname || post.user?.username || '匿名' }}</span>
            <span class="post-time">{{ formatTime(post.createdAt) }}</span>
          </div>
          <el-tag v-if="post.topic" size="small" class="post-topic-tag">
            {{ post.topic.name }}
          </el-tag>
        </div>

        <!-- 标题 -->
        <h1 class="post-title">{{ post.title }}</h1>

        <!-- 正文 -->
        <div class="post-content">{{ post.content }}</div>

        <!-- 媒体网格 -->
        <div v-if="post.media && post.media.length > 0" class="post-media-section">
          <div v-for="(m, idx) in post.media" :key="idx" class="media-block">
            <el-image
              v-if="m.mediaType === 'IMAGE'"
              :src="m.url"
              fit="contain"
              class="detail-media-img"
              :preview-src-list="imageUrls"
              :initial-index="imageIdx(m)"
              :preview-teleported="true"
            />
            <video
              v-else
              :src="m.url"
              controls
              class="detail-media-video"
              preload="metadata"
            />
          </div>
        </div>

        <!-- 操作栏 -->
        <div class="post-actions">
          <span class="post-comment-count">
            <el-icon><ChatDotRound /></el-icon>
            {{ post.commentCount }} 条评论
          </span>
          <el-button
            v-if="isOwner"
            type="danger"
            text
            size="small"
            @click="handleDelete"
          >
            删除帖子
          </el-button>
        </div>
      </article>

      <!-- 评论区 -->
      <section class="comment-section">
        <h3 class="comment-section-title">评论 ({{ post.commentCount }})</h3>

        <!-- 评论输入 -->
        <div class="comment-input-area">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            placeholder="发表评论... 支持表情和图片/视频"
          />
          <div class="comment-toolbar">
            <div class="toolbar-left">
              <EmojiPicker title="表情" @select="(e: string) => insertCommentEmoji(e)" />
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                accept="image/*"
                :show-file-list="false"
                :on-success="onCommentImageSuccess"
              >
                <el-button size="small" circle title="上传图片">
                  <el-icon><Picture /></el-icon>
                </el-button>
              </el-upload>
              <el-upload
                :action="uploadUrl"
                :headers="uploadHeaders"
                accept="video/*"
                :show-file-list="false"
                :on-success="onCommentVideoSuccess"
              >
                <el-button size="small" circle title="上传视频">
                  <el-icon><VideoCamera /></el-icon>
                </el-button>
              </el-upload>
            </div>
            <el-button type="primary" size="small" :loading="commentSubmitting" @click="submitComment">
              发表评论
            </el-button>
          </div>
          <!-- 附件预览 -->
          <div v-if="commentImageUrl" class="comment-attach-preview">
            <el-image :src="commentImageUrl" fit="cover" class="attach-thumb" />
            <el-button size="small" circle @click="commentImageUrl = null"><el-icon><Close /></el-icon></el-button>
          </div>
          <div v-if="commentVideoUrl" class="comment-attach-preview">
            <span class="attach-label">视频已选择</span>
            <el-button size="small" circle @click="commentVideoUrl = null"><el-icon><Close /></el-icon></el-button>
          </div>
        </div>

        <!-- 评论列表状态 -->
        <el-result
          v-if="commentError"
          icon="error"
          title="评论加载失败"
        >
          <template #extra>
            <el-button type="primary" size="small" @click="fetchComments">重试</el-button>
          </template>
        </el-result>

        <div v-else-if="commentLoading" class="comment-skeleton">
          <el-skeleton v-for="i in 3" :key="i" animated class="sk-comment">
            <template #template>
              <el-skeleton-item variant="circle" style="width: 32px; height: 32px" />
              <el-skeleton-item variant="text" style="width: 80%; margin: 8px 0" />
            </template>
          </el-skeleton>
        </div>

        <el-empty v-else-if="comments.length === 0" description="暂无评论，快来发表第一条评论吧" />

        <!-- 评论列表 -->
        <div v-else class="comment-list">
          <div v-for="c in comments" :key="c.id" class="comment-card">
            <div class="comment-main">
              <el-avatar :src="c.user?.avatarUrl" :size="32" class="comment-avatar" />
              <div class="comment-body">
                <div class="comment-header">
                  <span class="comment-author">{{ c.user?.nickname || c.user?.username }}</span>
                  <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
                </div>
                <div class="comment-content">{{ c.content }}</div>
                <!-- 评论图片/视频 -->
                <el-image
                  v-if="c.imageUrl"
                  :src="c.imageUrl"
                  fit="contain"
                  class="comment-media-img"
                  :preview-src-list="[c.imageUrl]"
                  :preview-teleported="true"
                />
                <video
                  v-if="c.videoUrl"
                  :src="c.videoUrl"
                  controls
                  class="comment-media-video"
                  preload="metadata"
                />
                <div class="comment-actions">
                  <el-button text size="small" @click="openReply(c)">回复</el-button>
                  <el-button
                    v-if="isCommentOwner(c)"
                    text
                    size="small"
                    type="danger"
                    @click="deleteComment(c.id)"
                  >
                    删除
                  </el-button>
                </div>

                <!-- 回复输入框 -->
                <div v-if="replyTarget?.id === c.id" class="reply-input-area">
                  <el-input
                    v-model="replyContent"
                    type="textarea"
                    :rows="2"
                    :placeholder="`回复 ${replyTarget.user?.nickname || '用户'}：`"
                  />
                  <div class="reply-toolbar">
                    <EmojiPicker title="表情" @select="(e: string) => { replyContent += e }" />
                    <el-button size="small" type="primary" @click="submitReply(c)">回复</el-button>
                    <el-button size="small" @click="replyTarget = null; replyContent = ''">取消</el-button>
                  </div>
                </div>

                <!-- 子回复 -->
                <div v-if="c.replies && c.replies.length > 0" class="nested-replies">
                  <div v-for="r in c.replies" :key="r.id" class="reply-card">
                    <el-avatar :src="r.user?.avatarUrl" :size="24" class="reply-avatar" />
                    <div class="reply-body">
                      <div class="reply-header">
                        <span class="reply-author">{{ r.user?.nickname || r.user?.username }}</span>
                        <span v-if="r.replyToUser" class="reply-to">
                          回复 <em>{{ r.replyToUser?.nickname || r.replyToUser?.username }}</em>
                        </span>
                        <span class="reply-time">{{ formatTime(r.createdAt) }}</span>
                      </div>
                      <div class="reply-content">{{ r.content }}</div>
                      <el-image
                        v-if="r.imageUrl"
                        :src="r.imageUrl"
                        fit="contain"
                        class="reply-media-img"
                        :preview-src-list="[r.imageUrl]"
                        :preview-teleported="true"
                      />
                      <video
                        v-if="r.videoUrl"
                        :src="r.videoUrl"
                        controls
                        class="reply-media-video"
                        preload="metadata"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 评论分页 -->
        <div v-if="commentTotal > commentSize" class="pagination">
          <el-pagination
            v-model:current-page="commentPage"
            :page-size="commentSize"
            :total="commentTotal"
            layout="prev, pager, next"
            @current-change="fetchComments"
          />
        </div>
      </section>
    </template>
    </div>
    <Sidebar show-hot />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { communityApi } from '@/api/modules/community'
import { useEmojiInsert } from '@/composables/useEmojiInsert'
import EmojiPicker from '@/components/common/EmojiPicker.vue'
import Sidebar from '@/components/common/Sidebar.vue'
import type { Post, PostComment } from '@/types'
import { ArrowLeft, ChatDotRound, Picture, VideoCamera, Close } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const postId = Number(route.params.id)

const post = ref<Post | null>(null)
const loading = ref(true)
const error = ref(false)

// 评论
const comments = ref<PostComment[]>([])
const commentLoading = ref(false)
const commentError = ref(false)
const commentPage = ref(1)
const commentSize = ref(10)
const commentTotal = ref(0)
const commentContent = ref('')
const commentImageUrl = ref<string | null>(null)
const commentVideoUrl = ref<string | null>(null)
const commentSubmitting = ref(false)

// 回复
const replyTarget = ref<PostComment | null>(null)
const replyContent = ref('')

const { insertEmoji: insertCommentEmoji } = useEmojiInsert(commentContent as any)

const currentUserId = computed(() => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    return user?.id
  } catch { return undefined }
})

const isOwner = computed(() => post.value?.userId === currentUserId.value)
const isCommentOwner = (c: PostComment) => c.userId === currentUserId.value

const uploadUrl = '/api/v1/files/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`,
}))

const imageUrls = computed(() =>
  post.value?.media?.filter(m => m.mediaType === 'IMAGE').map(m => m.url) || []
)
const imageIdx = (media: any) => Math.max(0, imageUrls.value.indexOf(media.url))

onMounted(() => {
  fetchPost()
  fetchComments()
})

async function fetchPost() {
  loading.value = true
  error.value = false
  try {
    post.value = await communityApi.getPost(postId)
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

async function fetchComments() {
  commentLoading.value = true
  commentError.value = false
  try {
    const result = await communityApi.listComments(postId, commentPage.value, commentSize.value)
    comments.value = result.records
    commentTotal.value = result.total
  } catch {
    commentError.value = true
  } finally {
    commentLoading.value = false
  }
}

function onCommentImageSuccess(response: any) {
  const url = response?.url || response?.data?.url
  if (url) commentImageUrl.value = url
}

function onCommentVideoSuccess(response: any) {
  const url = response?.url || response?.data?.url
  if (url) commentVideoUrl.value = url
}

async function submitComment() {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  commentSubmitting.value = true
  try {
    await communityApi.createComment(postId, commentContent.value.trim(), commentImageUrl.value, commentVideoUrl.value)
    commentContent.value = ''
    commentImageUrl.value = null
    commentVideoUrl.value = null
    commentPage.value = 1
    await Promise.all([fetchComments(), fetchPost()]) // 刷新评论和评论数
    ElMessage.success('评论成功')
  } catch {
    ElMessage.error('评论失败')
  } finally {
    commentSubmitting.value = false
  }
}

function openReply(c: PostComment) {
  replyTarget.value = c
  replyContent.value = ''
}

async function submitReply(parent: PostComment) {
  if (!replyContent.value.trim()) return
  try {
    await communityApi.createComment(
      postId, replyContent.value.trim(), null, null,
      parent.id, parent.userId,
    )
    replyTarget.value = null
    replyContent.value = ''
    await Promise.all([fetchComments(), fetchPost()])
    ElMessage.success('回复成功')
  } catch {
    ElMessage.error('回复失败')
  }
}

async function deleteComment(commentId: number) {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？', '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await communityApi.deleteComment(postId, commentId)
    await Promise.all([fetchComments(), fetchPost()])
    ElMessage.success('删除成功')
  } catch { /* cancelled */ }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定删除这个帖子吗？所有评论也会被删除。', '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await communityApi.deletePost(postId)
    ElMessage.success('删除成功')
    router.replace('/community')
  } catch { /* cancelled */ }
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
</script>

<style scoped>
.post-layout {
  display: flex;
  gap: 20px;
  padding: 12px 1.5vw 80px;
}

.post-detail-page {
  flex: 1;
  min-width: 0;
  max-width: 800px;
  margin: 0 auto;
}

.back-btn { margin-bottom: 16px; }

/* 骨架屏 */
.detail-skeleton {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.sk-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 帖子 */
.post-article {
  background: #fff;
  border-radius: 10px;
  padding: 16px 18px;
  border: 1px solid #f3f4f6;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.post-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.post-author { font-size: 14px; font-weight: 600; color: #1f2937; }
.post-time { font-size: 11px; color: #9ca3af; }

.post-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #1a1a2e;
  line-height: 1.4;
  text-align: left;
}

.post-content {
  font-size: 14px;
  line-height: 1.7;
  color: #374151;
  white-space: pre-wrap;
  word-break: break-word;
  margin-bottom: 16px;
  text-align: left;
}

/* 媒体 */
.post-media-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.media-block {
  border-radius: 8px;
  overflow: hidden;
  background: #f9fafb;
}

.detail-media-img {
  width: 100%;
  height: 260px;
  object-fit: cover;
  object-position: center;
  background: #f9fafb;
  cursor: pointer;
  border-radius: 8px;
}

.detail-media-video {
  width: 100%;
  max-height: 280px;
  border-radius: 8px;
  background: #000;
}

/* 操作栏 */
.post-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f3f4f6;
}

.post-comment-count {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: #9ca3af;
}

/* 评论区 */
.comment-section {
  margin-top: 24px;
}

.comment-section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #1f2937;
}

.comment-input-area {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #f3f4f6;
  margin-bottom: 20px;
}

.comment-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}

.toolbar-left {
  display: flex;
  gap: 8px;
  align-items: center;
}

.comment-attach-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
}

.attach-thumb {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  object-fit: cover;
}

.attach-label {
  font-size: 13px;
  color: #6b7280;
}

/* 评论列表 */
.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  border: 1px solid #f3f4f6;
}

.comment-main {
  display: flex;
  gap: 10px;
}

.comment-avatar { flex-shrink: 0; }
.comment-body { flex: 1; min-width: 0; }

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-author { font-size: 14px; font-weight: 600; }
.comment-time { font-size: 12px; color: #9ca3af; }

.comment-content {
  font-size: 14px;
  color: #374151;
  margin-top: 6px;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-media-img {
  max-width: 200px;
  max-height: 160px;
  margin-top: 8px;
  border-radius: 8px;
  cursor: pointer;
}

.comment-media-video {
  max-width: 220px;
  max-height: 160px;
  margin-top: 8px;
  border-radius: 8px;
}

.comment-actions {
  margin-top: 4px;
}

/* 嵌套回复 */
.nested-replies {
  margin-top: 12px;
  padding-left: 16px;
  border-left: 2px solid #f3f4f6;
}

.reply-card {
  display: flex;
  gap: 8px;
  padding: 8px 0;
}

.reply-avatar { flex-shrink: 0; }
.reply-body { flex: 1; min-width: 0; }

.reply-header { font-size: 13px; }
.reply-author { font-weight: 600; }
.reply-to { color: #9ca3af; margin-left: 4px; }
.reply-to em { color: #4f6ef7; font-style: normal; }
.reply-time { font-size: 11px; color: #c4c4c4; margin-left: 8px; }

.reply-content {
  font-size: 13px;
  color: #4b5563;
  margin-top: 4px;
  white-space: pre-wrap;
}

.reply-media-img {
  max-width: 180px;
  max-height: 150px;
  margin-top: 6px;
  border-radius: 6px;
  cursor: pointer;
}

.reply-media-video {
  max-width: 200px;
  max-height: 150px;
  margin-top: 6px;
  border-radius: 6px;
}

/* 回复输入区 */
.reply-input-area {
  margin-top: 10px;
}

.reply-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

/* 评论骨架屏 */
.comment-skeleton {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sk-comment {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
