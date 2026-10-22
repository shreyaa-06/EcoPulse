import { useCallback, useEffect, useState } from 'react';
import { listFacilities } from '../api/facilities';
import { createMeter, deleteMeter, listMeters, updateMeter } from '../api/meters';
import { Banner, ErrorState, LoadingState } from '../components/StateMessage';

const emptyForm = {
  facilityId: '',
  meterName: '',
  meterCode: '',
  resourceType: 'ENERGY',
  unit: 'kWh',
  alertThreshold: '',
  active: true,
};

const unitForType = { ENERGY: 'kWh', WATER: 'L', WASTE: 'kg' };

export default function MetersPage() {
  const [facilities, setFacilities] = useState([]);
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [facilityFilter, setFacilityFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [banner, setBanner] = useState({ type: '', message: '' });

  const load = useCallback(() => {
    setLoading(true);
    const params = {};
    if (facilityFilter) params.facilityId = facilityFilter;
    if (typeFilter) params.resourceType = typeFilter;
    Promise.all([listFacilities(), listMeters(params)])
      .then(([f, m]) => {
        setFacilities(f);
        setItems(m);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [facilityFilter, typeFilter]);

  useEffect(() => {
    load();
  }, [load]);

  const onTypeChange = (resourceType) => {
    setForm((prev) => ({ ...prev, resourceType, unit: unitForType[resourceType] }));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    const body = {
      ...form,
      facilityId: Number(form.facilityId),
      alertThreshold: Number(form.alertThreshold),
    };
    try {
      if (editingId) {
        await updateMeter(editingId, body);
        setBanner({ type: 'success', message: 'Meter updated.' });
      } else {
        await createMeter(body);
        setBanner({ type: 'success', message: 'Meter created.' });
      }
      setForm(emptyForm);
      setEditingId(null);
      load();
    } catch (err) {
      setBanner({ type: 'error', message: err.message });
    }
  };

  const onEdit = (m) => {
    setEditingId(m.id);
    setForm({
      facilityId: String(m.facilityId),
      meterName: m.meterName,
      meterCode: m.meterCode,
      resourceType: m.resourceType,
      unit: m.unit,
      alertThreshold: String(m.alertThreshold),
      active: m.active,
    });
  };

  const onDelete = async (id) => {
    if (!window.confirm('Delete this meter?')) return;
    try {
      await deleteMeter(id);
      setBanner({ type: 'success', message: 'Meter deleted.' });
      load();
    } catch (err) {
      setBanner({ type: 'error', message: err.message });
    }
  };

  if (loading) return <LoadingState />;
  if (error) return <ErrorState message={error} />;

  return (
    <div className="stack">
      <Banner {...banner} onClose={() => setBanner({ type: '', message: '' })} />
      <div className="filters">
        <label>
          Facility
          <select value={facilityFilter} onChange={(e) => setFacilityFilter(e.target.value)}>
            <option value="">All</option>
            {facilities.map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Resource type
          <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
            <option value="">All</option>
            <option value="ENERGY">Energy</option>
            <option value="WATER">Water</option>
            <option value="WASTE">Waste</option>
          </select>
        </label>
      </div>
      <div className="grid-2">
        <form className="card form-card" onSubmit={onSubmit}>
          <h3>{editingId ? 'Edit meter' : 'Register meter'}</h3>
          <label>
            Facility *
            <select required value={form.facilityId} onChange={(e) => setForm({ ...form, facilityId: e.target.value })}>
              <option value="">Select facility</option>
              {facilities.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Meter name *
            <input required value={form.meterName} onChange={(e) => setForm({ ...form, meterName: e.target.value })} />
          </label>
          <label>
            Meter code *
            <input required value={form.meterCode} onChange={(e) => setForm({ ...form, meterCode: e.target.value })} />
          </label>
          <label>
            Resource type *
            <select value={form.resourceType} onChange={(e) => onTypeChange(e.target.value)}>
              <option value="ENERGY">Energy</option>
              <option value="WATER">Water</option>
              <option value="WASTE">Waste</option>
            </select>
          </label>
          <label>
            Unit
            <input value={form.unit} readOnly />
          </label>
          <label>
            Alert threshold *
            <input required type="number" min="0.01" step="0.01" value={form.alertThreshold} onChange={(e) => setForm({ ...form, alertThreshold: e.target.value })} />
          </label>
          <label className="checkbox">
            <input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} />
            Active
          </label>
          <button type="submit" className="btn primary">
            {editingId ? 'Save' : 'Create'}
          </button>
        </form>
        <div className="card table-scroll">
          <table>
            <thead>
              <tr>
                <th>Facility</th>
                <th>Name</th>
                <th>Type</th>
                <th>Threshold</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {items.map((m) => (
                <tr key={m.id}>
                  <td>{m.facilityName}</td>
                  <td>{m.meterName}</td>
                  <td>{m.resourceType}</td>
                  <td>
                    {m.alertThreshold} {m.unit}
                  </td>
                  <td>{m.active ? 'Active' : 'Inactive'}</td>
                  <td className="row-actions">
                    <button type="button" className="link-btn" onClick={() => onEdit(m)}>
                      Edit
                    </button>
                    <button type="button" className="link-btn danger" onClick={() => onDelete(m.id)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
