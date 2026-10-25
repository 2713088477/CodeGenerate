<script setup lang="ts">
import { delete_, page, update } from '@/api'
import type { AdminUserUpdateRequest, AdminUserVo, PageResultAdminUserVo, UserQueryRequest } from '@/api'
import { ensureSuccess, errorMessage, readData } from '@/utils/response'
import type { FormInstance, FormItemRule, FormRules, TableInstance } from 'element-plus'
import {
  ElAvatar,
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElOption,
  ElPagination,
  ElSelect,
  ElTable,
  ElTableColumn,
  vLoading,
} from 'element-plus'
import { nextTick, onMounted, reactive, shallowRef, useTemplateRef } from 'vue'

const roleOptions = [
  { label: '管理员', value: 'admin' },
  { label: '用户', value: 'user' },
]

const tableRef = useTemplateRef<TableInstance>('userTable')
const loading = shallowRef(false)
const pageResult = reactive<Required<PageResultAdminUserVo>>({
  records: [],
  pageNum: 1,
  pageSize: 10,
  total: 0,
  totalPage: 0,
})
const userAccount = shallowRef('')
const username = shallowRef('')
const role = shallowRef('')
const sortField = shallowRef<string>()
const asc = shallowRef(true)
const editVisible = shallowRef(false)
const saving = shallowRef(false)
const editingId = shallowRef<string>()
const editingAccount = shallowRef('')
const editFormRef = useTemplateRef<FormInstance>('editFormRef')
const editForm = reactive<AdminUserUpdateRequest>({
  username: '',
  avatar: '',
  bio: '',
  userPassword: '',
})

const validatePassword: FormItemRule['validator'] = (_rule, value, callback) => {
  if (typeof value !== 'string' || value === '') {
    callback()
    return
  }
  if (value.length < 6 || value.length > 64) {
    callback(new Error('密码长度为 6 到 64 位'))
    return
  }
  callback()
}

const editRules: FormRules<AdminUserUpdateRequest> = {
  username: [{ max: 32, message: '用户名长度不能超过 32', trigger: 'blur' }],
  avatar: [{ max: 1024, message: '头像地址长度不能超过 1024', trigger: 'blur' }],
  bio: [{ max: 512, message: '个人简介长度不能超过 512', trigger: 'blur' }],
  userPassword: [{ validator: validatePassword, trigger: 'blur' }],
}

function applyPage(data: PageResultAdminUserVo) {
  pageResult.records = data.records ?? []
  pageResult.pageNum = data.pageNum ?? pageResult.pageNum
  pageResult.pageSize = data.pageSize ?? pageResult.pageSize
  pageResult.total = data.total ?? 0
  pageResult.totalPage = data.totalPage ?? 0
}

function buildQuery(): UserQueryRequest {
  const query: UserQueryRequest = {
    pageNum: pageResult.pageNum,
    pageSize: pageResult.pageSize,
  }
  const account = userAccount.value.trim()
  const name = username.value.trim()
  if (account) {
    query.userAccount = account
  }
  if (name) {
    query.username = name
  }
  if (role.value) {
    query.role = role.value
  }
  if (sortField.value) {
    query.sortField = sortField.value
    query.asc = asc.value
  }
  return query
}

async function loadUsers() {
  loading.value = true
  try {
    const result = await page({
      query: { query: buildQuery() },
      // 生成类型把条件包在 query 对象里，Spring 按字段名绑定，所以展开成顶层参数
      querySerializer: {
        object: { explode: true, style: 'form' },
      },
    })
    applyPage(readData<PageResultAdminUserVo>(result))
  } catch (error) {
    pageResult.records = []
    pageResult.total = 0
    pageResult.totalPage = 0
    ElMessage.error(errorMessage(error))
  } finally {
    loading.value = false
  }
}

function onSearch() {
  pageResult.pageNum = 1
  void loadUsers()
}

function onReset() {
  userAccount.value = ''
  username.value = ''
  role.value = ''
  sortField.value = undefined
  asc.value = true
  pageResult.pageNum = 1
  tableRef.value?.clearSort()
  void loadUsers()
}

function onPageChange() {
  void loadUsers()
}

function onSizeChange() {
  pageResult.pageNum = 1
  void loadUsers()
}

function onSortChange(payload: { prop: string | null; order: 'ascending' | 'descending' | null }) {
  sortField.value = payload.order && payload.prop ? payload.prop : undefined
  asc.value = payload.order !== 'descending'
  pageResult.pageNum = 1
  void loadUsers()
}

function roleText(value?: string) {
  return roleOptions.find((item) => item.value === value)?.label || value || ''
}

function formatTime(value?: string) {
  if (!value) {
    return ''
  }
  return value.replace('T', ' ').slice(0, 19)
}

function avatarText(row: AdminUserVo) {
  return (row.username || row.userAccount || '').slice(0, 1)
}

function openEdit(row: AdminUserVo) {
  if (row.id == null) {
    return
  }
  editingId.value = row.id
  editingAccount.value = row.userAccount ?? ''
  editForm.username = row.username ?? ''
  editForm.avatar = row.avatar ?? ''
  editForm.bio = row.bio ?? ''
  editForm.userPassword = ''
  editVisible.value = true
  void nextTick(() => editFormRef.value?.clearValidate())
}

async function submitEdit() {
  if (editingId.value == null || saving.value) {
    return
  }
  try {
    await editFormRef.value?.validate()
  } catch {
    return
  }
  const body: AdminUserUpdateRequest = {
    username: editForm.username ?? '',
    avatar: editForm.avatar ?? '',
    bio: editForm.bio ?? '',
  }
  if (editForm.userPassword) {
    body.userPassword = editForm.userPassword
  }
  saving.value = true
  try {
    readData<AdminUserVo>(await update({ path: { id: editingId.value }, body }))
    ElMessage.success('已保存')
    editVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(errorMessage(error))
  } finally {
    saving.value = false
  }
}

async function onDelete(row: AdminUserVo) {
  if (row.id == null) {
    return
  }
  const name = row.userAccount || row.username || '该用户'
  try {
    await ElMessageBox.confirm(`确定删除账号 ${name} 吗？`, '删除用户', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    ensureSuccess(await delete_({ path: { id: row.id } }))
    ElMessage.success('已删除')
    if (pageResult.records.length === 1 && pageResult.pageNum > 1) {
      pageResult.pageNum -= 1
    }
    await loadUsers()
  } catch (error) {
    ElMessage.error(errorMessage(error))
  }
}

onMounted(() => {
  void loadUsers()
})
</script>

<template>
  <section class="user-page">
      <el-form class="search-form" :inline="true" @submit.prevent="onSearch">
        <el-form-item label="账号">
          <el-input v-model="userAccount" class="search-input" placeholder="请输入账号" clearable />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="username" class="search-input" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="role" class="search-select" placeholder="全部" clearable>
            <el-option
              v-for="item in roleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button type="primary" native-type="submit">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div v-loading="loading" class="table-panel">
        <el-table ref="userTable" :data="pageResult.records" @sort-change="onSortChange">
          <el-table-column prop="userAccount" label="账号" min-width="140" sortable="custom" show-overflow-tooltip />
          <el-table-column prop="username" label="用户名" min-width="120" sortable="custom" show-overflow-tooltip />
          <el-table-column label="头像" width="80">
            <template #default="{ row }">
              <el-avatar :size="32" :src="row.avatar || undefined">{{ avatarText(row) }}</el-avatar>
            </template>
          </el-table-column>
          <el-table-column prop="bio" label="简介" min-width="180" show-overflow-tooltip />
          <el-table-column prop="role" label="角色" width="110" sortable="custom">
            <template #default="{ row }">{{ roleText(row.role) }}</template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="180" sortable="custom">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" min-width="180" sortable="custom">
            <template #default="{ row }">{{ formatTime(row.updatedAt) }}</template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="140">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="onDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="table-footer">
          <el-pagination
            v-model:current-page="pageResult.pageNum"
            v-model:page-size="pageResult.pageSize"
            :total="pageResult.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            background
            @current-change="onPageChange"
            @size-change="onSizeChange"
          />
        </div>
      </div>

      <el-dialog v-model="editVisible" title="编辑用户" width="480px">
        <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="72px">
          <el-form-item label="账号">
            <el-input :model-value="editingAccount" disabled />
          </el-form-item>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="editForm.username" maxlength="32" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="头像" prop="avatar">
            <el-input v-model="editForm.avatar" maxlength="1024" placeholder="请输入头像地址" clearable />
          </el-form-item>
          <el-form-item label="简介" prop="bio">
            <el-input v-model="editForm.bio" type="textarea" maxlength="512" placeholder="请输入简介" />
          </el-form-item>
          <el-form-item label="密码" prop="userPassword">
            <el-input
              v-model="editForm.userPassword"
              type="password"
              maxlength="64"
              show-password
              placeholder="不修改请留空"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
        </template>
      </el-dialog>
  </section>
</template>

<style scoped>
.user-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
  padding: 16px;
  border-radius: 8px;
  background: var(--el-bg-color);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.search-input,
.search-select {
  width: 200px;
}

.search-actions {
  margin-right: 0;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
