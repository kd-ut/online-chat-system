/** 群组模块API @module group */
import request from '../request'

/** 群聊视图对象 */
export interface GroupVO {
  /** 群ID */
  id: number
  /** 群名称 */
  name: string
  /** 群头像 */
  avatar: string | null
  /** 群公告 */
  notice: string | null
  /** 群主用户ID */
  ownerId: number
  /** 成员数量 */
  memberCount: number
  /** 未读消息数 */
  unreadCount: number
  /** 创建时间 */
  createdAt: string
}

/** 群消息视图对象 */
export interface GroupMessageVO {
  /** 消息ID */
  id: number
  /** 群ID */
  groupId: number
  /** 发送者用户ID */
  fromUserId: number
  /** 发送者昵称 */
  fromUserNickname: string
  /** 发送者头像 */
  fromUserAvatar: string | null
  /** 消息内容 */
  content: string
  /** 消息类型 */
  messageType: number
  /** 发送时间 */
  sendTime: string
  /** 是否已撤回 */
  isRecalled?: boolean
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

/** 群成员视图对象 */
export interface GroupMemberVO {
  /** 用户ID */
  userId: number
  /** 用户昵称 */
  nickname: string
  /** 用户头像 */
  avatar: string | null
  /** 群内昵称 */
  groupNickname: string | null
  /** 角色：0-成员 1-管理员 2-群主 */
  role: number
  /** 是否被禁言 */
  muted?: boolean
}

/** 创建群聊参数 */
export interface CreateGroupParams {
  /** 群名称 */
  name: string
  /** 群头像 */
  avatar?: string
  /** 群公告 */
  notice?: string
  /** 初始成员ID列表 */
  memberIds?: number[]
}

/** 邀请成员参数 */
export interface InviteMemberParams {
  /** 群ID */
  groupId: number
  /** 被邀请用户ID */
  userId: number
}

/** 创建群聊 @param data 创建参数 @returns 创建的群聊对象 */
export const createGroupApi = (data: CreateGroupParams) => {
  return request.post<any, GroupVO>('/group', data)
}

/** 获取群聊列表 @returns 群聊列表 */
export const getGroupListApi = () => {
  return request.get<any, GroupVO[]>('/group/list')
}

/** 获取群详情 @param groupId 群ID @returns 群聊详情 */
export const getGroupDetailApi = (groupId: number) => {
  return request.get<any, GroupVO>(`/group/${groupId}`)
}

/** 获取群聊天记录 @param groupId 群ID @param page 页码 @param size 每页条数 @returns 分页群消息列表 */
export const getGroupHistoryApi = (groupId: number, page: number = 1, size: number = 20) => {
  return request.get<any, { total: number, records: GroupMessageVO[] }>(`/group/message/${groupId}`, { params: { page, size } })
}

/** 获取群成员列表 @param groupId 群ID @returns 群成员列表 */
export const getGroupMembersApi = (groupId: number) => {
  return request.get<any, GroupMemberVO[]>(`/group/${groupId}/members`)
}

/** 邀请成员 @param data 邀请参数 @returns 操作结果 */
export const inviteMemberApi = (data: InviteMemberParams) => {
  return request.post('/group/invite', data)
}

/** 退出群聊 @param groupId 群ID @returns 操作结果 */
export const quitGroupApi = (groupId: number) => {
  return request.delete(`/group/${groupId}/quit`)
}

/** 解散群聊 @param groupId 群ID @returns 操作结果 */
export const disbandGroupApi = (groupId: number) => {
  return request.delete(`/group/${groupId}/disband`)
}

/** 清除群未读计数 @param groupId 群ID @returns 操作结果 */
export const clearGroupUnreadApi = (groupId: number) => {
  return request.put(`/group/${groupId}/read`)
}

/** 更新群公告 @param groupId 群ID @param notice 新公告内容 @returns 操作结果 */
export const updateGroupNoticeApi = (groupId: number, notice: string) => {
  return request.put(`/group/${groupId}/notice`, { notice })
}

/** 设置管理员 @param groupId 群ID @param memberId 成员用户ID @returns 操作结果 */
export const setGroupAdminApi = (groupId: number, memberId: number) => {
  return request.put(`/group/${groupId}/member/${memberId}/set-admin`)
}

/** 取消管理员 @param groupId 群ID @param memberId 成员用户ID @returns 操作结果 */
export const removeGroupAdminApi = (groupId: number, memberId: number) => {
  return request.put(`/group/${groupId}/member/${memberId}/remove-admin`)
}

/** 移除群成员 @param groupId 群ID @param memberId 成员用户ID @returns 操作结果 */
export const removeGroupMemberApi = (groupId: number, memberId: number) => {
  return request.delete(`/group/${groupId}/member/${memberId}`)
}

/** 禁言成员 @param groupId 群ID @param memberId 成员用户ID @param minutes 禁言时长（分钟） @returns 操作结果 */
export const muteGroupMemberApi = (groupId: number, memberId: number, minutes: number) => {
  return request.put(`/group/${groupId}/member/${memberId}/mute`, { minutes })
}

/** 取消禁言 @param groupId 群ID @param memberId 成员用户ID @returns 操作结果 */
export const unmuteGroupMemberApi = (groupId: number, memberId: number) => {
  return request.put(`/group/${groupId}/member/${memberId}/unmute`)
}

/** 批量禁言 @param groupId 群ID @param memberIds 成员用户ID列表 @param minutes 禁言时长（分钟） @returns 操作结果 */
export const batchMuteGroupApi = (groupId: number, memberIds: number[], minutes: number) => {
  return request.put(`/group/${groupId}/members/batch-mute`, { memberIds, minutes })
}

/** 撤回群消息 @param messageId 消息ID @returns 操作结果 */
export const recallGroupMessageApi = (messageId: number) => {
  return request.put(`/group/message/recall/${messageId}`)
}
