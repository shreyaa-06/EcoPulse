import client from './client';

export const listReadings = (params) => client.get('/api/readings', { params }).then((r) => r.data);
export const createReading = (body) => client.post('/api/readings', body).then((r) => r.data);
