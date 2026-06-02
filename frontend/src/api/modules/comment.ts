import request from '@/utils/request'
import type { PageData, Comment } from '@/types'

export const commentApi = {
  /** 获取商品评论列表（分页） */
  list: (productId: number, params: { page?: number; size?: number }) =>
    request.get(`/v1/products/${productId}/comments`, { params }) as Promise<PageData<Comment>>,

  /** 发表评论/回复（支持图片/视频附件） */
  create: (
    productId: number,
    content: string,
    parentId?: number | null,
    replyToUserId?: number | null,
    imageUrl?: string | null,
    videoUrl?: string | null,
  ) =>
    request.post(`/v1/products/${productId}/comments`, {
      content, parentId, replyToUserId, imageUrl, videoUrl,
    }) as Promise<Comment>,

  /** 投票：vote=1 赞 -1 踩 */
  vote: (productId: number, commentId: number, vote: number) =>
    request.put(`/v1/products/${productId}/comments/${commentId}/vote`, { vote }),

  /** 取消投票 */
  cancelVote: (productId: number, commentId: number) =>
    request.delete(`/v1/products/${productId}/comments/${commentId}/vote`),
}
