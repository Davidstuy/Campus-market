<template>
  <div class="publish-page">
    <h2>发布商品</h2>

    <!-- 加载态：分类下拉骨架 -->
    <template v-if="loading">
      <el-skeleton :rows="6" animated style="max-width: 700px" />
    </template>

    <!-- 错误态：分类加载失败 -->
    <el-result
      v-else-if="loadError"
      status="error"
      title="加载失败"
      sub-title="分类数据加载失败，请重试"
    >
      <template #extra>
        <el-button type="primary" @click="fetchCategories">重新加载</el-button>
      </template>
    </el-result>

    <!-- 正常态：表单 -->
    <el-form
      v-else
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      style="max-width: 700px"
    >
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入商品标题" maxlength="50" show-word-limit />
      </el-form-item>

      <el-form-item label="分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择分类">
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="价格" prop="price">
        <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" placeholder="请输入价格" />
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请描述商品成色、使用情况等"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="封面图">
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          :on-success="onCoverSuccess"
          :on-remove="onCoverRemove"
          :limit="1"
          :file-list="coverFileList"
          list-type="picture"
          accept="image/*"
        >
          <el-button type="primary">上传封面图</el-button>
          <template #tip>
            <div class="el-upload__tip">建议尺寸 800×800，支持 JPG/PNG/WebP</div>
          </template>
        </el-upload>
      </el-form-item>

      <el-form-item label="商品图片">
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          :on-success="onImageSuccess"
          :on-remove="onImageRemove"
          :file-list="imageFileList"
          list-type="picture-card"
          accept="image/*"
          multiple
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <div class="el-upload__tip">最多上传 9 张，第一张将作为封面展示</div>
      </el-form-item>

      <el-form-item label="微信">
        <el-input v-model="form.contactWechat" placeholder="选填，方便买家联系" />
      </el-form-item>

      <el-form-item label="QQ">
        <el-input v-model="form.contactQq" placeholder="选填" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ submitting ? '发布中...' : '发布' }}
        </el-button>
        <el-button @click="router.back()">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadFile, UploadUserFile } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { productApi } from '@/api/modules/product'
import { categoryApi } from '@/api/modules/category'
import type { Category } from '@/types'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const loading = ref(true)
const loadError = ref(false)
const categories = ref<Category[]>([])

const uploadUrl = '/api/v1/files/upload'
const uploadHeaders = { Authorization: `Bearer ${localStorage.getItem('token')}` }

const form = reactive({
  title: '',
  categoryId: null as number | null,
  price: 0,
  description: '',
  coverImage: '',
  images: [] as string[],
  contactWechat: '',
  contactQq: '',
})

// el-upload 的 file-list 用于控制展示
const coverFileList = ref<UploadUserFile[]>([])
const imageFileList = ref<UploadUserFile[]>([])

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
}

// el-upload 直接发请求，不经过 axios 拦截器
// 收到的是完整响应体 { code, message, data: { url: '...' } }
// 需要从 res.data.url 取 URL
const onCoverSuccess = (res: { data: { url: string } }) => {
  form.coverImage = res.data?.url || ''
}

const onCoverRemove = () => {
  form.coverImage = ''
}

const onImageSuccess = (res: { data: { url: string } }) => {
  form.images.push(res.data?.url || '')
}

const onImageRemove = (_file: UploadFile, fileList: UploadUserFile[]) => {
  form.images = fileList
    .map(f => (f.response as { data: { url: string } } | undefined)?.data?.url || '')
    .filter(Boolean)
}

// ========== 提交 ==========
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 如果有商品图片但没有封面图，用第一张商品图片作为封面
  if (!form.coverImage && form.images.length > 0) {
    form.coverImage = form.images[0]
  }

  submitting.value = true
  try {
    await productApi.create({
      title: form.title,
      price: form.price,
      categoryId: form.categoryId!,
      description: form.description,
      coverImage: form.coverImage,
      images: form.images,
      contactWechat: form.contactWechat,
      contactQq: form.contactQq,
    })
    ElMessage.success('发布成功')
    router.push('/')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitting.value = false
  }
}

// ========== 加载分类 ==========
const fetchCategories = async () => {
  loading.value = true
  loadError.value = false
  try {
    categories.value = await categoryApi.list()
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCategories()
})
</script>
