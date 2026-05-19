import { requestClient } from '#/api/request';

export interface ConnectRequest {
  jdbcUrl: string;
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
  TABLE_NAME: string;
  TABLE_COMMENT: string;
}

export function connectDatabase(data: ConnectRequest) {
  return requestClient.post<TableInfo[]>('/generator/connect', data);
}

export function previewCode(data: GenerateRequest) {
  return requestClient.post<Record<string, string>>('/generator/preview', data);
}

export function downloadCode(data: GenerateRequest) {
  return requestClient.post('/generator/download', data, {
    responseType: 'blob',
  });
}
