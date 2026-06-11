<template>
  <div class="notification-manage">
    <el-card>
      <template #header>
        <span class="card-title">发布系统通知</span>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="通知标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入通知标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="通知内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入通知内容"
            maxlength="2000" show-word-limit />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSend" :loading="sending">
            <el-icon><Promotion /></el-icon>
            <span>发送通知</span>
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <span class="card-title">已发送通知</span>
      </template>

      <el-table :data="sentNotifications" v-loading="loading" empty-text="暂无已发送通知">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="发送时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
/** 系统通知管理页面组件 @component */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import { sendNotificationApi, getAdminNotificationsApi, type SystemNotification } from '@/api/notification'

const formRef = ref()
const sending = ref(false)
const loading = ref(false)
const sentNotifications = ref<SystemNotification[]>([])

const form = reactive({ title: '', content: '' })

const rules = {
  title: [{ required: true, message: '请输入通知标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入通知内容', trigger: 'blur' }]
}

const handleSend = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  sending.value = true
  try {
    await sendNotificationApi(form.title, form.content)
    ElMessage.success('通知已发送')
    resetForm()
    loadSentNotifications()
  } catch {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const resetForm = () => {
  form.title = ''
  form.content = ''
  formRef.value?.resetFields()
}

const loadSentNotifications = async () => {
  loading.value = true
  try {
    const res = await getAdminNotificationsApi()
    sentNotifications.value = res || []
  } catch {
    sentNotifications.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadSentNotifications()
})
</script>

<style scoped>
.card-title {
  font-weight: 600;
  font-size: 15px;
}
</style>
