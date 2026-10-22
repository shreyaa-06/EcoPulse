import { useCallback, useEffect, useState } from 'react';
import { createFacility, deleteFacility, listFacilities, updateFacility } from '../api/facilities';
import { Banner, ErrorState, LoadingState } from '../components/StateMessage';

const emptyForm = { name: '', location: '', type: 'Office' };

export default function FacilitiesPage() {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [banner, setBanner] = useState({ type: '', message: '' });

  const load = useCallback(() => {
    setLoading(true);
    listFacilities()
      .then(setItems)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setBanner({ type: '', message: '' });
    try {
      if (editingId) {
        await updateFacility(editingId, { ...form, id: editingId });
        setBanner({ type: 'success', message: 'Facility updated.' });
      } else {
        await createFacility(form);
        setBanner({ type: 'success', message: 'Facility created.' });
      }
      setForm(emptyForm);
      setEditingId(null);
      load();
    } catch (err) {
      setBanner({ type: 'error', message: err.message });
    }
  };

  const onEdit = (f) => {
    setEditingId(f.id);
    setForm({ name: f.name, location: f.location, type: f.type });
  };

  const onDelete = async (id) => {
    if (!window.confirm('Delete this facility?')) return;
    try {
      await deleteFacility(id);
      setBanner({ type: 'success', message: 'Facility deleted.' });
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
      <div className="grid-2">
        <form className="card form-card" onSubmit={onSubmit}>
          <h3>{editingId ? 'Edit facility' : 'Add facility'}</h3>
          <label>
            Name *
            <input required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          </label>
          <label>
            Location *
            <input required value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} />
          </label>
          <label>
            Type *
            <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
              <option>Office</option>
              <option>College</option>
              <option>Hostel</option>
              <option>Commercial Building</option>
            </select>
          </label>
          <div className="form-actions">
            <button type="submit" className="btn primary">
              {editingId ? 'Save changes' : 'Create'}
            </button>
            {editingId && (
              <button type="button" className="btn" onClick={() => { setEditingId(null); setForm(emptyForm); }}>
                Cancel
              </button>
            )}
          </div>
        </form>
        <div className="card">
          <h3>All facilities</h3>
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Location</th>
                <th>Type</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {items.map((f) => (
                <tr key={f.id}>
                  <td>{f.name}</td>
                  <td>{f.location}</td>
                  <td>{f.type}</td>
                  <td className="row-actions">
                    <button type="button" className="link-btn" onClick={() => onEdit(f)}>
                      Edit
                    </button>
                    <button type="button" className="link-btn danger" onClick={() => onDelete(f.id)}>
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
