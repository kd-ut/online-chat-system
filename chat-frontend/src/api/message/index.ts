/** 消息模块API @module message */
import request from '../request'
import axios from 'axios'

/** 消息视图对象 */
export interface MessageVO {
  /** 消息ID */
  id: number
  /** 发送者用户ID */
  fromUserId: number
  /** 发送者昵称 */
  fromUserNickname: string
  /** 发送者头像 */
  fromUserAvatar?: string | null
  /** 接收者用户ID */
  toUserId: number
  /** 接收者昵称 */
  toUserNickname: string
  /** 消息类型：1-文本 2-图片 3-文件 4-语音 5-语音通话 6-视频通话 */
  messageType: number
  /** 消息内容 */
  content: string
  /** 是否已读 */
  isRead: boolean
  /** 是否已撤回 */
  isRecalled: boolean
  /** 发送时间 */
  sendTime: string
  /** 语音时长（秒） */
  duration?: number
}

/** 未读消息详情 */
export interface UnreadDetail {
  /** 好友用户ID */
  friendId: number
  /** 好友昵称 */
  friendNickname: string
  /** 好友头像 */
  friendAvatar: string | null
  /** 未读消息数 */
  unreadCount: number
}

/** 未读消息简要 */
export interface UnreadMessage {
  /** 消息ID */
  id: number
  /** 发送者用户ID */
  fromUserId: number
  /** 发送者昵称 */
  fromUserNickname: string
  /** 发送者头像 */
  fromUserAvatar: string | null
  /** 消息内容 */
  content: string
  /** 发送时间 */
  sendTime: string
  /** 消息类型 */
  messageType?: number
}

/** 未读消息计数视图对象 */
export interface UnreadCountVO {
  /** 总未读数 */
  total: number
  /** 各好友未读详情列表 */
  details: UnreadDetail[]
  /** 未读消息列表 */
  messages: UnreadMessage[]
}

/** 分页结果泛型接口 */
export interface PageResult<T> {
  /** 总记录数 */
  total: number
  /** 当前页数据列表 */
  records: T[]
}

/** 获取聊天记录 @param friendId 好友用户ID @param page 页码 @param size 每页条数 @returns 分页消息列表 */
export const getChatHistoryApi = (friendId: number, page: number = 1, size: number = 20) => {
  return request.get<any, PageResult<MessageVO>>(`/message/history/${friendId}`, { params: { page, size } })
}

/** 下载聊天记录 @param friendId 好友用户ID @param friendName 好友名称（用于文件名） @param limit 拉取消息条数限制 */
export const downloadChatHistoryApi = async (friendId: number, friendName: string, limit: number = 100) => {
  const token = localStorage.getItem('chat_token')
  const cleanToken = token ? token.replace(/"/g, '') : ''
  
  const response = await axios.get(`/api/message/download/${friendId}`, {
    params: { limit },
    responseType: 'blob',
    headers: { 
      'Authorization': `Bearer ${cleanToken}`
    }
  })
  
  const blob = response.data
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${friendName}_聊天记录_${Date.now()}.txt`
  a.click()
  URL.revokeObjectURL(url)
}

/** 标记消息已读 @param friendId 好友用户ID @returns 操作结果 */
export const markAsReadApi = (friendId: number) => {
  return request.put(`/message/read/${friendId}`)
}

/** 获取未读消息统计 @returns 未读消息计数对象 */
export const getUnreadCountApi = () => {
  return request.get<any, UnreadCountVO>('/message/unread/count')
}

/** 撤回消息 @param messageId 消息ID @returns 操作结果 */
export const recallMessageApi = (messageId: number) => {
  return request.put(`/message/recall/${messageId}`)
}

/** 上传图片 @param file 图片文件 @returns 图片URL */
export const uploadImageApi = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, string>('/message/upload/image', formData)
}

/** 上传语音 @param file 语音文件 @returns 语音文件URL */
export const uploadVoiceApi = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, string>('/message/upload/voice', formData)
}

/** 消息搜索结果 */
export interface MessageSearchResult {
  messageId: number
  content: string
  messageType: number
  sendTime: string
  fromUserId: number
  toUserId: number
  otherUserId: number
  otherNickname: string
  otherAvatar: string | null
}

/** 搜索文本消息 @param keyword 关键词 @param limit 数量上限 @returns 消息搜索结果列表 */
export const searchMessagesApi = (keyword: string, limit: number = 20) => {
  return request.get<any, MessageSearchResult[]>('/message/search', { params: { keyword, limit } })
}
