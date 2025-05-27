<template>
  <div>
    <app-header :nickname-value="userStore.userInfo.nickname" :avatar-value="userStore.userInfo.avatar" />
    <app-body>
      <div class="profile-container">
        <!-- 用户信息卡片 -->
        <div class="profile-header">
          <div class="profile-content">
            <div class="user-info">
              <div class="avatar-section">
                <el-upload class="avatar-upload" :action="`${baseURL}/file/upload`" :data="{ type: 'avatar' }"
                  :show-file-list="false" :on-success="handleAvatarSuccess" :before-upload="beforeAvatarUpload">
                  <el-avatar :size="120" :src="avatarUrl" class="user-avatar" />
                  <div class="avatar-hover-text">点击更换头像</div>
                </el-upload>
              </div>
              <div class="user-details">
                <h1 class="username">{{ userStore.userInfo.nickname }}</h1>
                <p class="join-time">{{ userStore.userInfo.username }}</p>
                <div class="action-buttons">
                  <el-button type="primary" @click="showEditDialog = true">
                    <el-icon>
                      <Edit />
                    </el-icon>
                    编辑个人信息
                  </el-button>
                  <el-button type="success" @click="editAddress = true">
                    <el-icon>
                      <Location />
                    </el-icon>
                    编辑收货地址
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 标签页导航 -->
        <div class="tabs-header">
          <div v-for="(tab, index) in tabs" :key="index" class="tab-item" :class="{ active: activeName === tab.name }"
            @click="handleTabClick(tab.name)">
            <el-icon>
              <component :is="tab.icon" />
            </el-icon>
            {{ tab.label }}
          </div>
        </div>

        <!-- 商品列表区域 -->
        <div class="idle-container">
          <div class="idle-container-list">
            <template v-if="dataList[Number(activeName) - 1]?.length > 0">
              <div v-for="(item, index) in dataList[Number(activeName) - 1]" :key="index"
                class="idle-container-list-item" @click="handleItemClick(item)">
                <div class="idle-container-list-content">
                  <div class="idle-container-list-img">
                    <el-image :src="item.imgUrl" fit="cover">
                      <template #error>
                        <div class="image-slot">
                          <el-icon>
                            <Picture />
                          </el-icon>
                          <span>暂无图片</span>
                        </div>
                      </template>
                    </el-image>
                  </div>
                  <div class="idle-container-list-info">
                    <div class="idle-container-list-title">{{ item.idleName || item.title }}</div>
                    <div class="idle-container-list-idle-details">{{ item.idleDetails }}</div>
                    <div class="idle-container-list-idle-time">{{ item.timeStr }}</div>
                    <div class="idle-item-foot">
                      <div class="idle-price">{{ item.idlePrice }}</div>
                      <div class="item-actions" @click.stop>
                        <template v-if="activeName === '1'">
                          <el-button type="primary" link @click="handleEditItem(item)">修改</el-button>
                          <el-button type="danger" link @click="handleOffShelf(item, index)">下架</el-button>
                        </template>
                        <template v-else-if="activeName === '2'">
                          <el-button type="success" link @click="handleRePublish(item, index)">重新上架</el-button>
                          <el-button type="danger" link @click="handleDelete(item, index)">删除</el-button>
                        </template>
                        <template v-else-if="activeName === '3'">
                          <el-button type="danger" link @click="handleCancelFavorite(item)">取消收藏</el-button>
                        </template>
                        <template v-else-if="activeName === '4'">
                          <div class="order-status-container">
                            <div class="order-status">{{ item.status }}</div>
                            <el-button v-if="item.orderStatus === 1" type="primary" link @click="shipOrder(item)">
                              发货
                            </el-button>
                          </div>
                        </template>
                        <template v-else-if="activeName === '5'">
                          <div class="order-status">{{ item.status }}</div>
                          <el-button
                            v-if="item.orderStatus === 1"
                            type="danger"
                            link
                            @click="handleCancelOrder(item, index)"
                          >取消订单</el-button>
                        </template>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无数据" />
          </div>
        </div>
      </div>
    </app-body>
    <app-footer />

    <!-- 编辑个人信息对话框 -->
    <el-dialog v-model="showEditDialog" title="编辑个人信息" width="400px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="80px" class="edit-form">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" maxlength="20" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="saveUserInfo">确定</el-button>
          <el-button type="warning" @click="showPasswordDialog = true">修改密码</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="400px" :close-on-click-modal="false" destroy-on-close>
      <el-form :model="passwordForm" label-width="100px" class="password-form">
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" @click="updatePassword">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 地址管理对话框 -->
    <el-dialog v-model="editAddress" title="编辑收货地址" width="800px" :close-on-click-modal="false">
      <!-- 左侧地址列表 -->
      <div class="address-container">
        <div class="address-list">
          <div class="address-list-header">
            <h3>我的收货地址</h3>
            <el-button type="primary" @click="handleAddAddress">新增地址</el-button>
          </div>
          <div v-if="addressData.length > 0" class="address-list-content">
            <div v-for="(item, index) in addressData" :key="index" class="address-item"
              :class="{ 'is-default': item.defaultFlag }">
              <div class="address-info">
                <div class="address-header">
                  <span class="name">{{ item.consigneeName }}</span>
                  <span class="phone">{{ item.consigneePhone }}</span>
                  <el-tag v-if="item.defaultFlag" type="success" size="small">默认地址</el-tag>
                </div>
                <div class="address-detail">
                  {{ item.provinceName }}{{ item.cityName }}{{ item.regionName }}{{ item.detailAddress }}
                </div>
              </div>
              <div class="address-actions">
                <el-button type="danger" link @click="handleDeleteAddress(index, item)">删除</el-button>
                <el-button v-if="!item.defaultFlag" type="success" link @click="handleSetDefault(index, item)">
                  设为默认
                </el-button>
                <el-button type="primary" link @click="handleEditAddress(item)">编辑</el-button>
              </div>
            </div>
          </div>
          <div v-else class="empty-address">
            <el-empty description="暂无收货地址" />
          </div>
        </div>

        <!-- 右侧编辑表单 -->
        <div v-if="showAddressForm" class="address-form">
          <h3>{{ isEditingAddress ? '编辑地址' : '新增地址' }}</h3>
          <el-form :model="addressInfo" label-width="100px">
            <el-form-item label="收货人">
              <el-input v-model="addressInfo.consigneeName" placeholder="请输入收货人姓名" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="addressInfo.consigneePhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="所在地区">
              <el-cascader v-model="selectedOptions" :options="options" :props="{
                expandTrigger: 'hover',
                value: 'value',
                label: 'label'
              }" @change="handleRegionChange" placeholder="请选择所在地区" />
            </el-form-item>
            <el-form-item label="详细地址">
              <el-input v-model="addressInfo.detailAddress" type="textarea" placeholder="请输入详细地址" />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="addressInfo.defaultFlag">设为默认地址</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveAddress">保存</el-button>
              <el-button @click="cancelEdit">取消</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-dialog>

    <!-- 编辑商品信息对话框 -->
    <el-dialog v-model="showEditItemDialog" title="编辑商品信息" width="480px">
      <el-form>
        <el-form-item label="商品图片">
          <div class="edit-item-multi-image">
            <div v-for="(img, idx) in editItemForm.pictureListArr" :key="idx" class="edit-item-image-wrapper">
              <img :src="getImageUrl(img)" class="edit-item-image-preview" />
              <el-icon class="edit-item-image-remove" @click.stop="removeEditItemImage(idx)"><Close /></el-icon>
            </div>
            <el-upload
              class="edit-item-uploader"
              :action="`${baseURL}/file/upload`"
              :show-file-list="false"
              :on-success="handleEditItemImageSuccess"
              :before-upload="beforeEditItemImageUpload"
              accept="/image/*"
              :multiple="true"
            >
              <el-icon class="edit-item-uploader-add"><Plus /></el-icon>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="editItemForm.idleName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="editItemForm.idleDetails" type="textarea" placeholder="请输入商品描述" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input v-model="editItemForm.idlePrice" placeholder="请输入价格" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditItemDialog = false">取消</el-button>
          <el-button type="primary" @click="saveEditItem">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Picture, Location, Close, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import AppBody from '@/components/common/AppPageBody.vue'
import AppFooter from '@/components/common/AppFoot.vue'
import api from '@/api'
import { getImageUrl } from '@/utils'
import addressOptions from '@/components/common/country-data'

const router = useRouter()
const userStore = useUserStore()
const baseURL = import.meta.env.VITE_API_URL || ''

// 添加订单状态常量
const orderStatus = {
  1: '待发货',
  2: '已发货',
  3: '已完成',
  4: '已取消'
}

// 编辑个人信息相关
const showEditDialog = ref(false)
const editForm = ref({
  nickname: ''
})

// 修改密码相关
const showPasswordDialog = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 地址管理相关
const editAddress = ref(false)
const showAddressForm = ref(false)
const addressData = ref([])
const addressInfo = ref({
  consigneeName: '',
  consigneePhone: '',
  provinceName: '',
  cityName: '',
  regionName: '',
  detailAddress: '',
  defaultFlag: false
})

// 商品列表相关数据
const activeName = ref('1')
const dataList = ref([[], [], [], [], []])
const operationName = ['下架', '上架', '取消收藏', '', '']

// 地区数据
const options = ref(addressOptions)

const selectedOptions = ref([])

// 编辑个人信息
const handleEditInfo = () => {
  editForm.value.nickname = userStore.userInfo.nickname
  showEditDialog.value = true
}

// 获取用户信息
const getUserInfo = async () => {
  try {
    const res = await api.getUserInfo(userStore.userInfo.id)
    if (res.status_code === 1) {
      userStore.userInfo = res.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 保存个人信息
const saveUserInfo = async () => {
  if (!editFormRef.value) return

  try {
    await editFormRef.value.validate()

    const res = await api.updateUserInfo({
      id: userStore.userInfo.id,
      nickname: editForm.value.nickname.trim()
    })

    if (res.status_code === 1) {
      ElMessage.success('修改成功')
      showEditDialog.value = false
      // 重新获取用户信息
      await getUserInfo()
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch (error) {
    if (error.message) {
      ElMessage.warning(error.message)
    } else {
      console.error('修改个人信息失败:', error)
      ElMessage.error('修改失败')
    }
  }
}

// 修改密码
const updatePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword || !passwordForm.value.confirmPassword) {
    ElMessage.warning('请填写完整的密码信息')
    return
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  try {
    const res = await api.updateUserPassword({
      id: userStore.userInfo.id,
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    if (res.status_code === 1) {
      ElMessage.success('密码修改成功')
      showPasswordDialog.value = false
      // 清空表单
      passwordForm.value = {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    ElMessage.error('修改失败')
  }
}

// 新增地址
const handleAddAddress = () => {
  showAddressForm.value = true
  addressInfo.value = {
    consigneeName: '',
    consigneePhone: '',
    provinceName: '',
    cityName: '',
    regionName: '',
    detailAddress: '',
    defaultFlag: false
  }
  selectedOptions.value = []
}

const handleDeleteAddress = async (index, item) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await api.deleteAddress({
      id: Number(item.id),
      userId: Number(userStore.userInfo.id)
    })
    if (res.status_code === 1 || res.code === 200) {
      ElMessage.success('地址删除成功')
      await getAddressData()
    } else {
      ElMessage.error(res.msg || '删除地址失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除地址失败:', error)
      ElMessage.error('删除地址失败')
    }
  }
}

const handleSetDefault = async (index, item) => {
  try {
    const res = await api.updateAddress({
      id: Number(item.id),
      userId: Number(userStore.userInfo.id),
      defaultFlag: true
    })
    if (res.status_code === 1) {
      ElMessage.success('设置默认地址成功')
      await getAddressData()
    } else {
      ElMessage.error(res.msg || '设置默认地址失败')
    }
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置默认地址失败')
  }
}

// 取消编辑
const cancelEdit = () => {
  showAddressForm.value = false
  addressInfo.value = {
    consigneeName: '',
    consigneePhone: '',
    provinceName: '',
    cityName: '',
    regionName: '',
    detailAddress: '',
    defaultFlag: false
  }
  selectedOptions.value = []
}

// 保存地址
const saveAddress = async () => {
  if (!addressInfo.value.consigneeName || !addressInfo.value.consigneePhone) {
    ElMessage.warning('请填写收货人和联系电话')
    return
  }
  if (!addressInfo.value.provinceName || !addressInfo.value.detailAddress) {
    ElMessage.warning('请填写完整的地址信息')
    return
  }

  try {
    let params = {
      userId: userStore.userInfo.id,
      consigneeName: addressInfo.value.consigneeName,
      consigneePhone: addressInfo.value.consigneePhone,
      provinceName: addressInfo.value.provinceName,
      cityName: addressInfo.value.cityName,
      regionName: addressInfo.value.regionName,
      detailAddress: addressInfo.value.detailAddress,
      defaultFlag: addressInfo.value.defaultFlag
    }
    if (isEditingAddress.value && editingAddressId.value) {
      params.id = editingAddressId.value
      const res = await api.updateAddress(params)
      if (res.status_code === 1) {
        ElMessage.success('地址修改成功')
        showAddressForm.value = false
        isEditingAddress.value = false
        editingAddressId.value = null
        addressInfo.value = {
          consigneeName: '',
          consigneePhone: '',
          provinceName: '',
          cityName: '',
          regionName: '',
          detailAddress: '',
          defaultFlag: false
        }
        selectedOptions.value = []
        getAddressData()
      } else {
        ElMessage.error(res.msg || '保存失败')
      }
    } else {
      // 新增
      const res = await api.addAddress(params)
      if (res.status_code === 1) {
        ElMessage.success('地址添加成功')
        showAddressForm.value = false
        addressInfo.value = {
          consigneeName: '',
          consigneePhone: '',
          provinceName: '',
          cityName: '',
          regionName: '',
          detailAddress: '',
          defaultFlag: false
        }
        selectedOptions.value = []
        getAddressData()
      } else {
        ElMessage.error(res.msg || '保存失败')
      }
    }
  } catch (error) {
    console.error('保存地址失败:', error)
    ElMessage.error('保存失败')
  }
}

// 地区选择变化处理
const handleRegionChange = (value) => {
  if (value.length === 3) {
    addressInfo.value.provinceName = value[0]
    addressInfo.value.cityName = value[1]
    addressInfo.value.regionName = value[2]
  }
}

// 获取地址列表
const getAddressData = async () => {
  try {
    const res = await api.getAddressList({
      userId: userStore.userInfo.id
    })
    if (res.status_code === 1) {
      addressData.value = res.data || []
    } else {
      ElMessage.error(res.msg || '获取地址列表失败')
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
    ElMessage.error('获取地址列表失败')
  }
}

// 头像上传相关
const handleAvatarSuccess = async (response) => {
  if (response.status_code === 1) {
    const imageUrl = response.data
    try {
      const res = await api.updateUserInfo({
        id: userStore.userInfo.id,
        avatar: imageUrl
      })
      if (res.status_code === 1) {
        ElMessage.success('头像更新成功')
        // 重新获取用户信息
        await getUserInfo()
      } else {
        ElMessage.error(res.msg || '更新失败')
      }
    } catch (error) {
      console.error('头像更新失败:', error)
      ElMessage.error('更新失败')
    }
  } else {
    ElMessage.error('头像上传失败')
  }
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg'
  const isPNG = file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG && !isPNG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 获取头像 URL
const avatarUrl = computed(() => {
  const avatar = userStore.userInfo.avatar || ''
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  if (avatar.startsWith('/image')) {
    return `${baseURL}/file${avatar}`
  }
  return `${baseURL}${avatar}`
})

// 获取商品列表数据
const getDataList = async (type) => {
  try {
    let result = []
    const userId = userStore.userInfo.id

    if (!userId) {
      ElMessage.error('获取用户信息失败')
      return
    }

    switch (type) {
      case '1': // 我发布的
        const res = await api.getAllIdleItem({
          userId,
          idleStatus: 1
        })
        if (res.status_code === 1) {
          result = (res.data || []).map(item => ({
            ...item,
            imgUrl: getImageUrl(item.pictureList),
            timeStr: new Date(item.releaseTime).toLocaleString(),
            idlePrice: item.idlePrice ? `¥${item.idlePrice}` : '面议'
          }))
        }
        break

      case '2': // 我下架的
        const res2 = await api.getAllIdleItem({
          userId,
          idleStatus: 2
        })
        if (res2.status_code === 1) {
          result = (res2.data || []).map(item => ({
            ...item,
            imgUrl: getImageUrl(item.pictureList),
            timeStr: new Date(item.releaseTime).toLocaleString(),
            idlePrice: item.idlePrice ? `¥${item.idlePrice}` : '面议'
          }))
        }
        break

      case '3': // 我收藏的
        const res3 = await api.getMyFavorite({
          userId: userId
        })
        if (res3.status_code === 1) {
          result = (res3.data || []).map(item => ({
            ...item,
            idleName: item.idleItem?.idleName,
            idleDetails: item.idleItem?.idleDetails,
            imgUrl: getImageUrl(item.idleItem?.pictureList),
            timeStr: new Date(item.createTime).toLocaleString(),
            idlePrice: item.idleItem?.idlePrice ? `¥${item.idleItem.idlePrice}` : '面议'
          }))
        }
        break

      case '4': // 我卖出的
        const res4 = await api.getMySoldOrders({
          userId: userId
        })
        if (res4.status_code === 1) {
          result = (res4.data || []).map(item => ({
            ...item,
            id: item.id,
            idleName: item.idleItem?.idleName,
            idleDetails: item.idleItem?.idleDetails,
            imgUrl: getImageUrl(item.idleItem?.pictureList),
            timeStr: new Date(item.createTime).toLocaleString(),
            idlePrice: item.orderPrice ? `¥${item.orderPrice}` : '面议',
            status: orderStatus[item.orderStatus] || '未知状态',
            orderStatus: item.orderStatus,
            addressId: item.addressId
          }))
        }
        break

      case '5': // 我买到的
        const res5 = await api.getMyOrders({
          userId: userId
        })
        if (res5.status_code === 1) {
          result = (res5.data || []).map(item => ({
            ...item,
            idleName: item.idleItem?.idleName,
            idleDetails: item.idleItem?.idleDetails,
            imgUrl: getImageUrl(item.idleItem?.pictureList),
            timeStr: new Date(item.createTime).toLocaleString(),
            idlePrice: item.idleItem?.idlePrice ? `¥${item.idleItem.idlePrice}` : '面议',
            status: orderStatus[item.orderStatus] || '未知状态'
          }))
        }
        break
    }

    dataList.value[Number(type) - 1] = result
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败')
  }
}

// 添加标签页配置
const tabs = [
  { name: '1', label: '我发布的', icon: 'Upload' },
  { name: '2', label: '我下架的', icon: 'Remove' },
  { name: '3', label: '我收藏的', icon: 'Star' },
  { name: '4', label: '我卖出的', icon: 'Sell' },
  { name: '5', label: '我买到的', icon: 'ShoppingCart' }
]

// 修改标签页点击处理函数
const handleTabClick = (tabName) => {
  activeName.value = tabName
  getDataList(tabName)
}

// 初始化数据
onMounted(() => {
  getDataList('1')
  getAddressData()
})

// 编辑商品信息相关
const showEditItemDialog = ref(false)
const editItemForm = ref({ idleName: '', idleDetails: '', idlePrice: '', id: '', pictureListArr: [] })
let currentItem = ref(null)

const handleEditItem = (item) => {
  currentItem.value = item
  let picArr = []
  if (item.pictureList) {
    try {
      const arr = typeof item.pictureList === 'string' ? JSON.parse(item.pictureList) : item.pictureList
      // 保证格式为 /image?imageName=xxx.jpg
      picArr = arr && arr.length > 0 ? arr.map(url => {
        if (/^\/image\?imageName=/.test(url)) return url
        // 兼容老数据
        const fileName = url.replace(/^.*imageName=/, '')
        return `/image?imageName=${fileName}`
      }) : []
    } catch (e) { picArr = [] }
  }
  editItemForm.value = {
    idleName: item.idleName || '',
    idleDetails: item.idleDetails || '',
    idlePrice: item.idlePrice ? String(item.idlePrice).replace('¥', '') : '',
    id: item.id,
    pictureListArr: picArr
  }
  showEditItemDialog.value = true
}

const handleEditItemImageSuccess = (response) => {
  if (response.status_code === 1) {
    // 只取文件名部分，拼接成 /image?imageName=xxx.jpg
    let fileName = response.data.replace(/^.*imageName=/, '')
    let imgUrl = `/image?imageName=${fileName}`
    if (!editItemForm.value.pictureListArr) editItemForm.value.pictureListArr = []
    editItemForm.value.pictureListArr.push(imgUrl)
  } else {
    ElMessage.error('图片上传失败')
  }
}

const removeEditItemImage = (idx) => {
  editItemForm.value.pictureListArr.splice(idx, 1)
}

const beforeEditItemImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const saveEditItem = async () => {
  try {
    if (!editItemForm.value.idleName || !editItemForm.value.idlePrice) {
      ElMessage.warning('请填写完整的商品信息')
      return
    }
    // 保证所有图片url为 /image?imageName=xxx.jpg 格式
    const picList = (editItemForm.value.pictureListArr || []).map(url => {
      if (/^\/image\?imageName=/.test(url)) return url
      const fileName = url.replace(/^.*imageName=/, '')
      return `/image?imageName=${fileName}`
    })
    const params = {
      id: editItemForm.value.id,
      idleName: editItemForm.value.idleName,
      idleDetails: editItemForm.value.idleDetails,
      idlePrice: editItemForm.value.idlePrice,
      pictureList: JSON.stringify(picList)
    }
    const res = await api.updateIdleItem(params)
    if (res.status_code === 1) {
      ElMessage.success('商品信息修改成功')
      showEditItemDialog.value = false
      getDataList(activeName.value)
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch (error) {
    console.error('修改商品信息失败:', error)
    ElMessage.error('修改失败')
  }
}

// 下架商品
const handleOffShelf = async (item, index) => {
  try {
    const res = await api.updateIdleItem({
      id: item.id,
      idleStatus: 2
    })
    if (res.status_code === 1) {
      ElMessage.success('商品已下架')
      dataList.value[0].splice(index, 1)
      getDataList('2')
    } else {
      ElMessage.error(res.msg || '下架失败')
    }
  } catch (error) {
    console.error('下架失败:', error)
    ElMessage.error('下架失败')
  }
}

// 重新上架商品
const handleRePublish = async (item, index) => {
  try {
    const res = await api.updateIdleItem({
      id: item.id,
      idleStatus: 1
    })
    if (res.status_code === 1) {
      ElMessage.success('商品已重新上架')
      dataList.value[1].splice(index, 1)
      getDataList('1')
    } else {
      ElMessage.error(res.msg || '重新上架失败')
    }
  } catch (error) {
    console.error('重新上架失败:', error)
    ElMessage.error('重新上架失败')
  }
}

// 删除商品
const handleDelete = async (item, index) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await api.updateIdleItem({
      id: item.id,
      idleStatus: 0
    })

    if (res.status_code === 1) {
      ElMessage.success('商品已删除')
      dataList.value[Number(activeName.value) - 1].splice(index, 1)
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 取消收藏
const handleCancelFavorite = async (item) => {
  try {
    const res = await api.deleteFavorite({
      id: item.id
    })
    if (res.status_code === 1) {
      ElMessage.success('取消收藏成功')
      getDataList('3')
    } else {
      ElMessage.error(res.msg || '取消收藏失败')
    }
  } catch (error) {
    console.error('取消收藏失败:', error)
    ElMessage.error('取消收藏失败')
  }
}

// 在 script setup 中添加发货功能
const shipOrder = async (item) => {
  try {
    await ElMessageBox.confirm('确认发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await api.updateOrder({
      id: item.id,
      orderStatus: 2, // 更新为待收货状态
      addressId: item.addressId,
      orderPrice: item.orderPrice
    })

    if (res.status_code === 1) {
      ElMessage.success('发货成功')
      getDataList('4') // 刷新列表
    } else {
      ElMessage.error(res.msg || '发货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error)
      ElMessage.error('发货失败，请稍后重试')
    }
  }
}

// 在 script setup 中添加处理点击事件的函数
const handleItemClick = (item) => {
  // 根据不同的标签页类型处理跳转逻辑
  if (activeName.value === '4' || activeName.value === '5') {
    // 对于订单类型，跳转到订单详情页
    router.push({
      path: '/order',
      query: { id: item.id }
    })
  } else {
    // 对于商品类型，跳转到商品详情页
    router.push({
      path: '/details',
      query: { id: item.idleItem?.id || item.id }
    })
  }
}

// 编辑表单规则
const editRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度应在 2 到 20 个字符之间', trigger: 'blur' }
  ]
}

const editFormRef = ref(null)

const handleCancelOrder = async (item, index) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    // 1. 让商品重新上架
    const res1 = await api.updateIdleItem({
      id: item.idleItem.id,
      idleStatus: 1
    });
    if (res1.status_code !== 1) {
      ElMessage.error(res1.msg || '商品重新上架失败');
      return;
    }

    // 2. 删除订单
    const res2 = await api.deleteOrder({
      id: item.id
    });
    if (res2.status_code === 1) {
      ElMessage.success('订单已取消，商品已重新上架');
      getDataList('5'); // 刷新"我买到的"列表
    } else {
      ElMessage.error(res2.msg || '订单删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败');
    }
  }
};

// 确认收货
const confirmOrder = async () => {
  try {
    await ElMessageBox.confirm('是否确认收货?', '提示', {
      type: 'warning'
    })

    // 1. 更新订单状态为已完成
    const res = await api.updateOrder({
      id: currentItem.value.id,
      userId: userStore.userInfo.id,
      orderStatus: 3,
      addressId: currentItem.value.addressId,
      orderPrice: currentItem.value.orderPrice
    })

    // 2. 同步更新商品状态为已完成（idleStatus: 3）
    if (res.status_code === 1) {
      await api.updateIdleItem({
        id: currentItem.value.idleItem.id,
        idleStatus: 3
      })
      ElMessage.success('确认收货成功')
      getDataList('5')
    } else {
      ElMessage.error(res.msg || '确认收货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败，请稍后重试')
    }
  }
}

const isEditingAddress = ref(false)
let editingAddressId = ref(null)

const handleEditAddress = (item) => {
  showAddressForm.value = true
  isEditingAddress.value = true
  editingAddressId.value = item.id
  addressInfo.value = {
    consigneeName: item.consigneeName,
    consigneePhone: item.consigneePhone,
    provinceName: item.provinceName,
    cityName: item.cityName,
    regionName: item.regionName,
    detailAddress: item.detailAddress,
    defaultFlag: item.defaultFlag
  }
  // 级联选择器回显
  selectedOptions.value = [item.provinceName, item.cityName, item.regionName].filter(Boolean)
}
</script>

<style scoped>
.profile-container {
  width: 100%;
  min-height: calc(100vh - 60px);
  background: transparent;
  padding: 20px;
  box-sizing: border-box;
  margin: 0 auto;
}

.profile-header {
  width: 100%;
  padding: 40px;
  margin-bottom: 30px;
  box-sizing: border-box;
}

.profile-header:hover {
  transform: none;
  box-shadow: none;
}

.profile-content {
  width: 100%;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-sizing: border-box;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 40px;
}

.avatar-section {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-upload {
  width: 100%;
  height: 100%;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-upload:hover .avatar-hover-text {
  opacity: 1;
}

.user-avatar {
  width: 100% !important;
  height: 100% !important;
  border: 3px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.avatar-hover-text {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.4);
  color: #fff;
  font-size: 14px;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 50%;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.username {
  font-size: 32px;
  margin: 0;
  font-weight: 600;
  color: #2c3e50;
  letter-spacing: 0.5px;
}

.join-time {
  font-size: 15px;
  margin: 0;
  color: #94a3b8;
  letter-spacing: 0.3px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons .el-button {
  height: 36px;
  padding: 0 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  border: none;
}

.action-buttons .el-button:first-child {
  background: #4096ff;
  color: white;
}

.action-buttons .el-button:last-child {
  background: #52c41a;
  color: white;
}

.action-buttons .el-button:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.action-buttons .el-icon {
  font-size: 16px;
}

.idle-container {
  width: 100%;
  padding: 0;
  box-sizing: border-box;
  background: transparent;
}

.idle-container-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  padding: 0;
  width: 100%;
  box-sizing: border-box;
  background: transparent;
}

.idle-container-list-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
  cursor: pointer;
}

.idle-container-list-item:hover {
  transform: translateY(-2px);
}

.idle-container-list-img {
  width: 100%;
  height: 160px;
  overflow: hidden;
}

.idle-container-list-info {
  padding: 10px;
}

.idle-container-list-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
  color: #333;
}

.idle-container-list-idle-details {
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  line-height: 1.4;
}

.idle-container-list-idle-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}

.idle-item-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px solid #f5f5f5;
}

.idle-price {
  font-size: 15px;
  color: #ff6b6b;
  font-weight: 500;
}

.item-actions {
  display: flex;
  gap: 8px;
}

.item-actions .el-button {
  padding: 4px 8px;
  font-size: 12px;
  background: transparent !important;
}

.item-actions .el-button--primary {
  color: #4096ff;
  border: none;
}

.item-actions .el-button--danger {
  color: #ff4d4f;
  border: none;
}

.item-actions .el-button--success {
  color: #52c41a;
  border: none;
}

.item-actions .el-button:hover {
  opacity: 0.8;
}

.image-slot {
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

.image-slot .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

/* 地址管理对话框样式 */
.address-container {
  display: flex;
  gap: 30px;
  min-height: 400px;
}

.address-list {
  flex: 1;
  border-right: 1px solid #f0f0f0;
  padding-right: 30px;
}

.address-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.address-list-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1f2937;
  font-weight: 600;
}

.address-list-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-item {
  padding: 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #4096ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.address-item.is-default {
  background: rgba(82, 196, 26, 0.05);
  border-color: #52c41a;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.name {
  font-weight: 600;
  color: #1f2937;
}

.phone {
  color: #6b7280;
}

.address-detail {
  color: #4b5563;
  line-height: 1.5;
  font-size: 14px;
}

.address-actions {
  margin-top: 12px;
  display: flex;
  gap: 12px;
}

.address-form {
  flex: 1;
  padding-left: 30px;
}

.address-form h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 24px;
  color: #1f2937;
}

.empty-address {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

:deep(.el-cascader) {
  width: 100%;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  min-height: 80px;
  resize: none;
}

:deep(.el-checkbox__label) {
  color: #4b5563;
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #4b5563;
}

:deep(.el-input__inner) {
  border-radius: 8px;
  border-color: #e5e7eb;
}

:deep(.el-input__inner:focus) {
  border-color: #4096ff;
  box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.1);
}

:deep(.el-button--primary) {
  border: none;
}

:deep(.el-button--default) {
  border-color: #e5e7eb;
  color: #6b7280;
}

:deep(.el-button--default:hover) {
  color: #4096ff;
  border-color: #4096ff;
  background: rgba(64, 150, 255, 0.1);
}

/* 地址管理对话框特殊样式 */
.address-container {
  display: flex;
  gap: 30px;
  min-height: 400px;
}

.address-list {
  flex: 1;
  border-right: 1px solid #f0f0f0;
  padding-right: 30px;
}

.address-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.address-list-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1f2937;
  font-weight: 600;
}

.address-list-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.address-item {
  padding: 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  transition: all 0.3s;
}

.address-item:hover {
  border-color: #4096ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.address-item.is-default {
  background: rgba(82, 196, 26, 0.05);
  border-color: #52c41a;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.name {
  font-weight: 600;
  color: #1f2937;
}

.phone {
  color: #6b7280;
}

.address-detail {
  color: #4b5563;
  line-height: 1.5;
  font-size: 14px;
}

.address-actions {
  margin-top: 12px;
  display: flex;
  gap: 12px;
}

.address-form {
  flex: 1;
  padding-left: 30px;
}

.address-form h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 24px;
  color: #1f2937;
}

:deep(.el-cascader) {
  width: 100%;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  min-height: 80px;
  resize: none;
}

:deep(.el-checkbox__label) {
  color: #4b5563;
}

.tabs-header {
  width: 100%;
  display: flex;
  gap: 20px;
  padding: 0 0 20px 0;
  margin-bottom: 20px;
  border-bottom: 1px solid #e5e7eb;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #64748b;
  background: transparent;
  font-weight: 400;
}

.tab-item:hover {
  color: #333;
}

.tab-item.active {
  color: #333;
  font-weight: 600;
  background: transparent;
}

.tab-item .el-icon {
  font-size: 16px;
}

.idle-container-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  padding: 0;
  width: 100%;
  box-sizing: border-box;
  background: transparent;
}

.idle-container-list-item {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  transition: all 0.3s;
  cursor: pointer;
}

.idle-container-list-item:hover {
  transform: translateY(-2px);
}

.idle-container-list-img {
  width: 100%;
  height: 160px;
  overflow: hidden;
}

.idle-container-list-info {
  padding: 10px;
}

.idle-container-list-title {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 6px;
  color: #333;
}

.idle-container-list-idle-details {
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
  line-height: 1.4;
}

.idle-container-list-idle-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}

.idle-item-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px solid #f5f5f5;
}

.idle-price {
  font-size: 15px;
  color: #ff6b6b;
  font-weight: 500;
}

.item-actions {
  display: flex;
  gap: 8px;
}

.item-actions .el-button {
  padding: 4px 8px;
  font-size: 12px;
}

.item-actions .el-button--primary {
  color: #4096ff;
}

.item-actions .el-button--danger {
  color: #ff4d4f;
}

.item-actions .el-button--success {
  color: #52c41a;
}

.edit-form {
  padding: 20px 0;
}

:deep(.el-dialog__body) {
  padding: 0 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #1f2937;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e5e7eb;
  border-radius: 8px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #4096ff;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.2);
}

:deep(.el-input__count) {
  background: transparent;
  font-size: 12px;
  color: #94a3b8;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.password-form {
  padding: 20px 0;
}

:deep(.el-dialog__body) {
  padding: 0 24px;
}

:deep(.el-button--warning) {
  background: #faad14;
  color: white;
  border: none;
}

:deep(.el-button--warning:hover) {
  background: #ffc53d;
  opacity: 0.9;
}

/* 新增样式 */
.edit-item-multi-image {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.edit-item-image-wrapper {
  position: relative;
  display: inline-block;
}
.edit-item-image-preview {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  background: #f5f7fa;
}
.edit-item-image-remove {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #fff;
  border-radius: 50%;
  color: #f56c6c;
  font-size: 18px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  z-index: 2;
}
.edit-item-uploader {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-bottom: 0;
}
.edit-item-uploader-add {
  font-size: 40px;
  color: #c0c4cc;
  width: 80px;
  height: 80px;
  line-height: 80px;
  text-align: center;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  background: #f5f7fa;
}
</style>
