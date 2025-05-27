<template>
  <div>
    <app-header />
    <app-body>
      <div class="categories-container">
        <div class="search-section">
          <div class="search-content">
            <div class="search-title">
              <h1>商品分类</h1>
              <p class="subtitle">希望你买到心仪的书籍</p>
            </div>
          </div>
          <div class="search-decoration"></div>
        </div>
        
        <!-- 分类列表 -->
        <div class="category-grid">
          <!-- 全部商品 -->
          <div 
            class="category-card"
            :class="{ active: currentCategory === '0' && route.name === 'CategoryView' }"
            @click="handleCategoryClick('0')"
          >
            <div class="category-icon all-icon">
              <el-icon><Grid /></el-icon>
            </div>
            <div class="category-info">
              <h3>所有书籍</h3>
              <span class="count">{{ formatCount(totalItems) }}</span>
            </div>
            <div class="card-hover-effect"></div>
          </div>
          
          <!-- 分类列表 -->
          <div 
            v-for="item in typeList" 
            :key="item.id"
            class="category-card"
            :class="{ active: currentCategory === String(item.id) && route.name === 'CategoryView' }"
            @click="handleCategoryClick(String(item.id))"
          >
            <div class="category-icon">
              <el-icon><Collection /></el-icon>
            </div>
            <div class="category-info">
              <h3>{{ item.name }}</h3>
              <span class="count">{{ formatCount(item.count || 0) }}</span>
            </div>
            <div class="card-hover-effect"></div>
          </div>
        </div>
      </div>
      <app-footer />
    </app-body>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Collection, Grid } from '@element-plus/icons-vue'
import AppHeader from '@/components/common/AppHeader.vue'
import AppBody from '@/components/common/AppPageBody.vue'
import AppFooter from '@/components/common/AppFoot.vue'
import api from '@/api'

const router = useRouter()
const route = useRoute()
const typeList = ref([])
const totalItems = ref(0)
const currentCategory = ref('')

// 格式化商品数量显示
const formatCount = (count) => {
  if (!count || count === 0) {
    return '暂无书籍'
  } else if (count >= 1000) {
    return `${(count / 1000).toFixed(1)}k 本书籍`
  } else if (count === 1) {
    return '1 本书籍'
  }
  return `${count} 本书籍`
}

// 获取分类列表和每个分类的书籍数量
const getTypeList = async () => {
  try {
    const res = await api.listType({ 
      begin: 0, 
      size: 999 
    })
    if (res.status_code === 1) {
      // 获取每个分类的书籍数量
      const categoryCounts = await Promise.all(
        res.data.map(async (category) => {
          try {
            const countRes = await api.findIdleItemByLabel({
              page: 1,
              nums: 1,
              idleLabel: category.id
            })
            return {
              ...category,
              count: countRes.data?.count || 0
            }
          } catch (error) {
            console.error(`获取分类 ${category.name} 的数量失败:`, error)
            return {
              ...category,
              count: 0
            }
          }
        })
      )
      
      typeList.value = categoryCounts
      // 计算总商品数
      totalItems.value = typeList.value.reduce((sum, item) => {
        const count = Number(item.count) || 0
        return sum + count
      }, 0)
    }
  } catch (error) {
    console.error('获取分类失败:', error)
  }
}

// 分类点击处理
const handleCategoryClick = (id) => {
  const categoryId = String(id)
  currentCategory.value = categoryId
  router.push(`/category/${categoryId === '0' ? 'all' : categoryId}`)
}

// 监听路由变化
watch(
  () => route.params.id,
  (newId) => {
    currentCategory.value = newId ? String(newId) : ''
  },
  { immediate: true }
)

onMounted(() => {
  getTypeList()
})
</script>

<style scoped>
.categories-container {
  min-height: 85vh;
  padding: 40px;
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
  margin: 0 0 40px 0;
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

/* 分类卡片网格布局 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.category-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
}

.category-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #1890ff;
}

.card-hover-effect {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.1) 0%, rgba(54, 207, 201, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.category-card:hover .card-hover-effect {
  opacity: 1;
}

.category-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #f0f5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #1890ff;
  transition: all 0.3s;
  font-size: 24px;
}

.all-icon {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  color: white;
}

.category-card:hover .category-icon {
  background: #1890ff;
  color: white;
  transform: scale(1.1);
}

.category-info {
  flex: 1;
  position: relative;
  z-index: 1;
}

.category-info h3 {
  font-size: 16px;
  color: #333;
  margin: 0 0 4px;
  font-weight: 600;
  transition: color 0.3s;
}

.count {
  font-size: 13px;
  color: #999;
  display: inline-block;
  padding: 2px 8px;
  background: #f5f7fa;
  border-radius: 100px;
  transition: all 0.3s;
}

.category-card:hover .count {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.category-card.active {
  background: linear-gradient(135deg, rgba(24, 144, 255, 0.1) 0%, rgba(54, 207, 201, 0.1) 100%);
  border-color: #1890ff;
}

.category-card.active .category-icon {
  background: #1890ff;
  color: white;
}

.category-card.active h3 {
  color: #1890ff;
}

.category-card.active .count {
  background: rgba(24, 144, 255, 0.15);
  color: #1890ff;
}

/* 响应式布局调整 */
@media screen and (max-width: 768px) {
  .categories-container {
    padding: 20px;
  }

  .search-section {
    padding: 40px 0;
    margin: 0 0 30px 0;
  }

  .search-title h1 {
    font-size: 28px;
  }

  .subtitle {
    font-size: 14px;
    padding: 0 20px;
  }

  .category-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 16px;
    padding: 0 16px;
  }
}
</style>