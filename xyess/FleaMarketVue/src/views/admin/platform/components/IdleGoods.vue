<template>
  <div class="idle-goods">
    <div class="header">
      <el-select v-model="status" placeholder="商品状态" @change="handleStatusChange">
        <el-option label="在售" :value="1" />
        <el-option label="已下架" :value="2" />
        <el-option label="已完成" :value="3" />
      </el-select>
    </div>

    <el-table :data="goodsList" style="width: 100%" v-loading="loading">
      <el-table-column label="序号" width="80">
        <template #default="scope">
          {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="idleName" label="商品名称" />
      <el-table-column prop="idlePrice" label="价格" width="100">
        <template #default="scope">
          ¥{{ scope.row.idlePrice }}
        </template>
      </el-table-column>
      <el-table-column label="地址" min-width="200">
        <template #default="scope">
          <div class="description-info">

              <span class="description-text">{{ scope.row.idlePlace || '暂无地址' }}</span>

          </div>
        </template>
      </el-table-column>
      <el-table-column label="卖家" width="120">
        <template #default="scope">
          <div class="seller-info">
              <span>{{ scope.row.user?.nickname || '未知用户' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="releaseTime" label="发布时间" width="180">
        <template #default="scope">
          {{ scope.row.releaseTime ? scope.row.releaseTime.substring(0, 19).replace('T', ' ') : '' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button 
            v-if="scope.row.idleStatus === 1"
            type="warning" 
            size="small" 
            @click="handleStatusUpdate(scope.row.id, 2)"
          >
            下架
          </el-button>
          <el-button 
            v-if="scope.row.idleStatus === 2"
            type="success" 
            size="small" 
            @click="handleStatusUpdate(scope.row.id, 1)"
          >
            上架
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model="currentPage"
        :page-size="pageSize"
        :page-sizes="[8, 16, 24, 32]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @update:page-size="val => pageSize = val"
        background
        class="custom-pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const status = ref(1)
const goodsList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 获取商品列表
const fetchGoodsList = async () => {
  loading.value = true
  try {
    const res = await api.idleList({
      status: status.value,
      page: currentPage.value,
      nums: pageSize.value
    })
    if (res.status_code === 1) {
      goodsList.value = res.data.list
      total.value = res.data.count
    }
  } catch (error) {
    ElMessage.error('获取商品列表失败')
  }
  loading.value = false
}

// 状态变化
const handleStatusChange = () => {
  currentPage.value = 1
  fetchGoodsList()
}

// 更新商品状态
const handleStatusUpdate = async (id, newStatus) => {
  try {
    await ElMessageBox.confirm(
      newStatus === 1 ? '确定要上架该商品吗？' : '确定要下架该商品吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await api.updateIdleStatus({
      id,
      idleStatus: newStatus
    })
    
    if (res.status_code === 1) {
      ElMessage.success(newStatus === 1 ? '商品已上架' : '商品已下架')
      fetchGoodsList()
    } else {
      ElMessage.error('操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchGoodsList()
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchGoodsList()
}

onMounted(() => {
  fetchGoodsList()
})
</script>

<style scoped>
.idle-goods {
  padding: 24px;
  min-height: calc(100vh - 180px);
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  gap: 24px;
}

.header {
  background-color: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-table {
  flex: 1;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

:deep(.el-table__header) {
  background-color: #f8faff;
}

:deep(.el-table__header th) {
  background-color: #f8faff;
  color: #1f2937;
  font-weight: 600;
  height: 56px;
  text-align: center;
}

:deep(.el-table__column-header-title) {
  display: inline-flex;
  justify-content: center;
  width: 100%;
}

:deep(.el-table__row) {
  transition: all 0.3s;
}

:deep(.el-table__row td) {
  padding: 16px 0;
  text-align: center;
}

:deep(.el-table__row:hover) {
  background-color: #f8faff;
}

.seller-info {
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.3s;
  cursor: default;
}

.seller-info:hover {
  background-color: transparent;
}

.seller-info span {
  color: #1f2937;
  font-weight: 500;
  transition: color 0.3s;
}

.seller-info span:hover {
  color: #1f2937;
}

.description-info {
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.description-info:hover {
  background-color: #f8faff;
}

.description-text {
  display: inline-block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #4b5563;
}

.pagination {
  padding: 20px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: center;
}

:deep(.el-button) {
  border-radius: 8px;
  transition: all 0.3s;
  padding: 8px 16px;
  font-weight: 500;
  border: none;
}

:deep(.el-button--default) {
  background-color: #f3f4f6;
  color: #374151;
}

:deep(.el-button--default:hover) {
  background-color: #e5e7eb;
  color: #1f2937;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409EFF 0%, #36cfc9 100%);
  color: white;
}

:deep(.el-button--primary:hover) {
  background: linear-gradient(135deg, #66b1ff 0%, #5cdbd3 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
}

:deep(.el-button--warning) {
  background: linear-gradient(135deg, #ff9800 0%, #ff5722 100%);
  color: white;
}

:deep(.el-button--warning:hover) {
  background: linear-gradient(135deg, #ffa726 0%, #ff7043 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #67c23a 0%, #4caf50 100%);
  color: white;
}

:deep(.el-button--success:hover) {
  background: linear-gradient(135deg, #85ce61 0%, #66bb6a 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.2);
}

:deep(.el-select) {
  width: 200px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.05);
  padding: 0 16px;
  height: 40px;
  transition: all 0.3s;
}

:deep(.el-select .el-input__wrapper:hover) {
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.el-select .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

:deep(.el-table--enable-row-hover .el-table__body tr:hover > td) {
  background-color: #f8faff;
}

:deep(.el-table td.el-table__cell) {
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-table::before) {
  display: none;
}
</style> 