import {requestClient} from '#/api/request';

export interface TaskRecord {
  assignee?: string;
  createTime?: string;
  endTime?: string;
  id: number;
  processInstanceId?: number;
  processName?: string;
  taskName?: string;
  title?: string;
}

export function getTodoList(params?: Record<string, any>) {
  return requestClient.get('/workflow/task/todo', { params });
}

export function getDoneList(params?: Record<string, any>) {
  return requestClient.get('/workflow/task/done', { params });
}

export function getCopyList(params?: Record<string, any>) {
  return requestClient.get('/workflow/task/copy', { params });
}

export function approveTask(id: number, data?: { comment?: string }) {
  return requestClient.post(`/workflow/task/${id}/approve`, data);
}

export function rejectTask(id: number, data?: { comment?: string }) {
  return requestClient.post(`/workflow/task/${id}/reject`, data);
}

export function transferTask(id: number, data: { targetUserId: number }) {
  return requestClient.post(`/workflow/task/${id}/transfer`, data);
}

export function delegateTask(id: number, data: { targetUserId: number }) {
  return requestClient.post(`/workflow/task/${id}/delegate`, data);
}

export function addSign(id: number, data: { userIds: number[] }) {
  return requestClient.post(`/workflow/task/${id}/add-sign`, data);
}

export function urgeTask(id: number) {
  return requestClient.post(`/workflow/task/${id}/urge`);
}

export function markCopyRead(id: number) {
  return requestClient.put(`/workflow/task/copy/${id}/read`);
}
