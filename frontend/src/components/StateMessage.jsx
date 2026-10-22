export function LoadingState({ label = 'Loading…' }) {
  return <p className="state-message">{label}</p>;
}

export function ErrorState({ message }) {
  return <p className="state-message error">{message}</p>;
}

export function EmptyState({ title, action }) {
  return (
    <div className="empty-state">
      <h3>{title}</h3>
      {action}
    </div>
  );
}

export function Banner({ type, message, onClose }) {
  if (!message) return null;
  return (
    <div className={`banner banner-${type}`}>
      <span>{message}</span>
      {onClose && (
        <button type="button" className="link-btn" onClick={onClose}>
          Dismiss
        </button>
      )}
    </div>
  );
}
