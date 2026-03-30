import { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import api from '../api/axios';
import {
  Package,
  Clock,
  CheckCircle,
  RefreshCw,
  ArrowRight,
  TrendingUp,
  IndianRupee,
} from 'lucide-react';

const statusClass = (status) => {
  switch (status) {
    case 'PENDING': return 'status-pending';
    case 'CONFIRMED': return 'status-confirmed';
    case 'DISPATCHED': return 'status-dispatched';
    case 'DELIVERED': return 'status-delivered';
    case 'CANCELLED': return 'status-cancelled';
    default: return 'bg-surfaceHover text-muted';
  }
};

const AdminOverview = () => {
  const [stats, setStats] = useState(null);
  const [recentOrders, setRecentOrders] = useState([]);
  const [loadingStats, setLoadingStats] = useState(true);
  const [loadingOrders, setLoadingOrders] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const fetchStats = async () => {
    try {
      const { data } = await api.get('/admin/dashboard');
      setStats(data);
    } catch (e) {
      console.error('Dashboard fetch error:', e);
    } finally {
      setLoadingStats(false);
    }
  };

  const fetchRecentOrders = async () => {
    try {
      const { data } = await api.get('/orders?page=0&size=6');
      setRecentOrders(data.content || []);
    } catch (e) {
      console.error('Recent orders fetch error:', e);
    } finally {
      setLoadingOrders(false);
    }
  };

  const refresh = async () => {
    setRefreshing(true);
    await Promise.all([fetchStats(), fetchRecentOrders()]);
    setRefreshing(false);
  };

  useEffect(() => {
    fetchStats();
    fetchRecentOrders();
  }, []);

  const statCards = stats ? [
    {
      title: 'Total Revenue',
      value: `₹${Number(stats.totalRevenue || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
      icon: <IndianRupee size={22} className="text-secondary" />,
      iconBg: 'bg-secondary/10 border border-secondary/20',
    },
    {
      title: 'Total Orders',
      value: stats.totalOrders,
      icon: <Package size={22} className="text-primary" />,
      iconBg: 'bg-primary/10 border border-primary/20',
    },
    {
      title: 'Pending',
      value: stats.pendingOrders,
      icon: <Clock size={22} className="text-accent" />,
      iconBg: 'bg-accent/10 border border-accent/20',
    },
    {
      title: 'Delivered',
      value: stats.deliveredOrders,
      icon: <CheckCircle size={22} className="text-emerald-400" />,
      iconBg: 'bg-emerald-500/10 border border-emerald-500/20',
    },
  ] : [];

  return (
    <div className="space-y-6 animate-fade-in">

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {loadingStats ? (
          Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className="card animate-pulse">
              <div className="h-12 bg-surfaceHover rounded-lg mb-3 w-12" />
              <div className="h-7 bg-surfaceHover rounded w-2/3 mb-2" />
              <div className="h-3 bg-surfaceHover rounded w-1/2" />
            </div>
          ))
        ) : statCards.map((s, idx) => (
          <div key={idx} className="card hover:border-primary/20 hover:-translate-y-0.5 transition-all duration-200 animate-slide-up" style={{ animationDelay: `${idx * 60}ms` }}>
            <div className="flex items-start justify-between mb-3">
              <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${s.iconBg}`}>
                {s.icon}
              </div>
              <TrendingUp size={14} className="text-muted opacity-50" />
            </div>
            <p className="text-2xl font-bold text-white mb-1">{s.value}</p>
            <p className="text-xs font-medium text-muted">{s.title}</p>
            <p className="text-[11px] text-muted/60 mt-1">{s.trend}</p>
          </div>
        ))}
      </div>

      {/* Recent Orders */}
      <div className="card p-0 overflow-hidden">
        <div className="flex items-center justify-between px-5 py-4 border-b border-surfaceBorder">
          <h3 className="font-semibold text-white text-sm">Recent Orders</h3>
          <div className="flex items-center gap-2">
            <button
              onClick={refresh}
              disabled={refreshing}
              className="btn btn-ghost btn-icon btn-sm"
              title="Refresh"
            >
              <RefreshCw size={14} className={refreshing ? 'animate-spin' : ''} />
            </button>
            <NavLink to="/orders" className="btn btn-ghost btn-sm gap-1">
              View All <ArrowRight size={12} />
            </NavLink>
          </div>
        </div>

        {loadingOrders ? (
          <div className="flex justify-center py-10">
            <span className="spinner spinner-md" />
          </div>
        ) : recentOrders.length === 0 ? (
          <div className="empty-state py-10">
            <Package size={40} className="empty-icon mx-auto" />
            <p className="text-muted text-sm">No orders yet.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead className="table-header">
                <tr>
                  <th className="table-th">Order</th>
                  <th className="table-th">Customer</th>
                  <th className="table-th">Date</th>
                  <th className="table-th text-right">Amount</th>
                  <th className="table-th">Status</th>
                </tr>
              </thead>
              <tbody>
                {recentOrders.map((o) => (
                  <tr key={o.id} className="table-row">
                    <td className="table-td font-mono text-xs text-muted">#{String(o.id).padStart(5, '0')}</td>
                    <td className="table-td font-medium text-white">{o.customerName || '—'}</td>
                    <td className="table-td text-muted text-xs">
                      {o.createdAt ? new Date(o.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : '—'}
                    </td>
                    <td className="table-td text-right font-semibold text-white">
                      ${Number(o.totalAmount || 0).toFixed(2)}
                    </td>
                    <td className="table-td">
                      <span className={`status-badge ${statusClass(o.status)}`}>{o.status}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminOverview;
