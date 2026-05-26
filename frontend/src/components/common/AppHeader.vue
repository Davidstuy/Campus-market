<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="logo">校园二手交易</router-link>
      <nav class="nav-links">
        <router-link to="/products">浏览商品</router-link>
      </nav>
      <div class="user-area">
        <template v-if="auth.isLoggedIn()">
          <el-dropdown trigger="click">
            <span class="user-trigger">
              <el-avatar :size="32" :src="auth.user?.avatarUrl" />
              <span>{{ auth.user?.nickname || auth.user?.username }}</span>
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
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

const handleLogout = () => {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  padding: 0 24px;
  height: 60px;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
}
.logo {
  font-size: 18px;
  font-weight: 700;
  color: #3b82f5;
  text-decoration: none;
  letter-spacing: 0.5px;
}
.nav-links {
  margin-left: 36px;
  display: flex;
  gap: 24px;
}
.nav-links a {
  color: #64748b;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.15s;
}
.nav-links a:hover {
  color: #3b82f5;
}
.nav-links .router-link-active {
  color: #3b82f5;
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
  font-size: 14px;
  color: #334155;
}
.user-trigger:hover {
  color: #3b82f5;
}

/* 移动端 */
@media (max-width: 768px) {
  .app-header {
    padding: 0 12px;
    height: 52px;
  }
  .header-inner {
    height: 52px;
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
