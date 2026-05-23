import {requestClient} from '#/api/request';

export function getServerInfo() {
  return requestClient.get('/rbac/monitor/server');
}
