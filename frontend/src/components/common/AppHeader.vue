<template>
  <header class="app-header" :class="{ 'is-scrolled': isScrolled }">
    <div class="header-inner">
      <router-link to="/" class="logo">校园二手交易</router-link>
      <nav class="nav-links">
        <router-link to="/products">浏览商品</router-link>
        <router-link to="/community">社区</router-link>
        <router-link v-if="auth.user?.role === 'ADMIN'" to="/admin/dashboard" class="admin-link">
          管理后台
        </router-link>
      </nav>
      <div class="user-area">
        <template v-if="auth.isLoggedIn()">
          <NotificationBell />
          <span class="icon-label" @click="$router.push('/notifications')">通知</span>
          <el-badge :value="notificationStore.chatUnreadCount" :max="99" :hidden="notificationStore.chatUnreadCount === 0">
            <el-icon :size="20" class="chat-nav-icon" @click="$router.push('/chat')">
              <ChatDotRound />
            </el-icon>
          </el-badge>
          <span class="icon-label" @click="$router.push('/chat')">私信</span>
          <el-dropdown trigger="click">
            <span class="user-trigger">
              <el-avatar :size="32" :src="auth.user?.avatarUrl" />
              <span>{{ auth.user?.nickname || auth.user?.username }}</span>
              <el-icon class="chevron"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <router-link to="/profile">个人资料</router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/profile/listings">我的发布</router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/profile/favorites">我的收藏</router-link>
                </el-dropdown-item>
                 <el-dropdown-item>
                  <router-link  to="/publish">发布商品</router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/orders">我的订单</router-link>
                </el-dropdown-item>
                <el-dropdown-item>
                  <router-link to="/profile/sales">我的卖出</router-link>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" size="small" @click="$router.push('/login')">登录</el-button>
          <el-button size="small" @click="$router.push('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ArrowDown, ChatDotRound } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import { useRouter } from 'vue-router'
import { useScrollObserver } from '@/composables/useScrollObserver'
import NotificationBell from '@/components/notification/NotificationBell.vue'

const auth = useAuthStore()
const notificationStore = useNotificationStore()
const router = useRouter()
const { isScrolled } = useScrollObserver(10)

const handleLogout = () => {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--bg-glass);
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 1px 0 0 rgba(255, 255, 255, 0.8) inset,
              0 1px 3px rgba(0, 0, 0, 0.04);
  padding: 0 var(--page-padding);
  height: var(--header-height);
  transition: box-shadow var(--transition-base);
}
.app-header.is-scrolled {
  box-shadow: 0 1px 0 0 rgba(255, 255, 255, 0.8) inset,
              var(--shadow-md);
}

.header-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: var(--header-height);
}

.logo {
  font-size: 18px;
  font-weight: 700;
  color: var(--el-color-primary);
  text-decoration: none;
  letter-spacing: 0.5px;
}

.nav-links {
  margin-left: 36px;
  display: flex;
  gap: 24px;
}
.nav-links a {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: var(--text-base);
  font-weight: 500;
  transition: color var(--transition-fast);
  position: relative;
  padding-bottom: 4px;
}
.nav-links a::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  border-radius: 1px;
  background: var(--gradient-primary);
  transform: scaleX(0);
  transition: transform var(--transition-fast);
}
.nav-links a:hover::after {
  transform: scaleX(1);
}
.nav-links a:hover {
  color: var(--el-color-primary);
}
.nav-links .router-link-active {
  color: var(--el-color-primary);
}
.nav-links .router-link-active::after {
  transform: scaleX(1);
}

.user-area {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: var(--text-base);
  color: var(--text-primary);
  transition: color var(--transition-fast);
}
.user-trigger:hover {
  color: var(--el-color-primary);
}
.user-trigger:hover :deep(.el-avatar) {
  box-shadow: 0 0 0 2px var(--el-color-primary-light-5);
}
.user-trigger :deep(.el-avatar) {
  transition: box-shadow var(--transition-fast);
}
.chevron {
  font-size: 12px;
  transition: transform var(--transition-fast);
}
.user-trigger:hover .chevron {
  transform: translateY(2px);
}

.chat-nav-icon {
  cursor: pointer;
  color: var(--text-secondary);
  transition: color var(--transition-fast);
}
.chat-nav-icon:hover {
  color: var(--el-color-primary);
}

.icon-label {
  font-size: 11px;
  color: var(--text-muted);
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
}
.icon-label:hover {
  color: var(--el-color-primary);
}

/* 移动端 */
@media (max-width: 768px) {
  .app-header {
    padding: 0 12px;
    height: 56px;
  }
  .header-inner {
    height: 56px;
  }
  .logo {
    font-size: 15px;
  }
  .nav-links {
    margin-left: 14px;
    gap: 12px;
    font-size: 13px;
  }
  .user-area .el-button {
    padding: 4px 10px;
    font-size: 12px;
  }
  .user-trigger span {
    display: none;
  }
}
</style>
