<template>
  <el-card class="section-card">
    <template #header>
      <div class="card-header-row">
        <span class="card-title">聊天记录审计</span>
      </div>
    </template>

    <div class="filter-toolbar">
      <div class="filter-row">
        <div class="filter-item">
          <label>发送者ID</label>
          <el-input v-model="filter.fromUserId" placeholder="输入发送者ID" clearable size="default" />
        </div>
        <div class="filter-item">
          <label>接收者ID</label>
          <el-input v-model="filter.toUserId" placeholder="输入接收者ID" clearable size="default" />
        </div>
        <div class="filter-item">
          <label>开始日期</label>
          <el-date-picker v-model="filter.startTime" type="date" placeholder="选择日期" size="default" />
        </div>
        <div class="filter-item">
          <label>结束日期</label>
          <el-date-picker v-model="filter.endTime" type="date" placeholder="选择日期" size="default" />
        </div>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <el-table :data="messages" border stripe v-loading="loadingMsg">
      <el-table-column prop="id" label="消息ID" width="80" align="center" />
      <el-table-column prop="fromUserNickname" label="发送者" width="120" />
      <el-table-column prop="fromUserId" label="发送者ID" width="90" />
      <el-table-column prop="toUserNickname" label="接收者" width="120" />
      <el-table-column prop="toUserId" label="接收者ID" width="90" />
      <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
      <el-table-column prop="sendTime" label="发送时间" width="180" />
    </el-table>

    <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
      layout="total, sizes, prev, pager, next" @current-change="loadMessages" @size-change="loadMessages" />
  </el-card>
</template>

<script setup lang="ts">
/** 消息审计页面组件 — 支持按条件搜索和分页查看聊天记录 @component */
import { ref } from 'vue'
import { getAdminMessagesApi } from '@/api/admin'
import { formatDate } from '@/utils/date'

const messages = ref<any[]>([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loadingMsg = ref(false)

const filter = ref({
  fromUserId: '',
  toUserId: '',
  startTime: '',
  endTime: ''
})

const loadMessages = async () => {
  loadingMsg.value = true
  try {
    const params: any = { page: currentPage.value, size: pageSize.value }
    if (filter.value.fromUserId) params.fromUserId = filter.value.fromUserId
    if (filter.value.toUserId) params.toUserId = filter.value.toUserId
    if (filter.value.startTime) params.startTime = formatDate(filter.value.startTime, 'YYYY-MM-DD')
    if (filter.value.endTime) params.endTime = formatDate(filter.value.endTime, 'YYYY-MM-DD')

    const res = await getAdminMessagesApi(params)
    messages.value = res.records
    total.value = res.total
  } finally {
    loadingMsg.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  loadMessages()
}

const handleReset = () => {
  filter.value = { fromUserId: '', toUserId: '', startTime: '', endTime: '' }
  currentPage.value = 1
  loadMessages()
}

loadMessages()
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

.filter-toolbar {
  background: var(--bg-color);
  border-radius: var(--border-radius-small);
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid var(--border-color-light);
}

.filter-row {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.filter-item label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.filter-item .el-input,
.filter-item .el-date-picker {
  width: 160px;
}

.filter-actions {
  display: flex;
  gap: 8px;
  padding-bottom: 1px;
}
</style>
