<template>
  <div class="emoji-grid">
    <button
      v-for="emoji in emojis"
      :key="emoji.id"
      class="emoji-card"
      type="button"
      @click="selectEmoji(emoji)"
    >
      <img class="emoji-card__image" :src="emoji.url" :alt="emoji.name" />
      <span class="emoji-card__name">{{ emoji.name }}</span>

      <el-button
        v-if="showDelete"
        class="emoji-card__delete"
        size="small"
        text
        @click.stop="deleteEmoji(emoji.id)"
      >
        删除
      </el-button>
    </button>

    <Empty v-if="!hasEmoji" :description="emptyText" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Empty from '@/components/common/Empty.vue'

type EmojiItem = {
  id: number
  name: string
  url: string
}

const props = withDefaults(defineProps<{
  emojis: EmojiItem[]
  showDelete?: boolean
  emptyText?: string
}>(), {
  showDelete: false,
  emptyText: '暂无表情'
})

const emit = defineEmits<{
  (e: 'select', emoji: EmojiItem): void
  (e: 'delete', id: number): void
}>()

const hasEmoji = computed(() => props.emojis.length > 0)

const selectEmoji = (emoji: EmojiItem) => {
  emit('select', emoji)
}

const deleteEmoji = (id: number) => {
  emit('delete', id)
}
</script>

<style scoped>
.emoji-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.emoji-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  min-width: 0;
  padding: 10px;
  border: 0;
  border-radius: 14px;
  background: transparent;
  color: inherit;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.2s;
}

.emoji-card:hover {
  background: #f3f0ff;
  transform: translateY(-2px);
}

.emoji-card:active {
  transform: scale(0.95);
}

.emoji-card__image {
  width: 48px;
  height: 48px;
  object-fit: contain;
  border-radius: 8px;
}

.emoji-card__name {
  max-width: 100%;
  overflow: hidden;
  font-size: 13px;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.emoji-card__delete {
  position: absolute;
  top: 0;
  right: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.emoji-card:hover .emoji-card__delete {
  opacity: 1;
}
</style>
