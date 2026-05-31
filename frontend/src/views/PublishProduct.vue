<template>
  <div class="publish-page">
    <h2 class="page-title">{{ isEdit ? '编辑商品' : '发布商品' }}</h2>

    <template v-if="loading || editLoading">
      <el-skeleton :rows="6" animated style="max-width: 700px" />
    </template>

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

    <div v-else class="publish-card">
      <el-form
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
            {{ submitting ? (isEdit ? '保存中...' : '发布中...') : (isEdit ? '保存修改' : '发布') }}
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { FormInstance, FormRules, UploadFile, UploadUserFile } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { productApi } from '@/api/modules/product'
import { categoryApi } from '@/api/modules/category'
import type { Category } from '@/types'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const loading = ref(true)
const loadError = ref(false)
const editLoading = ref(false)
const categories = ref<Category[]>([])

const isEdit = computed(() => !!route.params.id)
const editId = computed(() => Number(route.params.id) || null)

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

const coverFileList = ref<UploadUserFile[]>([])
const imageFileList = ref<UploadUserFile[]>([])

const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
}

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
  // 同时支持新上传的图片（response.data.url）和已有图片（url 属性）
  form.images = fileList
    .map(f => (f.response as { data: { url: string } } | undefined)?.data?.url || f.url || '')
    .filter(Boolean)
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!form.coverImage && form.images.length > 0) {
    form.coverImage = form.images[0]
  }

  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      await productApi.update(editId.value, {
        title: form.title,
        price: form.price,
        categoryId: form.categoryId!,
        description: form.description,
        coverImage: form.coverImage,
        images: form.images,
        contactWechat: form.contactWechat,
        contactQq: form.contactQq,
      })
      ElMessage.success('保存成功')
    } else {
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
    }
    router.push('/')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    submitting.value = false
  }
}

const loadProduct = async () => {
  editLoading.value = true
  try {
    const product = await productApi.detail(editId.value!)
    form.title = product.title
    form.categoryId = product.categoryId
    form.price = product.price
    form.description = product.description || ''
    form.coverImage = product.coverImage || ''
    form.images = product.images?.map(img => img.url) || []
    form.contactWechat = product.contactWechat || ''
    form.contactQq = product.contactQq || ''

    // 构建封面图 file-list（让 el-upload 显示已有图片）
    if (product.coverImage) {
      coverFileList.value = [{ name: 'cover', url: product.coverImage } as UploadUserFile]
    }
    // 构建商品图片 file-list
    imageFileList.value = product.images?.map(img => ({
      name: `img_${img.id}`,
      url: img.url,
    } as UploadUserFile)) || []
  } catch {
    loadError.value = true
  } finally {
    editLoading.value = false
  }
}

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

onMounted(async () => {
  await fetchCategories()
  if (isEdit.value) {
    await loadProduct()
  }
})
</script>

<style scoped>
.page-title {
  font-size: var(--text-2xl);
  font-weight: 700;
  margin-bottom: 24px;
  letter-spacing: -0.3px;
}

.publish-card {
  background: var(--bg-white);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  padding: 32px;
  box-shadow: var(--shadow-sm);
}

@media (max-width: 768px) {
  .publish-card {
    padding: 16px;
  }
}
</style>
