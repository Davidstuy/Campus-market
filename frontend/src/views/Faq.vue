<template>
  <div class="faq-page">
    <!-- 装饰背景 -->
    <div class="bg-decoration">
      <div class="bg-blob bg-blob--1" />
      <div class="bg-blob bg-blob--2" />
      <div class="bg-dots" />
    </div>

    <!-- Hero -->
    <header class="faq-hero">
      <div class="hero-badge">
        <span class="badge-dot" />
        帮助中心
      </div>
      <h1 class="hero-title">
        有什么可以
        <span class="hero-highlight">帮你</span>的？
      </h1>
      <p class="hero-desc">选一个你想了解的问题，或者搜一下关键词</p>
      <!-- 搜索栏 -->
      <div class="hero-search">
        <el-icon class="search-icon" :size="18"><Search /></el-icon>
        <input
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="搜索问题，如「安全」「发布」「退款」…"
          @input="onSearchInput"
        />
        <el-button
          v-if="keyword"
          class="search-clear"
          text
          circle
          size="small"
          @click="keyword = ''"
        >
          <el-icon :size="14"><Close /></el-icon>
        </el-button>
      </div>
    </header>

    <!-- 加载态 -->
    <div v-if="loading" class="faq-body">
      <div v-for="n in 5" :key="n" class="skeleton-card">
        <div class="skeleton-bar skeleton-bar--long" />
        <div class="skeleton-bar skeleton-bar--short" />
      </div>
    </div>

    <!-- 错误态 -->
    <div v-else-if="error" class="faq-body faq-state">
      <div class="state-icon state-icon--error">
        <el-icon :size="36"><WarnTriangleFilled /></el-icon>
      </div>
      <h3>加载失败</h3>
      <p>网络似乎不太稳定，检查一下再试试</p>
      <el-button type="primary" round size="large" @click="loadFaqs">
        重新加载
      </el-button>
    </div>

    <!-- 空态 -->
    <div v-else-if="filteredList.length === 0 && !keyword" class="faq-body faq-state">
      <div class="state-icon state-icon--empty">
        <el-icon :size="36"><Document /></el-icon>
      </div>
      <h3>暂无常见问题</h3>
      <p>管理员正在整理中，稍后再来看看</p>
    </div>

    <!-- 搜索无结果 -->
    <div v-else-if="filteredList.length === 0 && keyword" class="faq-body faq-state">
      <div class="state-icon state-icon--empty">
        <el-icon :size="36"><Search /></el-icon>
      </div>
      <h3>没找到相关结果</h3>
      <p>试试换一个关键词，比如「交易」「审核」「订单」</p>
      <el-button text type="primary" @click="keyword = ''">清除搜索</el-button>
    </div>

    <!-- 数据态 -->
    <div v-else class="faq-body">
      <div
        v-for="(item, index) in filteredList"
        :key="item.id"
        class="faq-card"
        :class="{ expanded: expandedId === item.id }"
        :style="{ animationDelay: `${index * 60}ms` }"
      >
        <button
          class="faq-card-trigger"
          @click="toggle(item.id)"
        >
          <span class="faq-card-num">{{ String(index + 1).padStart(2, '0') }}</span>
          <span class="faq-card-question">{{ item.question }}</span>
          <span class="faq-card-chevron" :class="{ open: expandedId === item.id }">
            <el-icon :size="18"><ArrowDown /></el-icon>
          </span>
        </button>
        <div class="faq-card-body" :ref="el => setAnswerRef(item.id, el)">
          <div class="faq-card-answer">
            <div class="answer-accent" />
            <p>{{ item.answer }}</p>
          </div>
        </div>
      </div>

      <!-- 底部提示 -->
      <div class="faq-footer">
        <div class="footer-divider" />
        <p>
          还没找到答案？
          <router-link to="/guide" class="footer-link">查看交易指南</router-link>
          或
          <a href="#" class="footer-link" @click.prevent="contactAdmin">联系客服</a>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Close, ArrowDown, WarnTriangleFilled, Document } from '@element-plus/icons-vue'
import { faqApi } from '@/api/modules/faq'
import { chatApi } from '@/api/modules/chat'
import { ElMessage } from 'element-plus'
import type { Faq } from '@/types'

const router = useRouter()

const loading = ref(true)
const error = ref('')
const list = ref<Faq[]>([])
const keyword = ref('')
const expandedId = ref<number | null>(null)
const answerRefs: Record<number, HTMLElement | null> = {}

const filteredList = computed(() => {
  if (!keyword.value.trim()) return list.value
  const kw = keyword.value.trim().toLowerCase()
  return list.value.filter(
    item =>
      item.question.toLowerCase().includes(kw) ||
      item.answer.toLowerCase().includes(kw)
  )
})

function setAnswerRef(id: number, el: unknown) {
  answerRefs[id] = el as HTMLElement | null
}

const toggle = async (id: number) => {
  if (expandedId.value === id) {
    // 收起
    const body = answerRefs[id]
    if (body) {
      body.style.maxHeight = body.scrollHeight + 'px'
      requestAnimationFrame(() => {
        body.style.maxHeight = '0px'
      })
    }
    expandedId.value = null
  } else {
    // 先收起上一个
    if (expandedId.value !== null) {
      const prev = answerRefs[expandedId.value]
      if (prev) {
        prev.style.maxHeight = '0px'
      }
    }
    expandedId.value = id
    await nextTick()
    const body = answerRefs[id]
    if (body) {
      body.style.maxHeight = body.scrollHeight + 'px'
    }
  }
}

// 搜索时自动展开第一条匹配项
const onSearchInput = () => {
  if (filteredList.value.length > 0 && keyword.value.trim()) {
    expandedId.value = filteredList.value[0].id
  } else {
    expandedId.value = null
  }
}

const loadFaqs = async () => {
  loading.value = true
  error.value = ''
  try {
    list.value = await faqApi.list()
  } catch {
    error.value = '加载失败'
  } finally {
    loading.value = false
  }
}

const contactAdmin = async () => {
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

onMounted(loadFaqs)
</script>

<style scoped>
/* ============================================
   FAQ Page — Warm Editorial Handbook Aesthetic
   ============================================ */

.faq-page {
  position: relative;
  max-width: 720px;
  margin: 0 auto;
  padding: 48px var(--page-padding) 80px;
  overflow: hidden;
}

/* ── 装饰背景 ── */
.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}
.bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.12;
}
.bg-blob--1 {
  width: 360px;
  height: 360px;
  top: -100px;
  right: -120px;
  background: var(--el-color-primary);
}
.bg-blob--2 {
  width: 240px;
  height: 240px;
  bottom: 200px;
  left: -80px;
  background: #f59e0b;
}
.bg-dots {
  position: absolute;
  top: 60px;
  right: 20px;
  width: 120px;
  height: 120px;
  background-image: radial-gradient(circle, rgba(79, 110, 247, 0.15) 1.5px, transparent 1.5px);
  background-size: 16px 16px;
}

/* ── Hero ── */
.faq-hero {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 0 0 40px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 16px;
  background: var(--el-color-primary-light-9);
  border-radius: 100px;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-color-primary);
  margin-bottom: 20px;
  letter-spacing: 0.02em;
}
.badge-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--el-color-primary);
  animation: badgePulse 2s ease-in-out infinite;
}
@keyframes badgePulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.3); }
}

.hero-title {
  font-size: 36px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 12px;
  line-height: 1.25;
  letter-spacing: -0.02em;
}
.hero-highlight {
  position: relative;
  display: inline-block;
}
.hero-highlight::after {
  content: '';
  position: absolute;
  bottom: 4px;
  left: -2px;
  right: -2px;
  height: 10px;
  background: rgba(79, 110, 247, 0.18);
  border-radius: 4px;
  z-index: -1;
}

.hero-desc {
  font-size: 16px;
  color: var(--text-muted);
  margin: 0 0 28px;
  line-height: 1.5;
}

/* ── 搜索栏 ── */
.hero-search {
  position: relative;
  max-width: 440px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  background: var(--bg-white);
  border: 2px solid var(--border-color);
  border-radius: 100px;
  padding: 0 16px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.hero-search:focus-within {
  border-color: var(--el-color-primary);
  box-shadow: 0 0 0 4px rgba(79, 110, 247, 0.08);
}
.search-icon {
  color: var(--text-muted);
  flex-shrink: 0;
  transition: color 0.3s;
}
.hero-search:focus-within .search-icon {
  color: var(--el-color-primary);
}
.search-input {
  flex: 1;
  border: none;
  outline: none;
  padding: 13px 10px;
  font-size: 15px;
  color: var(--text-primary);
  background: transparent;
  font-family: inherit;
}
.search-input::placeholder {
  color: var(--text-muted);
  font-size: 14px;
}
.search-clear {
  flex-shrink: 0;
  color: var(--text-muted);
}

/* ── FAQ Body ── */
.faq-body {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* ── FAQ Card ── */
.faq-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: cardIn 0.5s cubic-bezier(0.22, 0.61, 0.36, 1) both;
  box-shadow: var(--shadow-xs);
}
.faq-card:hover {
  border-color: #cbd5e1;
  box-shadow: var(--shadow-sm);
}
.faq-card.expanded {
  border-color: var(--el-color-primary-light-5);
  box-shadow: var(--shadow-md), 0 0 0 3px rgba(79, 110, 247, 0.04);
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ── Card Trigger ── */
.faq-card-trigger {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border: none;
  background: none;
  cursor: pointer;
  font-family: inherit;
  text-align: left;
  transition: background 0.2s;
}
.faq-card-trigger:hover {
  background: var(--bg-page);
}

.faq-card-num {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
  letter-spacing: 0.06em;
  font-variant-numeric: tabular-nums;
  flex-shrink: 0;
  width: 24px;
  text-align: center;
  transition: color 0.3s;
}
.faq-card.expanded .faq-card-num {
  color: var(--el-color-primary);
}

.faq-card-question {
  flex: 1;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
  transition: color 0.3s;
}
.faq-card.expanded .faq-card-question {
  color: var(--el-color-primary);
}

.faq-card-chevron {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--bg-page);
  color: var(--text-muted);
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}
.faq-card-chevron.open {
  transform: rotate(180deg);
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

/* ── Card Body (collapsible) ── */
.faq-card-body {
  max-height: 0;
  overflow: hidden;
  transition: max-height 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.faq-card-answer {
  display: flex;
  gap: 14px;
  padding: 0 20px 20px 58px;
}
.answer-accent {
  width: 3px;
  flex-shrink: 0;
  align-self: stretch;
  background: var(--el-color-primary-light-7);
  border-radius: 2px;
}
.faq-card-answer p {
  margin: 0;
  font-size: 14px;
  line-height: 1.85;
  color: var(--text-secondary);
}

/* ── Footer ── */
.faq-footer {
  text-align: center;
  padding-top: 28px;
}
.footer-divider {
  width: 40px;
  height: 3px;
  background: var(--border-color);
  border-radius: 2px;
  margin: 0 auto 20px;
}
.faq-footer p {
  margin: 0;
  font-size: 14px;
  color: var(--text-muted);
}
.footer-link {
  color: var(--el-color-primary);
  font-weight: 600;
  text-decoration: none;
  transition: opacity 0.2s;
}
.footer-link:hover {
  opacity: 0.75;
}

/* ── State Views ── */
.faq-state {
  align-items: center;
  text-align: center;
  padding: 64px 0;
}
.state-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
}
.state-icon--error {
  background: #fef2f2;
  color: #ef4444;
}
.state-icon--empty {
  background: var(--bg-page);
  color: var(--text-muted);
}
.faq-state h3 {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}
.faq-state p {
  margin: 0 0 20px;
  font-size: 14px;
  color: var(--text-muted);
}

/* ── Skeleton ── */
.skeleton-card {
  background: var(--bg-white);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.skeleton-bar {
  height: 14px;
  border-radius: 4px;
  background: linear-gradient(90deg, #f1f5f9 25%, #e2e8f0 50%, #f1f5f9 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}
.skeleton-bar--long { width: 85%; }
.skeleton-bar--short { width: 45%; }
@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .faq-page {
    padding: 24px var(--page-padding) 60px;
  }
  .hero-title {
    font-size: 26px;
  }
  .hero-desc {
    font-size: 14px;
  }
  .hero-search {
    max-width: 100%;
  }
  .faq-card-trigger {
    padding: 16px 16px;
    gap: 10px;
  }
  .faq-card-question {
    font-size: 14px;
  }
  .faq-card-answer {
    padding: 0 16px 16px 42px;
  }
  .faq-card-answer p {
    font-size: 13px;
  }
}
</style>
