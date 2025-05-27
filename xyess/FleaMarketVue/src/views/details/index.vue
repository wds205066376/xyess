<template>
  <div>
    <app-header />
    <app-body>
      <div class="idle-details-container" v-loading="loading">
        <!-- 用户信息头部 -->
        <div class="details-header">
          <div class="details-header-user-info">
            <el-avatar
              :size="48"
              :src="getAvatarUrl(idleItemInfo.user?.id)"
              @error="() => handleAvatarError(idleItemInfo.user?.id)"
            />
            <div class="user-info-text">
              <div class="details-header-user-info-nickname">
                发布者：{{ idleItemInfo.user?.nickname }}
              </div>
              <div class="details-header-user-info-time">
                于{{ idleItemInfo.timeStr }}加入平台
              </div>
            </div>
          </div>
        </div>

        <!-- 商品主体内容 -->
        <div class="details-main">
          <!-- 左侧图片展示区 -->
          <div class="details-left">
            <div class="details-picture">
              <el-carousel 
                :interval="4000" 
                arrow="always" 
                height="400px"
                indicator-position="outside"
                class="image-carousel"
              >
                <el-carousel-item v-for="(imgUrl, i) in idleItemInfo.pictureList" :key="i">
                  <el-image
                    :src="imgUrl"
                    fit="contain"
                    class="details-image"
                  >
                    <template #error>
                      <div class="image-slot">
                        <el-icon><Picture /></el-icon>
                        暂无图片
                      </div>
                    </template>
                  </el-image>
                </el-carousel-item>
              </el-carousel>
            </div>
          </div>

          <!-- 右侧信息展示区 -->
          <div class="details-right">
            <!-- 商品标题和价格 -->
            <div class="product-header">
              <h1 class="product-title">{{ idleItemInfo.idleName }}</h1>
              <div class="product-price">
                <span class="price-symbol">¥</span>
                <span class="price-value">{{ idleItemInfo.idlePrice }}</span>
              </div>
              <!-- 商品详情 -->
              <div class="product-details">
                <h2 class="details-section-title">商品详情</h2>
                <div class="details-content" v-html="idleItemInfo.idleDetails"></div>
              </div>
            </div>

            <!-- 操作按钮组 -->
            <div class="action-buttons">
              <template v-if="!isMaster && userStore.userInfo.id !== idleItemInfo.user?.id">
                <el-button 
                  type="danger" 
                  size="large"
                  class="buy-button"
                  @click="buyButton(idleItemInfo)"
                >
                  立即购买
                </el-button>
                <el-button 
                  type="primary"
                  size="large"
                  class="favorite-button"
                  @click="favoriteButton"
                >
                  {{ isFavorite ? '取消收藏' : '收藏' }}
                </el-button>
              </template>
              <template v-if="isMaster">
                <el-button 
                  v-if="idleItemInfo.idleStatus === 1" 
                  type="danger"
                  size="large"
                  @click="changeStatus(idleItemInfo, 2)"
                >
                  下架
                </el-button>
                <el-button 
                  v-if="idleItemInfo.idleStatus === 2" 
                  type="primary"
                  size="large"
                  @click="changeStatus(idleItemInfo, 1)"
                >
                  重新上架
                </el-button>
              </template>
            </div>

            
          </div>
        </div>

        <!-- 留言区域 -->
        <div class="message-container" id="replyMessageLocation">
          <div class="message-section">
            <div class="message-title">
              <h3>全部留言</h3>
              <span class="message-count">共 {{ total }} 条</span>
            </div>
            
            <div class="message-send">
              <!-- 未登录提示 -->
              <div v-if="!userStore.userInfo.id" class="login-tip">
                <el-alert
                  title="请登录后发表留言"
                  type="info"
                  :closable="false"
                  center
                />
              </div>
              
              <!-- 回复信息提示 -->
              <div v-if="isReply" class="reply-info">
                <el-tag 
                  closable 
                  @close="cancelReply"
                  type="info"
                  class="reply-tag"
                >
                  回复：{{ replyData.toMessage }} @{{ replyData.toUserNickname }}
                </el-tag>
              </div>
              
              <!-- 留言输入框 -->
              <div class="message-input-wrapper">
                <el-input
                  v-model="messageContent"
                  type="textarea"
                  :autosize="{ minRows: 3, maxRows: 6 }"
                  placeholder="请输入留言内容..."
                  maxlength="200"
                  show-word-limit
                  :disabled="sending || !userStore.userInfo.id"
                  class="message-input"
                />
                
                <!-- 发送按钮 -->
                <div class="message-send-button">
                  <el-button 
                    type="primary" 
                    @click="sendMessage" 
                    :loading="sending"
                    :disabled="!messageContent.trim() || !userStore.userInfo.id"
                    round
                  >
                    {{ sending ? '发送中...' : '发送' }}
                  </el-button>
                </div>
              </div>
            </div>

            <!-- 留言列表 -->
            <div id="messageListTop"></div>
            <div class="message-list" v-loading="loading">
              <template v-if="messageList.length > 0">
                <div v-for="(message, index) in messageList" 
                     :key="index" 
                     class="message-item"
                     :class="{ 'message-item-hover': true }"
                >
                  <div class="message-user">
                    <el-avatar
                      :size="40"
                      :src="getAvatarUrl(message.fromU?.id)"
                      shape="circle"
                      @error="() => handleAvatarError(message.fromU?.id)"
                      class="user-avatar"
                    />
                    <div class="message-info">
                      <div class="message-header">
                        <span class="message-nickname">{{ message.fromU.nickname }}</span>
                        <span class="message-time">{{ formatMessageTime(message.createTime) }}</span>
                      </div>
                      
                      <!-- 如果是回复其他留言 -->
                      <div v-if="message.toM" class="reply-content">
                        <span class="reply-to">回复 @{{ message.toU.nickname }}：</span>
                        <span class="quoted-message">{{ message.toM.content }}</span>
                      </div>
                      
                      <div class="message-content">{{ message.content }}</div>
                      
                      <div class="message-footer">
                        <div class="message-actions">
                          <el-button 
                            plain
                            class="action-button"
                            @click="replyMessage(message)"
                          >
                            <el-icon><ChatLineRound /></el-icon> 回复
                          </el-button>
                          <el-button 
                            v-if="message.userId === userStore.userInfo.id || isMaster"
                            plain
                            class="action-button delete-button"
                            @click="deleteMessage(message)"
                          >
                            <el-icon><Delete /></el-icon> 删除
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </template>
              
              <!-- 无留言时的提示 -->
              <el-empty
                v-else
                description="暂无留言"
                :image-size="120"
              >
                <template #description>
                  <p class="empty-text">来发表第一条留言吧~</p>
                </template>
              </el-empty>
              
              <!-- 分页控件 -->
              <div class="pagination" v-if="total > pageSize">
                <el-pagination
                  :current-page="currentPage"
                  :page-size="pageSize"
                  :total="total"
                  layout="prev, pager, next"
                  background
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                  :pager-count="5"
                  class="custom-pagination"
                />
              </div>
            </div>
          </div>
        </div>
        
        <!-- 推荐商品区域 -->
        <div class="recommended-container" v-loading="recommendLoading">
          <div class="section-title">
            <h3>猜你喜欢</h3>
          </div>
          
          <div class="recommended-items" v-if="recommendedItems.length > 0">
            <div 
              v-for="(item, index) in recommendedItems" 
              :key="item.id || index" 
              class="recommended-item"
              @click="router.push(`/details?id=${item.id}`)"
            >
              <div class="item-image-container">
                <el-image
                  :src="item.coverImage"
                  fit="cover"
                  class="item-image"
                  :alt="item.idleName"
                  lazy
                >
                  <template #error>
                    <div class="image-slot">
                      <el-icon><Picture /></el-icon>
                      <span>暂无图片</span>
                    </div>
                  </template>
                </el-image>
              </div>
              <div class="item-info">
                <div class="item-name text-ellipsis" :title="item.idleName">{{ item.idleName }}</div>
                <div class="item-price">¥{{ item.idlePrice }}</div>
              </div>
            </div>
          </div>
          
          <el-empty
            v-else
            description="暂无推荐商品"
            :image-size="80"
          />
        </div>
      </div>
      <app-footer />
    </app-body>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture, Location, Star, Close, ChatLineRound, Delete } from '@element-plus/icons-vue'
import AppHeader from '@/components/common/AppHeader.vue'
import AppBody from '@/components/common/AppPageBody.vue'
import AppFooter from '@/components/common/AppFoot.vue'
import { useUserStore } from '@/stores/user'
import api from '@/api'
import { getImageUrl } from '@/utils'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据
const idleItemInfo = ref({})
const messageList = ref([])
const messageContent = ref('')
const isMaster = ref(false)
const isFavorite = ref(false)
const favoriteId = ref(null)
const isReply = ref(false)
const replyData = ref({})
const loading = ref(false)

// 用于存储用户头像时间戳的Map
const avatarTimestamps = ref({})

// 发送状态
const sending = ref(false)

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 在 script setup 中添加新的响应式变量
const viewStartTime = ref(0)
const viewDuration = ref(0)
const viewTimer = ref(null)

// 推荐商品相关变量和方法
const recommendedItems = ref([])
const recommendLoading = ref(false)

// 统一的获取头像URL的方法
const getAvatarUrl = (userId) => {
  if (!userId) return ''
  
  // 如果没有这个用户的时间戳，初始化一个
  if (!avatarTimestamps.value[userId]) {
    avatarTimestamps.value[userId] = Date.now()
  }
  
  const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:9321'
  return `${baseURL}/file/avatar/${userId}?t=${avatarTimestamps.value[userId]}`
}

// 统一的头像加载错误处理
const handleAvatarError = (userId) => {
  if (!userId) return
  // 更新时间戳使头像重新加载
  avatarTimestamps.value[userId] = Date.now()
}

// 获取商品详情
const getIdleItemInfo = async () => {
  loading.value = true
  try {
    const res = await api.getIdleItem({ id: route.query.id })
    if (res.status_code === 1) {
      // 处理图片列表
      let pictureList = []
      try {
        pictureList = JSON.parse(res.data.pictureList)
        pictureList = pictureList.map(url => getImageUrl(url))
      } catch (e) {
        // 解析图片列表失败
      }

      // 更新发布者头像时间戳
      if (res.data.user?.id) {
        avatarTimestamps.value[res.data.user.id] = Date.now()
      }

      // 处理价格格式
      const price = res.data.idlePrice
      const formattedPrice = typeof price === 'number' ? price : Number(price)

      idleItemInfo.value = {
        ...res.data,
        pictureList,
        idlePrice: formattedPrice,
        timeStr: `${res.data.releaseTime.substring(0, 10)} ${res.data.releaseTime.substring(11, 19)}`
      }
      isMaster.value = res.data.user.id === userStore.userInfo.id
      
      // 如果不是自己发布的商品，才检查收藏状态
      if (!isMaster.value) {
        await checkFavorite()
      }
    }
  } catch (error) {
    // 获取商品详情失败
    ElMessage.error('获取商品详情失败')
  } finally {
    loading.value = false
  }
}

// 检查是否收藏
const checkFavorite = async () => {
  if (!userStore.userInfo.id) return

  try {
    const res = await api.checkFavorite({ 
      idleId: route.query.id,
      userId: userStore.userInfo.id 
    })
    if (res.status_code === 1) {
      isFavorite.value = Boolean(res.data)
      favoriteId.value = res.data
    } else {
      // 检查收藏状态失败
    }
  } catch (error) {
    // 检查收藏状态失败
  }
}

// 获取留言列表
const getMessageList = async () => {
  try {
    const res = await api.getMessage({
      idleId: route.query.id,
      page: currentPage.value,
      pageSize: pageSize.value
    })

    if (res.status_code === 1 && res.data) {
      // 处理每条留言
      messageList.value = res.data.list.map(item => ({
        ...item,
        createTime: item.createTime 
          ? `${item.createTime.substring(0, 10)} ${item.createTime.substring(11, 19)}`
          : '',
        fromU: item.fromU || { id: item.userId, nickname: '未知用户' },
        toU: item.toU || { id: item.toUser, nickname: '未知用户' }
      }))
      
      // 更新总数
      total.value = res.data.total || 0
    } else {
      messageList.value = []
      total.value = 0
    }
  } catch (error) {
    // 获取留言列表失败
    ElMessage.error('获取留言列表失败')
  }
}

// 处理页码改变
const handleCurrentChange = (val) => {
  currentPage.value = val
  getMessageList()
  // 滚动到留言区域顶部
  document.getElementById('messageListTop')?.scrollIntoView({ behavior: 'smooth' })
}

// 处理每页显示数量改变
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1 // 切换每页显示数量时重置为第一页
  getMessageList()
}

// 发送留言后的处理
const afterSendMessage = async () => {
  // 清空输入框
  messageContent.value = ''
  // 如果是回复模式，取消回复
  if (isReply.value) {
    cancelReply()
  }
  // 重置到第一页并刷新列表
  await getMessageList(1)
  // 滚动到留言区域顶部
  document.getElementById('messageListTop')?.scrollIntoView({ behavior: 'smooth' })
}

// 修改发送留言函数
const sendMessage = async () => {
  // 获取用户信息并验证登录状态
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) {
    ElMessage.warning('请先登录后再发送留言')
    return
  }

  // 内容验证
  if (!messageContent.value.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }
  if (messageContent.value.length > 200) {
    ElMessage.warning('留言内容不能超过200个字符')
    return
  }

  sending.value = true
  try {
    const params = {
      idleId: route.query.id,
      content: messageContent.value.trim(),
      userId: userInfo.id,
      toUser: isReply.value ? replyData.value.toUserId : idleItemInfo.value.user.id,
      toMessage: isReply.value ? replyData.value.toMessageId : null
    }

    const res = await api.addMessage(params)
    if (res.status_code === 1) {
      ElMessage.success('留言成功')
      await afterSendMessage()
    } else {
      ElMessage.error(res.msg || '留言失败')
    }
  } catch (error) {
    // 发送留言失败
    ElMessage.error('留言失败，请稍后重试')
  } finally {
    sending.value = false
  }
}

// 修改回复留言函数
const replyMessage = (message) => {
  // 验证登录状态
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) {
    ElMessage.warning('请先登录后再回复留言')
    return
  }

  // 滚动到留言输入框
  document.getElementById('replyMessageLocation')?.scrollIntoView({ behavior: 'smooth' })
  
  isReply.value = true
  replyData.value = {
    toUserId: message.fromU.id,
    toUserNickname: message.fromU.nickname,
    toMessageId: message.id,
    toMessage: message.content.length > 20 ? message.content.substring(0, 20) + '...' : message.content
  }
  
  // 聚焦输入框
  nextTick(() => {
    const textarea = document.querySelector('.message-send .el-textarea__inner')
    textarea?.focus()
  })
}

// 取消回复
const cancelReply = () => {
  isReply.value = false
  replyData.value = {}
}

// 收藏/取消收藏
const favoriteButton = async () => {
  if (!userStore.userInfo.id) {
    ElMessage.warning('请先登录后再收藏')
    return
  }

  try {
    if (isFavorite.value) {
      const res = await api.deleteFavorite({ id: favoriteId.value })
      if (res.status_code === 1) {
        isFavorite.value = false
        favoriteId.value = null
        ElMessage.success('取消收藏成功')
        await updateBehavior(0) // 取消收藏行为
      } else {
        ElMessage.error(res.msg || '取消收藏失败')
      }
    } else {
      const res = await api.addFavorite({
        userId: userStore.userInfo.id,
        idleId: idleItemInfo.value.id
      })
      if (res.status_code === 1) {
        isFavorite.value = true
        favoriteId.value = res.data
        ElMessage.success('收藏成功')
        await updateBehavior(2) // 收藏行为
      } else {
        ElMessage.error(res.msg || '收藏失败')
      }
    }
  } catch (error) {
    // 操作失败
    ElMessage.error('操作失败')
  }
}

// 购买商品
const buyButton = async (item) => {
  if (!userStore.userInfo.id) {
    ElMessage.warning('请先登录后再购买')
    return
  }

  try {
    await ElMessageBox.confirm('是否确认购买该商品?', '提示', {
      type: 'warning',
      confirmButtonText: '确认购买',
      cancelButtonText: '取消'
    })
    
    let price = 0
    if (typeof item.idlePrice === 'string') {
      price = Number(item.idlePrice.replace(/[^0-9.-]+/g, ''))
    } else if (typeof item.idlePrice === 'number') {
      price = item.idlePrice
    }
    
    const res = await api.addOrder({
      idleId: item.id,
      userId: userStore.userInfo.id,
      orderPrice: price
    })
    
    if (res.status_code === 1 && res.data) {
      // ElMessage.success('购买成功！')
      await updateBehavior(3) // 购买行为
      if (res.data.id) {
        await router.push({ 
          path: '/order', 
          query: { 
            id: res.data.id,
            t: Date.now()
          }
        })
      } else {
        ElMessage.error('订单创建成功但跳转失败')
      }
    } else {
      ElMessage.error(res.msg || '购买失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      // 购买失败
      ElMessage.error('购买失败，请稍后重试')
    }
  }
}

// 更改商品状态
const changeStatus = async (item, status) => {
  try {
    const res = await api.updateIdleItem({
      id: item.id,
      idleStatus: status
    })
    
    if (res.status_code === 1) {
      ElMessage.success(status === 1 ? '上架成功' : '下架成功')
      getIdleItemInfo()
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 格式化留言时间
const formatMessageTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  // 一小时内
  if (diff < 3600000) {
    const minutes = Math.floor(diff / 60000)
    return `${minutes}分钟前`
  }
  // 24小时内
  if (diff < 86400000) {
    const hours = Math.floor(diff / 3600000)
    return `${hours}小时前`
  }
  // 一周内
  if (diff < 604800000) {
    const days = Math.floor(diff / 86400000)
    return `${days}天前`
  }
  // 超过一周
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 判断是否可以删除留言
const canDeleteMessage = (message) => {
  // 是留言发送者或商品发布者
  return message.fromU.id === userStore.userInfo.id || isMaster.value
}

// 删除留言
const deleteMessage = async (message) => {
  try {
    await ElMessageBox.confirm('确定要删除这条留言吗？', '提示', {
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
      // 重新加载留言列表
      getMessageList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      // 删除留言失败
      ElMessage.error('删除失败')
    }
  }
}

// 在 script setup 中添加 watch
watch(
  () => route.query.id,
  async (newId, oldId) => {
    if (newId && newId !== oldId) {
      // 重置页面状态
      messageList.value = []
      messageContent.value = ''
      currentPage.value = 1
      
      // 重新加载数据
      await getIdleItemInfo()
      await getMessageList()
      
      // 滚动到页面顶部
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
  },
  { immediate: true }
)

// 添加浏览时长跟踪函数
const startViewTracking = () => {
  viewStartTime.value = Date.now()
  // 每30秒更新一次浏览行为
  viewTimer.value = setInterval(async () => {
    const duration = Math.floor((Date.now() - viewStartTime.value) / 1000)
    if (duration >= 30) {
      await updateBehavior(1) // 更新浏览行为
      viewStartTime.value = Date.now() // 重置计时
    }
  }, 30000)
}

const stopViewTracking = async () => {
  if (viewTimer.value) {
    clearInterval(viewTimer.value)
    viewTimer.value = null
  }
  const duration = Math.floor((Date.now() - viewStartTime.value) / 1000)
  viewDuration.value = duration
  if (duration >= 10) { // 只记录超过10秒的浏览
    await updateBehavior(1)
  }
}

// 统一的行为更新函数
const updateBehavior = async (type) => {
  if (!userStore.userInfo.id || !route.query.id) return
  
  try {
    await api.updateUserBehavior(route.query.id, type, userStore.userInfo.id)
  } catch (error) {
    // 更新用户行为失败
  }
}

// 获取推荐商品
const getRecommendedItems = async () => {
  recommendLoading.value = true
  try {
    // 构建API请求参数
    const params = {
      userId: userStore.userInfo.id || 0,
      limit: 4, // 只显示4个推荐商品
      currentItemId: route.query.id // 添加当前浏览的商品ID，排除在推荐结果外
    }
    
    const res = await api.getRecommendedItems(params)
    
    if (res.status_code === 1 && res.data) {
      // 处理推荐商品的图片 - 简化版本
      recommendedItems.value = res.data.map(item => {
        // 创建新对象，避免直接修改原始对象
        const processedItem = {...item}
        
        // 直接使用优化后的getImageUrl函数处理图片
        try {
          processedItem.coverImage = getImageUrl(item.pictureList)
        } catch (err) {
          processedItem.coverImage = '/placeholder.jpg'
        }
        
        return processedItem
      })
      
      // 限制只显示前4个商品
      if (recommendedItems.value.length > 4) {
        recommendedItems.value = recommendedItems.value.slice(0, 4)
      }
    } else {
      recommendedItems.value = []
    }
  } catch (error) {
    recommendedItems.value = []
  } finally {
    recommendLoading.value = false
  }
}

// 在商品详情加载完成后获取推荐商品
watch(() => idleItemInfo.value.id, (newVal) => {
  if (newVal) {
    getRecommendedItems()
  }
})

// 修改现有的 onMounted
onMounted(() => {
  getIdleItemInfo()
  getMessageList()
  getRecommendedItems()
  startViewTracking()
})

// 添加 onBeforeUnmount
onBeforeUnmount(() => {
  stopViewTracking()
})
</script>

<style scoped>
.idle-details-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  background: transparent;
}

.details-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-radius: 12px;
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
}

.details-header-user-info {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.details-header-user-info:hover {
  background: #f8f9fa;
}

:deep(.el-avatar) {
  border: 2px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

:deep(.el-avatar:hover) {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
}

.user-info-text {
  margin-left: 20px;
}

.details-header-user-info-nickname {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
}

.details-header-user-info-nickname::before {
  content: '';
  display: inline-block;
  width: 4px;
  height: 16px;
  background: linear-gradient(to bottom, #409EFF, #36cfc9);
  border-radius: 2px;
  margin-right: 8px;
}

.details-header-user-info-time {
  font-size: 14px;
  color: #909399;
  display: flex;
  align-items: center;
}

.details-header-user-info-time::before {
  content: '⌚';
  margin-right: 6px;
  font-size: 12px;
}

.details-main {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  align-items: flex-start;
  min-width: 0;
}

.details-left {
  width: 400px;
  min-width: 320px;
  max-width: 450px;
  flex-shrink: 0;
}

.details-right {
  flex: 1;
  min-width: 320px;
  padding: 0 30px;
  display: flex;
  flex-direction: column;
  min-height: 400px;
  overflow: hidden;
}

.details-picture {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.image-carousel {
  width: 100%;
  background: #f8f9fa;
  border-radius: 8px;
}

.details-image {
  width: 100%;
  height: 100%;
  background: #fff;
}

:deep(.el-carousel__arrow) {
  background-color: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  width: 36px;
  height: 36px;
  font-size: 12px;
}

:deep(.el-carousel__arrow:hover) {
  background-color: rgba(0, 0, 0, 0.5);
}

:deep(.el-carousel__indicators) {
  bottom: -25px;
}

:deep(.el-carousel__indicators--outside button) {
  background-color: #dcdfe6;
  width: 30px;
  height: 2px;
  border-radius: 1px;
}

:deep(.el-carousel__indicators--outside button.is-active) {
  background-color: #409EFF;
}

:deep(.image-slot) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 14px;
}

:deep(.image-slot .el-icon) {
  font-size: 24px;
  margin-bottom: 8px;
  color: #909399;
}

.product-header {
  margin-bottom: 20px;
}

.product-title {
  font-size: 28px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  margin: 0;
}

.product-price {
  display: flex;
  align-items: baseline;
  color: #ff4d4f;
  margin-top: 16px;
}

.price-symbol {
  font-size: 20px;
  margin-right: 4px;
}

.price-value {
  font-size: 32px;
  font-weight: 600;
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 24px;
}

.publisher-detail {
  flex: 1;
}

.publisher-name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.publish-time {
  font-size: 13px;
  color: #999;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: auto;
  padding-top: 40px;
}

.buy-button,
.favorite-button {
  flex: 1;
  height: 40px;
  font-size: 14px;
  border-radius: 4px;
}

.buy-button {
  background: #ff4d4f;
  border: none;
}

.buy-button:hover {
  background: #ff7875;
}

.favorite-button {
  background: #fff;
}

.favorite-button:hover {
  background: #fff2f0;
}

.product-details {
  border-top: 1px solid #eee;
  padding-top: 24px;
}

.details-section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.details-content {
  font-size: 16px;
  line-height: 1.8;
  color: #666;
  margin-bottom: 40px;
  word-break: break-all;
  white-space: pre-line;
  overflow: visible;
  max-width: 100%;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .details-main {
    flex-direction: column;
    gap: 24px;
  }

  .details-left,
  .details-right {
    max-width: 100%;
  }

  .details-image {
    height: 300px;
  }

  .action-buttons {
    flex-direction: column;
  }
}

.message-container {
  margin-top: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.message-section {
  padding: 24px;
}

.message-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.message-title h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.message-count {
  font-size: 14px;
  color: #909399;
}

.message-send {
  margin-bottom: 30px;
}

.login-tip {
  margin-bottom: 16px;
}

.reply-info {
  margin-bottom: 16px;
}

.reply-tag {
  padding: 8px 12px;
  font-size: 13px;
}

.message-input-wrapper {
  position: relative;
  margin-bottom: 20px;
}

.message-input {
  margin-bottom: 12px;
}

.message-send-button {
  display: flex;
  justify-content: flex-end;
}

.message-list {
  margin-top: 20px;
}

.message-item {
  padding: 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  margin-bottom: 16px;
  background-color: #fafafa;
}

.message-item-hover:hover {
  background-color: #f5f7fa;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.message-user {
  display: flex;
  gap: 16px;
}

.user-avatar {
  flex-shrink: 0;
}

.message-info {
  flex: 1;
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.message-nickname {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.message-time {
  font-size: 13px;
  color: #909399;
}

.reply-content {
  margin: 8px 0;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
}

.reply-to {
  color: #409EFF;
  margin-right: 4px;
}

.quoted-message {
  color: #606266;
}

.message-content {
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  margin: 12px 0;
  word-break: break-all;
}

.message-footer {
  margin-top: 12px;
}

.message-actions {
  display: flex;
  gap: 12px;
}

.action-button {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  height: auto;
  font-size: 13px;
  color: #606266;
  border: none;
  background: transparent;
}

.action-button:hover {
  color: #409EFF !important;
  text-decoration: none !important;
  border: none !important;
  background: transparent !important;
}

.delete-button {
  color: #909399;
}

.delete-button:hover {
  color: #F56C6C !important;
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.empty-text {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

:deep(.el-input__wrapper) {
  box-shadow: none !important;
  border: 1px solid #dcdfe6;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  border-color: #c0c4cc;
}

:deep(.el-input__wrapper.is-focus) {
  border-color: #409EFF;
}

:deep(.el-textarea__inner) {
  min-height: 80px !important;
  resize: none;
  font-size: 14px;
  line-height: 1.6;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: #409EFF;
}

/* 下架按钮样式 */
.el-button--danger {
  background: #ff4d4f;
  border: none;
}

.el-button--danger:hover {
  background: #ff7875;
}

/* 重新上架按钮样式 */
.el-button--primary {
  background: #409EFF;
  border: none;
}

.el-button--primary:hover {
  background: #66b1ff;
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

/* 推荐商品样式 */
.recommended-container {
  margin-top: 30px;
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.section-title h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
}

.recommended-items {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.recommended-item {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.recommended-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
}

.item-image-container {
  width: 100%;
  height: 180px;
  overflow: hidden;
}

.item-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.recommended-item:hover .item-image {
  transform: scale(1.05);
}

.item-info {
  padding: 12px;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  height: 42px;
}

.item-price {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

@media screen and (max-width: 768px) {
  .recommended-items {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style> 