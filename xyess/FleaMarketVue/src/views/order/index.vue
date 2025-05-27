<template>
  <div>
    <app-header />
    <app-body>
      <div class="order-page-container">
        <!-- 商品信息 -->
        <div v-if="orderInfo.idleItem" class="idle-info-container">
          <el-image v-if="orderInfo.idleItem.imgUrl" :src="orderInfo.idleItem.imgUrl" fit="cover" class="idle-image"
            :preview-src-list="[orderInfo.idleItem.imgUrl]">
            <template #error>
              <div class="image-error">
                <el-icon>
                  <Picture />
                </el-icon>
                <span>图片加载失败</span>
              </div>
            </template>
          </el-image>
          <div v-else class="image-placeholder">
            <el-icon>
              <Picture />
            </el-icon>
            <span>暂无图片</span>
          </div>
          <div class="idle-info-title">
            {{ orderInfo.userId === userId ? '买到的' : '卖出的' }}：{{ orderInfo.idleItem.idleName }}
          </div>
          <div class="idle-info-price">￥{{ orderInfo.orderPrice }}</div>
        </div>
        <div v-else class="error-message">
          <el-empty description="订单信息加载失败" />
        </div>

        <!-- 收货地址 -->
        <div v-if="orderInfo.id" class="address-container"
          :class="{ 'clickable': orderInfo.userId === userId && orderInfo.orderStatus === 0 }"
          @click.stop="selectAddressDialog">
          <template v-if="addressInfo.id">
            <div class="address-title">
              收货人：{{ addressInfo.consigneeName }} {{ addressInfo.consigneePhone }}
            </div>
            <div class="address-details">
              收货地址: {{ addressInfo.provinceName }}{{ addressInfo.cityName }}{{ addressInfo.regionName }}{{
                addressInfo.detailAddress }}
            </div>
          </template>
          <el-button v-else-if="orderInfo.userId === userId && orderInfo.orderStatus === 0" type="primary"
            @click="selectAddressDialog">
            选择收货地址
          </el-button>
          <div v-else-if="orderInfo.userId !== userId && !addressInfo.id" class="no-address">
            <el-tag type="info">买家暂未填写收货地址</el-tag>
          </div>
          <div v-else class="no-address">
            <el-tag type="info">暂无收货地址</el-tag>
          </div>
        </div>

        <!-- 选择地址对话框 -->
        <el-dialog v-model="addressDialogVisible" title="选择地址" width="800px">
          <el-table :data="addressData" stripe empty-text="无地址信息，请先在个人中心添加地址">
            <el-table-column prop="consigneeName" label="收货人姓名" width="120" />
            <el-table-column prop="consigneePhone" label="手机号" width="140" />
            <el-table-column prop="detailAddressText" label="地址" />
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-button size="small" @click="selectAddress(scope.$index, scope.row)">
                  选择
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-dialog>

        <!-- 订单信息 -->
        <div v-if="orderInfo.id" class="order-info-container">
          <div class="order-info-item">订单编号：{{ orderInfo.orderNumber }}</div>
          <div class="order-info-item">创建时间：{{ orderInfo.createTime }}</div>
          <div class="order-info-item">订单状态：{{ orderStatus[orderInfo.orderStatus] }}</div>
          <div class="order-info-item">订单金额：￥{{ orderInfo.orderPrice }}</div>

          <div class="menu">
            <el-button v-if="orderInfo.orderStatus === 0" type="primary" @click="payOrder">
              立即付款
            </el-button>
            <el-button v-if="orderInfo.orderStatus === 0" type="danger" @click="cancelOrder">
              取消订单
            </el-button>
            <el-button v-if="orderInfo.orderStatus === 2 && orderInfo.userId === userId" type="success" @click="confirmOrder">
              确认收货
            </el-button>
          </div>
        </div>
      </div>
      <app-footer />
    </app-body>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import AppHeader from '@/components/common/AppHeader.vue'
import AppBody from '@/components/common/AppPageBody.vue'
import AppFooter from '@/components/common/AppFoot.vue'
import api from '@/api'
import { Picture } from '@element-plus/icons-vue'
import { getImageUrl } from '@/utils'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = userStore.userInfo.id
const orderStatus = ['待付款', '待发货', '待收货', '已完成', '已取消']
const addressDialogVisible = ref(false)

// 数据
const orderInfo = ref({})
const addressInfo = ref({})
const addressData = ref([])
const orderId = ref(null)

// 获取订单信息
const getOrderInfo = async () => {
  try {
    const res = await api.getOrderInfo({
      id: route.query.id,
      userId: userStore.userInfo.id
    });

    if (res.status_code === 1 && res.data) {
      // 确保所有必要的数据都存在
      if (!res.data.idleItem) {
        ElMessage.error('订单信息不完整');
        return;
      }

      // 处理商品图片
      let imgUrl = '';
      try {
        const pictureList = JSON.parse(res.data.idleItem.pictureList);
        imgUrl = pictureList[0] || '';
        imgUrl = getImageUrl(imgUrl);
      } catch (e) {
        // 解析商品图片失败
      }

      // 更新订单信息
      orderInfo.value = {
        ...res.data,
        createTime: res.data.createTime
          ? `${res.data.createTime.substring(0, 10)} ${res.data.createTime.substring(11, 19)}`
          : '',
        idleItem: {
          ...res.data.idleItem,
          imgUrl
        }
      };

      // 通过paymentWay获取地址信息
      if (res.data.paymentWay) {
        try {
          const addressRes = await api.getAddressById(res.data.paymentWay, res.data.userId);
          if (addressRes.status_code === 1 && addressRes.data) {
            addressInfo.value = addressRes.data;
          } else {
            addressInfo.value = {};
          }
        } catch (error) {
          // 获取地址信息失败
          addressInfo.value = {};
        }
      } else {
        addressInfo.value = {};
      }
    } else {
      ElMessage.error(res.msg || '获取订单信息失败');
      router.push('/');
    }
  } catch (error) {
    ElMessage.error('获取订单信息失败');
    router.push('/');
  }
};

// 获取地址列表
const getAddressData = async () => {
  try {
    const res = await api.getAddressList({
      userId: userStore.userInfo.id
    })

    if (res.status_code === 1) {
      addressData.value = res.data.map(item => ({
        ...item,
        detailAddressText: item.provinceName + item.cityName + item.regionName + item.detailAddress
      }))
    } else {
      ElMessage.error(res.msg || '获取地址列表失败')
    }
  } catch (error) {
    ElMessage.error('获取地址列表失败')
  }
}

// 选择地址对话框
const selectAddressDialog = () => {
  if (orderInfo.value.userId !== userId || orderInfo.value.orderStatus !== 0) return
  addressDialogVisible.value = true
  getAddressData()
}

// 选择地址
const selectAddress = async (index, row) => {
  try {
    const selectedAddressId = row.id;

    const res = await api.updateOrder({
      id: orderInfo.value.id,
      userId: userStore.userInfo.id,
      orderStatus: orderInfo.value.orderStatus,
      orderPrice: orderInfo.value.orderPrice,
      paymentWay: row.id // 使用地址ID作为paymentWay
    });

    if (res.status_code === 1) {
      // 直接更新地址信息
      addressInfo.value = {
        id: row.id,
        ...row
      };
      addressDialogVisible.value = false;
      ElMessage.success('地址选择成功');
    } else {
      ElMessage.error(res.msg || '地址选择失败');
    }
  } catch (error) {
    ElMessage.error('地址选择失败，请稍后重试');
  }
};

// 支付订单
const payOrder = async () => {
  // 检查地址信息是否完整
  if (!addressInfo.value?.id) {
    ElMessage.warning('请先选择收货地址')
    return
  }

  try {
    await ElMessageBox.confirm('是否确认支付该订单?', '提示', {
      type: 'warning'
    })

    const res = await api.updateOrder({
      id: orderInfo.value.id,
      userId: userStore.userInfo.id,
      orderStatus: 1,
      addressId: addressInfo.value.id,
      orderPrice: orderInfo.value.orderPrice,
      paymentStatus: 1,
      paymentWay: orderId.value,
    })

    if (res.data.paymentStatus === 1) {
      ElMessage.success('支付成功')
      await getOrderInfo() // 支付成功后再刷新订单信息
    } else {
      ElMessage.error(res.msg || '支付失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('支付失败，请稍后重试')
    }
  }
}

// 确认收货
const confirmOrder = async () => {
  try {
    await ElMessageBox.confirm('是否确认收货?', '提示', {
      type: 'warning'
    })

    const res = await api.updateOrder({
      id: orderInfo.value.id,
      userId: userStore.userInfo.id,
      orderStatus: 3,
      addressId: addressInfo.value.id,
      orderPrice: orderInfo.value.orderPrice
    })

    // 2. 同步更新商品状态为已完成（idleStatus: 3）
    if (res.status_code === 1) {
      await api.updateIdleItem({
        id: orderInfo.value.idleId,
        idleStatus: 3
      })
      ElMessage.success('确认收货成功')
      await getOrderInfo()
    } else {
      ElMessage.error(res.msg || '确认收货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      // 确认收货失败
      ElMessage.error('确认收货失败，请稍后重试')
    }
  }
}

// 取消订单
const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await api.updateOrder({
      id: orderInfo.value.id,
      userId: userStore.userInfo.id,
      orderStatus: 4,
      addressId: addressInfo.value.id,
      orderPrice: orderInfo.value.orderPrice
    })

    if (res.status_code === 1) {
      ElMessage.success('订单已取消')
      getOrderInfo() // 刷新订单信息
    } else {
      ElMessage.error(res.msg || '取消订单失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      // 取消订单失败
      ElMessage.error('取消订单失败，请稍后重试')
    }
  }
}

// 跳转商品详情
const toDetails = (id) => {
  router.push({ path: '/details', query: { id } })
}

onMounted(() => {
  getOrderInfo()
})
</script>

<style scoped>
.order-page-container {
  padding: 20px;
}

.idle-info-container {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 20px solid #f6f6f6;
}

.idle-image {
  width: 150px;
  height: 150px;
  border-radius: 4px;
}

.idle-info-title {
  font-size: 18px;
  font-weight: 600;
  max-width: 750px;
  margin-left: 10px;
}

.idle-info-price {
  font-size: 18px;
  color: #f56c6c;
  margin-left: 10px;
}

.address-container {
  min-height: 60px;
  padding: 20px;
  border-bottom: 20px solid #f6f6f6;
}

.clickable {
  cursor: pointer;
}

.address-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
}

.address-details {
  font-size: 16px;
  color: #444;
}

.order-info-container {
  padding: 20px;
}

.order-info-item {
  margin: 10px 0;
  font-size: 14px;
  color: #444;
}

.menu {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.image-error,
.image-placeholder {
  width: 150px;
  height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  border-radius: 4px;
  gap: 8px;
}

.image-error .el-icon,
.image-placeholder .el-icon {
  font-size: 32px;
}

.error-message {
  padding: 40px;
  text-align: center;
}

.no-address {
  color: #909399;
  font-style: italic;
  padding: 20px 0;
  display: flex;
  justify-content: center;
}

.address-visibility {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
}

.item-status {
  font-size: 14px;
  color: #909399;
  margin-left: 8px;
}
</style>