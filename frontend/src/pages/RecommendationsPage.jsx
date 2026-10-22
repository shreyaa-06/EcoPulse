import { useEffect, useState } from 'react';
import { listRecommendations, updateRecommendationStatus } from '../api/recommendations';
import { Banner, ErrorState, LoadingState } from '../components/StateMessage';

const statuses = ['PENDING', 'IN_PROGRESS', 'COMPLETED'];

export default function RecommendationsPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [banner, setBanner] = useState({ type: '', message: '' });

  const load = () => {
    setLoading(true);
    listRecommendations()
      .then(setItems)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const onStatusChange = async (id, status) => {
    try {
      await updateRecommendationStatus(id, status);
      setBanner({ type: 'success', message: 'Recommendation updated.' });
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
      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Created</th>
              <th>Facility</th>
              <th>Resource</th>
              <th>Action</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {items.map((r) => (
              <tr key={r.id}>
                <td>{new Date(r.createdAt).toLocaleString()}</td>
                <td>{r.facilityName}</td>
                <td>{r.resourceType}</td>
                <td>{r.actionText}</td>
                <td>
                  <select value={r.status} onChange={(e) => onStatusChange(r.id, e.target.value)}>
                    {statuses.map((s) => (
                      <option key={s} value={s}>
                        {s}
                      </option>
                    ))}
                  </select>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
