<template>
  <div>
    <app-header />
    <app-body>
      <div class="home-container">
        <!-- 顶部搜索区域 -->
        <div class="search-section">
          <div class="search-content">
            <div class="search-title">
              <h1>校园二手书交易系统</h1>
            </div>
            <div class="search-box" ref="searchBoxRef">
              <el-input v-model="searchValue" placeholder="请输入想要搜索的书籍" @keyup.enter="handleSearch"
                @focus="showHistory = true" class="custom-search-input">
                <template #prefix>
                  <div class="search-prefix">
                    <el-icon>
                      <Search />
                    </el-icon>
                  </div>
                </template>
                <template #suffix>
                  <el-button @click="handleSearch" class="search-button" type="primary">
                    搜索
                  </el-button>
                </template>
              </el-input>

              <!-- 搜索历史 -->
              <div v-if="showHistory && searchHistory.length > 0" class="search-history">
                <div class="history-header">
                  <span>搜索历史</span>
                  <el-button type="text" @click="clearHistory" class="clear-button">
                    清空历史
                  </el-button>
                </div>
                <div class="history-list">
                  <el-tag v-for="item in searchHistory" :key="item.id" closable @click="useHistory(item.keyword)"
                    @close.stop="deleteHistory(item.id)" class="history-item">
                    {{ item.keyword }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分类导航 -->
        <div class="category-nav">
          <div class="category-list">
            <div class="category-item" :class="{ active: labelName === '0' }" @click="handleCategoryClick('0')">
              所有书籍
              <span class="count">({{ totalBooks }})</span>
            </div>
            <div v-for="item in typeList" :key="item.id" class="category-item"
              :class="{ active: labelName === (item.id + '') }" @click="handleCategoryClick(item.id + '')">
              {{ item.name }}
              <span class="count">({{ item.count || 0 }})</span>
            </div>
          </div>
        </div>

        <!-- 排序工具栏 -->
        <div class="sort-toolbar">
          <div class="sort-left">
            <span class="sort-label">排序方式：</span>
            <div class="sort-buttons">
              <div class="sort-item" :class="{ active: sortType === 'default' }" @click="handleSortChange('default')">
                <el-icon>
                  <Sort />
                </el-icon>默认排序
              </div>
              <div class="sort-item" :class="{ active: sortType === 'priceAsc' }" @click="handleSortChange('priceAsc')">
                <el-icon>
                  <SortUp />
                </el-icon>价格从低到高
              </div>
              <div class="sort-item" :class="{ active: sortType === 'priceDesc' }"
                @click="handleSortChange('priceDesc')">
                <el-icon>
                  <SortDown />
                </el-icon>价格从高到低
              </div>
              <div class="sort-item" :class="{ active: sortType === 'time' }" @click="handleSortChange('time')">
                <el-icon>
                  <Timer />
                </el-icon>最新发布
              </div>
            </div>
          </div>
          <div class="sort-right">
            <span class="result-count">找到 <em>{{ totalItem }}</em> 本书籍</span>
          </div>
        </div>

        <!-- 书籍列表 -->
        <div class="goods-list">
          <el-row :gutter="20">
            <el-col :span="6" v-for="(idle, index) in sortedList" :key="index">
              <book-card :item="idle" @click="toDetails" />
            </el-col>
          </el-row>
        </div>

        <div class="pagination">
          <el-pagination background :current-page="currentPage" :page-size="pageSize" :total="totalItem"
            layout="prev, pager, next" @current-change="handleCurrentChange" :pager-count="5"
            class="custom-pagination" />
        </div>
      </div>
      <app-footer />
    </app-body>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Menu, Collection, Picture, Timer, Sort, SortUp, SortDown } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import AppHeader from '@/components/common/AppHeader.vue'
import AppBody from '@/components/common/AppPageBody.vue'
import AppFooter from '@/components/common/AppFoot.vue'
import BookCard from '@/components/common/BookCard.vue'
import api from '@/api'
import { useGlobalStore } from '@/stores'
import { getImageUrl } from '@/utils'

const router = useRouter()
const route = useRoute()
const store = useGlobalStore()

const labelName = ref('0')
const idleList = ref([])
const currentPage = ref(1)
const pageSize = ref(8)
const totalItem = ref(0)
const typeList = ref([])
const searchValue = ref('')
const sortType = ref('default')
const totalBooks = ref(0)
const searchBoxRef = ref(null)
const showHistory = ref(false)

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

// 搜索历史相关
const searchHistory = ref([])

// 获取搜索历史
const getSearchHistory = async () => {
  try {
    const res = await api.getSearchHistory()
    if (res.status_code === 1) {
      searchHistory.value = res.data
    }
  } catch (error) {
    console.error('获取搜索历史失败:', error)
  }
}

// 点击外部区域关闭搜索历史
const handleClickOutside = (event) => {
  const searchBox = searchBoxRef.value
  if (searchBox && !searchBox.contains(event.target)) {
    showHistory.value = false
  }
}

// 使用历史记录
const useHistory = (keyword) => {
  searchValue.value = keyword
  showHistory.value = false
  handleSearch()
}

// 删除单条历史记录
const deleteHistory = async (id) => {
  try {
    const res = await api.deleteSearchHistory(id)
    if (res.status_code === 1) {
      await getSearchHistory()
    }
  } catch (error) {
    console.error('删除搜索历史失败:', error)
  }
}

// 清空历史记录
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有搜索历史吗？', '提示', {
      type: 'warning'
    })
    const res = await api.clearSearchHistory()
    if (res.status_code === 1) {
      searchHistory.value = []
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空搜索历史失败:', error)
    }
  }
}

// 修改搜索处理函数
const handleSearch = async () => {
  if (!searchValue.value || searchValue.value.trim() === '') {
    return
  }

  // 添加搜索历史
  try {
    await api.addSearchHistory(searchValue.value)
    await getSearchHistory() // 刷新搜索历史
  } catch (error) {
    console.error('添加搜索历史失败:', error)
  }

  router.push({
    path: '/search',
    query: {
      searchValue: searchValue.value,
      page: 1
    }
  })
}

// 获取商品列表
const findIdleItems = async (page) => {
  try {
    const params = {
      page: page,
      nums: pageSize.value
    }

    const res = labelName.value !== '0'
      ? await api.findIdleItemByLabel({ ...params, idleLabel: parseInt(labelName.value) })
      : await api.findIdleItem({ ...params, findValue: '' })

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
      // 如果是获取所有书籍，同时更新 totalBooks
      if (labelName.value === '0') {
        totalBooks.value = res.data.count
      }
    }
  } catch (error) {
    console.error('获取商品列表失败:', error)
  }
}

// 分类点击处理
const handleCategoryClick = (id) => {
  labelName.value = id
  currentPage.value = 1
  router.push({
    query: {
      page: 1,
      labelName: id
    }
  }).then(() => {
    // 路由更新后立即获取数据
    findIdleItems(1)
    // 滚动到中间
    const categoryNav = document.querySelector('.category-nav')
    const activeItem = document.querySelector('.category-item.active')
    if (categoryNav && activeItem) {
      const navWidth = categoryNav.offsetWidth
      const itemLeft = activeItem.offsetLeft
      const itemWidth = activeItem.offsetWidth
      const scrollLeft = itemLeft - (navWidth / 2) + (itemWidth / 2)
      categoryNav.scrollTo({
        left: scrollLeft,
        behavior: 'smooth'
      })
    }
  })
}

// 监听路由变化
watch(
  () => route.query,
  (query) => {
    labelName.value = query.labelName || '0'
    const page = parseInt(query.page) || 1
    currentPage.value = page
  },
  { immediate: true }
)

// 页码切换
const handleCurrentChange = (val) => {
  router.push({
    query: {
      page: val,
      labelName: labelName.value
    }
  }).then(() => {
    findIdleItems(val)
  })
}

// 获取商品分类
const getTypeList = async () => {
  try {
    const res = await api.listType({
      begin: 0,
      size: 999
    })
    if (res.status_code === 1) {
      typeList.value = res.data.map(item => ({
        ...item,
        count: 0
      }))

      // 获取每个分类的数量
      for (let item of typeList.value) {
        try {
          const countRes = await api.findIdleItemByLabel({
            idleLabel: item.id,
            page: 1,
            nums: 1
          })
          if (countRes.status_code === 1 && countRes.data?.count) {
            item.count = countRes.data.count
          }
        } catch (error) {
          console.error('获取分类数量失败:', error)
        }
      }

      // 获取所有书籍的数量
      try {
        const allBooksRes = await api.findIdleItem({
          page: 1,
          nums: 1,
          findValue: ''
        })
        if (allBooksRes.status_code === 1 && allBooksRes.data?.count) {
          totalBooks.value = allBooksRes.data.count
        }
      } catch (error) {
        console.error('获取所有书籍数量失败:', error)
      }
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 跳转详情
const toDetails = (idle) => {
  router.push({ path: '/details', query: { id: idle.id } })
}

onMounted(() => {
  getTypeList()
  findIdleItems(currentPage.value)
  getSearchHistory() // 获取搜索历史
  // 初始滚动到选中项
  nextTick(() => {
    const categoryNav = document.querySelector('.category-nav')
    const activeItem = document.querySelector('.category-item.active')
    if (categoryNav && activeItem) {
      const navWidth = categoryNav.offsetWidth
      const itemLeft = activeItem.offsetLeft
      const itemWidth = activeItem.offsetWidth
      const scrollLeft = itemLeft - (navWidth / 2) + (itemWidth / 2)
      categoryNav.scrollTo({
        left: scrollLeft,
        behavior: 'smooth'
      })
    }
  })
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.home-container {
  min-height: 85vh;
  background-color: #f5f5f5;
  padding: 20px;
}

/* 搜索区域 */
.search-section {
  position: relative;
  padding: 60px 0;
  background: linear-gradient(135deg,
      rgba(255, 255, 255, 0.95) 0%,
      rgba(255, 255, 255, 0.85) 100%);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 24px;
  margin: 20px 0 30px 0;
  box-shadow:
    0 8px 20px rgba(0, 0, 0, 0.05),
    0 12px 28px rgba(0, 0, 0, 0.03);
  transform: translateY(0);
  transition: all 0.3s;
  overflow: visible;
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

.search-box {
  position: relative;
  max-width: 600px;
  margin: 0 auto;
  z-index: 20;
}

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
}

.search-prefix {
  display: flex;
  align-items: center;
  padding: 0 16px;
  color: #999;
  font-size: 18px;
  transition: all 0.3s;
}

.custom-search-input :deep(.el-input__suffix) {
  padding: 0;
  border: none;
  background: transparent;
  margin-right: 4px;
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
  z-index: 1;
}

.search-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.3);
  background: linear-gradient(45deg, #66b1ff, #40d9d3);
}

.search-button:active {
  transform: translateY(0);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

/* 搜索框动画效果 */
@keyframes searchFloat {

  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-5px);
  }
}

.search-decoration {
  position: absolute;
  top: -50%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle,
      rgba(64, 158, 255, 0.1) 0%,
      rgba(54, 207, 201, 0.05) 50%,
      transparent 70%);
  border-radius: 50%;
  z-index: 1;
  animation: float 8s ease-in-out infinite;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }

  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

/* 分类导航 */
.category-nav {
  background: #fff;
  padding: 16px 24px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
}

.category-list {
  display: flex;
  gap: 12px;
  white-space: nowrap;
  padding-bottom: 4px;
  padding-right: 24px;
}

/* 隐藏滚动条但保持可以滚动 */
.category-nav::-webkit-scrollbar {
  display: none;
}

.category-nav {
  -ms-overflow-style: none;
  /* IE and Edge */
  scrollbar-width: none;
  /* Firefox */
}

.category-item {
  padding: 6px 16px;
  border-radius: 100px;
  font-size: 14px;
  color: #666;
  background: #f5f5f5;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.category-item:hover {
  background: #f0f0f0;
  transform: translateY(-1px);
  color: #333;
}

.category-item.active {
  background: #e6f4ff;
  color: #1890ff;
}

.count {
  font-size: 12px;
  color: #999;
}

.category-item.active .count {
  color: #1890ff;
  opacity: 0.8;
}

/* 添加响应式布局 */
@media screen and (max-width: 768px) {
  .category-list {
    gap: 8px;
  }

  .category-item {
    padding: 4px 12px;
    font-size: 13px;
  }
}

/* 书籍列表 */
.goods-list {
  padding: 10px 0;
}

.goods-list .el-row {
  margin-bottom: -24px !important;
}

.goods-list .el-col {
  padding-bottom: 24px;
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

/* 调整标签页式 */
:deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(228, 231, 237, 0.6);
}

:deep(.el-tabs__item) {
  transition: all 0.3s;
}

:deep(.el-tabs__item:hover) {
  color: #409EFF;
  transform: translateY(-1px);
}

:deep(.el-tabs__item.is-active) {
  color: #409EFF;
  font-weight: 500;
}

/* 排序工具栏 */
.sort-toolbar {
  background: linear-gradient(135deg,
      rgba(255, 255, 255, 0.95) 0%,
      rgba(255, 255, 255, 0.85) 100%);
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

/* 搜索历史样式 */
.search-history {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  z-index: 100;
  animation: slideDown 0.3s ease-out;
  border: 1px solid rgba(255, 255, 255, 0.8);
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  color: #606266;
  font-size: 14px;
}

.clear-button {
  color: #909399;
  font-size: 13px;
  padding: 0;
}

.clear-button:hover {
  color: #409EFF;
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-item {
  cursor: pointer;
  transition: all 0.3s;
  background: #f5f7fa;
  border-color: #e4e7ed;
  color: #606266;
  font-size: 13px;
}

.history-item:hover {
  transform: translateY(-2px);
  background: #ecf5ff;
  border-color: #409EFF;
  color: #409EFF;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
}

:deep(.el-tag .el-tag__close) {
  color: #909399;
  transition: all 0.3s;
}

:deep(.el-tag .el-tag__close:hover) {
  background-color: #409EFF;
  color: #fff;
}
</style>