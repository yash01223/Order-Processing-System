import { useState, useEffect, useCallback } from 'react';
import api from '../api/axios';
import { toast } from 'react-toastify';
import {
  Bell, CheckCircle, RefreshCw, Package,
  ChevronLeft, ChevronRight,
} from 'lucide-react';

const Notifications = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const PAGE_SIZE = 4;

  const fetchNotifications = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    try {
      const { data } = await api.get(`/notifications?page=${page}&size=${PAGE_SIZE}`);
      const notifPage = data.notifications;
      setNotifications(notifPage?.content || []);
      setTotalPages(notifPage?.totalPages || 1);
      setTotalElements(notifPage?.totalElements || 0);
    } catch (e) {
      toast.error('Failed to load notifications');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [page]);

  useEffect(() => { fetchNotifications(); }, [fetchNotifications]);

  // ── Auto-refresh: Poll every 60s to sync with backend cleanup ──
  useEffect(() => {
    const timer = setInterval(() => {
      fetchNotifications(true);
    }, 60000);
    return () => clearInterval(timer);
  }, [fetchNotifications]);

  const NOTIFICATION_EXPIRY_MS = 1 * 60 * 60 * 1000;

  const getTypeIcon = (type) => {
    switch (type) {
      case 'ORDER_PLACED':    return <div className="w-8 h-8 rounded-full bg-primary/15 border border-primary/25 flex items-center justify-center"><Package size={15} className="text-primary" /></div>;
      case 'ORDER_CONFIRMED': return <div className="w-8 h-8 rounded-full bg-blue-500/15 border border-blue-500/25 flex items-center justify-center"><CheckCircle size={15} className="text-blue-400" /></div>;
      case 'ORDER_DISPATCHED':return <div className="w-8 h-8 rounded-full bg-violet-500/15 border border-violet-500/25 flex items-center justify-center"><Package size={15} className="text-violet-400" /></div>;
      case 'ORDER_DELIVERED': return <div className="w-8 h-8 rounded-full bg-emerald-500/15 border border-emerald-500/25 flex items-center justify-center"><CheckCircle size={15} className="text-emerald-400" /></div>;
      case 'ORDER_CANCELLED': return <div className="w-8 h-8 rounded-full bg-red-500/15 border border-red-500/25 flex items-center justify-center"><Bell size={15} className="text-red-400" /></div>;
      default: return <div className="w-8 h-8 rounded-full bg-surfaceHover border border-surfaceBorder flex items-center justify-center"><Bell size={15} className="text-muted" /></div>;
    }
  };

  const formatTime = (dt) => {
    if (!dt) return '';
    const date = new Date(dt);
    const now = new Date();
    const diffMs = now - date;
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHr = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHr / 24);
    if (diffDay > 0) return `${diffDay}d ago`;
    if (diffHr > 0) return `${diffHr}h ago`;
    if (diffMin > 0) return `${diffMin}m ago`;
    return 'Just now';
  };

  return (
    <div className="space-y-5 max-w-3xl">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title flex items-center gap-2">
            <Bell size={22} className="text-primary" />
            Notifications
          </h1>
          <p className="page-subtitle">
            Order status updates and history
            {!loading && ` · ${totalElements}`}
          </p>
        </div>
      </div>

      {/* List */}
      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="card animate-pulse flex gap-4 items-start p-4">
              <div className="w-8 h-8 rounded-full bg-surfaceHover flex-shrink-0" />
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-surfaceHover rounded w-3/4" />
                <div className="h-3 bg-surfaceHover rounded w-1/2" />
              </div>
            </div>
          ))}
        </div>
      ) : notifications.length === 0 ? (
        <div className="empty-state py-16">
          <Bell size={48} className="empty-icon mx-auto mb-3" />
          <h3 className="text-white font-semibold mb-1">No notifications yet</h3>
          <p className="text-muted text-sm">You will receive updates when your orders change status.</p>
        </div>
      ) : (
        <div className="space-y-2">
          {notifications
            .filter(n => {
              const diff = Date.now() - new Date(n.createdAt).getTime();
              return diff < NOTIFICATION_EXPIRY_MS;
            })
            .map((n, idx) => (
            <div
              key={n.id}
              className="card flex items-start gap-4 p-4 transition-all duration-300 animate-fade-in hover:border-surfaceBorder/80"
              style={{ animationDelay: `${idx * 40}ms` }}
            >
              {/* Icon */}
              <div className="flex-shrink-0 mt-0.5">{getTypeIcon(n.type)}</div>

              {/* Content */}
              <div className="flex-1 min-w-0">
                <p className="text-sm leading-relaxed text-mutedLight">
                  {n.message}
                </p>
                <div className="flex items-center gap-2 mt-1">
                  <p className="text-[10px] text-muted">{formatTime(n.createdAt)}</p>
                  {!n.isRead && (
                    <span className="w-1 h-1 rounded-full bg-primary animate-pulse" />
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pagination */}
      {!loading && totalPages > 1 && (
        <div className="flex items-center justify-between pt-2">
          <p className="text-xs text-muted">Page {page + 1} of {totalPages}</p>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage(p => Math.max(0, p - 1))}
              disabled={page === 0}
              className="btn btn-outline btn-sm gap-1"
            >
              <ChevronLeft size={13} /> Prev
            </button>
            <button
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="btn btn-outline btn-sm gap-1"
            >
              Next <ChevronRight size={13} />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default Notifications;
