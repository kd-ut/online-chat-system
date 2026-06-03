/** 好友状态管理 @module friendStore */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { FriendGroupVO, FriendRequestVO } from '@/api/friend'
import { getFriendListApi, getFriendRequestsApi } from '@/api/friend'

/** 好友相关状态 store */
export const useFriendStore = defineStore('friend', () => {
  /** 好友列表（按分组） */
  const friendList = ref<FriendGroupVO[]>([])
  /** 好友申请列表 */
  const friendRequests = ref<FriendRequestVO[]>([])
  
  /** 加载好友列表 */
  const loadFriendList = async () => {
    try {
      const res = await getFriendListApi()
      console.log('加载好友列表:', res)
      friendList.value = res
    } catch (error) {
      console.error('加载好友列表失败', error)
    }
  }
  
  /** 加载好友申请列表 */
  const loadFriendRequests = async () => {
    try {
      const res = await getFriendRequestsApi()
      friendRequests.value = res
    } catch (error) {
      console.error('加载好友申请失败', error)
    }
  }
  
  /** 获取所有分组名称 @returns 分组名去重列表 */
  const getGroupNames = () => {
    return [...new Set(friendList.value.map(g => g.groupName))]
  }
  
  /** 根据用户ID查找好友 @param userId 用户ID @returns 好友信息或null */
  const getFriendById = (userId: number) => {
    for (const group of friendList.value) {
      const friend = group.friends.find(f => f.userId === userId)
      if (friend) return friend
    }
    return null
  }
  
  /** 清除指定好友的未读计数 @param friendId 好友用户ID */
  const clearUnreadForFriend = (friendId: number) => {
    for (const group of friendList.value) {
      const friend = group.friends.find(f => f.userId === friendId)
      if (friend) {
        friend.unreadCount = 0
        break
      }
    }
  }

  /** 增加指定好友的未读计数 @param friendId 好友用户ID */
  const incrementUnreadForFriend = (friendId: number) => {
    for (const group of friendList.value) {
      const friend = group.friends.find(f => f.userId === friendId)
      if (friend) {
        friend.unreadCount = (friend.unreadCount || 0) + 1
        break
      }
    }
  }

  /** 更新好友在线状态 @param userId 用户ID @param isOnline 是否在线 */
  const updateFriendOnlineStatus = (userId: number, isOnline: boolean) => {
    console.log('updateFriendOnlineStatus 被调用:', userId, isOnline)
    for (const group of friendList.value) {
      const friend = group.friends.find(f => f.userId === userId)
      if (friend) {
        friend.isOnline = isOnline
        console.log(`✅ 更新 ${friend.nickname} => ${isOnline ? '在线' : '离线'}`)
        break
      }
    }
  }
  
  return {
    friendList,
    friendRequests,
    loadFriendList,
    loadFriendRequests,
    getGroupNames,
    getFriendById,
    clearUnreadForFriend,
    incrementUnreadForFriend,
    updateFriendOnlineStatus
  }
})
