import { useEffect, useState } from 'react';
import { listAlerts, updateAlertStatus } from '../api/alerts';
import { Banner, ErrorState, LoadingState } from '../components/StateMessage';

const statuses = ['OPEN', 'ACKNOWLEDGED', 'RESOLVED'];

export default function AlertsPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [banner, setBanner] = useState({ type: '', message: '' });

  const load = () => {
    setLoading(true);
    listAlerts()
      .then(setItems)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const onStatusChange = async (id, status) => {
    try {
      await updateAlertStatus(id, status);
      setBanner({ type: 'success', message: 'Alert status updated.' });
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
              <th>Type</th>
              <th>Severity</th>
              <th>Message</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {items.map((a) => (
              <tr key={a.id}>
                <td>{new Date(a.createdAt).toLocaleString()}</td>
                <td>{a.facilityName}</td>
                <td>{a.resourceType}</td>
                <td>{a.severity}</td>
                <td>{a.message}</td>
                <td>
                  <select value={a.status} onChange={(e) => onStatusChange(a.id, e.target.value)}>
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
