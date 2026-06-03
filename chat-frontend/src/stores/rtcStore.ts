import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { ElMessage } from 'element-plus'
import { Device } from 'mediasoup-client'
import { io, type Socket } from 'socket.io-client'
import { useUserStore } from '@/stores/userStore'

interface RtcUser {
  id: string
  username?: string
  nickname?: string
}

interface RtcInvite {
  inviteId: string
  roomId: string
  roomName?: string
  roomType?: 'direct' | 'group'
  callType?: 'video'
  fromUser: RtcUser
}

interface RemoteMedia {
  producerId: string
  kind: 'audio' | 'video'
  stream: MediaStream
  user: RtcUser | null
}

interface RoomMember {
  id: string
  username?: string
  nickname?: string
  isCreator?: boolean
  isJoined?: boolean
}

const RTC_SOCKET_URL = import.meta.env.VITE_RTC_SOCKET_URL || window.location.origin

export const useRtcStore = defineStore('rtc', () => {
  const visible = ref(false)
  const roomId = ref('')
  const roomTitle = ref('视频通话')
  const statusText = ref('未连接')
  const pendingInvite = ref<RtcInvite | null>(null)
  const members = ref<RoomMember[]>([])
  const remoteMedias = ref<RemoteMedia[]>([])
  const localStream = ref<MediaStream | null>(null)
  const isJoined = ref(false)
  const isPublishing = ref(false)
  const isMicMuted = ref(false)
  const isCameraOff = ref(false)

  let socket: Socket | null = null
  let device: Device | null = null
  let sendTransport: any = null
  const recvTransports = new Map<string, any>()
  const producers = new Map<string, any>()
  const consumers = new Map<string, any>()
  const consumersByProducer = new Map<string, any>()
  const consumedProducerIds = new Set<string>()
  const pendingProducerIds = new Set<string>()
  let isFlushingPendingProducers = false

  const isIncoming = computed(() => Boolean(pendingInvite.value && !isJoined.value))
  const remoteVideos = computed(() => remoteMedias.value.filter(media => media.kind === 'video'))
  const remoteAudios = computed(() => remoteMedias.value.filter(media => media.kind === 'audio'))

  const currentUserPayload = () => {
    const userStore = useUserStore()
    return {
      id: String(userStore.userInfo?.id || ''),
      username: userStore.userInfo?.username,
      nickname: userStore.userInfo?.nickname || userStore.userInfo?.username
    }
  }

  const ensureSocket = () => {
    const userStore = useUserStore()
    const token = userStore.token?.replace(/"/g, '')
    if (!token) throw new Error('请先登录')

    if (socket) {
      if (!socket.connected) socket.connect()
      return socket
    }

    socket = io(RTC_SOCKET_URL, {
      autoConnect: false,
      auth: { token },
      reconnection: true,
      reconnectionAttempts: Infinity,
      reconnectionDelay: 1000,
      reconnectionDelayMax: 10000
    })

    socket.on('connect_error', (err: Error) => {
      statusText.value = `媒体服务连接失败：${err.message}`
      if (err.message.startsWith('UNAUTHORIZED')) {
        ElMessage.error('视频服务登录已过期，请重新登录')
      }
    })

    socket.on('incomingInvite', (invite: RtcInvite) => {
      pendingInvite.value = invite
      roomId.value = invite.roomId
      roomTitle.value = invite.roomName || `${invite.fromUser.nickname || invite.fromUser.username || '好友'}的视频通话`
      statusText.value = `${invite.fromUser.nickname || invite.fromUser.username || '有人'} 邀请你加入视频通话`
      visible.value = true
    })

    socket.on('inviteResponded', ({ accepted, by }: { accepted: boolean; by: RtcUser }) => {
      statusText.value = accepted
        ? `${by.nickname || by.username || '对方'} 已接受邀请`
        : `${by.nickname || by.username || '对方'} 已拒绝邀请`
    })

    socket.on('roomMembersUpdate', ({ roomId: updatedRoomId, members: nextMembers }: { roomId: string; members: RoomMember[] }) => {
      if (updatedRoomId === roomId.value) members.value = Array.isArray(nextMembers) ? nextMembers : []
    })

    socket.on('newProducer', ({ producerId, user }: { producerId: string; user: RtcUser }) => {
      enqueueProducerForConsume(producerId, user)
    })

    socket.on('producerClosed', ({ producerId }: { producerId: string }) => {
      removeRemoteMedia(producerId)
    })

    socket.on('peerLeft', ({ user, reason }: { user: RtcUser; reason?: string }) => {
      statusText.value = `${user.nickname || user.username || '成员'} ${reason || '离开了通话'}`
    })

    socket.on('roomEnded', ({ reason }: { reason?: string }) => {
      statusText.value = reason || '通话已结束'
      resetMediaState()
      visible.value = false
    })

    socket.on('disconnect', () => {
      if (isJoined.value) statusText.value = '媒体连接断开，正在重连...'
    })

    socket.connect()
    return socket
  }

  const waitForSocketConnected = async () => {
    const activeSocket = ensureSocket()
    if (activeSocket.connected) return

    await new Promise<void>((resolve, reject) => {
      const onConnect = () => {
        cleanup()
        resolve()
      }
      const onError = (err: Error) => {
        cleanup()
        reject(err)
      }
      const cleanup = () => {
        activeSocket.off('connect', onConnect)
        activeSocket.off('connect_error', onError)
      }

      activeSocket.on('connect', onConnect)
      activeSocket.on('connect_error', onError)
    })
  }

  const request = async <T = any>(type: string, data: Record<string, unknown> = {}) => {
    await waitForSocketConnected()
    return await new Promise<T>((resolve, reject) => {
      socket!.emit(type, data, (response: any) => {
        if (response?.error) {
          reject(new Error(response.error))
          return
        }
        resolve(response)
      })
    })
  }

  const resetMediaState = () => {
    for (const consumer of consumers.values()) {
      try { consumer.close() } catch {}
    }
    for (const producer of producers.values()) {
      try { producer.close() } catch {}
    }
    for (const transport of recvTransports.values()) {
      try { transport.close() } catch {}
    }
    if (sendTransport) {
      try { sendTransport.close() } catch {}
    }
    if (localStream.value) {
      localStream.value.getTracks().forEach(track => track.stop())
    }

    device = null
    sendTransport = null
    recvTransports.clear()
    producers.clear()
    consumers.clear()
    consumersByProducer.clear()
    consumedProducerIds.clear()
    pendingProducerIds.clear()
    remoteMedias.value = []
    localStream.value = null
    isJoined.value = false
    isPublishing.value = false
    isMicMuted.value = false
    isCameraOff.value = false
  }

  const setRoomFromResponse = (data: any, fallbackTitle: string) => {
    roomId.value = data.roomId
    roomTitle.value = data.roomName || fallbackTitle
    members.value = Array.isArray(data.members) ? data.members : []
    pendingInvite.value = null
    visible.value = true
  }

  const startDirectVideoCall = async (targetUser: any) => {
    if (!targetUser?.userId && !targetUser?.id) {
      ElMessage.warning('请选择视频通话对象')
      return
    }

    const me = currentUserPayload()
    const targetName = targetUser.nickname || targetUser.remark || '好友'
    statusText.value = '正在发起视频通话...'
    visible.value = true

    try {
      const data = await request('createRoomInvite', {
        targetUserId: String(targetUser.userId || targetUser.id),
        callType: 'video',
        roomName: `${me.nickname || '我'} 与 ${targetName} 的视频通话`
      })
      setRoomFromResponse(data, `${targetName}的视频通话`)
      await joinAndPublish()
      statusText.value = `已邀请 ${targetName}`
    } catch (error: any) {
      visible.value = false
      ElMessage.error(error?.message || '发起视频通话失败')
    }
  }

  const startGroupVideoCall = async (group: any, participantIds: number[]) => {
    if (!group?.id) return

    statusText.value = '正在创建群视频通话...'
    visible.value = true

    try {
      const data = await request('createGroupRoom', {
        roomId: `group-${group.id}`,
        roomName: `${group.name || '群聊'}的视频通话`,
        callType: 'video',
        participantIds: participantIds.map(id => String(id))
      })
      setRoomFromResponse(data, `${group.name || '群聊'}的视频通话`)
      await joinAndPublish()
      statusText.value = '群视频通话已开始'
    } catch (error: any) {
      visible.value = false
      ElMessage.error(error?.message || '创建群视频通话失败')
    }
  }

  const acceptIncoming = async () => {
    if (!pendingInvite.value) return

    try {
      const data = await request('respondRoomInvite', {
        inviteId: pendingInvite.value.inviteId,
        accept: true
      })
      setRoomFromResponse(data, pendingInvite.value.roomName || '视频通话')
      await joinAndPublish()
      statusText.value = '已加入视频通话'
    } catch (error: any) {
      ElMessage.error(error?.message || '加入视频通话失败')
    }
  }

  const rejectIncoming = async () => {
    if (!pendingInvite.value) {
      visible.value = false
      return
    }

    try {
      await request('respondRoomInvite', {
        inviteId: pendingInvite.value.inviteId,
        accept: false
      })
    } catch (error) {
      console.error(error)
    } finally {
      pendingInvite.value = null
      visible.value = false
    }
  }

  const joinAndPublish = async () => {
    if (!roomId.value) throw new Error('房间不存在')
    resetMediaState()

    statusText.value = '正在加入媒体房间...'
    const data = await request('joinRoom', { roomId: roomId.value })
    device = new Device()
    await device.load({ routerRtpCapabilities: data.rtpCapabilities })
    members.value = Array.isArray(data.members) ? data.members : []
    isJoined.value = true

    await createSendTransport()
    for (const producer of data.existingProducers || []) {
      enqueueProducerForConsume(producer.producerId, producer.user)
    }
    await publishLocalMedia()
    statusText.value = '通话中'
  }

  const createSendTransport = async () => {
    const params = await request('createWebRtcTransport', { direction: 'send' })
    sendTransport = device!.createSendTransport(params)

    sendTransport.on('connect', async ({ dtlsParameters }: any, callback: () => void, errback: (error: Error) => void) => {
      try {
        await request('connectTransport', { transportId: sendTransport.id, dtlsParameters })
        callback()
      } catch (error: any) {
        errback(error)
      }
    })

    sendTransport.on('produce', async (parameters: any, callback: (data: { id: string }) => void, errback: (error: Error) => void) => {
      try {
        const { id } = await request<{ id: string }>('produce', {
          transportId: sendTransport.id,
          kind: parameters.kind,
          rtpParameters: parameters.rtpParameters
        })
        callback({ id })
      } catch (error: any) {
        errback(error)
      }
    })
  }

  const publishLocalMedia = async () => {
    if (localStream.value) return

    localStream.value = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
    isPublishing.value = true

    for (const track of localStream.value.getTracks()) {
      const producer = await sendTransport.produce({ track })
      producers.set(producer.id, producer)
    }
  }

  const consumeProducer = async (producerId: string, fallbackUser: RtcUser | null = null) => {
    if (!device || consumedProducerIds.has(producerId)) return
    consumedProducerIds.add(producerId)

    try {
      const data = await request('consume', {
        producerId,
        rtpCapabilities: device.rtpCapabilities
      })
      const { transportOptions, consumerOptions, user } = data

      let recvTransport = recvTransports.get(transportOptions.id)
      if (!recvTransport) {
        recvTransport = device.createRecvTransport(transportOptions)
        recvTransport.on('connect', async ({ dtlsParameters }: any, callback: () => void, errback: (error: Error) => void) => {
          try {
            await request('connectTransport', {
              transportId: recvTransport.id,
              dtlsParameters
            })
            callback()
          } catch (error: any) {
            errback(error)
          }
        })
        recvTransports.set(transportOptions.id, recvTransport)
      }

      const consumer = await recvTransport.consume(consumerOptions)
      consumers.set(consumer.id, consumer)
      consumersByProducer.set(producerId, consumer)

      consumer.on('transportclose', () => removeRemoteMedia(producerId))
      consumer.on('producerclose', () => removeRemoteMedia(producerId))

      const stream = new MediaStream([consumer.track])
      remoteMedias.value.push({
        producerId,
        kind: consumer.kind,
        stream,
        user: user || fallbackUser
      })
      await request('resume', { consumerId: consumer.id })
    } catch (error) {
      consumedProducerIds.delete(producerId)
      throw error
    }
  }

  const enqueueProducerForConsume = (producerId: string, user: RtcUser | null = null) => {
    if (!producerId || consumedProducerIds.has(producerId) || pendingProducerIds.has(producerId)) return
    pendingProducerIds.add(producerId)
    void flushPendingProducers(user)
  }

  const flushPendingProducers = async (user: RtcUser | null = null) => {
    if (isFlushingPendingProducers || !device || !isJoined.value) return
    isFlushingPendingProducers = true

    try {
      for (const producerId of [...pendingProducerIds]) {
        try {
          await consumeProducer(producerId, user)
        } catch (error: any) {
          console.error('订阅远端媒体失败:', error?.message || error)
        } finally {
          pendingProducerIds.delete(producerId)
        }
      }
    } finally {
      isFlushingPendingProducers = false
      if (pendingProducerIds.size > 0 && device && isJoined.value) {
        void flushPendingProducers()
      }
    }
  }

  const removeRemoteMedia = (producerId: string) => {
    const consumer = consumersByProducer.get(producerId)
    if (consumer) {
      try { consumer.close() } catch {}
      consumers.delete(consumer.id)
      consumersByProducer.delete(producerId)
    }

    consumedProducerIds.delete(producerId)
    pendingProducerIds.delete(producerId)
    remoteMedias.value = remoteMedias.value.filter(media => media.producerId !== producerId)
  }

  const toggleMic = async () => {
    const producer = [...producers.values()].find(item => item.kind === 'audio')
    if (!producer) return

    if (producer.paused) {
      producer.resume()
      await request('resumeProducer', { producerId: producer.id })
      isMicMuted.value = false
    } else {
      producer.pause()
      await request('pauseProducer', { producerId: producer.id })
      isMicMuted.value = true
    }
  }

  const toggleCamera = async () => {
    const producer = [...producers.values()].find(item => item.kind === 'video')
    if (!producer) return

    if (producer.paused) {
      producer.resume()
      await request('resumeProducer', { producerId: producer.id })
      isCameraOff.value = false
    } else {
      producer.pause()
      await request('pauseProducer', { producerId: producer.id })
      isCameraOff.value = true
    }
  }

  const leaveCall = async () => {
    try {
      if (isJoined.value) await request('leaveRoom')
    } catch (error) {
      console.error(error)
    } finally {
      resetMediaState()
      pendingInvite.value = null
      visible.value = false
      roomId.value = ''
      members.value = []
    }
  }

  const endCall = async () => {
    try {
      if (isJoined.value) await request('endRoom')
    } catch (error) {
      console.error(error)
    } finally {
      resetMediaState()
      pendingInvite.value = null
      visible.value = false
      roomId.value = ''
      members.value = []
    }
  }

  const disconnect = () => {
    resetMediaState()
    pendingInvite.value = null
    visible.value = false
    roomId.value = ''
    members.value = []
    if (socket) {
      socket.disconnect()
      socket = null
    }
  }

  return {
    visible,
    roomId,
    roomTitle,
    statusText,
    pendingInvite,
    members,
    remoteMedias,
    remoteVideos,
    remoteAudios,
    localStream,
    isJoined,
    isIncoming,
    isPublishing,
    isMicMuted,
    isCameraOff,
    ensureSocket,
    startDirectVideoCall,
    startGroupVideoCall,
    acceptIncoming,
    rejectIncoming,
    toggleMic,
    toggleCamera,
    leaveCall,
    endCall,
    disconnect
  }
})
