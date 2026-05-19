import { requestClient } from '#/api/request';

export function getRunningInstances(params?: Record<string, any>) {
  return requestClient.get('/workflow/monitor/instances', { params });
}

export function terminateInstance(id: number) {
  return requestClient.post(`/workflow/monitor/${id}/terminate`);
}

export function getStatistics() {
  return requestClient.get('/workflow/monitor/statistics');
}
