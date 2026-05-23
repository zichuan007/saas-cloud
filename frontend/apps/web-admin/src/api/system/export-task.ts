import {requestClient} from '#/api/request';

export interface ExportTaskRecord {
  createTime?: string;
  downloadCount?: number;
  errorMsg?: string;
  expireTime?: string;
  fileName?: string;
  fileSize?: number;
  id: number;
  status?: number;
  taskName?: string;
  taskType?: string;
}

export function getExportTaskList(params?: Record<string, any>) {
  return requestClient.get('/rbac/export-task/list', { params });
}

export function downloadExportTask(id: number) {
  return requestClient.get(`/rbac/export-task/${id}/download`);
}

export function deleteExportTask(id: number) {
  return requestClient.delete(`/rbac/export-task/${id}`);
}
