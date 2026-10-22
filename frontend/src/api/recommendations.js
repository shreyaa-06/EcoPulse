import client from './client';

export const listRecommendations = () => client.get('/api/recommendations').then((r) => r.data);
export const updateRecommendationStatus = (id, status) =>
  client.put(`/api/recommendations/${id}/status`, { status }).then((r) => r.data);
