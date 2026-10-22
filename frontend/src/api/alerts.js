import client from './client';

export const listAlerts = () => client.get('/api/alerts').then((r) => r.data);
export const listActiveAlerts = () => client.get('/api/alerts/active').then((r) => r.data);
export const updateAlertStatus = (id, status) =>
  client.put(`/api/alerts/${id}/status`, { status }).then((r) => r.data);
