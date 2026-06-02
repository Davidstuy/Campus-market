<template>
  <div class="create-layout">
    <div class="create-post-page">
    <h2 class="page-title">发布帖子</h2>

    <el-form label-position="top" class="post-form" @submit.prevent>
      <!-- 主题选择 -->
      <el-form-item label="选择主题" required>
        <div class="topic-selector">
          <el-radio-group v-model="form.topicId" class="topic-radio-group">
            <el-radio-button
              v-for="t in topics"
              :key="t.id"
              :value="t.id"
            >
              <el-icon v-if="t.icon" class="topic-radio-icon"><component :is="t.icon" /></el-icon>
              {{ t.name }}
            </el-radio-button>
          </el-radio-group>
        </div>
      </el-form-item>

      <!-- 标题 -->
      <el-form-item label="标题" required>
        <el-input
          v-model="form.title"
          placeholder="一句话概括你想说的（最多200字）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 正文 -->
      <el-form-item label="正文" required>
        <div class="content-editor">
          <el-input
            v-model="contentRef"
            type="textarea"
            :rows="6"
            placeholder="分享你的想法... 支持 emoji 表情和图片/视频上传"
            class="content-textarea"
          />
          <div class="editor-toolbar">
            <EmojiPicker title="插入表情" @select="insertEmoji" />
          </div>
        </div>
      </el-form-item>

      <!-- 媒体上传 -->
      <el-form-item label="图片/视频（最多9个）">
        <el-upload
          ref="uploadRef"
          :action="uploadUrl"
          :headers="uploadHeaders"
          accept="image/*,video/*"
          list-type="picture-card"
          :file-list="uploadFiles"
          :limit="9"
          :on-success="onUploadSuccess"
          :on-remove="onUploadRemove"
          :before-upload="beforeUpload"
          multiple
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <p class="upload-tip">支持 jpg/png/gif/webp/mp4/mov，单文件最大 100MB</p>
      </el-form-item>

      <!-- 提交 -->
      <el-form-item>
        <el-button type="primary" size="large" :loading="submitting" @click="submitPost" class="submit-btn">
          发布
        </el-button>
        <el-button size="large" @click="$router.back()">取消</el-button>
      </el-form-item>
    </el-form>
    </div>
    <Sidebar show-hot />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { communityApi } from '@/api/modules/community'
import { useEmojiInsert } from '@/composables/useEmojiInsert'
import EmojiPicker from '@/components/common/EmojiPicker.vue'
import Sidebar from '@/components/common/Sidebar.vue'
import type { Topic } from '@/types'
import { Plus } from '@element-plus/icons-vue'

const router = useRouter()
const topics = ref<Topic[]>([])
const submitting = ref(false)

const form = reactive({
  topicId: 0,
  title: '',
  content: '',
  media: [] as string[],
})

const uploadFiles = ref<any[]>([])
const contentRef = ref('')
const { insertEmoji } = useEmojiInsert(contentRef)
watch(contentRef, (v) => { form.content = v })
watch(() => form.content, (v) => { if (v !== contentRef.value) contentRef.value = v })

const uploadUrl = '/api/v1/files/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`,
}))

onMounted(async () => {
  try {
    topics.value = await communityApi.listTopics()
  } catch {
    ElMessage.error('加载主题失败')
  }
})

function beforeUpload(file: File) {
  const isValidType = file.type.startsWith('image/') || file.type.startsWith('video/')
  if (!isValidType) {
    ElMessage.error('只支持图片和视频文件')
    return false
  }
  const isLt100M = file.size / 1024 / 1024 < 100
  if (!isLt100M) {
    ElMessage.error('文件大小不能超过 100MB')
    return false
  }
  return true
}

function onUploadSuccess(response: any) {
  if (response?.url) {
    form.media.push(response.url)
  } else if (response?.data?.url) {
    form.media.push(response.data.url)
  }
}

function onUploadRemove(_file: any) {
  const url = _file?.response?.url || _file?.response?.data?.url || _file?.url
  if (url) {
    const idx = form.media.indexOf(url)
    if (idx > -1) form.media.splice(idx, 1)
  }
}

async function submitPost() {
  if (!form.topicId) {
    ElMessage.warning('请选择主题')
    return
  }
  if (!form.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请输入正文内容')
    return
  }

  submitting.value = true
  try {
    const post = await communityApi.createPost({
      title: form.title.trim(),
      content: form.content.trim(),
      topicId: form.topicId,
      media: form.media,
    })
    ElMessage.success('发布成功')
    router.replace(`/community/${post.id}`)
  } catch {
    ElMessage.error('发布失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-layout {
  display: flex;
  gap: 20px;
  padding: 16px 1.5vw 80px;
}

.create-post-page {
  flex: 1;
  min-width: 0;
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 24px;
  color: var(--text-primary, #1a1a2e);
}

.post-form {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #f3f4f6;
}

.topic-selector {
  width: 100%;
}

.topic-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-radio-icon {
  font-size: 14px;
}

.content-editor {
  position: relative;
  width: 100%;
}

.content-textarea {
  width: 100%;
}

.editor-toolbar {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  padding: 4px 0;
}

.upload-tip {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 8px;
}

.submit-btn {
  min-width: 120px;
}
</style>
