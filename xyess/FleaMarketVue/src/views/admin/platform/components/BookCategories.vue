<template>
  <div class="book-categories">
    <div class="header">
      <el-button type="primary" @click="handleAdd">添加分类</el-button>
    </div>

    <el-table :data="categoryList" style="width: 100%" v-loading="loading">
      <el-table-column label="序号" width="80">
        <template #default="scope">
          {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
        </template>
      </el-table-column>
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip>
        <template #default="scope">
          {{ scope.row.description || '无' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="scope">
          {{ scope.row.createTime || '' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button 
            type="primary" 
            size="small" 
            @click="handleEdit(scope.row)"
          >
            编辑
          </el-button>
          <el-button 
            type="danger" 
            size="small" 
            @click="handleDelete(scope.row.id)"
          >
            删除
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
      />
    </div>

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加分类' : '编辑分类'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            placeholder="请输入分类描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/api'

const allCategories = ref([]) // 存储所有分类数据
const categoryList = ref([]) // 当前页显示的数据
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)

// 对话框相关
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const categoryForm = ref({
  id: null,
  name: '',
  description: ''
})

// 获取所有分类列表
const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await api.listType({
      begin: 0,
      size: 1000 // 获取所有数据
    })
    console.log('分类列表响应:', res)
    if (res.status_code === 1) {
      allCategories.value = res.data || []
      total.value = allCategories.value.length
      updatePageData() // 更新当前页数据
      console.log('处理后的分类列表:', categoryList.value)
    } else {
      ElMessage.error(res.msg || '获取分类列表失败')
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
    ElMessage.error('获取分类列表失败')
  }
  loading.value = false
}

// 更新当前页数据
const updatePageData = () => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  categoryList.value = allCategories.value.slice(start, end)
}

// 添加分类
const handleAdd = () => {
  dialogType.value = 'add'
  categoryForm.value = {
    id: null,
    name: '',
    description: ''
  }
  dialogVisible.value = true
}

// 编辑分类
const handleEdit = (row) => {
  dialogType.value = 'edit'
  categoryForm.value = {
    id: row.id,
    name: row.name,
    description: row.description
  }
  dialogVisible.value = true
}

// 删除分类
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await api.deleteType({ id })
    if (res.status_code === 1) {
      ElMessage.success('删除成功')
      fetchCategories()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!categoryForm.value.name) {
    ElMessage.warning('请输入分类名称')
    return
  }

  try {
    const data = {
      ...categoryForm.value
    }
    const res = dialogType.value === 'add' 
      ? await api.addType(data)
      : await api.updateType(data)
    
    if (res.status_code === 1) {
      ElMessage.success(dialogType.value === 'add' ? '添加成功' : '更新成功')
      dialogVisible.value = false
      fetchCategories()
      categoryForm.value = {
        id: null,
        name: '',
        description: ''
      }
    } else {
      ElMessage.error(res.msg || (dialogType.value === 'add' ? '添加失败' : '更新失败'))
    }
  } catch (error) {
    console.error(dialogType.value === 'add' ? '添加失败:' : '更新失败:', error)
    ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败')
  }
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1 // 重置到第一页
  updatePageData()
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  updatePageData()
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.book-categories {
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
  justify-content: flex-end;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 20px;
  background-color: #f8faff;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: #1f2937;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  background-color: #f8faff;
  border-top: 1px solid #f0f0f0;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #374151;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
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