import { requestClient } from '#/api/request';

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
  tenantId?: number;
  tenantName?: string;
}

export interface OrderQuery {
  pageNum: number;
  pageSize: number;
  payStatus?: number;
  tenantId?: number;
}

export function getOrderPage(params: OrderQuery) {
  return requestClient.get('/platform/order/page', { params });
}

export function confirmPay(id: number) {
  return requestClient.put(`/platform/order/${id}/confirm-pay`);
}

export function cancelOrder(id: number) {
  return requestClient.put(`/platform/order/${id}/cancel`);
}
