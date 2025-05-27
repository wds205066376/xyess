<template>
  <div>
    <app-header />
    <app-body>
      <div class="category-container">
        <!-- 分类头部 -->
        <div class="category-section">
          <div class="category-content">
            <div class="category-title">
              <h1>{{ categoryTitle }}</h1>
              <p class="subtitle">共找到 <span>{{ totalItem }}</span> 本书籍</p>
            </div>
          </div>
          <div class="category-decoration"></div>
        </div>

        <!-- 排序工具栏 -->
        <div class="sort-toolbar">
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
                :class="{ active: sortType === 'priceAsc' }"
                @click="handleSortChange('priceAsc')"
              >
                <el-icon><SortUp /></el-icon>价格从低到高
              </div>
              <div
                class="sort-item"
                :class="{ active: sortType === 'priceDesc' }"
                @click="handleSortChange('priceDesc')"
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
            <span class="result-count">找到 <em>{{ totalItem }}</em> 本书籍</span>
          </div>
        </div>

        <!-- 商品列表 -->
        <div class="goods-list" v-loading="loading">
          <el-row :gutter="24">
            <el-col
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              v-for="(idle, index) in sortedList"
              :key="index"
              class="goods-col"
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
        />
      </div>
      <app-footer />
    </app-body>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Picture, Timer, Sort, SortUp, SortDown } from '@element-plus/icons-vue'
import AppHeader from '@/components/common/AppHeader.vue'
import AppBody from '@/components/common/AppPageBody.vue'
import AppFooter from '@/components/common/AppFoot.vue'
import BookCard from '@/components/common/BookCard.vue'
import { useGlobalStore } from '@/stores'
import api from '@/api'
import { getImageUrl } from '@/utils'

const route = useRoute()
const router = useRouter()
const store = useGlobalStore()

const currentCategory = ref(null)
const idleList = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const totalItem = ref(0)
const sortType = ref('default')
const loading = ref(false)

// 判断是否是所有书籍页面
const isAllItems = computed(() => {
  return !route.params.id || route.params.id === '0' || route.params.id === 'all'
})

// 获取分类信息
const getCategoryInfo = async () => {
  try {
    const res = await api.listType({ begin: 0, size: 999 })
    if (res.status_code === 1) {
      const categoryId = route.params.id
      currentCategory.value = res.data.find(item => item.id == categoryId)
    }
  } catch (error) {
    console.error('获取分类信息失败:', error)
  }
}

// 获取商品列表
const findIdleItems = async (page) => {
  loading.value = true
  try {
    const labelId = route.params.id
    const params = {
      page: page,
      nums: pageSize.value
    }

    let res;
    // 如果是所有书籍或者labelId是0，使用findIdleItem接口
    if (!labelId || labelId === 'all' || labelId === '0') {
      res = await api.findIdleItem({ ...params, findValue: '' })
    } else {
      // 否则使用findIdleItemByLabel接口
      res = await api.findIdleItemByLabel({
        ...params,
        idleLabel: parseInt(labelId)
      })
    }

    console.log('商品列表数据:', res.data?.list)

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
    console.error('获取商品列表失败:', error)
    idleList.value = []
    totalItem.value = 0
  } finally {
    loading.value = false
  }
}

// 排序后的列表
const sortedList = computed(() => {
  const list = [...idleList.value]
  switch (sortType.value) {
    case 'time':
      return list.sort((a, b) => {
        const timeA = a.releaseTime ? new Date(a.releaseTime) : new Date(0)
        const timeB = b.releaseTime ? new Date(b.releaseTime) : new Date(0)
        return timeB - timeA
      })
    case 'priceAsc':
      return list.sort((a, b) => (Number(a.idlePrice) || 0) - (Number(b.idlePrice) || 0))
    case 'priceDesc':
      return list.sort((a, b) => (Number(b.idlePrice) || 0) - (Number(a.idlePrice) || 0))
    default:
      return list
  }
})

// 排序变化处理
const handleSortChange = (value) => {
  sortType.value = value
}

// 页码切换
const handleCurrentChange = (val) => {
  currentPage.value = val
  findIdleItems(val)
}

// 跳转详情
const toDetails = (idle) => {
  router.push({ path: '/details', query: { id: idle.id } })
}

// 监听路由变化
watch(
  () => route.params.id,
  (newId) => {
    currentPage.value = 1
    if (newId) {
      getCategoryInfo()
      findIdleItems(1)
    }
  },
  { immediate: true }
)

// 修改标题显示
const categoryTitle = computed(() => {
  if (isAllItems.value) {
    return '所有书籍'
  }
  return currentCategory.value?.name || '所有书籍'
})

onMounted(() => {
  findIdleItems(1)
})
</script>

<style scoped>
.category-container {
  padding: 24px;
  background: #f5f5f5;
  min-height: 85vh;
}

/* 分类头部新样式 */
.category-section {
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

.category-content {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
  position: relative;
  z-index: 2;
}

.category-title h1 {
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

.category-decoration {
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

/* 商品列表样式 */
.goods-list {
  margin-bottom: 24px;
}

.goods-col {
  margin-bottom: 24px;
  padding: 0 12px;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .goods-col {
    margin-bottom: 16px;
  }
}

/* 继承首页的商品卡片样式 */
.goods-list {
  margin-bottom: 24px;
}

@media screen and (max-width: 768px) {
  .category-header {
    flex-direction: column;
    gap: 24px;
    padding: 24px;
  }

  .sort-tabs {
    width: 100%;
    overflow-x: auto;
    padding-bottom: 4px;
  }

  .sort-tab {
    white-space: nowrap;
  }
}

/* 商品卡片样式 */
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
}

.book-card:hover {
  transform: translateY(-4px);
  box-shadow:
    0 6px 16px rgba(0, 0, 0, 0.08),
    0 12px 32px rgba(0, 0, 0, 0.06);
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

/* 图片占位样式 */
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

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

/* 定义分页样式 */
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
</style>
