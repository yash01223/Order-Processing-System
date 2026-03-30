import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';
import { toast } from 'react-toastify';
import {
  FileText, Eye, XCircle, X,
  Package, Calendar, IndianRupee, User2, ChevronRight, ChevronLeft,
  RefreshCw,
} from 'lucide-react';

/* ── Status helpers ─────────────────────────────────────────────────────── */
const STATUS_SEQ = ['PENDING', 'CONFIRMED', 'DISPATCHED', 'DELIVERED'];

const statusClass = (status) => {
  switch (status) {
    case 'PENDING':    return 'status-pending';
    case 'CONFIRMED':  return 'status-confirmed';
    case 'DISPATCHED': return 'status-dispatched';
    case 'DELIVERED':  return 'status-delivered';
    case 'CANCELLED':  return 'status-cancelled';
    default: return 'bg-surfaceHover text-muted';
  }
};

const nextStatus = (current) => {
  const idx = STATUS_SEQ.indexOf(current);
  return idx >= 0 && idx < STATUS_SEQ.length - 1 ? STATUS_SEQ[idx + 1] : null;
};

/* ── Order Detail Modal ─────────────────────────────────────────────────── */
const OrderDetailModal = ({ orderId, onClose }) => {
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const { data } = await api.get(`/orders/${orderId}`);
        setOrder(data);
      } catch (e) {
        toast.error('Failed to load order details');
        onClose();
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [orderId]);

  return (
    <div className="modal-backdrop" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal-box max-w-lg" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-primary/10 border border-primary/20 flex items-center justify-center">
              <FileText size={15} className="text-primary" />
            </div>
            <div>
              <h2 className="font-semibold text-white text-sm">
                Order #{order ? String(order.id).padStart(5,'0') : '—'}
              </h2>
              {order && <p className="text-[11px] text-muted">Placed {new Date(order.createdAt).toLocaleString()}</p>}
            </div>
          </div>
          <button onClick={onClose} className="btn btn-ghost btn-icon p-1"><X size={18} /></button>
        </div>

        <div className="modal-body">
          {loading ? (
            <div className="flex justify-center py-8"><span className="spinner spinner-md" /></div>
          ) : order ? (
            <div className="space-y-5">
              {/* Meta */}
              <div className="grid grid-cols-2 gap-3">
                {[
                  { label: 'Customer', value: order.customerName || '—', icon: <User2 size={13} className="text-primary" /> },
                  { label: 'Status', value: <span className={`status-badge ${statusClass(order.status)}`}>{order.status}</span>, icon: null },
                  { label: 'Total', value: `₹${Number(order.totalAmount || 0).toFixed(2)}`, icon: <IndianRupee size={13} className="text-secondary" /> },
                  { label: 'Date', value: new Date(order.createdAt).toLocaleDateString(), icon: <Calendar size={13} className="text-muted" /> },
                ].map((m, i) => (
                  <div key={i} className="bg-dark rounded-lg p-3 border border-surfaceBorder">
                    <p className="text-[10px] text-muted uppercase tracking-wider mb-1 flex items-center gap-1">
                      {m.icon}{m.label}
                    </p>
                    <div className="text-sm font-semibold text-white">{m.value}</div>
                  </div>
                ))}
              </div>

              {/* Items */}
              {order.items && order.items.length > 0 ? (
                <div>
                  <p className="text-xs font-semibold text-muted uppercase tracking-wider mb-3">Order Items</p>
                  <div className="space-y-2">
                    {order.items.map((item, i) => (
                      <div key={i} className="flex items-center gap-3 py-2.5 px-3 bg-dark rounded-lg border border-surfaceBorder">
                        <div className="w-8 h-8 rounded-lg bg-surfaceHover flex items-center justify-center flex-shrink-0">
                          <Package size={14} className="text-muted" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium text-white truncate">{item.productName}</p>
                          <p className="text-xs text-muted">Qty: {item.quantity} × ₹{Number(item.priceAtPurchase).toFixed(2)}</p>
                        </div>
                        <div className="font-bold text-white text-sm flex-shrink-0">
                          ₹{Number(item.lineTotal).toFixed(2)}
                        </div>
                      </div>
                    ))}
                  </div>
                  <div className="flex justify-between items-center px-3 pt-3 border-t border-surfaceBorder mt-3">
                    <span className="text-sm text-muted">Grand Total</span>
                    <span className="font-bold text-white text-lg">₹{Number(order.totalAmount).toFixed(2)}</span>
                  </div>
                </div>
              ) : (
                <p className="text-center text-muted text-sm py-4">Item detail not available.</p>
              )}
            </div>
          ) : null}
        </div>

        <div className="modal-footer">
          <button onClick={onClose} className="btn btn-outline">Close</button>
        </div>
      </div>
    </div>
  );
};

/* ── Orders Page ────────────────────────────────────────────────────────── */
const Orders = () => {
  const { isAdmin } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [detailId, setDetailId] = useState(null);

  const PAGE_SIZE = 10;

  const fetchOrders = useCallback(async (isRefresh = false) => {
    if (isRefresh) setRefreshing(true);
    else setLoading(true);
    try {
      const { data } = await api.get(`/orders?page=${page}&size=${PAGE_SIZE}`);
      setOrders(data.content || []);
      setTotalPages(data.totalPages || 1);
      setTotalElements(data.totalElements || 0);
    } catch (e) {
      toast.error('Failed to fetch orders');
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [page]);

  useEffect(() => { fetchOrders(); }, [fetchOrders]);
 
   /**
    * Auto-refresh: Poll every 30s to sync with backend cleanup.
    * This ensures the dashboard stays tidy by removing expired orders automatically.
    */
   useEffect(() => {
     const timer = setInterval(() => {
       fetchOrders(true);
     }, 30000);
     return () => clearInterval(timer);
   }, [fetchOrders]);
 
   const ORDER_EXPIRY_MS = 5 * 60 * 1000;

  const advanceStatus = async (id, currentStatus) => {
    const next = nextStatus(currentStatus);
    if (!next) return;
    try {
      await api.patch(`/orders/${id}/status`);
      toast.success(`Order advanced to ${next}`);
      fetchOrders(true);
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to update status');
    }
  };

  const cancelOrder = async (id) => {
    try {
      await api.patch(`/orders/${id}/cancel`);
      toast.info('Order cancelled');
      fetchOrders(true);
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to cancel order');
    }
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title flex items-center gap-2">
            <FileText size={22} className="text-primary" />
            {isAdmin ? 'All Orders' : 'My Orders'}
          </h1>
          <p className="page-subtitle">
            {isAdmin ? 'Manage and advance order statuses.' : 'Track your order history and statuses.'}
            {!loading && ` · ${totalElements} order${totalElements !== 1 ? 's' : ''}`}
          </p>
        </div>
        <button
          onClick={() => fetchOrders(true)}
          disabled={refreshing}
          className="btn btn-outline btn-sm gap-2"
        >
          <RefreshCw size={13} className={refreshing ? 'animate-spin' : ''} />
          Refresh
        </button>
      </div>

      {/* Content */}
      {loading ? (
        <div className="flex justify-center py-16"><span className="spinner spinner-lg" /></div>
      ) : orders.length === 0 ? (
        <div className="empty-state py-16">
          <FileText size={48} className="empty-icon mx-auto mb-3" />
          <h3 className="text-white font-semibold mb-1">No orders found</h3>
          <p className="text-muted text-sm">
            {isAdmin ? 'No orders have been placed yet.' : 'You have not placed any orders yet.'}
          </p>
        </div>
      ) : (
        <>
          <div className="table-container">
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead className="table-header">
                  <tr>
                    <th className="table-th">Order ID</th>
                    {isAdmin && <th className="table-th">Customer</th>}
                    <th className="table-th">Date</th>
                    <th className="table-th text-right">Amount</th>
                    <th className="table-th">Status</th>
                    <th className="table-th text-right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {orders
                    .filter(o => {
                      if (o.status !== 'DELIVERED' && o.status !== 'CANCELLED') return true;
                      const updatedAt = o.statusUpdatedAt ? new Date(o.statusUpdatedAt).getTime() : new Date(o.createdAt).getTime();
                      const diff = Date.now() - updatedAt;
                      return diff < ORDER_EXPIRY_MS;
                    })
                    .map((o) => (
                    <tr key={o.id} className="table-row animate-fade-in shadow-sm">
                      <td className="table-td font-mono text-xs text-muted">
                        #{String(o.id).padStart(5, '0')}
                      </td>
                      {isAdmin && (
                        <td className="table-td font-medium text-white">{o.customerName || '—'}</td>
                      )}
                      <td className="table-td text-xs text-muted">
                        {o.createdAt
                          ? new Date(o.createdAt).toLocaleString('en-US', {
                              month: 'short', day: 'numeric', year: 'numeric',
                              hour: '2-digit', minute: '2-digit',
                            })
                          : '—'}
                      </td>
                      <td className="table-td text-right font-bold text-white">
                        ₹{Number(o.totalAmount || 0).toFixed(2)}
                      </td>
                      <td className="table-td">
                        <span className={`status-badge ${statusClass(o.status)}`}>{o.status}</span>
                      </td>
                      <td className="table-td text-right">
                        <div className="flex items-center justify-end gap-2">
                          {/* View Details */}
                          <button
                            onClick={() => setDetailId(o.id)}
                            className="btn btn-ghost btn-icon btn-sm"
                            title="View details"
                          >
                            <Eye size={15} />
                          </button>

                          {/* Advance Status (Admin) */}
                          {isAdmin && nextStatus(o.status) && (
                            <button
                              onClick={() => advanceStatus(o.id, o.status)}
                              className="btn btn-outline btn-sm gap-1 text-xs"
                              title={`Advance to ${nextStatus(o.status)}`}
                            >
                              <ChevronRight size={13} />
                              {nextStatus(o.status)}
                            </button>
                          )}

                          {/* Cancel (Customer - only PENDING) */}
                          {!isAdmin && o.status === 'PENDING' && (
                            <button
                              onClick={() => cancelOrder(o.id)}
                              className="btn btn-danger btn-sm gap-1"
                              title="Cancel order"
                            >
                              <XCircle size={13} /> Cancel
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between">
              <p className="text-xs text-muted">
                Page {page + 1} of {totalPages} · {totalElements} total orders
              </p>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setPage(p => Math.max(0, p - 1))}
                  disabled={page === 0}
                  className="btn btn-outline btn-sm gap-1"
                >
                  <ChevronLeft size={13} /> Prev
                </button>
                <span className="text-xs text-muted px-1">{page + 1} / {totalPages}</span>
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
        </>
      )}

      {/* Order Detail Modal */}
      {detailId && (
        <OrderDetailModal orderId={detailId} onClose={() => setDetailId(null)} />
      )}
    </div>
  );
};

export default Orders;
