import {requestClient} from '#/api/request';

export interface ProcessDefinition {
  bpmnXml?: string;
  createTime?: string;
  deployTime?: string;
  id: number;
  processKey: string;
  processName: string;
  remark?: string;
  status?: number;
  version?: number;
}

export function getDefinitionList(params?: Record<string, any>) {
  return requestClient.get('/workflow/definition/list', { params });
}

export function getDefinitionDetail(id: number) {
  return requestClient.get<ProcessDefinition>(`/workflow/definition/${id}`);
}

export function createDefinition(data: Partial<ProcessDefinition>) {
  return requestClient.post('/workflow/definition', data);
}

export function updateDefinition(id: number, data: Partial<ProcessDefinition>) {
  return requestClient.put(`/workflow/definition/${id}`, data);
}

export function deleteDefinition(id: number) {
  return requestClient.delete(`/workflow/definition/${id}`);
}

export function deployDefinition(id: number, bpmnXml: string) {
  return requestClient.post(`/workflow/definition/${id}/deploy`, { bpmnXml });
}

export function updateDefinitionStatus(id: number, status: number) {
  return requestClient.put(`/workflow/definition/${id}/status`, null, {
    params: { status },
  });
}

export function getBpmnXml(id: number) {
  return requestClient.get<string>(`/workflow/definition/${id}/bpmn-xml`);
}

export function saveBpmnDraft(id: number, bpmnXml: string) {
  return requestClient.put(`/workflow/definition/${id}/bpmn-xml`, { bpmnXml });
}

export function saveNodeConfig(data: Record<string, any>) {
  return requestClient.post('/workflow/definition/node-config', data);
}

export function getNodeConfigs(id: number) {
  return requestClient.get(`/workflow/definition/${id}/node-configs`);
}

export function getTemplateList() {
  return requestClient.get('/workflow/template/list');
}

export function importTemplate(id: number) {
  return requestClient.post(`/workflow/template/${id}/import`);
}
