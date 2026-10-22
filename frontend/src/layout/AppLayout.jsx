import { NavLink, Outlet, useLocation } from 'react-router-dom';

const navItems = [
  { to: '/', label: 'Dashboard' },
  { to: '/facilities', label: 'Facilities' },
  { to: '/meters', label: 'Resource Meters' },
  { to: '/readings', label: 'Readings' },
  { to: '/alerts', label: 'Alerts' },
  { to: '/recommendations', label: 'Recommendations' },
  { to: '/reports', label: 'Sustainability Reports' },
];

const titles = Object.fromEntries(navItems.map((n) => [n.to, n.label]));

export default function AppLayout() {
  const { pathname } = useLocation();
  const title = titles[pathname] || 'EcoPulse';

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">EcoPulse</div>
        <nav>
          {navItems.map((item) => (
            <NavLink key={item.to} to={item.to} end={item.to === '/'} className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>
      <div className="main-panel">
        <header className="topbar">
          <h1>{title}</h1>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
