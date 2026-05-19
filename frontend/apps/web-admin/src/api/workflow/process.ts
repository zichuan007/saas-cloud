import { requestClient } from '#/api/request';

export interface ProcessInstance {
  createTime?: string;
  currentAssignee?: string;
  currentTask?: string;
  endTime?: string;
  id: number;
  processKey?: string;
  processName?: string;
  startUser?: string;
  status?: string;
  title?: string;
}

export function getStartableList() {
  return requestClient.get('/workflow/process/startable-list');
}

export function startProcess(data: {
  formData: Record<string, any>;
  processKey: string;
  title: string;
}) {
  return requestClient.post('/workflow/process/start', data);
}

export function getMyInitiated(params?: Record<string, any>) {
  return requestClient.get('/workflow/process/my-initiated', { params });
}

export function getProcessDetail(id: number) {
  return requestClient.get<ProcessInstance>(`/workflow/process/${id}`);
}

export function getProcessDiagram(id: number) {
  return requestClient.get(`/workflow/process/${id}/diagram`);
}

export function cancelProcess(id: number) {
  return requestClient.post(`/workflow/process/${id}/cancel`);
}
