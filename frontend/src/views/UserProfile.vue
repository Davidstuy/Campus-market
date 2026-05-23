<template>
  <div class="profile-page">
    <h2>个人资料</h2>

    <!-- Loading -->
    <div v-if="loading" class="profile-loading">
      <el-skeleton :rows="5" animated />
    </div>

    <!-- Error -->
    <div v-else-if="error" class="profile-error">
      <el-result icon="error" title="加载失败" :sub-title="error">
        <template #extra>
          <el-button type="primary" @click="fetchProfile">重新加载</el-button>
        </template>
      </el-result>
    </div>

    <!-- Success: form -->
    <el-form
      v-else
      ref="formRef"
      :model="form"
      label-width="100px"
      style="max-width: 500px"
    >
      <el-form-item label="头像">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="onAvatarSuccess"
          :before-upload="beforeAvatarUpload"
        >
          <el-avatar :size="64" :src="form.avatarUrl" />
          <span class="avatar-tip">点击更换头像</span>
        </el-upload>
      </el-form-item>

      <el-form-item label="用户名">
        <el-input :model-value="form.username" disabled />
      </el-form-item>

      <el-form-item label="昵称">
        <el-input v-model="form.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
      </el-form-item>

      <el-form-item label="手机号">
        <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
      </el-form-item>

      <el-form-item label="微信">
        <el-input v-model="form.wechat" placeholder="请输入微信号" maxlength="30" />
      </el-form-item>

      <el-form-item label="QQ">
        <el-input v-model="form.qq" placeholder="请输入QQ号" maxlength="15" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadProps } from 'element-plus'
import { userApi } from '@/api/modules/user'

const loading = ref(false)
const saving = ref(false)
const error = ref('')

const uploadUrl = '/api/v1/files/upload'
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` }

const form = reactive({
  username: '',
  nickname: '',
  avatarUrl: '',
  phone: '',
  wechat: '',
  qq: '',
})

/** 保存原始数据，用于取消时恢复 */
let originalForm = { ...form }

const fetchProfile = async () => {
  loading.value = true
  error.value = ''
  try {
    const data = await userApi.getProfile() as Record<string, string>
    Object.assign(form, data)
    originalForm = { ...form }
  } catch (e: any) {
    error.value = e?.response?.data?.message || '网络异常，请稍后重试'
  } finally {
    loading.value = false
  }
}

const beforeAvatarUpload: UploadProps['beforeUpload'] = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const onAvatarSuccess = (res: { data: { url: string } }) => {
  form.avatarUrl = res.data?.url || ''
  ElMessage.success('头像上传成功')
}

const handleSave = async () => {
  saving.value = true
  try {
    await userApi.updateProfile({
      nickname: form.nickname,
      avatarUrl: form.avatarUrl,
      phone: form.phone,
      wechat: form.wechat,
      qq: form.qq,
    })
    originalForm = { ...form }
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  Object.assign(form, originalForm)
  ElMessage.info('已恢复未保存的修改')
}

onMounted(fetchProfile)
</script>

<style scoped>
.profile-page {
  padding: 24px;
}

.profile-page h2 {
  margin-bottom: 24px;
}

.profile-loading {
  max-width: 500px;
}

.avatar-uploader {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-uploader .el-avatar {
  cursor: pointer;
  border: 2px dashed var(--el-border-color);
}

.avatar-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
