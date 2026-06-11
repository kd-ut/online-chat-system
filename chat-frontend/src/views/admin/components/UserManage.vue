<template>
  <el-card class="section-card">
    <template #header>
      <div class="card-header-row">
        <span class="card-title">用户管理</span>
        <div class="card-toolbar">
          <el-input v-model="keyword" placeholder="搜索用户名/昵称..." class="search-input"
            prefix-icon="Search" clearable @clear="onSearch" @keyup.enter="onSearch" />
        </div>
      </div>
    </template>

    <ConfirmDialog v-model="showToggleDialog" title="提示" :message="toggleMsg" type="warning"
      confirm-text="确定" cancel-text="取消" @confirm="confirmToggle" />
    <el-table :data="users" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="nickname" label="昵称" width="150" />
      <el-table-column prop="role" label="角色" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : 'info'" size="small" effect="plain">
            {{ row.role === 'admin' ? '管理员' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" effect="plain">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="180" />
      <el-table-column label="操作" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.role !== 'admin'" :type="row.status === 1 ? 'danger' : 'success'" size="small"
            @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <span v-else class="admin-badge">—</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
      layout="total, sizes, prev, pager, next" @current-change="loadUsers" @size-change="loadUsers" />
  </el-card>
</template>

<script setup lang="ts">
/** 用户管理页面组件 — 支持搜索、分页和禁用/启用用户 @component */
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminUsersApi, updateUserStatusApi, type UserManageVO } from '@/api/admin'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'

const users = ref<UserManageVO[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')
const loading = ref(false)
const showToggleDialog = ref(false)
const toggleMsg = ref('')
const toggleTarget = ref<{ id: number; newStatus: number } | null>(null)

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getAdminUsersApi(currentPage.value, pageSize.value, keyword.value || undefined)
    users.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  currentPage.value = 1
  loadUsers()
}

const toggleStatus = async (row: UserManageVO) => {
  const newStatus = row.status === 1 ? 0 : 1
  toggleMsg.value = newStatus === 0 ? '确定要禁用该用户吗？' : '确定要启用该用户吗？'
  toggleTarget.value = { id: row.id, newStatus }
  showToggleDialog.value = true
}

const confirmToggle = async () => {
  if (!toggleTarget.value) return
  await updateUserStatusApi(toggleTarget.value.id, toggleTarget.value.newStatus)
  ElMessage.success('操作成功')
  loadUsers()
}

let debounceTimer: ReturnType<typeof setTimeout> | null = null
watch(keyword, () => {
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    currentPage.value = 1
    loadUsers()
  }, 300)
})

loadUsers()
</script>

<style scoped>
.section-card {
  margin-bottom: 0;
}

.card-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.card-title {
  font-weight: 600;
  font-size: 15px;
}

.search-input {
  width: 260px;
}

.admin-badge {
  color: var(--text-placeholder);
  font-size: 14px;
}
</style>
