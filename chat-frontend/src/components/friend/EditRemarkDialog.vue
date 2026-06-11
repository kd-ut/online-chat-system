<template>
  <BaseDialog v-model="visible" title="修改备注" width="420px" top="12vh">
    <div class="remark-content">
      <div class="friend-info">
        <el-avatar :size="40" :src="friend?.avatar || ''">
          {{ (friend?.nickname || 'U').charAt(0) }}
        </el-avatar>
        <span class="friend-name">{{ friend?.nickname }}</span>
      </div>
      <el-input
        v-model="remark"
        placeholder="输入备注（留空则清除备注）"
        maxlength="30"
        show-word-limit
        clearable
        class="remark-input"
        @keyup.enter="handleSave"
      />
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-cancel" @click="visible = false">取消</el-button>
        <el-button type="primary" class="btn-save" @click="handleSave" :loading="saving">保存</el-button>
      </div>
    </template>
  </BaseDialog>
</template>

<script setup lang="ts">
/** 修改好友备注对话框 @component */
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import BaseDialog from '@/components/common/BaseDialog.vue'
import { updateFriendRemarkApi } from '@/api/friend'

const props = defineProps<{
  modelValue: boolean
  friend: { id: number; nickname: string; avatar: string | null; remark: string | null } | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'success'): void
}>()

const visible = ref(false)
const remark = ref('')
const saving = ref(false)

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.friend) {
    remark.value = props.friend.remark || ''
  }
})

watch(visible, (val) => emit('update:modelValue', val))

const handleSave = async () => {
  if (!props.friend) return
  saving.value = true
  try {
    await updateFriendRemarkApi(props.friend.id, remark.value)
    ElMessage.success('备注修改成功')
    visible.value = false
    emit('success')
  } catch (e: any) {
    ElMessage.error(e?.message || '修改失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.remark-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.friend-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}

.remark-input :deep(.el-input__wrapper) {
  border-radius: 10px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.btn-cancel, .btn-save {
  min-width: 80px;
  height: 38px;
  border-radius: 10px !important;
  font-weight: 600 !important;
}
</style>
