/** 消息模块类型定义 @module types/message */
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
  /** 消息类型：1-文本 2-图片 3-文件 4-语音 */
  messageType: number
  /** 消息内容 */
  content: string
  /** 是否已读 */
  isRead: boolean
  /** 是否已撤回 */
  isRecalled: boolean
  /** 发送时间 */
  sendTime: string
  /** 引用的消息ID */
  replyToId?: number
  /** 被引用的消息信息 */
  repliedMessage?: {
    messageId: number
    content: string
    fromUserNickname: string
    messageType: number
  }
}

/** 发送消息参数 */
export interface SendMessageParams {
  /** 目标用户ID */
  toUserId: number
  /** 消息内容 */
  content: string
  /** 消息类型 */
  messageType?: number
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

/** 未读消息计数视图对象 */
export interface UnreadCountVO {
  /** 总未读数 */
  total: number
  /** 各好友未读详情列表 */
  details: UnreadDetail[]
}

/** 聊天历史记录响应 */
export interface ChatHistoryResponse {
  /** 总记录数 */
  total: number
  /** 消息列表 */
  records: MessageVO[]
}
