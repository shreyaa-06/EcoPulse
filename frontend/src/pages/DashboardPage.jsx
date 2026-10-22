import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import { getDashboardSummary } from '../api/dashboard';
import { EmptyState, ErrorState, LoadingState } from '../components/StateMessage';

function StatCard({ label, value, unit }) {
  return (
    <div className="card stat-card">
      <p className="stat-label">{label}</p>
      <p className="stat-value">
        {value} {unit && <span className="unit">{unit}</span>}
      </p>
    </div>
  );
}

function TrendChart({ title, data, color, unit }) {
  return (
    <div className="card chart-card">
      <h3>{title}</h3>
      {data?.length ? (
        <ResponsiveContainer width="100%" height={240}>
          <LineChart data={data}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
            <XAxis dataKey="date" tick={{ fontSize: 11 }} />
            <YAxis tick={{ fontSize: 11 }} />
            <Tooltip formatter={(v) => [`${v} ${unit}`, 'Total']} />
            <Line type="monotone" dataKey="total" stroke={color} strokeWidth={2} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      ) : (
        <p className="muted">No trend data for the last 30 days.</p>
      )}
    </div>
  );
}

export default function DashboardPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getDashboardSummary()
      .then(setData)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingState />;
  if (error) return <ErrorState message={error} />;

  const empty = data.totalFacilities === 0;

  if (empty) {
    return (
      <EmptyState
        title="No facilities yet"
        action={
          <Link className="btn primary" to="/facilities">
            Add your first facility
          </Link>
        }
      />
    );
  }

  return (
    <div className="stack">
      <div className="stat-grid">
        <StatCard label="Total facilities" value={data.totalFacilities} />
        <StatCard label="Energy (30d)" value={data.totalEnergyKwh} unit="kWh" />
        <StatCard label="Water (30d)" value={data.totalWaterLiters} unit="L" />
        <StatCard label="Waste (30d)" value={data.totalWasteKg} unit="kg" />
        <StatCard label="Active alerts" value={data.activeAlerts} />
      </div>

      <div className="grid-2">
        <TrendChart title="Energy trend" data={data.energyTrend} color="#166534" unit="kWh" />
        <TrendChart title="Water trend" data={data.waterTrend} color="#2563eb" unit="L" />
      </div>
      <TrendChart title="Waste trend" data={data.wasteTrend} color="#92400e" unit="kg" />

      <div className="grid-2">
        <div className="card">
          <h3>Recent readings</h3>
          <table>
            <thead>
              <tr>
                <th>Facility</th>
                <th>Type</th>
                <th>Value</th>
                <th>When</th>
              </tr>
            </thead>
            <tbody>
              {data.recentReadings?.map((r) => (
                <tr key={r.id}>
                  <td>{r.facilityName}</td>
                  <td>{r.resourceType}</td>
                  <td>
                    {r.readingValue} {r.unit}
                  </td>
                  <td>{new Date(r.readingTimestamp).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="card">
          <h3>Recent alerts</h3>
          <table>
            <thead>
              <tr>
                <th>Severity</th>
                <th>Message</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {data.recentAlerts?.map((a) => (
                <tr key={a.id}>
                  <td>{a.severity}</td>
                  <td>{a.message}</td>
                  <td>{a.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="card">
        <h3>Recommendations needing attention</h3>
        <ul className="plain-list">
          {data.recommendationsNeedingAttention?.length ? (
            data.recommendationsNeedingAttention.map((rec) => (
              <li key={rec.id}>
                <strong>{rec.facilityName}</strong> — {rec.actionText} <span className="badge">{rec.status}</span>
              </li>
            ))
          ) : (
            <li className="muted">No pending recommendations.</li>
          )}
        </ul>
      </div>

      <div className="card">
        <h3>Facility consumption (30d)</h3>
        <table>
          <thead>
            <tr>
              <th>Facility</th>
              <th>Energy (kWh)</th>
              <th>Water (L)</th>
              <th>Waste (kg)</th>
            </tr>
          </thead>
          <tbody>
            {data.facilitySummaries?.map((f) => (
              <tr key={f.facilityId}>
                <td>{f.facilityName}</td>
                <td>{f.energyKwh}</td>
                <td>{f.waterLiters}</td>
                <td>{f.wasteKg}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
