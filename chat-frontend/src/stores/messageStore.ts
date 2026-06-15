/** 消息状态管理 @module messageStore */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCountApi, type UnreadCountVO } from '@/api/message'

/** 消息缓存条目 */
interface CacheEntry {
  messages: any[]
  page: number
  hasMore: boolean
  total: number
  timestamp: number
}

/** 缓存 TTL：5 分钟 */
const CACHE_TTL = 5 * 60 * 1000
/** 最大缓存会话数 */
const MAX_CACHE_SIZE = 20

/** 消息相关状态 store */
export const useMessageStore = defineStore('message', () => {
  /** 未读消息计数 */
  const unreadCount = ref<UnreadCountVO | null>(null)

  /** 消息缓存（按 friendId 索引） */
  const messageCache = ref<Map<number, CacheEntry>>(new Map())

  /** 加载未读计数 */
  const loadUnreadCount = async () => {
    try {
      const res = await getUnreadCountApi()
      unreadCount.value = res
    } catch (error) {
      console.error('加载未读计数失败', error)
    }
  }

  /** 清除指定好友的未读计数 @param friendId 好友用户ID */
  const clearUnreadForFriend = (friendId: number) => {
    if (unreadCount.value) {
      unreadCount.value.messages = unreadCount.value.messages.filter(
        m => m.fromUserId !== friendId
      )
      const detail = unreadCount.value.details.find(d => d.friendId === friendId)
      if (detail) {
        unreadCount.value.total -= detail.unreadCount
        unreadCount.value.details = unreadCount.value.details.filter(d => d.friendId !== friendId)
      }
    }
  }

  /** 获取缓存的消息 @param friendId 好友用户ID @returns 缓存条目或 null */
  const getCachedMessages = (friendId: number): CacheEntry | null => {
    const entry = messageCache.value.get(friendId)
    if (entry && Date.now() - entry.timestamp < CACHE_TTL) {
      return entry
    }
    // 过期则清除
    if (entry) messageCache.value.delete(friendId)
    return null
  }

  /** 存入消息缓存 @param friendId 好友用户ID @param entry 缓存条目 */
  const setCachedMessages = (friendId: number, entry: CacheEntry) => {
    // LRU 淘汰：超过上限时删除最旧的
    if (messageCache.value.size >= MAX_CACHE_SIZE && !messageCache.value.has(friendId)) {
      let oldestKey = friendId
      let oldestTime = Infinity
      for (const [key, val] of messageCache.value) {
        if (val.timestamp < oldestTime) {
          oldestTime = val.timestamp
          oldestKey = key
        }
      }
      messageCache.value.delete(oldestKey)
    }
    messageCache.value.set(friendId, entry)
  }

  /** 收到新消息时更新缓存（将新消息追加到对应会话） */
  const appendToCache = (friendId: number, message: any) => {
    const entry = messageCache.value.get(friendId)
    if (entry) {
      entry.messages = [...entry.messages, message]
      entry.total += 1
      entry.timestamp = Date.now()
    }
  }

  /** 清除指定好友的缓存（撤回/删除等场景） */
  const invalidateCache = (friendId: number) => {
    messageCache.value.delete(friendId)
  }

  return {
    unreadCount,
    loadUnreadCount,
    clearUnreadForFriend,
    getCachedMessages,
    setCachedMessages,
    appendToCache,
    invalidateCache
  }
})
