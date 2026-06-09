

/** 表情视图对象 */
export interface EmojiVO {
  /** 表情ID */
  id: number
  /** 表情名称 */
  name: string
  /** 表情图片URL */
  url: string
  /** 表情分类 */
  category: string
  /** 上传用户ID（自定义表情） */
  userId?: number
  /** 是否为系统表情 */
  isSystem: boolean
  /** 创建时间 */
  createdAt: string
}

/** 获取系统表情包 @returns 系统表情列表 */
export const getSystemEmojisApi = () => {
  return request.get<any, EmojiVO[]>('/emoji/system')
}

/** 获取用户自定义表情包 @returns 用户自定义表情列表 */
export const getUserEmojisApi = () => {
  return request.get<any, EmojiVO[]>('/emoji/user')
}

/** 上传自定义表情 @param file 表情文件 @param name 表情名称 @param category 表情分类 @returns 上传后的表情对象 */
export const uploadEmojiApi = (file: File, name: string, category?: string) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('name', name)
  if (category) formData.append('category', category)
  return request.post<any, EmojiVO>('/emoji/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/** 删除自定义表情 @param emojiId 表情ID @returns 操作结果 */
export const deleteEmojiApi = (emojiId: number) => {
  return request.delete(`/emoji/${emojiId}`)
}
