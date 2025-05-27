import request from '@/utils/request'

// API 接口
const api = {
  // 用户相关
  login: data => request({
    url: '/user/login',
    method: 'post',
    data: new URLSearchParams(data).toString(),
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  }),
  register: data => request.post('/user/sign-in', data),
  logout: () => request({
    url: '/user/logout',
    method: 'get'
  }),
  getUserInfo: (id) => request({
    url: '/user/info',
    method: 'get',
    params: { id }
  }),
  updateUserInfo: (data) => request({
    url: '/user/info',
    method: 'post',
    data
  }),
  updateUserPassword: (data) => request({
    url: '/user/password',
    method: 'get',
    params: {
      id: data.id,
      oldPassword: data.oldPassword,
      newPassword: data.newPassword
    }
  }),

  // 商品相关
  getIdleItem: params => request.get('/idle/info', { params }),
  getAllIdleItem: (params) => request.get('/idle/all', { params }),
  addIdleItem: data => request.post('/idle/add', data),
  updateIdleItem: data => request.post('/idle/update', data),
  findIdleItem: params => request.get('/idle/find', { params }),
  findIdleItemByLabel: params => request.get('/idle/lable', { params }),
  getUserIdleItems: params => request.get('/idle/find', { 
    params: {
      findValue: '',
      page: 1,
      nums: 100,
      userId: params.userId,
      idleStatus: params.idleStatus
    }
  }),
  listType: params => request.get('/type/listByCondition', { params }),

  // 订单相关
  getMyOrders: params => request({
    url: '/order/my',
    method: 'get',
    params
  }),
  getMySoldOrders: params => request({
    url: '/order/my-sold',
    method: 'get',
    params
  }),
  addOrder: data => request({
    url: '/order/add',
    method: 'post',
    data
  }),
  updateOrder: data => request({
    url: '/order/update',
    method: 'post',
    data
  }),
  getOrderInfo: params => request({
    url: '/order/info',
    method: 'get',
    params
  }),
  deleteOrder: params => request({
    url: '/order/delete',
    method: 'post',
    params
  }),

  // 收藏相关
  addFavorite: data => request({
    url: '/favorite/add',
    method: 'post',
    data
  }),
  deleteFavorite: params => request({
    url: '/favorite/delete',
    method: 'get',
    params
  }),
  checkFavorite: params => request({
    url: '/favorite/check',
    method: 'get',
    params
  }),
  getMyFavorite: params => request({
    url: '/favorite/my',
    method: 'get',
    params
  }),
  cancelFavorite: (id) => request({
    url: '/favorite/delete',
    method: 'get',
    params: { id },
    withCredentials: true
  }),

  // 消息相关
  getUnreadMessageCount: (userId) => request({
    url: '/message/unread-count',
    method: 'get',
    params: { userId }
  }),
  // 获取闲置物品的留言
  getIdleMessages: params => request({
    url: '/message/idle',
    method: 'get',
    params
  }),
  // 发送消息
  sendMessage: data => request({
    url: '/message/send',
    method: 'post',
    data
  }),
  // 获取我的所有消息
  getMyMessages: (userId) => request({
    url: '/message/my',
    method: 'get',
    params: { userId }
  }),
  // 删除消息
  deleteMessage: (params) => request({
    url: '/message/delete',
    method: 'get',
    params
  }),
  // 更新消息已读状态
  updateMessageStatus: (messageId) => request({
    url: '/message/read',
    method: 'post',
    params: { messageId }
  }),

  // 管理员相关
  adminLogin: data => request.post('/admin/login', data),
  adminLoginOut: data => request.post('/admin/loginOut', data),
  getUserData: params => request.get('/admin/userList', { params }),
  getUserManage: params => request.get('/admin/manages', { params }),
  updateUserStatus: params => request.get('/admin/updateUserStatus', { params }),
  // 管理员商品管理
  idleList: params => request.get('/admin/idleList', { params }),
  updateIdleStatus: params => request.get('/admin/updateIdleStatus', { params }),
  // 管理员订单管理
  orderList: params => request.get('/admin/orderList', { params }),
  // deleteOrder: params => request.get('/order/delete', { params }),
  // 书籍分类管理
  listType: params => request({
    url: '/type/listByCondition',
    method: 'get',
    params: {
      begin: params.begin,
      size: params.size
    }
  }),
  addType: data => request.post('/type/add', data),
  updateType: data => request.put('/type/update', data),
  deleteType: params => request.delete('/type/delete', { params }),

  // 地址相关
  getAddressList: (data) => request({
    url: '/address/info',
    method: 'get',
    params: {
      userId: data.userId
    }
  }),
  getAddressById: (id, userId) => request({
    url: '/address/info',
    method: 'get',
    params: { 
      id,
      userId 
    }
  }),
  addAddress: (data) => request({
    url: '/address/add',
    method: 'post',
    data
  }),
  updateAddress: (data) => request({
    url: '/address/update',
    method: 'post',
    data
  }),
  deleteAddress: (data) => request({
    url: '/address/delete',
    method: 'post',
    data
  }),
  setDefaultAddress: id => request({
    url: `/user/address/${id}/default`,
    method: 'put'
  }),

  // 订单地址相关
  addOrderAddress: (data) => request({
    url: '/order-address/add',
    method: 'post',
    data
  }),
  updateOrderAddress: (data) => request({
    url: '/order-address/update',
    method: 'post',
    data
  }),
  getOrderAddress: (orderId) => request({
    url: '/order-address/info',
    method: 'get',
    params: { orderId }
  }),

  // 商品状态相关
  updateIdleStatus: data => request.post('/idle/update', data),
  offShelfItem: (id) => request({
    url: '/idle/update',
    method: 'post',
    data: {
      id,
      idleStatus: 2 // 2表示下架状态
    }
  }),
  deleteItem: id => request({
    url: `/item/${id}`,
    method: 'delete'
  }),
  cancelFavorite: id => request({
    url: `/favorite/${id}`,
    method: 'delete'
  }),

  // 文件上传
  uploadFile: (file, type = '') => {
    const formData = new FormData()
    formData.append('file', file)
    if (type) {
      formData.append('type', type)
    }
    return request({
      url: '/file/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 发送留言
  addMessage: (data) => request({
    url: '/message/send',
    method: 'post',
    data
  }),

  // 获取留言列表
  getMessage: (params) => request({
    url: '/message/idle',
    method: 'get',
    params
  }),

  // 获取用户的所有留言
  getAllMyMessage: (userId) => request({
    url: '/message/my',
    method: 'get',
    params: { userId }
  }),

  // 获取商品推荐
  getRecommendedItems: (params) => request({
    url: '/api/recommendation/items',
    method: 'get',
    params
  }),

  // 更新用户行为记录
  updateUserBehavior: (idleId, behaviorType, userId) => request({
    url: '/user-behavior/add',
    method: 'post',
    data: {
      idleId,
      behaviorType,
      userId
    }
  }),

  // 搜索历史相关
  getSearchHistory: (limit = 10) => request({
    url: '/search-history/recent',
    method: 'get',
    params: { limit }
  }),

  addSearchHistory: (keyword) => request({
    url: '/search-history/add',
    method: 'post',
    params: { keyword }
  }),

  deleteSearchHistory: (id) => request({
    url: `/search-history/${id}`,
    method: 'delete'
  }),

  clearSearchHistory: () => request({
    url: '/search-history/clear',
    method: 'delete'
  })
}

export default api
