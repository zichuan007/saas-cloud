import { requestClient } from '#/api/request';

export interface PackageInfo {
  id: number;
  maxDepts?: number;
  maxRoles?: number;
  maxStorageMb?: number;
  maxUsers?: number;
  packageCode: string;
  packageName: string;
  priceMonthly?: number;
  priceYearly?: number;
  status?: number;
}

export interface OrderRecord {
  amount?: number;
  createTime?: string;
  expireTime?: string;
  id: number;
  orderNo: string;
  orderType?: number;
  packageName?: string;
  payChannel?: string;
  payStatus?: number;
  payTime?: string;
}

export interface SubscribeParams {
  months?: number;
  orderType: number;
  packageId: number;
  payChannel?: string;
}

export function getPackageList() {
  return requestClient.get<PackageInfo[]>('/platform/package/list');
}

export function subscribe(tenantId: number, data: SubscribeParams) {
  return requestClient.post<string>(`/platform/order/subscribe/${tenantId}`, data);
}

export function getMyOrders(params: { pageNum: number; pageSize: number }) {
  return requestClient.get('/platform/order/page', { params });
}

export function cancelOrder(id: number) {
  return requestClient.put(`/platform/order/${id}/cancel`);
}
