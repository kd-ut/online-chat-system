<template>
  <BaseDialog v-model="visible" title="移动分组" width="420px" top="12vh">
    <div class="move-content">
      <div class="friend-info" v-if="friend">
        <el-avatar :size="36" :src="friend.avatar || ''">
          {{ (friend.nickname || 'U').charAt(0) }}
        </el-avatar>
        <span class="friend-name">{{ friend.remark || friend.nickname }}</span>
        <span class="current-group">当前：{{ currentGroupName }}</span>
      </div>

      <div class="group-list">
        <div
          v-for="group in availableGroups"
          :key="group.id || 'default'"
          class="group-item"
          :class="{ active: selectedGroup === (group.id || 'default') }"
          @click="selectedGroup = (group.id || 'default')"
        >
          <el-icon :size="16"><Folder /></el-icon>
          <span class="group-name">{{ group.name }}</span>
          <span class="group-count">{{ group.friendCount || 0 }}人</span>
          <el-icon v-if="selectedGroup === (group.id || 'default')" :size="16" color="var(--color-primary)"><Check /></el-icon>
        </div>
      </div>

      <!-- 快速创建新分组 -->
      <div class="quick-create">
        <el-input
          v-model="newGroupName"
          placeholder="输入新分组名称"
          size="small"
          class="create-input"
          @keyup.enter="handleCreateAndSelect"
        />
        <el-button size="small" type="primary" text @click="handleCreateAndSelect" :disabled="!newGroupName.trim()">
          创建并移入
        </el-button>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="btn-cancel" @click="visible = false">取消</el-button>
        <el-button type="primary" class="btn-save" @click="handleMove" :loading="moving">确定</el-button>
      </div>
    </template>
  </BaseDialog>
</template>

<script setup lang="ts">
/** 移动好友分组对话框 @component */
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Folder, Check } from '@element-plus/icons-vue'
import BaseDialog from '@/components/common/BaseDialog.vue'
import { moveFriendGroupApi, createFriendGroupApi } from '@/api/friend'
import { useFriendStore } from '@/stores/friendStore'

const props = defineProps<{
  modelValue: boolean
  friend: { id: number; nickname: string; avatar: string | null; remark: string | null; groupName?: string } | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'success'): void
}>()

const friendStore = useFriendStore()
const visible = ref(false)
const selectedGroup = ref<string>('default')
const newGroupName = ref('')
const moving = ref(false)

const currentGroupName = computed(() => {
  if (!props.friend) return ''
  const gn = props.friend.groupName || ''
  return gn === '' ? ' ' : gn
})

const availableGroups = computed(() => {
  const groups: { id: string; name: string; friendCount: number }[] = [
    { id: 'default', name: ' ', friendCount: 0 }
  ]
  for (const g of friendStore.friendList) {
    if (g.groupName === ' ' || g.groupName === '') {
      groups[0].friendCount = g.friends.length
    } else {
      groups.push({
        id: String(g.groupId || g.groupName),
        name: g.groupName,
        friendCount: g.friends.length
      })
    }
  }
  // 预设当前分组
  if (props.friend) {
    const currentGn = props.friend.groupName || ''
    const match = groups.find(g => {
      if (currentGn === '') return g.id === 'default'
      return g.name === currentGn
    })
    if (match) selectedGroup.value = match.id
  }
  return groups
})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (!val) {
    newGroupName.value = ''
  }
})

watch(visible, (val) => emit('update:modelValue', val))

const handleCreateAndSelect = async () => {
  const name = newGroupName.value.trim()
  if (!name) return
  try {
    const group = await createFriendGroupApi(name)
    selectedGroup.value = String(group.id)
    newGroupName.value = ''
    ElMessage.success('分组已创建')
    friendStore.loadFriendList()
  } catch (e: any) {
    ElMessage.error(e?.message || '创建失败')
  }
}

const handleMove = async () => {
  if (!props.friend) return
  moving.value = true
  try {
    let targetName: string
    if (selectedGroup.value === 'default') {
      targetName = ''
    } else {
      // Find the group name from availableGroups
      const g = availableGroups.value.find(g => g.id === selectedGroup.value)
      targetName = g?.name || selectedGroup.value
    }
    await moveFriendGroupApi(props.friend.id, targetName)
    ElMessage.success('移动成功')
    visible.value = false
    emit('success')
  } catch (e: any) {
    ElMessage.error(e?.message || '移动失败')
  } finally {
    moving.value = false
  }
}
</script>

<style scoped>
.move-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.friend-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.current-group {
  font-size: 12px;
  color: var(--text-secondary);
  margin-left: auto;
}

.group-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid var(--border-color-light);
  border-radius: 10px;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  cursor: pointer;
  transition: background 0.15s;
}

.group-item:hover {
  background: var(--bg-color);
}

.group-item.active {
  background: var(--color-primary-light-1);
}

.group-name {
  font-size: 14px;
  color: var(--text-primary);
  flex: 1;
}

.group-count {
  font-size: 12px;
  color: var(--text-secondary);
}

.quick-create {
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
