<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
    <el-alert
      v-if="loginError"
      :title="loginError"
      type="error"
      show-icon
      :closable="false"
      style="margin-bottom: 16px"
    />
    <el-form-item label="用户名" prop="username">
      <el-input v-model="form.username" placeholder="请输入用户名" />
    </el-form-item>
    <el-form-item label="密码" prop="password">
      <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleLogin">
        登录
      </el-button>
    </el-form-item>
    <div class="form-footer">
      还没有账号？<router-link to="/register">立即注册</router-link>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
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
.form-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
}
.form-footer a {
  color: #409eff;
}
</style>
