<template>
  <div>
    <app-header />
    <app-body>
      <div class="release-container">
        <div class="search-section">
          <div class="search-content">
            <div class="search-title">
              <h1>发布二手书籍</h1>
              <p class="subtitle">让闲置的书籍流转起来，为知识赋予新的生命</p>
            </div>
          </div>
          <div class="search-decoration"></div>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="100px"
          class="release-form"
        >
          <div class="form-section hover-effect">
            <div class="section-icon">
              <el-icon><Document /></el-icon>
            </div>
            <h3 class="section-title">基本信息</h3>
            <!-- 书籍名称 -->
            <el-form-item label="书籍名称" prop="idleName">
              <el-input 
                v-model="form.idleName" 
                placeholder="请输入书籍名称"
                maxlength="50"
                show-word-limit
              >
                <template #prefix>
                  <el-icon><Reading /></el-icon>
                </template>
              </el-input>
            </el-form-item>

            <div class="form-row">
              <!-- 书籍分类 -->
              <el-form-item label="书籍分类" prop="idleLabel">
                <el-select 
                  v-model="form.idleLabel" 
                  placeholder="请选择书籍分类"
                  class="category-select"
                >
                  <el-option
                    v-for="item in typeList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>

              <!-- 价格 -->
              <el-form-item label="价格" prop="idlePrice">
                <el-input-number 
                  v-model="form.idlePrice"
                  :min="0"
                  :precision="2"
                  :step="0.1"
                  class="price-input"
                  :controls="false"
                >
                  <template #prefix>
                    <span class="price-prefix">￥</span>
                  </template>
                </el-input-number>
              </el-form-item>
            </div>

            <!-- 地区 -->
            <el-form-item label="所在地区" prop="idlePlace">
              <el-cascader
                v-model="selectedRegion"
                :options="options"
                :props="cascaderProps"
                placeholder="请选择所在地区"
                clearable
                class="region-select"
                @change="handleRegionChange"
              />
            </el-form-item>
          </div>

          <div class="form-section hover-effect">
            <div class="section-icon">
              <el-icon><Edit /></el-icon>
            </div>
            <h3 class="section-title">详细描述</h3>
            <el-form-item prop="idleDetails">
              <el-input
                v-model="form.idleDetails"
                type="textarea"
                :rows="6"
                placeholder="请详细描述书籍的成色、使用情况等信息，让买家更好地了解商品"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </div>

          <div class="form-section hover-effect">
            <div class="section-icon">
              <el-icon><Picture /></el-icon>
            </div>
            <h3 class="section-title">书籍图片</h3>
            <el-form-item label="商品图片" prop="pictureList">
              <el-upload
                v-model="fileList"
                :action="uploadConfig.action"
                :headers="uploadConfig.headers"
                :data="uploadConfig.data"
                list-type="picture-card"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :on-remove="handleRemove"
                :on-preview="handlePictureCardPreview"
                accept="image/*"
                multiple
              >
                <el-icon><Plus /></el-icon>
              </el-upload>
              <el-dialog v-model="dialogVisible">
                <img w-full :src="dialogImageUrl" alt="Preview Image" />
              </el-dialog>
            </el-form-item>
          </div>

          <div class="form-actions">
            <el-button 
              type="primary" 
              @click="submitForm" 
              :loading="submitting"
              size="large"
              class="submit-button"
            >
              <el-icon><Check /></el-icon>
              发布书籍
            </el-button>
            <el-button 
              @click="resetForm"
              size="large"
              class="reset-button"
            >
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </div>
        </el-form>

        <el-dialog
          v-model="dialogVisible"
          width="50%"
          center
          destroy-on-close
          :close-on-click-modal="true"
          :show-close="true"
        >
          <div class="preview-container">
            <img :src="dialogImageUrl" alt="Preview Image" class="preview-image" />
          </div>
        </el-dialog>
      </div>
      <app-footer />
    </app-body>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, InfoFilled, Document, Edit, Picture, Reading, Check, Refresh } from '@element-plus/icons-vue'
import AppHeader from '@/components/common/AppHeader.vue'
import AppBody from '@/components/common/AppPageBody.vue'
import AppFooter from '@/components/common/AppFoot.vue'
import api from '@/api'
import options from '@/components/common/country-data'
import { useGlobalStore } from '@/stores'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const typeList = ref([])
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const selectedRegion = ref([])
const store = useGlobalStore()
const fileList = ref([])
const uploadRef = ref(null)

// 表单数据
const form = ref({
  idleName: '',
  idleLabel: '',
  idlePrice: 0,
  idlePlace: '',
  idleDetails: '',
  pictureList: []
})

// 表单验证规则
const rules = {
  idleName: [
    { required: true, message: '请输入书籍名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  idleLabel: [
    { required: true, message: '请选择书籍分类', trigger: 'change' }
  ],
  idlePrice: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格必须大于等于0', trigger: 'blur' }
  ],
  idlePlace: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  idleDetails: [
    { required: true, message: '请输入详细描述', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  pictureList: [
    { required: true, validator: (rule, value, callback) => {
      if (!form.value.pictureList || form.value.pictureList.length === 0) {
        callback(new Error('请至少上传一张图片'))
      } else {
        callback()
      }
    }, trigger: 'change' }
  ]
}

// 获取分类列表
const getTypeList = async () => {
  try {
    const res = await api.listType({ begin: 0, size: 999 })
    if (res.status_code === 1) {
      typeList.value = res.data
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 修改重置表单方法
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields() // 重置表单字段
    form.value.pictureList = [] // 清空图片列表
    selectedRegion.value = [] // 清空地区选择
    fileList.value = [] // 清空文件列表
    
    // 使用 upload 组件的 clearFiles 方法清空
    if (uploadRef.value) {
      uploadRef.value.clearFiles()
    }
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (selectedRegion.value && selectedRegion.value.length > 0) {
      form.value.idlePlace = selectedRegion.value.map(item => item).join('/')
    }

    // 从 localStorage 获取用户信息
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    
    // 处理图片路径列表
    const processedPictureList = form.value.pictureList.map(url => {
      // 如果是完整URL，提取相对路径部分
      if (url.startsWith('http')) {
        const urlObj = new URL(url)
        let pathname = urlObj.pathname
        // 移除多余的/file前缀
        if (pathname.startsWith('/file/')) {
          pathname = pathname.replace('/file/', '/')
        }
        return pathname
      }
      // 移除多余的/file前缀
      if (url.startsWith('/file/')) {
        url = url.replace('/file/', '/')
      }
      // 确保以/开头
      return url.startsWith('/') ? url : `/${url}`
    })
    
    const params = {
      idleName: form.value.idleName,
      idleLabel: parseInt(form.value.idleLabel),
      idlePrice: parseFloat(form.value.idlePrice),
      idlePlace: form.value.idlePlace,
      idleDetails: form.value.idleDetails,
      pictureList: JSON.stringify(processedPictureList),
      userId: userInfo.id
    }
    
    const res = await api.addIdleItem(params)
    
    if (res.status_code === 1) {
      ElMessage.success('发布成功！')
      resetForm()
      router.push('/')
    } else {
      ElMessage.error(res.msg || '发布失败')
    }
  } catch (error) {
    // 发布失败
    ElMessage.error('发布失败，请检查填写内容')
  } finally {
    submitting.value = false
  }
}

// 图片上传相关配置
const uploadConfig = {
  action: store.baseApi + '/file/upload',
  headers: {
    token: store.token
  },
  data: {
    type: 'idle'
  }
}

// 图片上传成功
const handleUploadSuccess = (response, uploadFile) => {
  if (response.status_code === 1) {
    // 处理图片路径，移除多余的/file前缀
    let imageUrl = response.data
    if (imageUrl.startsWith('/file/')) {
      imageUrl = imageUrl.replace('/file/', '/')
    }
    if (!imageUrl.startsWith('/')) {
      imageUrl = `/${imageUrl}`
    }
    
    form.value.pictureList.push(imageUrl)
    
    // 用于显示的完整URL
    const fullUrl = `${store.baseApi}${imageUrl}`
    fileList.value.push({
      name: imageUrl.split('/').pop(),
      url: fullUrl
    })
  } else {
    ElMessage.error('图片上传失败')
  }
}

// 图片上传失败
const handleUploadError = () => {
  ElMessage.error('图片上传失败')
}

// 图片预览
const handlePictureCardPreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url
  dialogVisible.value = true
}

// 移除图片
const handleRemove = (uploadFile) => {
  const index = fileList.value.findIndex(file => file.url === uploadFile.url)
  if (index !== -1) {
    fileList.value.splice(index, 1)
    form.value.pictureList.splice(index, 1)
  }
}

// 上传前验证
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 地区选择器配置
const cascaderProps = {
  expandTrigger: 'hover',
  value: 'label',
  label: 'label',
  children: 'children',
  checkStrictly: false,  // 不允许选择任意一级
  multiple: false,       // 单选
  emitPath: true        // 返回完整路径
}

// 添加地区选择变化处理方法
const handleRegionChange = (value) => {
  if (value && value.length > 0) {
    // 只取最后一级地区名称
    form.value.idlePlace = value[value.length - 1]
  } else {
    form.value.idlePlace = ''
  }
}

// 监听 pictureList 变化，更新 fileList
watch(() => form.value.pictureList, (newVal) => {
  fileList.value = newVal.map(url => ({
    url: url.startsWith('http') ? url : `${store.baseApi}${url}`,
    name: url.split('/').pop()
  }))
}, { deep: true })

onMounted(() => {
  getTypeList()
})
</script>

<style scoped>
.release-container {
  padding: 24px;
  min-height: 85vh;
  background: #f5f5f5;
}

/* 搜索区域样式 */
.search-section {
  position: relative;
  padding: 60px 0;
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%,
    rgba(255, 255, 255, 0.85) 100%
  );
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  margin: 20px 0 30px 0;
  box-shadow: 
    0 8px 20px rgba(0, 0, 0, 0.05),
    0 12px 28px rgba(0, 0, 0, 0.03);
  transform: translateY(0);
  transition: all 0.3s;
  overflow: hidden;
}

.search-content {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
  position: relative;
  z-index: 2;
}

.search-title {
  margin-bottom: 32px;
}

.search-title h1 {
  font-size: 32px;
  color: #2c3e50;
  margin-bottom: 12px;
  font-weight: 600;
  background: linear-gradient(45deg, #409EFF, #36cfc9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.subtitle {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
}

.search-decoration {
  position: absolute;
  top: -50%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: radial-gradient(
    circle,
    rgba(64, 158, 255, 0.1) 0%,
    rgba(54, 207, 201, 0.05) 50%,
    transparent 70%
  );
  border-radius: 50%;
  z-index: 1;
  animation: float 8s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

/* 响应式布局调整 */
@media screen and (max-width: 768px) {
  .release-container {
    padding: 16px;
  }

  .search-section {
    padding: 40px 0;
    margin: 10px 0 20px 0;
  }

  .search-title h1 {
    font-size: 28px;
  }

  .subtitle {
    font-size: 14px;
    padding: 0 20px;
  }
}

.release-form {
  max-width: 800px;
  margin: 0 auto;
  padding: 0;
  background: transparent;
}

.form-section {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%,
    rgba(255, 255, 255, 0.85) 100%
  );
  backdrop-filter: blur(10px);
  padding: 32px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 
    0 8px 20px rgba(0, 0, 0, 0.05),
    0 12px 28px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.8);
  transition: all 0.3s ease;
  position: relative;
}

.hover-effect:hover {
  transform: translateY(-4px);
  box-shadow: 
    0 12px 24px rgba(0, 0, 0, 0.08),
    0 16px 32px rgba(0, 0, 0, 0.06);
}

.section-icon {
  position: absolute;
  top: -15px;
  left: 32px;
  width: 30px;
  height: 30px;
  background: linear-gradient(135deg, #409EFF 0%, #36cfc9 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
  padding-left: 40px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #1890ff;
}

:deep(.el-input__prefix-inner) {
  color: #1890ff;
}

.price-prefix {
  color: #1890ff;
  font-weight: bold;
  margin-right: 4px;
}

.upload-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

.upload-tip .el-icon {
  color: #909399;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #666;
}

.upload-icon {
  font-size: 24px;
  color: #1890ff;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}

.submit-button {
  background: linear-gradient(135deg, #409EFF 0%, #36cfc9 100%);
  border: none;
  padding: 0 32px;
  backdrop-filter: blur(4px);
}

.reset-button {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(200, 200, 200, 0.3);
}

:deep(.el-button .el-icon) {
  margin-right: 6px;
}

/* 添加预览图片样式 */
.preview-image {
  width: 100%;
  height: auto;
  max-height: 70vh;
  object-fit: contain;
}

/* 修改上传组件样式 */
:deep(.el-upload--picture-card) {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  border: 1px dashed rgba(200, 200, 200, 0.5);
  border-radius: 12px;
  transition: all 0.3s;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(200, 200, 200, 0.3);
  border-radius: 12px;
}

/* 添加/修改预览相关样式 */
.preview-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 300px;
  background: #f5f7fa;
  border-radius: 4px;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

:deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

:deep(.el-dialog__body) {
  padding: 16px;
}
</style> 