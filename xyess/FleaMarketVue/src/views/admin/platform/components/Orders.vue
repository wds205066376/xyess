<template>
  <div class="orders">
    <el-table :data="orderList" style="width: 100%" v-loading="loading">
      <el-table-column label="序号" width="80">
        <template #default="scope">
          {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="orderNumber" label="订单编号" width="180" />
      <el-table-column label="商品名称" min-width="160">
        <template #default="scope">
          <span class="item-title">{{ scope.row.idleItem?.idleName }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="orderPrice" label="价格" width="100" align="center">
        <template #default="scope">
          <span class="item-price">¥{{ scope.row.orderPrice }}</span>
        </template>
      </el-table-column>
      <el-table-column label="卖家" align="center">
        <template #default="scope">
          <div class="user-info seller">
            <span>{{ userNames[scope.row.idleItem?.userId] || '未知用户' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="买家" align="center">
        <template #default="scope">
          <div class="user-info buyer">
            <span>{{ userNames[scope.row.userId] || '未知用户' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="scope">
          {{ scope.row.createTime ? scope.row.createTime.substring(0, 19).replace('T', ' ') : '' }}
        </template>
      </el-table-column>
      <el-table-column prop="orderStatus" label="订单状态" width="120">
        <template #default="scope">
          <el-tag :type="getOrderStatusType(scope.row.orderStatus)">
            {{ getOrderStatusText(scope.row.orderStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="paymentStatus" label="支付状态" width="120">
        <template #default="scope">
          <el-tag :type="getPaymentStatusType(scope.row.paymentStatus)">
            {{ getPaymentStatusText(scope.row.paymentStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination v-model="currentPage" :page-size="pageSize" :page-sizes="[8, 16, 24, 32]" :total="total"
        layout="total, sizes, prev, pager, next" @size-change="handleSizeChange" @current-change="handleCurrentChange"
        @update:page-size="val => pageSize = val" background class="custom-pagination" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const orderList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const userNames = reactive({}); 

// 获取订单列表
const fetchOrderList = async () => {
  loading.value = true
  try {
    const res = await api.orderList({
      page: currentPage.value,
      nums: pageSize.value
    })
    if (res.status_code === 1) {
      orderList.value = res.data.list
      total.value = res.data.count
      // 初始化用户信息
      fetchUserNames()
    }
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  }
  loading.value = false
}

// 初始化所有用户的名字
const fetchUserNames = async () => {
  const userIds = new Set(orderList.value.flatMap(row => [row.userId, row.idleItem?.userId]).filter(Boolean));
  console.log(userIds)  
  for (const userId of userIds) {
    if (!userNames[userId]) {
      const name = await api.getUserInfo(userId); // 获取用户名
      console.log(name);
      userNames[userId] = name.data.nickname; // 赋值
      console.log(userNames[userId]); // 输出调试信息
    }
  }
};
// 订单状态类型
const getOrderStatusType = (status) => {
  const types = {
    0: 'info',    // 待付款
    1: 'warning', // 待发货
    2: 'primary', // 待收货
    3: 'success', // 已完成
    4: 'danger'   // 已取消
  }
  return types[status] || 'info'
}

// 订单状态文本
const getOrderStatusText = (status) => {
  const texts = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消'
  }
  return texts[status] || '未知'
}

// 支付状态类型
const getPaymentStatusType = (status) => {
  const types = {
    0: 'danger',  // 未支付
    1: 'success', // 已支付
    2: 'info'     // 已退款
  }
  return types[status] || 'info'
}

// 支付状态文本
const getPaymentStatusText = (status) => {
  switch (status) {
    case 0:
      return '未支付'
    case 1:
      return '已支付'
    case 2:
      return '已退款'
    default:
      return '未知'
  }
}

// 删除订单
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await api.deleteOrder({ id })
    if (res.status_code === 1) {
      ElMessage.success('删除成功')
      fetchOrderList()
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchOrderList()
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchOrderList()
}

onMounted(() => {
  fetchOrderList()
})
</script>

<style scoped>
.orders {
  padding: 24px;
  min-height: calc(100vh - 180px);
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
  gap: 24px;
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

.item-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.item-info:hover {
  background-color: #f8faff;
}

.item-title {
  font-weight: 500;
  color: #1f2937;
}

.item-price {
  color: #f56c6c;
  font-weight: 500;
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

:deep(.el-tag--warning) {
  background-color: #fdf6ec;
  border-color: #f5dab1;
  color: #e6a23c;
}

:deep(.el-tag--danger) {
  background-color: #fef0f0;
  border-color: #fbc4c4;
  color: #f56c6c;
}

:deep(.el-tag--info) {
  background-color: #f4f4f5;
  border-color: #e9e9eb;
  color: #909399;
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

.user-info {
  display: flex;
  align-items: center;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.3s;
}

.user-info span {
  font-weight: 500;
  transition: color 0.3s;
}

.user-info.seller {
  background-color: rgba(64, 158, 255, 0.1);
}

.user-info.seller span {
  color: #409EFF;
}

.user-info.seller:hover {
  background-color: rgba(64, 158, 255, 0.2);
}

.user-info.buyer {
  background-color: rgba(103, 194, 58, 0.1);
}

.user-info.buyer span {
  color: #67c23a;
}

.user-info.buyer:hover {
  background-color: rgba(103, 194, 58, 0.2);
}
</style>
