import client from './client';

export const listMeters = (params) => client.get('/api/meters', { params }).then((r) => r.data);
export const createMeter = (body) => client.post('/api/meters', body).then((r) => r.data);
export const updateMeter = (id, body) => client.put(`/api/meters/${id}`, body).then((r) => r.data);
export const deleteMeter = (id) => client.delete(`/api/meters/${id}`);
