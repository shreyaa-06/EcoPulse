import client from './client';

export const listFacilities = () => client.get('/api/facilities').then((r) => r.data);
export const getFacility = (id) => client.get(`/api/facilities/${id}`).then((r) => r.data);
export const createFacility = (body) => client.post('/api/facilities', body).then((r) => r.data);
export const updateFacility = (id, body) => client.put(`/api/facilities/${id}`, body).then((r) => r.data);
export const deleteFacility = (id) => client.delete(`/api/facilities/${id}`);
