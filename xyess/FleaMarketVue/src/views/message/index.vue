<template>
  <div class="message-page">
    <app-header />
    <app-body>
      <div class="page-container">
        <div class="message-header">
          <div class="title">我的消息</div>
          <div class="message-count">共 {{ total }} 条消息</div>
        </div>
        
        <div class="message-content" v-loading="loading">
          <template v-if="messageList.length > 0">
            <div 
              v-for="(message, index) in messageList" 
              :key="index" 
              class="message-item"
              :class="{ 'unread': !message.isRead }"
            >
              <div class="user-info">
                <el-avatar
                  :size="40"
                  :src="getAvatarUrl(message.fromU?.id)"
                  @error="() => handleAvatarError(message.fromU?.id)"
                />
                <div class="username">{{ message.fromU?.nickname }}</div>
              </div>
              
              <div class="item-content">
                <!-- 商品信息 -->
                <div v-if="message.idle" class="product-info" @click="viewDetail(message)">
                  <el-image
                    class="product-image"
                    :src="getProductImage(message.idle)"
                    fit="cover"
                  >
                    <template #error>
                      <div class="image-slot">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div class="product-detail">
                    <div class="product-name">{{ message.idle.idleName }}</div>
                    <div class="product-price">￥{{ parseFloat(message.idle.idlePrice).toFixed(2) }}</div>
                  </div>
                </div>

                <!-- 消息内容 -->
                <div class="message-text">{{ message.content }}</div>
                
                <div class="message-footer">
                  <span class="message-time">{{ formatTime(message.createTime) }}</span>
                  <div class="message-actions">
                    <el-button 
                      type="primary" 
                      link
                      @click="showReplyForm(message)"
                    >
                      <el-icon><ChatLineRound /></el-icon>
                      回复
                    </el-button>
                    <el-button 
                      type="danger" 
                      link
                      @click="deleteMessage(message)"
                    >
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </div>
                </div>

                <!-- 回复表单 -->
                <div class="reply-form" v-if="message.showReply">
                  <el-input
                    v-model="message.replyContent"
                    class="reply-input"
                    placeholder="输入回复内容..."
                    @keyup.enter="handleReply(message)"
                  />
                  <div class="reply-actions">
                    <el-button
                      type="primary"
                      size="small"
                      :loading="message.replying"
                      @click="handleReply(message)"
                    >
                      发送
                    </el-button>
                    <el-button
                      size="small"
                      @click="hideReplyForm(message)"
                    >
                      取消
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <el-empty
            v-else
            description="暂无消息"
            :image-size="120"
          >
            <template #description>
              <p class="empty-text">还没有收到任何消息哦~</p>
            </template>
          </el-empty>
        </div>

        <div class="pagination" v-if="total > 0">
          <el-pagination
            background
            :current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="prev, pager, next"
            @current-change="handleCurrentChange"
            :pager-count="5"
            class="custom-pagination"
          />
        </div>
      </div>
    </app-body>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, Delete, Picture, ChatLineRound } from '@element-plus/icons-vue'
import AppHeader from '@/components/common/AppHeader.vue'
import AppBody from '@/components/common/AppPageBody.vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'
import { getImageUrl } from '@/utils'

const router = useRouter()
const userStore = useUserStore()

const messageList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const avatarTimestamps = ref({})

// 获取头像URL
const getAvatarUrl = (userId) => {
  if (!userId) return ''
  if (!avatarTimestamps.value[userId]) {
    avatarTimestamps.value[userId] = Date.now()
  }
  const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:9321'
  return `${baseURL}/file/avatar/${userId}?t=${avatarTimestamps.value[userId]}`
}

// 头像加载失败处理
const handleAvatarError = (userId) => {
  if (!userId) return
  avatarTimestamps.value[userId] = Date.now()
}

// 获取消息列表
const getMessageList = async () => {
  if (!userStore.userInfo.id) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    const res = await api.getAllMyMessage(userStore.userInfo.id)
    
    if (res.status_code === 1 && res.data) {
      // 对消息进行排序（按时间倒序）
      const allMessages = res.data.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      
      // 初始化所有消息
      allMessages.forEach(message => {
        message.showReply = false
        message.replyContent = ''
        message.replying = false
      })
      
      // 计算总数
      total.value = allMessages.length

      // 计算当前页的消息
      const start = (currentPage.value - 1) * pageSize.value
      const end = start + pageSize.value
      messageList.value = allMessages.slice(start, end)
    }
  } catch (error) {
    console.error('获取消息列表失败:', error)
    ElMessage.error('获取消息列表失败')
  } finally {
    loading.value = false
  }
}

// 查看详情
const viewDetail = (message) => {
  if (message.idle?.id) {
    router.push(`/details?id=${message.idle.id}`)
  }
}

// 删除消息
const deleteMessage = async (message) => {
  try {
    await ElMessageBox.confirm('确定要删除这条消息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await api.deleteMessage({
      id: message.id,
      userId: userStore.userInfo.id
    })

    if (res.status_code === 1) {
      ElMessage.success('删除成功')
      getMessageList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除消息失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 3600000) { // 1小时内
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  } else if (diff < 86400000) { // 24小时内
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  } else if (diff < 604800000) { // 7天内
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  } else {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  getMessageList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  getMessageList()
}

// 获取商品图片
const getProductImage = (idle) => {
  if (!idle?.pictureList) return ''
  try {
    const pictures = JSON.parse(idle.pictureList)
    if (Array.isArray(pictures) && pictures.length > 0) {
      return getImageUrl(pictures[0])
    }
  } catch (error) {
    console.error('解析商品图片失败:', error)
  }
  return ''
}

// 显示回复表单
const showReplyForm = (message) => {
  message.showReply = true
  message.replyContent = ''
}

// 隐藏回复表单
const hideReplyForm = (message) => {
  message.showReply = false
  message.replyContent = ''
}

// 处理回复
const handleReply = async (message) => {
  if (!message.replyContent?.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  message.replying = true
  try {
    const res = await api.addMessage({
      idleId: message.idle.id,
      content: message.replyContent,
      userId: userStore.userInfo.id,
      toUser: message.fromU.id,
      toMessage: message.id
    })

    if (res.status_code === 1) {
      ElMessage.success('回复成功')
      hideReplyForm(message)
      getMessageList() // 刷新消息列表
    } else {
      ElMessage.error(res.msg || '回复失败')
    }
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  } finally {
    message.replying = false
  }
}

onMounted(() => {
  getMessageList()
})
</script>

<style scoped>
.message-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.page-container {
  width: 100%;
  max-width: 1140px;
  margin: 0 auto;
  padding: 20px 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.message-header .title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
}

.message-header .message-count {
  font-size: 14px;
  color: #666;
}

.message-content {
  margin-bottom: 20px;
}

.message-item {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  padding: 20px;
  margin-bottom: 16px;
  display: flex;
  border-left: 3px solid transparent;
}

.message-item.unread {
  border-left: 3px solid #409EFF;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 70px;
  margin-right: 20px;
}

.username {
  margin-top: 8px;
  font-size: 14px;
  color: #606266;
  text-align: center;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.product-info {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.product-info:hover {
  background-color: #f0f0f0;
}

.product-image {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
  margin-right: 12px;
}

.product-detail {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  font-size: 14px;
  color: #f56c6c;
  font-weight: 500;
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  margin-bottom: 16px;
  word-break: break-all;
}

.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-actions {
  display: flex;
  gap: 16px;
}

.reply-form {
  margin-top: 16px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.reply-input {
  margin-bottom: 12px;
}

.reply-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
}

.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 自定义分页样式 */
:deep(.custom-pagination) {
  --el-pagination-bg-color: transparent;
  --el-pagination-hover-color: #409EFF;
  --el-pagination-button-color: #666;
  --el-pagination-button-bg-color: transparent;
  --el-pagination-button-disabled-color: #C0C4CC;
  --el-pagination-button-disabled-bg-color: transparent;
  --el-pagination-border-radius: 20px;
}

:deep(.custom-pagination .el-pager li) {
  border: 1px solid #e4e7ed;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  margin: 0 4px;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  border-radius: 16px;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
}

:deep(.custom-pagination .el-pager li:hover) {
  border-color: #409EFF;
  color: #409EFF;
  background: rgba(255, 255, 255, 0.9);
}

:deep(.custom-pagination .el-pager li.is-active) {
  background: linear-gradient(90deg, #409EFF, #36cfc9);
  color: white;
  border: none;
  font-weight: normal;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

:deep(.custom-pagination .btn-prev),
:deep(.custom-pagination .btn-next) {
  border: 1px solid #e4e7ed;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  margin: 0 4px;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
  transition: all 0.3s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
}

:deep(.custom-pagination .btn-prev:hover),
:deep(.custom-pagination .btn-next:hover) {
  border-color: #409EFF;
  color: #409EFF;
  background: rgba(255, 255, 255, 0.9);
}

:deep(.custom-pagination .btn-prev:disabled),
:deep(.custom-pagination .btn-next:disabled) {
  border-color: #e4e7ed;
  background: rgba(255, 255, 255, 0.5);
}

.empty-text {
  color: #909399;
  font-size: 14px;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 20px;
}

@media screen and (max-width: 768px) {
  .page-container {
    padding: 15px;
  }
  
  .message-item {
    flex-direction: column;
    padding: 15px;
  }
  
  .user-info {
    flex-direction: row;
    width: 100%;
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .username {
    margin-top: 0;
    margin-left: 12px;
    text-align: left;
  }
}
</style> 