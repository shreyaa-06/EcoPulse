import { useEffect, useState } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Legend } from 'recharts';
import { listFacilities } from '../api/facilities';
import { downloadReportCsv, getReportSummary, getReportTrends } from '../api/reports';
import { Banner, ErrorState, LoadingState } from '../components/StateMessage';

function defaultRange() {
  const end = new Date();
  const start = new Date();
  start.setDate(end.getDate() - 29);
  return {
    startDate: start.toISOString().slice(0, 10),
    endDate: end.toISOString().slice(0, 10),
  };
}

export default function ReportsPage() {
  const [facilities, setFacilities] = useState([]);
  const [range, setRange] = useState(defaultRange);
  const [facilityId, setFacilityId] = useState('');
  const [summary, setSummary] = useState(null);
  const [trends, setTrends] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [banner, setBanner] = useState({ type: '', message: '' });

  const params = () => {
    const p = { startDate: range.startDate, endDate: range.endDate };
    if (facilityId) p.facilityId = facilityId;
    return p;
  };

  const load = () => {
    setLoading(true);
    Promise.all([listFacilities(), getReportSummary(params()), getReportTrends(params())])
      .then(([f, s, t]) => {
        setFacilities(f);
        setSummary(s);
        setTrends(t.trends || []);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const onApply = (e) => {
    e.preventDefault();
    load();
  };

  const onExport = async () => {
    try {
      const blob = await downloadReportCsv(params());
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'ecopulse-report.csv';
      a.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      setBanner({ type: 'error', message: err.message });
    }
  };

  if (loading && !summary) return <LoadingState />;
  if (error) return <ErrorState message={error} />;

  const cmp = summary?.comparison;

  return (
    <div className="stack">
      <Banner {...banner} onClose={() => setBanner({ type: '', message: '' })} />
      <form className="card filters" onSubmit={onApply}>
        <label>
          Start
          <input type="date" value={range.startDate} onChange={(e) => setRange({ ...range, startDate: e.target.value })} />
        </label>
        <label>
          End
          <input type="date" value={range.endDate} onChange={(e) => setRange({ ...range, endDate: e.target.value })} />
        </label>
        <label>
          Facility
          <select value={facilityId} onChange={(e) => setFacilityId(e.target.value)}>
            <option value="">All facilities</option>
            {facilities.map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </select>
        </label>
        <button type="submit" className="btn primary">
          Apply
        </button>
        <button type="button" className="btn" onClick={onExport}>
          Download CSV
        </button>
      </form>

      {summary && (
        <>
          <p className="muted">
            Period: {summary.startDate} to {summary.endDate}
          </p>
          <div className="stat-grid">
            <div className="card stat-card">
              <p className="stat-label">Energy</p>
              <p className="stat-value">
                {summary.totalEnergyKwh} <span className="unit">kWh</span>
              </p>
            </div>
            <div className="card stat-card">
              <p className="stat-label">Water</p>
              <p className="stat-value">
                {summary.totalWaterLiters} <span className="unit">L</span>
              </p>
            </div>
            <div className="card stat-card">
              <p className="stat-label">Waste</p>
              <p className="stat-value">
                {summary.totalWasteKg} <span className="unit">kg</span>
              </p>
            </div>
            <div className="card stat-card">
              <p className="stat-label">Alerts generated</p>
              <p className="stat-value">{summary.alertsGenerated}</p>
            </div>
            <div className="card stat-card">
              <p className="stat-label">Recommendations completed</p>
              <p className="stat-value">{summary.recommendationsCompleted}</p>
            </div>
          </div>

          {cmp && (
            <div className="card">
              <h3>Previous period comparison ({cmp.previousStartDate} – {cmp.previousEndDate})</h3>
              <p>
                Energy: {cmp.previousEnergyKwh} kWh · Water: {cmp.previousWaterLiters} L · Waste: {cmp.previousWasteKg} kg
              </p>
            </div>
          )}

          <div className="card chart-card">
            <h3>Consumption trends</h3>
            <ResponsiveContainer width="100%" height={280}>
              <LineChart data={trends}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="energyKwh" name="Energy (kWh)" stroke="#166534" dot={false} />
                <Line type="monotone" dataKey="waterLiters" name="Water (L)" stroke="#2563eb" dot={false} />
                <Line type="monotone" dataKey="wasteKg" name="Waste (kg)" stroke="#92400e" dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>

          <div className="card">
            <h3>By facility</h3>
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
                {summary.byFacility?.map((row) => (
                  <tr key={row.facilityId}>
                    <td>{row.facilityName}</td>
                    <td>{row.energyKwh}</td>
                    <td>{row.waterLiters}</td>
                    <td>{row.wasteKg}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
