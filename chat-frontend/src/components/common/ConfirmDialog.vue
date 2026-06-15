<template>
  <el-dialog v-model="visible" :title="title" :width="width" :before-close="handleCancel"
    class="beautiful-confirm" top="30vh" center>
    <div class="confirm-body">
      <div class="confirm-icon" :class="type">
        <el-icon :size="36">
          <WarningFilled v-if="type === 'warning'" />
          <CircleCloseFilled v-else-if="type === 'danger'" />
          <SuccessFilled v-else />
        </el-icon>
      </div>
      <div class="confirm-message" v-html="message"></div>
      <slot />
    </div>
    <template #footer>
      <div class="confirm-footer">
        <el-button class="btn-cancel" @click="handleCancel">{{ cancelText }}</el-button>
        <el-button class="btn-confirm" :type="btnType" @click="handleConfirm" :icon="confirmIcon">
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/** 确认对话框组件，支持 info/warning/danger 三种类型 @component */
import { computed } from 'vue'
import { WarningFilled, CircleCloseFilled, SuccessFilled } from '@element-plus/icons-vue'

/** 组件属性：显示状态、标题、消息、类型、按钮文本、宽度 */
const props = defineProps<{
  modelValue: boolean
  title?: string
  message: string
  type?: 'info' | 'warning' | 'danger'
  confirmText?: string
  cancelText?: string
  width?: string
}>()

/** 组件事件：更新显示状态、确认、取消 */
const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

/** 对话框可见性计算属性（双向绑定） */
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

/** 确认按钮类型 @returns 'danger' | 'primary' */
const btnType = computed(() => props.type === 'danger' ? 'danger' : 'primary')
/** 确认按钮图标 @returns 图标名称字符串 */
const confirmIcon = computed(() => {
  if (props.type === 'danger') return 'Delete'
  if (props.type === 'warning') return 'WarningFilled'
  return 'Check'
})

/** 确认操作 @returns void */
const handleConfirm = () => { emit('confirm'); visible.value = false }
/** 取消操作 @returns void */
const handleCancel = () => { emit('cancel'); visible.value = false }
</script>

<style scoped>
.confirm-icon :deep(.el-icon) { display: flex; }
.confirm-message { max-width: 340px; }
</style>
