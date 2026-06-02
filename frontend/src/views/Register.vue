<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="auth-form" @keyup.enter="handleRegister">
    <el-alert
      v-if="registerError"
      :title="registerError"
      type="error"
      show-icon
      :closable="false"
      class="auth-alert"
    />
    <el-form-item label="用户名" prop="username">
      <el-input v-model="form.username" placeholder="请输入用户名" />
    </el-form-item>
    <el-form-item label="手机号" prop="phone">
      <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
    </el-form-item>
    <el-form-item label="密码" prop="password">
      <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
    </el-form-item>
    <el-form-item label="确认密码" prop="confirmPassword">
      <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" size="large" class="auth-submit-btn" :loading="loading" @click="handleRegister">
        注册
      </el-button>
    </el-form-item>
    <div class="form-footer">
      <span>已有账号？</span>
      <router-link to="/login" class="form-link">
        立即登录 <el-icon><ArrowRight /></el-icon>
      </router-link>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const registerError = ref('')

const form = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

const validateConfirm = (_rule: unknown, value: string, callback: (e?: Error) => void) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { max: 9, message: '用户名长度需小于10', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' },
  ],
}

const handleRegister = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  registerError.value = ''
  loading.value = true
  try {
    await auth.register(form)
    router.push('/login')
  } catch (e: any) {
    registerError.value = e?.message || '注册失败，请重试'
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
