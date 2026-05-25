import { requestClient } from '#/api/request';

export interface OverviewData {
  activeTenants: number;
  newTenantsThisMonth: number;
  totalRevenue: number;
  totalTenants: number;
}

export interface TrendItem {
  count: number;
  date: string;
}

export interface PackageDistItem {
  count: number;
  packageName: string;
}

export interface TopTenantItem {
  createTime: string;
  packageName: string;
  status: number;
  tenantId: number;
  tenantName: string;
}

export function getDashboardOverview() {
  return requestClient.get<OverviewData>('/platform/dashboard/overview');
}

export function getTenantTrend(days: number = 30) {
  return requestClient.get<TrendItem[]>('/platform/dashboard/tenant-trend', {
    params: { days },
  });
}

export function getPackageDistribution() {
  return requestClient.get<PackageDistItem[]>(
    '/platform/dashboard/package-distribution',
  );
}

export function getTopTenants(limit: number = 10) {
  return requestClient.get<TopTenantItem[]>('/platform/dashboard/top-tenants', {
    params: { limit },
  });
}
