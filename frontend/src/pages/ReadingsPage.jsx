import { useCallback, useEffect, useState } from 'react';
import { listFacilities } from '../api/facilities';
import { listMeters } from '../api/meters';
import { createReading, listReadings } from '../api/readings';
import { Banner, ErrorState, LoadingState } from '../components/StateMessage';

export default function ReadingsPage() {
  const [facilities, setFacilities] = useState([]);
  const [meters, setMeters] = useState([]);
  const [items, setItems] = useState([]);
  const [form, setForm] = useState({ meterId: '', readingValue: '', notes: '' });
  const [facilityFilter, setFacilityFilter] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [banner, setBanner] = useState({ type: '', message: '' });

  const load = useCallback(() => {
    setLoading(true);
    const params = {};
    if (facilityFilter) params.facilityId = facilityFilter;
    if (typeFilter) params.resourceType = typeFilter;
    Promise.all([listFacilities(), listMeters({}), listReadings(params)])
      .then(([f, m, r]) => {
        setFacilities(f);
        setMeters(m);
        setItems(r);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [facilityFilter, typeFilter]);

  useEffect(() => {
    load();
  }, [load]);

  const onSubmit = async (e) => {
    e.preventDefault();
    try {
      await createReading({
        meterId: Number(form.meterId),
        readingValue: Number(form.readingValue),
        notes: form.notes || null,
      });
      setBanner({ type: 'success', message: 'Reading saved. Alerts evaluated automatically.' });
      setForm({ meterId: '', readingValue: '', notes: '' });
      load();
    } catch (err) {
      setBanner({ type: 'error', message: err.message });
    }
  };

  const filtered = items.filter((r) => {
    if (!search) return true;
    const q = search.toLowerCase();
    return (
      r.facilityName?.toLowerCase().includes(q) ||
      r.meterName?.toLowerCase().includes(q) ||
      r.notes?.toLowerCase().includes(q)
    );
  });

  if (loading) return <LoadingState />;
  if (error) return <ErrorState message={error} />;

  return (
    <div className="stack">
      <Banner {...banner} onClose={() => setBanner({ type: '', message: '' })} />
      <div className="grid-2">
        <form className="card form-card" onSubmit={onSubmit}>
          <h3>Record reading</h3>
          <p className="muted small">Each value is treated as consumption for that period (not a cumulative meter dial).</p>
          <label>
            Meter *
            <select required value={form.meterId} onChange={(e) => setForm({ ...form, meterId: e.target.value })}>
              <option value="">Select meter</option>
              {meters.map((m) => (
                <option key={m.id} value={m.id}>
                  {m.facilityName} — {m.meterName} ({m.resourceType})
                </option>
              ))}
            </select>
          </label>
          <label>
            Consumption value *
            <input required type="number" min="0" step="0.01" value={form.readingValue} onChange={(e) => setForm({ ...form, readingValue: e.target.value })} />
          </label>
          <label>
            Notes
            <textarea value={form.notes} onChange={(e) => setForm({ ...form, notes: e.target.value })} rows={3} />
          </label>
          <button type="submit" className="btn primary">
            Save reading
          </button>
        </form>
        <div className="card">
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
              Type
              <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
                <option value="">All</option>
                <option value="ENERGY">Energy</option>
                <option value="WATER">Water</option>
                <option value="WASTE">Waste</option>
              </select>
            </label>
            <label>
              Search
              <input placeholder="Facility, meter, notes…" value={search} onChange={(e) => setSearch(e.target.value)} />
            </label>
          </div>
          <table>
            <thead>
              <tr>
                <th>When</th>
                <th>Facility</th>
                <th>Meter</th>
                <th>Type</th>
                <th>Value</th>
                <th>Notes</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((r) => (
                <tr key={r.id}>
                  <td>{new Date(r.readingTimestamp).toLocaleString()}</td>
                  <td>{r.facilityName}</td>
                  <td>{r.meterName}</td>
                  <td>{r.resourceType}</td>
                  <td>
                    {r.readingValue} {r.unit}
                  </td>
                  <td>{r.notes || '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
