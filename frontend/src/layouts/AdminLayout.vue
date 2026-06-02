<template>
  <div class="admin-layout">
    <AppHeader />
    <div class="admin-body">
      <aside class="admin-sidebar">
        <h3 class="sidebar-title">管理后台</h3>
        <el-menu
          :default-active="activeMenu"
          router
          class="admin-menu"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据看板</span>
          </el-menu-item>
          <el-menu-item index="/admin/review">
            <el-icon><Checked /></el-icon>
            <span>商品审核</span>
          </el-menu-item>
          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/categories">
            <el-icon><Grid /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/faqs">
            <el-icon><QuestionFilled /></el-icon>
            <span>FAQ 管理</span>
          </el-menu-item>
        </el-menu>
        <div class="back-link">
          <el-button text @click="$router.push('/')">
            <el-icon><ArrowLeft /></el-icon> 返回前台
          </el-button>
        </div>
      </aside>
      <main class="admin-main">
        <router-view />
      </main>
    </div>
    <AppFooter />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import AppFooter from '@/components/common/AppFooter.vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.admin-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.admin-body {
  display: flex;
  flex: 1;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
  padding: 20px;
  gap: 20px;
}

.admin-sidebar {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 8px;
  padding: 16px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
}

.sidebar-title {
  font-size: 16px;
  color: #333;
  padding: 0 20px 12px;
  border-bottom: 1px solid #eee;
  margin: 0;
}

.admin-menu {
  border-right: none;
  flex: 1;
}

.back-link {
  padding: 12px 20px;
  border-top: 1px solid #eee;
}

.admin-main {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  min-height: 500px;
}
</style>
