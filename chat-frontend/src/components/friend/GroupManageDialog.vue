<template>
  <BaseDialog v-model="visible" title="管理分组" width="420px" top="12vh">
    <div class="manage-content">
      <!-- 分组列表 -->
      <div class="group-list">
        <div class="group-item default-group">
          <el-icon :size="16"><Folder /></el-icon>
          <span class="group-name"> </span>
          <span class="group-hint">系统默认</span>
        </div>
        <div
          v-for="group in groups"
          :key="group.id"
          class="group-item"
        >
          <el-icon :size="16"><Folder /></el-icon>
          <template v-if="editingId === group.id">
            <el-input
              v-model="editName"
              size="small"
              class="edit-input"
              @keyup.enter="handleRename(group)"
              @blur="handleRename(group)"
            />
          </template>
          <template v-else>
            <span class="group-name" @dblclick="startEdit(group)">{{ group.groupName }}</span>
            <span class="group-count">{{ getGroupFriendCount(group.groupName) }}人</span>
          </template>
          <div class="group-actions" v-if="editingId !== group.id">
            <el-button size="small" text @click="startEdit(group)" :icon="Edit" />
            <el-popconfirm title="删除分组后好友将移至默认分组" @confirm="handleDelete(group)">
              <template #reference>
                <el-button size="small" text :icon="Delete" />
              </template>
            </el-popconfirm>
          </div>
        </div>
      </div>

      <!-- 创建新分组 -->
      <div class="create-row">
        <el-input
          v-model="createName"
          placeholder="输入新分组名称"
          size="small"
          class="create-input"
          @keyup.enter="handleCreate"
        />
        <el-button size="small" type="primary" text @click="handleCreate" :disabled="!createName.trim()">
          创建
        </el-button>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-close" @click="visible = false">完成</el-button>
      </div>
    </template>
  </BaseDialog>
</template>

<script setup lang="ts">
/** 好友分组管理对话框 @component */
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Folder, Edit, Delete } from '@element-plus/icons-vue'
import BaseDialog from '@/components/common/BaseDialog.vue'
import { getFriendGroupListApi, createFriendGroupApi, renameFriendGroupApi, deleteFriendGroupApi, type FriendGroup } from '@/api/friend'
import { useFriendStore } from '@/stores/friendStore'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'changed'): void
}>()

const friendStore = useFriendStore()
const visible = ref(false)
const groups = ref<FriendGroup[]>([])
const createName = ref('')
const editingId = ref<number | null>(null)
const editName = ref('')

const loadGroups = async () => {
  try {
    groups.value = await getFriendGroupListApi()
  } catch { /* ignore */ }
}

const getGroupFriendCount = (groupName: string) => {
  const g = friendStore.friendList.find(fg => fg.groupName === groupName)
  return g?.friends.length || 0
}

const startEdit = (group: FriendGroup) => {
  editingId.value = group.id
  editName.value = group.groupName
}

const handleRename = async (group: FriendGroup) => {
  const newName = editName.value.trim()
  if (!newName || newName === group.groupName) {
    editingId.value = null
    return
  }
  try {
    await renameFriendGroupApi(group.id, newName)
    ElMessage.success('重命名成功')
    editingId.value = null
    loadGroups()
    emit('changed')
  } catch (e: any) {
    ElMessage.error(e?.message || '重命名失败')
  }
}

const handleDelete = async (group: FriendGroup) => {
  try {
    await deleteFriendGroupApi(group.id)
    ElMessage.success('分组已删除')
    loadGroups()
    emit('changed')
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败')
  }
}

const handleCreate = async () => {
  const name = createName.value.trim()
  if (!name) return
  try {
    await createFriendGroupApi(name)
    ElMessage.success('分组已创建')
    createName.value = ''
    loadGroups()
    emit('changed')
  } catch (e: any) {
    ElMessage.error(e?.message || '创建失败')
  }
}

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadGroups()
    createName.value = ''
    editingId.value = null
  }
})

watch(visible, (val) => emit('update:modelValue', val))
</script>

<style scoped>
.manage-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.group-list {
  max-height: 260px;
  overflow-y: auto;
  border: 1px solid var(--border-color-light);
  border-radius: 10px;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
}

.group-item:not(:last-child) {
  border-bottom: 1px solid var(--border-color-lighter);
}

.group-item.default-group {
  background: var(--bg-color-sunken);
}

.group-name {
  font-size: 14px;
  color: var(--text-primary);
  flex: 1;
  cursor: default;
}

.group-hint {
  font-size: 11px;
  color: var(--text-placeholder);
}

.group-count {
  font-size: 12px;
  color: var(--text-secondary);
}

.group-actions {
  display: flex;
  gap: 2px;
  opacity: 0;
  transition: opacity 0.15s;
}

.group-item:hover .group-actions {
  opacity: 1;
}

.edit-input {
  flex: 1;
}

.edit-input :deep(.el-input__wrapper) {
  border-radius: 6px;
  height: 28px;
}

.create-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.create-input {
  flex: 1;
}

.create-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: center;
}

.btn-close {
  min-width: 100px;
  height: 38px;
  border-radius: 10px !important;
  font-weight: 600 !important;
}
</style>
