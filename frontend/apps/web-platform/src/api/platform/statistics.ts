import { requestClient } from '#/api/request';

export interface OverviewData {
  activeTenants: number;
  frozenTenants: number;
  monthlyProcessInstances: number;
  todayActiveUsers: number;
  totalProcessInstances: number;
  totalTenants: number;
  totalUsers: number;
  trialTenants: number;
}

export function getOverview() {
  return requestClient.get<OverviewData>('/platform/statistics/overview');
}
