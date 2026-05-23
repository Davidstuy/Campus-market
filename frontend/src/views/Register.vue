<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
    <el-alert
      v-if="registerError"
      :title="registerError"
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
    <el-form-item label="确认密码" prop="confirmPassword">
      <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="handleRegister">
        注册
      </el-button>
    </el-form-item>
    <div class="form-footer">
      已有账号？<router-link to="/login">立即登录</router-link>
    </div>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const registerError = ref('')

const form = reactive({
  username: '',
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
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' },
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
.form-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
}
.form-footer a {
  color: #409eff;
}
</style>
