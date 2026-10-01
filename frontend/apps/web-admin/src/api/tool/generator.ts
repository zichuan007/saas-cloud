import {baseRequestClient, requestClient} from '#/api/request';

export interface ConnectRequest {
  dbType: string;
  host: string;
  port: string;
  dbName: string;
  username: string;
  password: string;
}

export interface GenerateRequest extends ConnectRequest {
  packageName: string;
  author?: string;
  tablePrefix?: string[];
  tables?: string[];
  previewTable?: string;
}

export interface TableInfo {
  name: string;
  comment: string;
}

export function connectDatabase(data: ConnectRequest) {
  return requestClient.post<TableInfo[]>('/generator/connect', data);
}

export function previewCode(data: GenerateRequest) {
  return requestClient.post<Record<string, string>>('/generator/preview', data);
}

export function downloadCode(data: GenerateRequest) {
  // 用 baseRequestClient 跳过响应拦截器（download 返回原始 zip blob，不是 {code,data} JSON）
  return baseRequestClient.post('/generator/download', data, {
    responseType: 'blob',
  });
}
