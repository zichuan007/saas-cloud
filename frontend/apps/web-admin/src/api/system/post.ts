import {requestClient} from '#/api/request';

export interface PostRecord {
  createTime?: string;
  id: number;
  postCode: string;
  postName: string;
  remark?: string;
  sortOrder?: number;
  status?: number;
}

export interface PostQuery {
  pageNum: number;
  pageSize: number;
  postName?: string;
  status?: number;
}

export function getPostList(params: PostQuery) {
  return requestClient.get('/rbac/post/list', { params });
}

export function getPostSelect() {
  return requestClient.get<PostRecord[]>('/rbac/post/select');
}

export function getPostDetail(id: number) {
  return requestClient.get<PostRecord>(`/rbac/post/${id}`);
}

export function createPost(data: Partial<PostRecord>) {
  return requestClient.post('/rbac/post', data);
}

export function updatePost(id: number, data: Partial<PostRecord>) {
  return requestClient.put(`/rbac/post/${id}`, data);
}

export function deletePost(id: number) {
  return requestClient.delete(`/rbac/post/${id}`);
}
