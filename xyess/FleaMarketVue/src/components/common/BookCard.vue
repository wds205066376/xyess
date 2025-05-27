<template>
  <div class="book-card" @click="handleClick">
    <el-image
      :src="item.imgUrl"
      fit="cover"
      class="book-image"
    >
      <template #error>
        <div class="image-slot">
          <el-icon><Picture /></el-icon>
          暂无图片
        </div>
      </template>
    </el-image>

    <div class="book-info">
      <h3 class="book-title">{{ item.idleName }}</h3>
      <div class="book-meta">
        <span class="book-price">{{ item.idlePrice }}</span>
        <span class="book-location">{{ item.idlePlace }}</span>
      </div>
      <div class="user-info" v-if="item.user">
        <el-avatar
          :size="24"
          :src="userAvatarUrl"
          @error="handleAvatarError"
        />
        <span class="user-nickname">{{ item.user.nickname }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Picture } from '@element-plus/icons-vue'
import { defineProps, defineEmits, computed, ref } from 'vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

// 添加时间戳引用，用于强制刷新头像
const avatarTimestamp = ref(Date.now())

// 处理头像URL
const userAvatarUrl = computed(() => {
  if (!props.item.user || !props.item.user.id) return ''
  
  const baseURL = import.meta.env.VITE_API_URL || 'http://localhost:9321'
  return `${baseURL}/file/avatar/${props.item.user.id}?t=${avatarTimestamp.value}`
})

// 处理头像加载错误
const handleAvatarError = () => {
  // 头像加载失败，重试
  avatarTimestamp.value = Date.now()
}

const handleClick = () => {
  emit('click', props.item)
}
</script>

<style scoped>
.book-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
  border: 1px solid rgba(0, 0, 0, 0.06);
  height: 100%;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
}

.book-card:hover {
  transform: translateY(-4px);
  box-shadow:
    0 6px 16px rgba(0, 0, 0, 0.08),
    0 12px 32px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.book-image {
  width: 100%;
  height: 180px;
  background-color: #fafafa;
  position: relative;
  overflow: hidden;
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
}

.book-info {
  padding: 12px;
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 8px;
}

.book-title {
  font-size: 15px;
  color: #333;
  line-height: 1.4;
  height: 42px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  font-weight: 600;
  transition: color 0.3s;
}

.book-card:hover .book-title {
  color: #1890ff;
}

.book-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2px 0;
}

.book-price {
  font-size: 20px;
  color: #ff4d4f;
  font-weight: 600;
  display: flex;
  align-items: baseline;
  gap: 2px;
  line-height: 1;
}

.book-price::before {
  content: '¥';
  font-size: 16px;
  font-weight: 600;
  margin-right: 1px;
}

.book-location {
  font-size: 12px;
  color: #999;
  padding: 2px 8px;
  background: #f5f5f5;
  border-radius: 100px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px dashed rgba(0, 0, 0, 0.06);
}

.user-nickname {
  font-size: 12px;
  color: #666;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.image-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  font-size: 12px;
  gap: 8px;
  background: #f5f5f5;
}

.image-slot .el-icon {
  font-size: 24px;
  opacity: 0.5;
}
</style>
