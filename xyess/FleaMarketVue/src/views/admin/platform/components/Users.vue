<template>
  <div class="users">
    <div class="header">
      <el-select v-model="status" placeholder="用户状态" @change="handleStatusChange">
        <el-option label="正常" :value="0" />
        <el-option label="已禁用" :value="1" />
      </el-select>
    </div>

    <el-table :data="userList" style="width: 100%" v-loading="loading">
      <el-table-column label="序号" width="80">
        <template #default="scope">
          {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column label="用户信息" min-width="200" align="center">
        <template #default="scope">
          <div class="user-info-container">
            <el-avatar :size="40" :src="handleAvatarPath(scope.row.avatar)" />
            <div class="user-detail">
              <span class="nickname">{{ scope.row.nickname }}</span>
              <span class="email">{{ scope.row.email }}</span>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="accountNumber" label="手机号" width="120">
        <template #default="scope">
          {{ scope.row.accountNumber || '未设置' }}
        </template>
      </el-table-column>
      <el-table-column prop="signInTime" label="注册时间" width="180">
        <template #default="scope">
          {{ new Date(scope.row.signInTime).toLocaleString() }}
        </template>
      </el-table-column>
      <el-table-column prop="userStatus" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.userStatus === 0 ? 'success' : 'danger'">
            {{ scope.row.userStatus === 0 ? '正常' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button 
            :type="scope.row.userStatus === 0 ? 'danger' : 'success'"
            size="small" 
            @click="handleStatusUpdate(scope.row)"
          >
            {{ scope.row.userStatus === 0 ? '禁用' : '启用' }}
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

const status = ref(0)
const userList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await api.getUserData({
      status: status.value,
      page: currentPage.value,
      nums: pageSize.value
    })
    if (res.status_code === 1) {
      userList.value = res.data.list
      total.value = res.data.count
    }
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  }
  loading.value = false
}

const handleAvatarPath = (path) => {
  if (path.includes('cat.jpg')) {
    return '/file/image?imageName=cat.jpg';
  }
  return `${path}`;
};

// 状态变化
const handleStatusChange = () => {
  currentPage.value = 1
  fetchUserList()
}

// 更新用户状态
const handleStatusUpdate = async (user) => {
  const newStatus = user.userStatus === 0 ? 1 : 0
  try {
    await ElMessageBox.confirm(
      `确定要${newStatus === 0 ? '启用' : '禁用'}该用户吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await api.updateUserStatus({
      id: user.id,
      status: newStatus
    })
    
    if (res.status_code === 1) {
      ElMessage.success(newStatus === 0 ? '用户已启用' : '用户已禁用')
      fetchUserList()
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
  fetchUserList()
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchUserList()
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.users {
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

.user-info-container {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nickname {
  font-weight: 500;
  color: #1f2937;
}

.email {
  color: #6b7280;
  font-size: 13px;
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

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #f56c6c 0%, #e74c3c 100%);
  color: white;
}

:deep(.el-button--danger:hover) {
  background: linear-gradient(135deg, #f78989 0%, #eb6b5e 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.2);
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

:deep(.el-tag) {
  border-radius: 6px;
  padding: 4px 8px;
  font-weight: 500;
}

:deep(.el-tag--success) {
  background-color: #e6f7e6;
  border-color: #c2e7c2;
  color: #67c23a;
}

:deep(.el-tag--danger) {
  background-color: #fef0f0;
  border-color: #fbc4c4;
  color: #f56c6c;
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

:deep(.el-avatar) {
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style> 