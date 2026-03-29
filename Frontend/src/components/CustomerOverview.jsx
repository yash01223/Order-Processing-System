import { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import api from '../api/axios';
import { Package, Receipt, ShoppingCart, Clock, CheckCircle, ArrowRight } from 'lucide-react';

const statusClass = (status) => {
  switch (status) {
    case 'PENDING':    return 'status-pending';
    case 'CONFIRMED':  return 'status-confirmed';
    case 'DISPATCHED': return 'status-dispatched';
    case 'DELIVERED':  return 'status-delivered';
    case 'CANCELLED':  return 'status-cancelled';
    default: return '';
  }
};

const CustomerOverview = () => {
  const [recentOrders, setRecentOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ total: 0, pending: 0, delivered: 0 });

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const { data } = await api.get('/orders?page=0&size=50');
        const orders = data.content || [];
        setRecentOrders(orders.slice(0, 5));
        setStats({
          total: orders.length,
          pending: orders.filter(o => o.status === 'PENDING' || o.status === 'CONFIRMED' || o.status === 'DISPATCHED').length,
          delivered: orders.filter(o => o.status === 'DELIVERED').length,
        });
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    fetchOrders();
  }, []);

  const quickLinks = [
    {
      title: 'Browse Products',
      desc: 'Explore our catalogue and add items to your cart.',
      icon: <Package size={28} className="text-primary" />,
      bg: 'bg-primary/10 border-primary/20',
      to: '/products',
      btnClass: 'btn-primary',
      btnLabel: 'View Catalogue',
    },
    {
      title: 'My Orders',
      desc: 'Track your recent purchases and order statuses.',
      icon: <Receipt size={28} className="text-secondary" />,
      bg: 'bg-secondary/10 border-secondary/20',
      to: '/orders',
      btnClass: 'btn-secondary',
      btnLabel: 'View Orders',
    },
    {
      title: 'My Cart',
      desc: 'Review items in your cart and place an order.',
      icon: <ShoppingCart size={28} className="text-accent" />,
      bg: 'bg-accent/10 border-accent/20',
      to: '/cart',
      btnClass: 'bg-accent/10 text-accent border border-accent/20 hover:bg-accent/20 btn',
      btnLabel: 'Open Cart',
    },
  ];

  const statItems = [
    { label: 'Total Orders', value: stats.total, icon: <Package size={16} className="text-primary" /> },
    { label: 'Active / In-Progress', value: stats.pending, icon: <Clock size={16} className="text-accent" /> },
    { label: 'Delivered', value: stats.delivered, icon: <CheckCircle size={16} className="text-emerald-400" /> },
  ];

  return (
    <div className="space-y-6 animate-fade-in">

      {/* Mini stats row */}
      <div className="grid grid-cols-3 gap-4">
        {statItems.map((s, i) => (
          <div key={i} className="card flex items-center gap-3 p-4">
            <div className="p-2 rounded-lg bg-surfaceHover">
              {s.icon}
            </div>
            <div>
              <p className="text-xl font-bold text-white">{loading ? '—' : s.value}</p>
              <p className="text-xs text-muted">{s.label}</p>
            </div>
          </div>
        ))}
      </div>

      {/* Quick Links */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {quickLinks.map((item, idx) => (
          <div key={idx} className="card flex flex-col items-center text-center py-7 hover:border-primary/20 hover:-translate-y-0.5 transition-all duration-200">
            <div className={`w-14 h-14 rounded-2xl border flex items-center justify-center mb-4 ${item.bg}`}>
              {item.icon}
            </div>
            <h3 className="font-bold text-white mb-1">{item.title}</h3>
            <p className="text-xs text-muted px-2 mb-5 leading-relaxed">{item.desc}</p>
            <NavLink to={item.to} className={`btn ${item.btnClass} gap-2`}>
              {item.btnLabel} <ArrowRight size={14} />
            </NavLink>
          </div>
        ))}
      </div>

      {/* Recent Orders */}
      {!loading && recentOrders.length > 0 && (
        <div className="card p-0 overflow-hidden">
          <div className="flex items-center justify-between px-5 py-4 border-b border-surfaceBorder">
            <h3 className="font-semibold text-white text-sm">Recent Orders</h3>
            <NavLink to="/orders" className="btn btn-ghost btn-sm gap-1">
              View All <ArrowRight size={12} />
            </NavLink>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left">
              <thead className="table-header">
                <tr>
                  <th className="table-th">Order</th>
                  <th className="table-th">Date</th>
                  <th className="table-th text-right">Amount</th>
                  <th className="table-th">Status</th>
                </tr>
              </thead>
              <tbody>
                {recentOrders.map((o) => (
                  <tr key={o.id} className="table-row">
                    <td className="table-td font-mono text-xs text-muted">#{String(o.id).padStart(5, '0')}</td>
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
        </div>
      )}
    </div>
  );
};

export default CustomerOverview;
