<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="auth-form" @keyup.enter="handleLogin">
    <el-alert
      v-if="loginError"
      :title="loginError"
      type="error"
      show-icon
      :closable="false"
      class="auth-alert"
    />
    <el-form-item label="用户名" prop="username">
      <el-input v-model="form.username" placeholder="请输入用户名" />
    </el-form-item>
    <el-form-item label="密码" prop="password">
      <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" size="large" class="auth-submit-btn" :loading="loading" @click="handleLogin">
        登录
      </el-button>
    </el-form-item>
    <div class="form-footer">
      <span>还没有账号？</span>
      <router-link to="/register" class="form-link">
        立即注册 <el-icon><ArrowRight /></el-icon>
      </router-link>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const loginError = ref('')

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loginError.value = ''
  loading.value = true
  try {
    await auth.login(form)
    router.push((route.query.redirect as string) || '/')
  } catch (e: any) {
    loginError.value = e?.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-form :deep(.el-form-item) {
  margin-bottom: 22px;
}
.auth-form :deep(.el-form-item__label) {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--text-primary);
  padding-bottom: 4px;
}
.auth-form :deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  padding: 4px 12px;
}
.auth-form :deep(.el-input__inner) {
  height: 42px;
  font-size: var(--text-base);
}

.auth-alert {
  margin-bottom: 16px;
  border-radius: var(--radius-md);
}

.auth-submit-btn {
  width: 100%;
  height: 44px;
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  font-weight: 600;
  background: var(--gradient-primary);
  border: none;
  transition: all var(--transition-base);
}
.auth-submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-button-primary);
}

.form-footer {
  text-align: center;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 4px;
}
.form-link {
  color: var(--el-color-primary);
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  transition: gap var(--transition-fast);
}
.form-link:hover {
  gap: 6px;
  color: var(--el-color-primary-dark-2);
}
</style>
