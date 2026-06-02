import request from '@/utils/request'
import type { Topic, Post, PostComment, CreatePostRequest, PageData } from '@/types'

export const communityApi = {
  /** 获取所有社区主题 */
  listTopics(): Promise<Topic[]> {
    return request.get('/v1/topics')
  },

  /** 热搜话题（实时） */
  getHotTopics(): Promise<{ rank: number; topicId: number; name: string; count: number }[]> {
    return request.get('/v1/topics/hot')
  },

  /** 分页获取帖子列表（支持 ?topicId= 筛选） */
  listPosts(page = 1, size = 10, topicId?: number): Promise<PageData<Post>> {
    return request.get('/v1/posts', { params: { page, size, topicId } })
  },

  /** 帖子详情 */
  getPost(id: number): Promise<Post> {
    return request.get(`/v1/posts/${id}`)
  },

  /** 创建帖子 */
  createPost(data: CreatePostRequest): Promise<Post> {
    return request.post('/v1/posts', data)
  },

  /** 删除帖子 */
  deletePost(id: number): Promise<void> {
    return request.delete(`/v1/posts/${id}`)
  },

  /** 获取帖子评论列表 */
  listComments(postId: number, page = 1, size = 10): Promise<PageData<PostComment>> {
    return request.get(`/v1/posts/${postId}/comments`, { params: { page, size } })
  },

  /** 发表评论/回复 */
  createComment(
    postId: number,
    content: string,
    imageUrl?: string | null,
    videoUrl?: string | null,
    parentId?: number | null,
    replyToUserId?: number | null,
  ): Promise<PostComment> {
    return request.post(`/v1/posts/${postId}/comments`, {
      content, imageUrl, videoUrl, parentId, replyToUserId,
    })
  },

  /** 删除评论 */
  deleteComment(postId: number, commentId: number): Promise<void> {
    return request.delete(`/v1/posts/${postId}/comments/${commentId}`)
  },
}
