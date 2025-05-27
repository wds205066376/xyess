<template>
  <div>
    <app-header />
    <app-body>
      <div class="search-container">
        <!-- 搜索框区域 -->
        <div class="search-section">
          <div class="search-content">
            <div class="search-title">
              <h1>搜索结果</h1>
              <p class="subtitle">共找到 <span>{{ totalItem }}</span> 个相关商品</p>
            </div>
            <div class="search-box search-box-relative">
              <el-input
                v-model="searchValue"
                placeholder="搜索商品名称、描述..."
                @keyup.enter="handleSearch"
                class="custom-search-input"
              >
                <template #prefix>
                  <el-icon class="search-prefix"><Search /></el-icon>
                  <el-icon
                    v-if="searchValue"
                    class="clear-prefix"
                    @click="handleClear"
                    style="cursor:pointer;margin-left:8px;"
                  >
                    <CircleClose />
                  </el-icon>
                </template>
              </el-input>
              <el-button
                class="search-btn-absolute"
                type="primary"
                :loading="loading"
                @click="handleSearch"
              >
                {{ loading ? '搜索中...' : '搜索' }}
              </el-button>
            </div>
          </div>
          <div class="search-decoration"></div>
        </div>
        

        <!-- 排序工具栏 -->
        <div class="sort-toolbar" v-if="totalItem > 0">
          <div class="sort-left">
            <span class="sort-label">排序方式：</span>
            <div class="sort-buttons">
              <div 
                class="sort-item" 
                :class="{ active: sortType === 'default' }"
                @click="handleSortChange('default')"
              >
                <el-icon><Sort /></el-icon>默认排序
              </div>
              <div 
                class="sort-item"
                :class="{ active: sortType === 'price-asc' }"
                @click="handleSortChange('price-asc')"
              >
                <el-icon><SortUp /></el-icon>价格从低到高
              </div>
              <div 
                class="sort-item"
                :class="{ active: sortType === 'price-desc' }"
                @click="handleSortChange('price-desc')"
              >
                <el-icon><SortDown /></el-icon>价格从高到低
              </div>
              <div 
                class="sort-item"
                :class="{ active: sortType === 'time' }"
                @click="handleSortChange('time')"
              >
                <el-icon><Timer /></el-icon>最新发布
              </div>
            </div>
          </div>
          <div class="sort-right">
            <span class="result-count">找到 <em>{{ totalItem }}</em> 个相关商品</span>
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="goods-list" v-loading="loading">
          <el-row :gutter="20">
            <el-col
              :span="6"
              v-for="(idle, index) in sortedIdleList"
              :key="index"
            >
              <BookCard
                :item="idle"
                @click="toDetails"
              ></BookCard>
            </el-col>
          </el-row>
        </div>

        <!-- 分页 -->
        <div class="pagination" v-if="totalItem > 0">
          <el-pagination
            background
            :current-page="currentPage"
            :page-size="pageSize"
            :total="totalItem"
            layout="prev, pager, next"
            @current-change="handleCurrentChange"
            :pager-count="5"
            class="custom-pagination"
          />
        </div>

        <!-- 无数据展示 -->
        <el-empty
          v-if="!loading && idleList.length === 0"
          description="暂无相关商品"
        >
          <template #description>
            <p class="empty-text">换个关键词试试吧~</p>
          </template>
        </el-empty>
      </div>
      <app-footer />
    </app-body>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, CircleClose, Sort, SortUp, SortDown, Timer } from '@element-plus/icons-vue'
import AppHeader from '@/components/common/AppHeader.vue'
import AppBody from '@/components/common/AppPageBody.vue'
import AppFooter from '@/components/common/AppFoot.vue'
import BookCard from '@/components/common/BookCard.vue'
import api from '@/api'
import { useGlobalStore } from '@/stores'
import { getImageUrl } from '@/utils'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const store = useGlobalStore()

const searchValue = ref('')
const idleList = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const totalItem = ref(0)
const loading = ref(false)
const sortType = ref('default')

// 搜索处理
const handleSearch = () => {
  if (!searchValue.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  currentPage.value = 1
  router.push({
    query: { 
      searchValue: searchValue.value.trim(),
      page: 1,
      sort: sortType.value
    }
  })
}

// 清空搜索
const handleClear = () => {
  searchValue.value = ''
  idleList.value = []
  totalItem.value = 0
  router.push({ path: '/search' })
}

// 获取搜索结果
const fetchSearchResults = async () => {
  loading.value = true
  try {
    const params = {
      findValue: searchValue.value.trim(),
      page: currentPage.value,
      nums: pageSize.value
    }

    const res = await api.findIdleItem(params)
    if (res.data?.list) {
      idleList.value = res.data.list.map(item => {
        // 处理图片URL
        let imgUrl = '';
        if (item.pictureList) {
          try {
            const urls = JSON.parse(item.pictureList);
            if (Array.isArray(urls) && urls.length > 0) {
              imgUrl = getImageUrl(urls[0]);
            }
          } catch (e) {
            console.error('解析pictureList失败:', e);
          }
        }

        // 处理用户头像
        let avatar = '';
        if (item.user && item.user.avatar) {
          avatar = getImageUrl(item.user.avatar);
        }

        return {
          ...item,
          timeStr: `${item.releaseTime.substring(0, 10)} ${item.releaseTime.substring(11, 19)}`,
          imgUrl: imgUrl,
          idlePrice: parseFloat(item.idlePrice).toFixed(2),
          idlePlace: item.idlePlace.split('/').pop() || item.idlePlace,
          user: item.user ? { ...item.user, avatar } : null
        }
      })
      totalItem.value = res.data.count
    } else {
      idleList.value = []
      totalItem.value = 0
    }
  } catch (error) {
    console.error('搜索失败:', error)
    ElMessage.error('搜索失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 排序后的商品列表
const sortedIdleList = computed(() => {
  const list = [...idleList.value]
  switch (sortType.value) {
    case 'price-asc':
      return list.sort((a, b) => parseFloat(a.idlePrice) - parseFloat(b.idlePrice))
    case 'price-desc':
      return list.sort((a, b) => parseFloat(b.idlePrice) - parseFloat(a.idlePrice))
    case 'time':
      return list.sort((a, b) => new Date(b.releaseTime) - new Date(a.releaseTime))
    default:
      return list
  }
})

// 排序变化处理
const handleSortChange = (value) => {
  sortType.value = value
  router.push({
    query: {
      ...route.query,
      sort: value
    }
  })
}

// 页码切换
const handleCurrentChange = (val) => {
  router.push({ 
    query: { 
      ...route.query,
      page: val
    } 
  })
}

// 跳转详情
const toDetails = (idle) => {
  router.push({ path: '/details', query: { id: idle.id } })
}

// 监听路由变化
watch(
  () => route.query,
  (query) => {
    searchValue.value = query.searchValue || ''
    sortType.value = query.sort || 'default'
    currentPage.value = parseInt(query.page) || 1
    if (searchValue.value) {
      fetchSearchResults()
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.search-container {
  min-height: 85vh;
  background-color: #f5f5f5;
  padding: 20px;
}

/* 搜索区域 */
.search-section {
  position: relative;
  padding: 40px 0;
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

.subtitle span {
  color: #409EFF;
  font-weight: bold;
}

.search-box {
  max-width: 600px;
  margin: 0 auto;
  position: relative;
}

/* 复用主页的搜索框样式 */
.custom-search-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50px !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06) !important;
  padding: 4px 4px 4px 0;
  border: 2px solid transparent;
  transition: all 0.3s;
  display: flex;
  align-items: center;
}

.custom-search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #409EFF;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15) !important;
}

.custom-search-input :deep(.el-input__inner) {
  height: 44px;
  font-size: 16px;
  padding: 0 20px;
  color: #333;
  padding-right: 110px !important;
}

.search-prefix {
  display: flex;
  align-items: center;
  padding: 0 16px;
  color: #999;
  font-size: 18px;
  transition: all 0.3s;
}

.search-button {
  height: 40px;
  padding: 0 24px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 50px !important;
  background: linear-gradient(45deg, #409EFF, #36cfc9);
  border: none;
  transition: all 0.3s;
  letter-spacing: 2px;
  white-space: nowrap;
}

.search-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.3);
  background: linear-gradient(45deg, #66b1ff, #40d9d3);
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

/* 商品列表样式 */
.goods-list {
  padding: 10px 0;
}

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
  font-size: 13px;
  font-weight: normal;
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

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

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

:deep(.custom-pagination .el-pager li.is-active) {
  background: linear-gradient(90deg, #409EFF, #36cfc9);
  color: white;
  border: none;
  font-weight: normal;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

/* 排序工具栏样式 */
.sort-toolbar {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.95) 0%,
    rgba(255, 255, 255, 0.85) 100%
  );
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  margin-bottom: 24px;
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
}

.sort-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.sort-label {
  color: #666;
  font-size: 14px;
}

.sort-buttons {
  display: flex;
  gap: 12px;
}

.sort-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 100px;
  font-size: 14px;
  color: #666;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.3s;
  user-select: none;
  border: 1px solid transparent;
}

.sort-item:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.sort-item.active {
  background: linear-gradient(45deg, #409EFF, #36cfc9);
  color: white;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
}

.sort-item .el-icon {
  font-size: 16px;
}

.result-count {
  font-size: 14px;
  color: #666;
}

.result-count em {
  font-style: normal;
  color: #409EFF;
  font-weight: 600;
  margin: 0 4px;
}

/* 添加动画效果 */
@keyframes sortItemHover {
  0% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-2px);
  }
  100% {
    transform: translateY(0);
  }
}

.sort-item:hover {
  animation: sortItemHover 0.3s ease-in-out;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .sort-toolbar {
    flex-direction: column;
    gap: 12px;
    padding: 12px;
  }
  
  .sort-buttons {
    flex-wrap: wrap;
  }
  
  .sort-item {
    padding: 6px 12px;
    font-size: 13px;
  }
}

.empty-text {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

/* 优化搜索框样式 */
.custom-search-input :deep(.el-input__wrapper) {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50px !important;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06) !important;
  padding: 4px 4px 4px 0;
  border: 2px solid transparent;
  transition: all 0.3s;
  display: flex;
  align-items: center;
}

.custom-search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #409EFF;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15) !important;
}

.custom-search-input :deep(.el-input__inner) {
  height: 44px;
  font-size: 16px;
  padding: 0 20px;
  color: #333;
  padding-right: 110px !important;
}

.custom-search-input :deep(.clear-prefix) {
  color: #bbb;
  font-size: 18px;
  transition: color 0.2s;
}
.custom-search-input :deep(.clear-prefix):hover {
  color: #409EFF;
}

.search-box-relative {
  position: relative;
}
.search-btn-absolute {
  position: absolute;
  top: 50%;
  right: 8px;
  transform: translateY(-50%);
  height: 40px;
  padding: 0 24px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 50px !important;
  background: linear-gradient(45deg, #409EFF, #36cfc9);
  border: none;
  transition: all 0.3s;
  letter-spacing: 2px;
  white-space: nowrap;
  z-index: 2;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.08);
  display: flex;
  align-items: center;
}
.search-btn-absolute:hover {
  background: linear-gradient(45deg, #66b1ff, #40d9d3);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.18);
}
</style> 