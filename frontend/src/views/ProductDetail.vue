<template>
  <!-- 加载态 -->
  <div v-if="loading" class="product-detail">
    <el-skeleton :rows="8" animated />
  </div>

  <!-- 错误态 -->
  <el-result
    v-else-if="error"
    icon="error"
    title="加载失败"
    sub-title="无法获取商品详情，请重试"
  >
    <template #extra>
      <el-button type="primary" @click="fetchData">重新加载</el-button>
    </template>
  </el-result>

  <!-- 商品不存在 -->
  <el-empty v-else-if="!product" description="商品不存在" />

  <!-- 正常态 -->
  <div v-else class="product-detail">
    <div class="breadcrumb">
      <router-link to="/">首页</router-link>
      <span class="sep">/</span>
      <router-link to="/products">商品</router-link>
      <span class="sep">/</span>
      <span>{{ product.title }}</span>
    </div>

    <div class="detail-main">
      <div class="images-section">
        <el-image
          v-if="images.length === 0"
          :src="thumbUrl(product.coverImage)"
          fit="contain"
          class="main-image"
          :preview-src-list="[product.coverImage]"
          :preview-teleported="true"
        />
        <el-carousel v-else class="main-image" trigger="click">
          <el-carousel-item v-for="img in images" :key="img.id">
            <el-image
              :src="thumbUrl(img.url)"
              fit="contain"
              class="carousel-image"
              :preview-src-list="images.map(i => i.url)"
              :preview-teleported="true"
              :initial-index="images.findIndex(i => i.id === img.id)"
            />
          </el-carousel-item>
        </el-carousel>
      </div>

      <div class="info-section">
        <h1 class="title">{{ product.title }}</h1>
        <div class="price">¥{{ product.price }}</div>
        <div class="meta">
          <span>分类：{{ product.category?.name }}</span>
          <span>状态：{{ statusMap[product.status] }}</span>
          <span>发布时间：{{ product.createdAt?.slice(0, 10) }}</span>
        </div>
        <div class="seller">
          <el-image
            fit="cover"
            class="seller-avatar"
            :src="product.seller?.avatarUrl"
            :preview-src-list="[product.seller?.avatarUrl]"
            :preview-teleported="true"
          >
            <template #error>
              <el-icon :size="20"><UserFilled /></el-icon>
            </template>
          </el-image>
          <span class="seller-name" @click="goToShop">{{ product.seller?.nickname || product.seller?.username }}</span>
          <el-icon :size="14" class="shop-arrow" @click="goToShop"><ArrowRight /></el-icon>
        </div>
        <div v-if="product.contactWechat" class="contact">微信：{{ product.contactWechat }}</div>
        <div v-if="product.contactQq" class="contact">QQ：{{ product.contactQq }}</div>

        <div class="actions">
          <el-button
            :type="product.isFavorited ? 'warning' : 'default'"
            @click="requireLogin(toggleFavorite)"
          >
            {{ product.isFavorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button
            v-if="canBuy"
            type="danger"
            @click="requireLogin(() => router.push(`/checkout/${product!.id}`))"
          >
            立即购买
          </el-button>
          <el-button
            v-if="canContact"
            type="primary"
            @click="requireLogin(contactSeller)"
            :loading="contactLoading"
          >
            联系卖家
          </el-button>
        </div>

        <el-descriptions :column="1" border class="desc-section">
          <el-descriptions-item label="商品描述">
            {{ product.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <!-- 评论区 -->
    <div class="comment-section">
      <h2 class="comment-title">评论区（{{ commentTotal }}）</h2>

      <!-- 发表评论 -->
      <div v-if="isLoggedIn" class="comment-form">
        <el-input
          v-model="commentContent"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="写下你的想法..."
        />
        <el-button
          type="primary"
          :loading="commentSubmitting"
          :disabled="!commentContent.trim()"
          @click="submitComment"
        >
          发表评论
        </el-button>
      </div>
      <div v-else class="comment-login-hint">
        <router-link :to="`/login?redirect=${encodeURIComponent(route.fullPath)}`">登录</router-link>
        后即可发表评论
      </div>

      <!-- 评论列表 — 加载态 -->
      <div v-if="commentLoading" class="comment-list">
        <div v-for="i in 3" :key="i" class="comment-card skeleton-card">
          <el-skeleton :rows="2" animated />
        </div>
      </div>

      <!-- 评论列表 — 错误态 -->
      <el-result
        v-else-if="commentError"
        icon="error"
        title="加载评论失败"
        sub-title="请重试"
      >
        <template #extra>
          <el-button type="primary" @click="fetchComments">重新加载</el-button>
        </template>
      </el-result>

      <!-- 评论列表 — 空态 -->
      <el-empty v-else-if="comments.length === 0" description="暂无评论，来抢沙发吧~" />

      <!-- 评论列表 — 正常态 -->
      <div v-else class="comment-list">
        <div
          v-for="c in comments"
          :key="c.id"
          class="comment-card"
          :class="{ 'is-replying': replyTarget?.id === c.id }"
          @click="startReply(c)"
        >
          <!-- 头部：头像 + 昵称 + 时间 + 回复链接 -->
          <div class="comment-header">
            <el-image fit="cover" class="comment-avatar" :src="c.user?.avatarUrl">
              <template #error>
                <el-icon :size="18"><UserFilled /></el-icon>
              </template>
            </el-image>
            <span class="comment-nickname">{{ c.user?.nickname || c.user?.username || '匿名' }}</span>
            <span class="comment-time">{{ formatTime(c.createdAt) }}</span>
            <span class="comment-reply-link" @click.stop="startReply(c)">回复</span>
          </div>

          <!-- 内容 -->
          <p class="comment-content">{{ c.content }}</p>

          <!-- 底部：赞/踩 在右侧 -->
          <div class="comment-footer">
            <div class="vote-group">
              <button
                class="vote-btn"
                :class="{ 'is-active': c.myVote === 1 }"
                @click.stop="handleVote(c, 1)"
                title="赞"
              >
                <span class="vote-emoji">👍</span>
                <span v-if="c.upCount" class="vote-num">{{ c.upCount }}</span>
              </button>
              <button
                class="vote-btn"
                :class="{ 'is-active is-down': c.myVote === -1 }"
                @click.stop="handleVote(c, -1)"
                title="踩"
              >
                <span class="vote-emoji">👎</span>
                <span v-if="c.downCount" class="vote-num">{{ c.downCount }}</span>
              </button>
            </div>
          </div>

          <!-- 内联回复输入框 -->
          <div v-if="replyTarget?.id === c.id" class="inline-reply" @click.stop>
            <el-input
              v-model="replyContent"
              type="textarea"
              :rows="2"
              maxlength="500"
              show-word-limit
              :placeholder="`回复 @${replyTarget.user?.nickname || replyTarget.user?.username}：`"
              @keyup.enter.ctrl="submitReply(c)"
            />
            <div class="inline-reply-actions">
              <span class="reply-hint">Ctrl+Enter 发送</span>
              <el-button size="small" @click="cancelReply">取消</el-button>
              <el-button
                size="small"
                type="primary"
                :loading="replySubmitting"
                :disabled="!replyContent.trim()"
                @click="submitReply(c)"
              >
                回复
              </el-button>
            </div>
          </div>

          <!-- 嵌套回复列表 -->
          <div v-if="c.replies?.length" class="replies-section">
            <div
              v-for="r in c.replies"
              :key="r.id"
              class="reply-card"
              @click.stop="startReply(c)"
            >
              <div class="reply-header">
                <el-image fit="cover" class="reply-avatar" :src="r.user?.avatarUrl">
                  <template #error>
                    <el-icon :size="14"><UserFilled /></el-icon>
                  </template>
                </el-image>
                <span class="reply-nickname">{{ r.user?.nickname || r.user?.username || '匿名' }}</span>
                <template v-if="r.replyToUser">
                  <span class="reply-sep">回复</span>
                  <span class="reply-target-name">@{{ r.replyToUser.nickname || r.replyToUser.username }}</span>
                </template>
                <span class="reply-time">{{ formatTime(r.createdAt) }}</span>
              </div>
              <p class="reply-content">{{ r.content }}</p>
              <!-- 回复的赞/踩 -->
              <div class="comment-footer">
                <div class="vote-group">
                  <button
                    class="vote-btn vote-btn-sm"
                    :class="{ 'is-active': r.myVote === 1 }"
                    @click.stop="handleVote(r, 1)"
                  >
                    <span class="vote-emoji">👍</span>
                    <span v-if="r.upCount" class="vote-num">{{ r.upCount }}</span>
                  </button>
                  <button
                    class="vote-btn vote-btn-sm"
                    :class="{ 'is-active is-down': r.myVote === -1 }"
                    @click.stop="handleVote(r, -1)"
                  >
                    <span class="vote-emoji">👎</span>
                    <span v-if="r.downCount" class="vote-num">{{ r.downCount }}</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分页 -->
        <el-pagination
          v-if="commentTotal > commentSize"
          class="comment-pagination"
          v-model:current-page="commentPage"
          :page-size="commentSize"
          :total="commentTotal"
          layout="prev, pager, next"
          @current-change="fetchComments"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi } from '@/api/modules/product'
import { favoriteApi } from '@/api/modules/favorite'
import { chatApi } from '@/api/modules/chat'
import { commentApi } from '@/api/modules/comment'
import type { Product, ProductImage, Comment } from '@/types'
import { ArrowRight, UserFilled } from '@element-plus/icons-vue'
import { PRODUCT_STATUS } from '@/utils/constants'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref(false)
const product = ref<Product | null>(null)
const images = ref<ProductImage[]>([])
const statusMap = PRODUCT_STATUS

const currentUserId = computed(() => {
  const raw = localStorage.getItem('user')
  if (!raw) return 0
  return JSON.parse(raw).id as number
})
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const canBuy = computed(() =>
  product.value &&
  product.value.status === 'ACTIVE' &&
  product.value.sellerId !== currentUserId.value
)
const canContact = computed(() =>
  product.value &&
  product.value.status !== 'DELISTED' &&
  product.value.sellerId !== currentUserId.value
)
const contactLoading = ref(false)

// ============ 评论区 ============
const comments = ref<Comment[]>([])
const commentPage = ref(1)
const commentSize = ref(10)
const commentTotal = ref(0)
const commentLoading = ref(false)
const commentError = ref(false)
const commentContent = ref('')
const commentSubmitting = ref(false)
const replyTarget = ref<Comment | null>(null)
const replyToUser = ref<{ id: number; nickname: string } | null>(null)
const replyContent = ref('')
const replySubmitting = ref(false)

const startReply = (parent: Comment) => {
  if (!isLoggedIn.value) {
    router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
    return
  }
  replyTarget.value = parent
  replyToUser.value = { id: parent.userId, nickname: parent.user?.nickname || parent.user?.username || '匿名' }
}

const cancelReply = () => {
  replyTarget.value = null
  replyToUser.value = null
  replyContent.value = ''
}

const submitReply = async (parent: Comment) => {
  if (!replyContent.value.trim()) return
  replySubmitting.value = true
  try {
    const id = Number(route.params.id)
    const newReply = await commentApi.create(
      id,
      replyContent.value.trim(),
      parent.id,
      replyToUser.value?.id
    )
    // 追加到父评论的 replies
    if (!parent.replies) parent.replies = []
    parent.replies!.push(newReply)
    replyContent.value = ''
    replyTarget.value = null
    replyToUser.value = null
  } catch { /* ignore */ }
  finally {
    replySubmitting.value = false
  }
}

const fetchComments = async () => {
  const id = Number(route.params.id)
  if (!id) return
  commentLoading.value = true
  commentError.value = false
  try {
    const result = await commentApi.list(id, {
      page: commentPage.value,
      size: commentSize.value,
    })
    comments.value = result.records
    commentTotal.value = result.total
  } catch {
    commentError.value = true
  } finally {
    commentLoading.value = false
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) return
  commentSubmitting.value = true
  try {
    const id = Number(route.params.id)
    const newComment = await commentApi.create(id, commentContent.value.trim())
    comments.value.unshift(newComment)
    commentTotal.value++
    commentContent.value = ''
  } catch { /* ignore */ }
  finally {
    commentSubmitting.value = false
  }
}

const handleVote = (comment: Comment, vote: number) => {
  if (!isLoggedIn.value) {
    router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
    return
  }
  const productId = Number(route.params.id)
  // 点同一个按钮 → 取消投票
  if (comment.myVote === vote) {
    commentApi.cancelVote(productId, comment.id).then(() => {
      if (vote === 1) comment.upCount--
      else comment.downCount--
      comment.myVote = null
    }).catch(() => {})
    return
  }
  // 新投票或改变投票
  commentApi.vote(productId, comment.id, vote).then(() => {
    // 如果之前投了相反的，需要减掉
    if (comment.myVote === 1) comment.upCount--
    else if (comment.myVote === -1) comment.downCount--
    // 新的票
    if (vote === 1) comment.upCount++
    else comment.downCount++
    comment.myVote = vote
  }).catch(() => {})
}

const formatTime = (dateStr: string) => {
  const d = new Date(dateStr)
  const now = Date.now()
  const diff = now - d.getTime()
  const min = Math.floor(diff / 60000)
  if (min < 1) return '刚刚'
  if (min < 60) return `${min}分钟前`
  const hours = Math.floor(min / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  return d.toLocaleDateString()
}

const requireLogin = (action: () => void) => {
  if (!isLoggedIn.value) {
    router.push(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
    return
  }
  action()
}

const thumbUrl = (url: string) => {
  if (!url || url === '/placeholder.svg') return url
  if (url.includes('aliyuncs.com')) {
    return url + '?x-oss-process=image/resize,m_lfit,w_400'
  }
  return url.replace('/v1/files/', '/v1/files/thumb/')
}

const fetchData = async () => {
  loading.value = true
  error.value = false
  try {
    const id = Number(route.params.id)
    product.value = await productApi.detail(id)
    images.value = (product.value as unknown as { images?: ProductImage[] }).images || []
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

const toggleFavorite = async () => {
  if (!product.value) return
  try {
    if (product.value.isFavorited) {
      await favoriteApi.remove(product.value.id)
    } else {
      await favoriteApi.add(product.value.id)
    }
    product.value.isFavorited = !product.value.isFavorited
  } catch { /* ignore */ }
}

const contactSeller = async () => {
  if (!product.value) return
  contactLoading.value = true
  try {
    const conv = await chatApi.getOrCreateConversation(product.value.sellerId, product.value.id)
    router.push(`/chat?conversation=${conv.id}`)
  } catch {
    /* ignore */
  } finally {
    contactLoading.value = false
  }
}

const goToShop = () => {
  if (product.value?.sellerId) {
    router.push(`/shop/${product.value.sellerId}`)
  }
}

onMounted(() => {
  fetchData()
  fetchComments()
})
</script>

<style scoped>
.product-detail { min-height: 300px; }

.breadcrumb {
  font-size: var(--text-sm);
  color: var(--text-muted);
  margin-bottom: 24px;
  display: flex;
  gap: 8px;
  align-items: center;
}
.breadcrumb a {
  color: var(--text-secondary);
  transition: color var(--transition-fast);
}
.breadcrumb a:hover {
  color: var(--el-color-primary);
}
.sep { color: var(--border-color); }

.detail-main {
  display: flex;
  gap: 40px;
  flex-wrap: wrap;
}

.images-section {
  width: 480px;
  max-width: 100%;
  background: #f1f5f9;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}
.main-image {
  width: 100%;
  height: 360px;
  border-radius: var(--radius-lg);
  overflow: hidden;
}
.main-image :deep(img),
.carousel-image :deep(img) {
  object-position: center;
}
.carousel-image {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
}

.info-section {
  flex: 1;
  min-width: 300px;
}
.title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 16px;
  line-height: 1.3;
}
.price {
  color: var(--el-color-danger);
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 20px;
  letter-spacing: -0.5px;
}
.meta {
  display: flex;
  gap: 16px;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.seller {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: var(--el-color-primary-light-9);
  border-radius: var(--radius-md);
}
.seller-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}
.seller-avatar :deep(img) {
  width: 40px;
  height: 40px;
  object-fit: cover;
}
.seller-avatar :deep(.el-image__error) {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--el-color-info-light-8);
  display: flex;
  align-items: center;
  justify-content: center;
}
.seller-name {
  font-weight: 500;
  color: var(--text-primary);
  flex: 1;
  cursor: pointer;
}
.seller-name:hover {
  color: var(--el-color-primary);
}
.shop-arrow {
  color: var(--text-muted);
  flex-shrink: 0;
  cursor: pointer;
}
.shop-arrow:hover {
  color: var(--el-color-primary);
}
.contact {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: 4px;
}
.actions {
  margin: 24px 0;
  display: flex;
  gap: 12px;
}
.actions :deep(.el-button) {
  border-radius: var(--radius-md);
  padding: 10px 24px;
}
.desc-section {
  margin-top: 24px;
}
.desc-section :deep(.el-descriptions__body) {
  border-radius: var(--radius-md);
}

/* ============ 评论区 ============ */
.comment-section {
  margin-top: 48px;
  border-top: 1px solid var(--border-color);
  padding-top: 32px;
}

.comment-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}

.comment-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 32px;
  padding: 16px;
  background: var(--el-color-primary-light-9);
  border-radius: var(--radius-md);
}
.comment-form :deep(.el-textarea__inner) {
  border-radius: var(--radius-sm);
  resize: none;
}
.comment-form .el-button {
  align-self: flex-end;
}

.comment-login-hint {
  margin-bottom: 32px;
  padding: 20px;
  text-align: center;
  background: #f8fafc;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
}
.comment-login-hint a {
  color: var(--el-color-primary);
  font-weight: 500;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-card {
  padding: 16px 20px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: #fff;
  transition: box-shadow var(--transition-fast);
}
.comment-card:hover {
  box-shadow: var(--shadow-sm);
}

.skeleton-card {
  border: none;
  padding: 12px 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.comment-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--el-color-info-light-8);
}
.comment-avatar :deep(img) {
  width: 32px;
  height: 32px;
  object-fit: cover;
}
.comment-avatar :deep(.el-image__error) {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--el-color-info-light-8);
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-nickname {
  font-weight: 500;
  color: var(--text-primary);
  font-size: var(--text-sm);
}

.comment-time {
  margin-left: auto;
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.comment-content {
  margin: 0;
  font-size: var(--text-base);
  color: var(--text-primary);
  line-height: 1.6;
  word-break: break-word;
}

/* 回复链接 */
.comment-reply-link {
  font-size: var(--text-xs);
  color: var(--text-muted);
  cursor: pointer;
  user-select: none;
  transition: color var(--transition-fast);
  margin-left: 12px;
}
.comment-reply-link:hover {
  color: var(--el-color-primary);
}

/* 评论底部：赞/踩按钮在右下角 */
.comment-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}
.vote-group {
  display: flex;
  gap: 2px;
  background: #f8fafc;
  border-radius: 20px;
  padding: 2px;
  border: 1px solid var(--border-color);
}
.vote-btn {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 3px 10px;
  border: none;
  border-radius: 20px;
  background: transparent;
  cursor: pointer;
  font-size: var(--text-xs);
  transition: all 0.2s;
  line-height: 1;
  filter: grayscale(1);
  opacity: 0.55;
}
.vote-btn:hover {
  background: #e8ecf1;
  filter: grayscale(0);
  opacity: 0.85;
}
.vote-btn.is-active {
  background: var(--el-color-primary-light-9);
  filter: grayscale(0);
  opacity: 1;
}
.vote-btn.is-active.is-down {
  background: var(--el-color-danger-light-9);
}
.vote-emoji {
  font-size: 14px;
  line-height: 1;
}
.vote-num {
  font-weight: 600;
  font-size: 11px;
  color: var(--text-secondary);
  min-width: 12px;
}
.vote-btn.is-active .vote-num {
  color: var(--el-color-primary);
}
.vote-btn.is-active.is-down .vote-num {
  color: var(--el-color-danger);
}
.vote-btn-sm {
  padding: 2px 7px;
}
.vote-btn-sm .vote-emoji {
  font-size: 12px;
}

/* 内联回复表单 */
.inline-reply {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed var(--border-color);
}
.inline-reply :deep(.el-textarea__inner) {
  border-radius: var(--radius-sm);
  resize: none;
}
.inline-reply-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}
.reply-hint {
  font-size: var(--text-xs);
  color: var(--text-muted);
  margin-right: auto;
}

/* 嵌套回复 */
.replies-section {
  margin-top: 12px;
  padding-left: 44px;
  border-left: 2px solid var(--el-color-primary-light-7);
}
.reply-card {
  padding: 10px 14px;
  margin-top: 8px;
  background: #fafbfc;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.2s;
}
.reply-card:first-child {
  margin-top: 4px;
}
.reply-card:hover {
  background: #f1f5f9;
}
.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}
.reply-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--el-color-info-light-8);
}
.reply-avatar :deep(img) {
  width: 22px;
  height: 22px;
  object-fit: cover;
}
.reply-avatar :deep(.el-image__error) {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--el-color-info-light-8);
  display: flex;
  align-items: center;
  justify-content: center;
}
.reply-nickname {
  font-weight: 500;
  font-size: var(--text-xs);
  color: var(--text-primary);
}
.reply-sep {
  font-size: var(--text-xs);
  color: var(--text-muted);
}
.reply-target-name {
  font-size: var(--text-xs);
  color: var(--el-color-primary);
  font-weight: 500;
}
.reply-time {
  margin-left: auto;
  font-size: var(--text-xs);
  color: var(--text-muted);
}
.reply-content {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-primary);
  line-height: 1.5;
  word-break: break-word;
}

/* 回复态高亮当前评论卡片 */
.comment-card.is-replying {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
}

.comment-pagination {
  margin-top: 20px;
  justify-content: center;
}

@media (max-width: 768px) {
  .images-section {
    width: 100%;
  }
  .main-image {
    height: 280px;
    border-radius: var(--radius-lg);
    overflow: hidden;
  }
  .info-section {
    min-width: 0;
  }
  .comment-card {
    padding: 12px 14px;
  }
  .comment-section {
    margin-top: 32px;
    padding-top: 24px;
  }
  .replies-section {
    padding-left: 28px;
  }
  .reply-card {
    padding: 8px 10px;
  }
  .vote-btn {
    padding: 3px 10px;
  }
}
</style>
