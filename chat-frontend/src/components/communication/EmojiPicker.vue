<template>
  <el-drawer
    v-model="drawerVisible"
    class="beautiful-drawer"
    title="表情包"
    direction="btt"
    size="420px"
    @close="closeDrawer"
  >
    <section class="emoji-panel">
      <header class="emoji-panel__toolbar">
        <div class="emoji-panel__tabs">
          <el-button
            v-for="tab in tabs"
            :key="tab.value"
            size="small"
            :type="activeTab === tab.value ? 'primary' : 'default'"
            @click="switchTab(tab.value)"
          >
            {{ tab.label }}
          </el-button>
        </div>

        <el-button size="small" @click="emit('upload')">上传表情</el-button>
      </header>

      <EmojiGrid
        :emojis="currentEmojis"
        :show-delete="activeTab === 'user'"
        :empty-text="emptyText"
        @select="emit('select', $event)"
        @delete="emit('delete', $event)"
      />
    </section>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import EmojiGrid from './EmojiGrid.vue'

type EmojiTab = 'system' | 'user'

const props = defineProps<{
  modelValue: boolean
  systemEmojis: any[]
  userEmojis: any[]
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void
  (event: 'select', emoji: any): void
  (event: 'upload'): void
  (event: 'delete', id: number): void
}>()

const tabs: Array<{ label: string; value: EmojiTab }> = [
  { label: '系统表情', value: 'system' },
  { label: '我的表情', value: 'user' }
]

const activeTab = ref<EmojiTab>('system')

const drawerVisible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const currentEmojis = computed(() => (
  activeTab.value === 'system' ? props.systemEmojis : props.userEmojis
))

const emptyText = computed(() => (
  activeTab.value === 'system' ? '暂无系统表情' : '暂无自定义表情'
))

const switchTab = (tab: EmojiTab) => {
  activeTab.value = tab
}

const closeDrawer = () => {
  drawerVisible.value = false
}
</script>

<style scoped>
.emoji-panel {
  max-height: 400px;
  padding: 16px;
  overflow-y: auto;
}

.emoji-panel__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
}

.emoji-panel__tabs {
  display: flex;
  gap: 10px;
}

.emoji-panel__toolbar :deep(.el-button) {
  border-radius: 12px;
  font-weight: 600;
}
</style>
