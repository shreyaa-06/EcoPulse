import client from './client';

export const getReportSummary = (params) => client.get('/api/reports/summary', { params }).then((r) => r.data);
export const getReportTrends = (params) => client.get('/api/reports/trends', { params }).then((r) => r.data);

export const downloadReportCsv = (params) =>
  client.get('/api/reports/export', { params, responseType: 'blob' }).then((r) => r.data);
