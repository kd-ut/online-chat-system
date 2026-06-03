/** WebSocket 服务单例 @module websocket */
import { useUserStore } from '@/stores/userStore'

/** WebSocket 消息回调类型 */
type MessageCallback = (data: any) => void

/** WebSocket 服务类（单例模式） */
class WebSocketService {
  /** WebSocket 连接实例 */
  private ws: WebSocket | null = null
  /** 重连定时器 */
  private reconnectTimer: number | null = null
  /** 心跳定时器 */
  private heartbeatTimer: number | null = null
  /** 消息回调列表 */
  private messageCallbacks: MessageCallback[] = []
  /** 在线状态回调列表 */
  private statusCallbacks: MessageCallback[] = []
  /** 群消息回调列表 */
  private groupMessageCallbacks: MessageCallback[] = []
  /** 通话信令回调列表 */
  private callSignalCallbacks: MessageCallback[] = []
  /** 系统通知回调列表 */
  private notificationCallbacks: MessageCallback[] = []
  /** 好友请求回调列表 */
  private friendRequestCallbacks: MessageCallback[] = []
  /** 好友请求处理结果回调列表 */
  private friendRequestHandledCallbacks: MessageCallback[] = []

  /** 是否已连接 @returns 连接状态 */
  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN
  }

  /** 建立 WebSocket 连接 */
  connect() {
    const userStore = useUserStore()
    const token = userStore.token

    if (!token) {
      console.warn('未登录，无法连接WebSocket')
      return
    }

    const cleanToken = token ? token.replace(/"/g, '') : ''
    const wsBaseUrl = import.meta.env.VITE_WS_URL || `${location.protocol === 'https:' ? 'wss:' : 'ws:'}//${location.host}/ws`
    const wsUrl = `${wsBaseUrl}?token=${cleanToken}`
    console.log('WebSocket连接中:', wsUrl)
    this.ws = new WebSocket(wsUrl)

    this.ws.onopen = () => {
      console.log('WebSocket连接成功')
      this.startHeartbeat()
      if (this.reconnectTimer) clearTimeout(this.reconnectTimer)
    }

    this.ws.onmessage = (event) => {
      const data = JSON.parse(event.data)
      console.log('WebSocket收到消息:', data)

      if (data.type === 'message' || data.type === 'impression') {
        this.messageCallbacks.forEach(cb => cb(data))
      } else if (data.type === 'status') {
        this.statusCallbacks.forEach(cb => cb(data))
      } else if (data.type === 'group_message') {
        this.groupMessageCallbacks.forEach(cb => cb(data))
      } else if (data.type === 'call') {
        this.callSignalCallbacks.forEach(cb => cb(data))
      } else if (data.type === 'notification') {
        this.notificationCallbacks.forEach(cb => cb(data))
      } else if (data.type === 'friend_request') {
        this.friendRequestCallbacks.forEach(cb => cb(data))
      } else if (data.type === 'friend_request_handled') {
        this.friendRequestHandledCallbacks.forEach(cb => cb(data))
      }
    }

    this.ws.onclose = () => {
      console.log('WebSocket断开，尝试重连...')
      this.stopHeartbeat()
      this.reconnect()
    }

    this.ws.onerror = (error) => {
      console.error('WebSocket错误', error)
    }
  }

  /** 自动重连（3秒后） */
  private reconnect() {
    if (this.reconnectTimer) return
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this.connect()
    }, 3000)
  }

  /** 开始心跳（每30秒发送ping） */
  private startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'ping' }))
      }
    }, 30000)
  }

  /** 停止心跳 */
  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /** 发送单聊消息 @param toUserId 目标用户ID @param content 消息内容 @param messageType 消息类型 @param duration 语音时长（仅语音消息） */
  sendMessage(toUserId: number, content: string, messageType: number = 1, duration?: number) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const message: any = {
        type: 'message',
        toUserId: toUserId,
        content: content,
        messageType: messageType
      }
      if (duration !== undefined && messageType === 4) {
        message.duration = duration
      }
      console.log('发送消息:', message)
      this.ws.send(JSON.stringify(message))
    } else {
      console.warn('WebSocket未连接')
    }
  }

  /** 发送群消息 @param groupId 群ID @param content 消息内容 @param messageType 消息类型 */
  sendGroupMessage(groupId: number, content: string, messageType: number = 1) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify({
        type: 'group_message',
        groupId: groupId,
        content: content,
        messageType: messageType
      }))
    } else {
      console.warn('WebSocket未连接')
    }
  }

  /** 发送通话信令 @param data 信令数据 */
  sendCallSignal(data: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('发送通话信令:', data)
      this.ws.send(JSON.stringify({ type: 'call', ...data }))
    } else {
      console.warn('WebSocket未连接，无法发送通话信令')
    }
  }

  /** 注册消息回调 @param callback 回调函数 @returns 取消注册的函数 */
  onMessage(callback: MessageCallback) {
    this.messageCallbacks.push(callback)
    return () => { this.messageCallbacks = this.messageCallbacks.filter(cb => cb !== callback) }
  }

  /** 注册在线状态回调 @param callback 回调函数 @returns 取消注册的函数 */
  onStatus(callback: MessageCallback) {
    this.statusCallbacks.push(callback)
    return () => { this.statusCallbacks = this.statusCallbacks.filter(cb => cb !== callback) }
  }

  /** 注册群消息回调 @param callback 回调函数 @returns 取消注册的函数 */
  onGroupMessage(callback: MessageCallback) {
    this.groupMessageCallbacks.push(callback)
    return () => { this.groupMessageCallbacks = this.groupMessageCallbacks.filter(cb => cb !== callback) }
  }

  /** 注册通话信令回调 @param callback 回调函数 @returns 取消注册的函数 */
  onCallSignal(callback: MessageCallback) {
    this.callSignalCallbacks.push(callback)
    return () => { this.callSignalCallbacks = this.callSignalCallbacks.filter(cb => cb !== callback) }
  }

  /** 注册系统通知回调 @param callback 回调函数 @returns 取消注册的函数 */
  onNotification(callback: MessageCallback) {
    this.notificationCallbacks.push(callback)
    return () => { this.notificationCallbacks = this.notificationCallbacks.filter(cb => cb !== callback) }
  }

  /** 注册好友请求回调 @param callback 回调函数 @returns 取消注册的函数 */
  onFriendRequest(callback: MessageCallback) {
    this.friendRequestCallbacks.push(callback)
    return () => { this.friendRequestCallbacks = this.friendRequestCallbacks.filter(cb => cb !== callback) }
  }

  /** 注册好友请求处理结果回调 @param callback 回调函数 @returns 取消注册的函数 */
  onFriendRequestHandled(callback: MessageCallback) {
    this.friendRequestHandledCallbacks.push(callback)
    return () => { this.friendRequestHandledCallbacks = this.friendRequestHandledCallbacks.filter(cb => cb !== callback) }
  }

  /** 断开连接 */
  disconnect() {
    this.stopHeartbeat()
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }
}

/** WebSocket 服务单例导出 */
export const websocketService = new WebSocketService()
