import {requestClient} from '#/api/request';

export function getFanTrend(params: {
  accountId: number;
  endDate?: string;
  startDate?: string;
}) {
  return requestClient.get('/wechat-oa/dashboard/fan-trend', { params });
}

export function getArticleRank(params: {
  accountId: number;
  endDate?: string;
  startDate?: string;
}) {
  return requestClient.get('/wechat-oa/dashboard/article-rank', { params });
}
