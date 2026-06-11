<template>
  <Teleport to="body">
    <transition name="search-fade">
      <div v-if="visible" class="search-overlay" @click.self="close">
        <div class="search-modal">
          <div class="search-input-wrap">
            <el-icon class="search-icon" :size="20"><Search /></el-icon>
            <input
              ref="inputRef"
              v-model="keyword"
              class="search-input"
              :placeholder="placeholder"
              @input="onSearch"
              @keydown="handleKeydown"
              autofocus
            />
            <kbd class="search-kbd">ESC</kbd>
          </div>

          <div class="search-results" v-if="results.length > 0">
            <div class="result-category" v-if="contactResults.length > 0">
              <div class="category-label">联系人</div>
              <div
                v-for="(item, index) in contactResults"
                :key="item.id"
                class="search-item"
                :class="{ highlighted: highlightIndex === index }"
                @click="select(item)"
                @mouseenter="highlightIndex = index"
              >
                <div class="item-icon" :class="item.resultType">
                  <el-icon :size="16">
                    <User v-if="item.resultType === 'friend'" />
                    <ChatDotRound v-else />
                  </el-icon>
                </div>
                <div class="item-info">
                  <span class="item-name">{{ item.name }}</span>
                  <span class="item-type">{{ item.resultType === 'friend' ? '好友' : '群聊' }}</span>
                </div>
                <span class="item-hint">↵</span>
              </div>
            </div>

            <div class="result-category" v-if="messageResults.length > 0">
              <div class="category-label">聊天记录</div>
              <div
                v-for="(item, index) in messageResults"
                :key="item.id"
                class="search-item"
                :class="{ highlighted: highlightIndex === contactResults.length + index }"
                @click="select(item)"
                @mouseenter="highlightIndex = contactResults.length + index"
              >
                <div class="item-icon message">
                  <el-icon :size="16"><Message /></el-icon>
                </div>
                <div class="item-info">
                  <span class="item-name">{{ item.name }}</span>
                  <span class="item-preview">{{ item.preview }}</span>
                </div>
                <span class="item-hint">↵</span>
              </div>
            </div>
          </div>

          <div class="search-empty" v-else-if="keyword && searched">
            <el-empty description="未找到结果" :image-size="60" />
          </div>

          <div class="search-hint" v-else-if="!keyword">
            <span>搜索好友、群聊或聊天记录</span>
            <span class="hint-keys">
              <kbd>↑↓</kbd> 导航 <kbd>Enter</kbd> 选择 <kbd>Esc</kbd> 关闭
            </span>
          </div>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup lang="ts">
/** 全局搜索组件 (Ctrl+K 唤起)，搜索好友/群聊/消息 @component */
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Search, User, ChatDotRound, Message } from '@element-plus/icons-vue'
import { useFriendStore } from '@/stores/friendStore'
import { searchMessagesApi, type MessageSearchResult } from '@/api/message'

const router = useRouter()
const friendStore = useFriendStore()

const visible = ref(false)
const keyword = ref('')
const searched = ref(false)
const highlightIndex = ref(0)
const inputRef = ref<HTMLInputElement>()
const searching = ref(false)

/** 消息搜索结果 */
const msgResults = ref<MessageSearchResult[]>([])

const placeholder = '搜索好友、群聊或聊天记录...'

interface SearchItem {
  id: string
  name: string
  resultType: 'friend' | 'group' | 'message'
  preview?: string
  targetId: number
}

/** 联系人结果 */
const contactResults = ref<SearchItem[]>([])
/** 消息结果 */
const messageResults = ref<SearchItem[]>([])

/** 全部结果 */
const results = computed(() => [...contactResults.value, ...messageResults.value])

/** 搜索防抖定时器 */
let debounceTimer: ReturnType<typeof setTimeout> | null = null

/** 格式化消息搜索预览文本（通话记录类型显示中文标签） */
const formatMessagePreview = (content: string, messageType: number) => {
  if (messageType === 5) {
    const sec = parseInt(content, 10)
    if (!isNaN(sec) && sec > 0) {
      const m = Math.floor(sec / 60)
      const s = sec % 60
      return `语音通话 ${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    }
    return '语音通话'
  }
  if (messageType === 6) {
    const sec = parseInt(content, 10)
    if (!isNaN(sec) && sec > 0) {
      const m = Math.floor(sec / 60)
      const s = sec % 60
      return `视频通话 ${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
    }
    return '视频通话'
  }
  return content.length > 60 ? content.slice(0, 60) + '...' : content
}

/** 搜索联系人 */
const searchContacts = (kw: string): SearchItem[] => {
  const found: SearchItem[] = []
  for (const group of friendStore.friendList) {
    for (const friend of group.friends) {
      const name = friend.remark || friend.nickname || friend.username || ''
      if (name.toLowerCase().includes(kw) || (friend.username || '').toLowerCase().includes(kw)) {
        found.push({
          id: `friend-${friend.userId || friend.id}`,
          name: name || friend.username || '好友',
          resultType: 'friend',
          targetId: friend.userId || friend.id
        })
      }
    }
  }
  return found
}

/** 搜索消息 (API) */
const searchMessages = async (kw: string) => {
  searching.value = true
  try {
    const res = await searchMessagesApi(kw, 10)
    msgResults.value = res || []
    messageResults.value = (res || []).map(m => ({
      id: `msg-${m.messageId}`,
      name: m.otherNickname || '聊天记录',
      resultType: 'message' as const,
      preview: formatMessagePreview(m.content, m.messageType),
      targetId: m.otherUserId
    }))
  } catch {
    msgResults.value = []
    messageResults.value = []
  } finally {
    searching.value = false
  }
}

/** 搜索入口 (防抖 300ms) */
const onSearch = () => {
  highlightIndex.value = 0
  const kw = keyword.value.trim()
  if (!kw) {
    contactResults.value = []
    messageResults.value = []
    searched.value = false
    return
  }

  // 联系人搜索（同步，即时）
  contactResults.value = searchContacts(kw)

  // 消息搜索（异步，防抖）
  if (debounceTimer) clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    searchMessages(kw)
    searched.value = true
  }, 300)
}

/** 键盘导航 */
const handleKeydown = (e: KeyboardEvent) => {
  const total = results.value.length
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    highlightIndex.value = Math.min(highlightIndex.value + 1, total - 1)
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    highlightIndex.value = Math.max(highlightIndex.value - 1, 0)
  } else if (e.key === 'Enter') {
    e.preventDefault()
    const all = results.value
    if (all[highlightIndex.value]) {
      select(all[highlightIndex.value])
    }
  } else if (e.key === 'Escape') {
    close()
  }
}

/** 选择搜索结果 */
const select = (item: SearchItem) => {
  if (item.resultType === 'friend' || item.resultType === 'message') {
    router.push({ name: 'Main', query: { friendId: item.targetId } })
  }
  close()
}

const open = () => {
  visible.value = true
  keyword.value = ''
  contactResults.value = []
  messageResults.value = []
  searched.value = false
  highlightIndex.value = 0
  setTimeout(() => inputRef.value?.focus(), 50)
}

const close = () => {
  visible.value = false
}

const onGlobalKeydown = (e: KeyboardEvent) => {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    visible.value ? close() : open()
  }
}

const onHeaderSearch = () => {
  if (!visible.value) open()
}

onMounted(() => {
  window.addEventListener('keydown', onGlobalKeydown)
  window.addEventListener('open-global-search', onHeaderSearch)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', onGlobalKeydown)
  window.removeEventListener('open-global-search', onHeaderSearch)
})
</script>

<style scoped>
.search-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  padding-top: 15vh;
}

.search-modal {
  width: 560px;
  max-height: 480px;
  background: var(--bg-color-white);
  border-radius: var(--border-radius-large);
  box-shadow: var(--box-shadow-2xl);
  border: 1px solid var(--border-color-light);
  overflow: hidden;
  animation: searchModalIn 0.2s ease;
  align-self: flex-start;
  display: flex;
  flex-direction: column;
}

@keyframes searchModalIn {
  from { opacity: 0; transform: scale(0.96) translateY(-10px); }
  to { opacity: 1; transform: scale(1) translateY(0); }
}

.search-input-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color-light);
  flex-shrink: 0;
}

.search-icon {
  color: var(--text-secondary);
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 16px;
  color: var(--text-primary);
  background: transparent;
  line-height: 1.5;
}

.search-input::placeholder {
  color: var(--text-placeholder);
}

.search-kbd {
  padding: 2px 8px;
  font-size: 11px;
  border-radius: 4px;
  background: var(--bg-color);
  color: var(--text-secondary);
  border: 1px solid var(--border-color-light);
  font-family: inherit;
}

.search-results {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.result-category {
  margin-bottom: 4px;
}

.category-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-placeholder);
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 8px 12px 4px;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.search-item:hover,
.search-item.highlighted {
  background: var(--color-primary-light-1);
}

.item-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-icon.friend {
  background: #eef0ff;
  color: var(--color-primary);
}

.item-icon.group {
  background: #ecfdf5;
  color: var(--color-success);
}

.item-icon.message {
  background: #fff7ed;
  color: var(--color-warning);
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.item-type {
  font-size: 11px;
  color: var(--text-secondary);
}

.item-preview {
  font-size: 12px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-hint {
  font-size: 12px;
  color: var(--text-placeholder);
  opacity: 0;
}

.search-item:hover .item-hint,
.search-item.highlighted .item-hint {
  opacity: 1;
}

.search-empty {
  padding: 30px 0;
}

.search-hint {
  padding: 20px;
  text-align: center;
  font-size: 13px;
  color: var(--text-secondary);
}

.search-hint .hint-keys {
  display: block;
  margin-top: 8px;
}

.search-hint kbd {
  display: inline-block;
  padding: 1px 6px;
  font-size: 11px;
  border-radius: 3px;
  background: var(--bg-color);
  border: 1px solid var(--border-color-light);
  font-family: inherit;
}

.search-fade-enter-active { transition: all 0.2s ease; }
.search-fade-leave-active { transition: all 0.15s ease; }
.search-fade-enter-from,
.search-fade-leave-to {
  opacity: 0;
}
</style>
