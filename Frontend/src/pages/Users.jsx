import { useState, useEffect } from 'react';
import api from '../api/axios';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import { Users as UsersIcon, Shield, User, Search, X, Mail, Hash, Calendar, Trash2 } from 'lucide-react';

/* ── User Detail Modal ──────────────────────────────────────────────────── */
const UserDetailModal = ({ userId, users, onClose, onDelete }) => {
  const user = users.find(u => u.id === userId);
  const { user: currentUser } = useAuth();
  const isSelf = currentUser?.id === userId;

  if (!user) return null;

  return (
    <div className="modal-backdrop" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal-box max-w-md animate-slide-up" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-primary/10 border border-primary/20 flex items-center justify-center">
              <User size={18} className="text-primary" />
            </div>
            <div>
              <h2 className="font-semibold text-white text-sm">User Profile</h2>
              <p className="text-[11px] text-muted">Joined {user.createdAt ? new Date(user.createdAt).toLocaleDateString(undefined, { month: 'long', day: 'numeric', year: 'numeric' }) : '—'}</p>
            </div>
          </div>
          <button onClick={onClose} className="btn btn-ghost btn-icon p-1"><X size={18} /></button>
        </div>

        <div className="modal-body p-5">
          <div className="space-y-4">
            {/* Main Info Grid */}
            <div className="grid grid-cols-2 gap-3">
              {[
                { label: 'Full Name', value: user.name, icon: <User size={13} className="text-primary" /> },
                { label: 'Email Address', value: user.email, icon: <Mail size={13} className="text-secondary" /> },
                { label: 'Account Role', value: user.role, icon: <Shield size={13} className="text-accent" /> },
                { label: 'User ID', value: `#${user.id}`, icon: <Hash size={13} className="text-muted" /> },
              ].map((m, i) => (
                <div key={i} className="bg-dark/50 rounded-xl p-3 border border-surfaceBorder">
                  <p className="text-[10px] text-muted uppercase tracking-wider mb-1 flex items-center gap-1.5 font-semibold">
                    {m.icon}{m.label}
                  </p>
                  <div className="text-sm font-semibold text-white truncate" title={m.value}>{m.value}</div>
                </div>
              ))}
            </div>

            {/* Registration Date Banner */}
            <div className="bg-primary/5 rounded-xl p-4 border border-primary/10 flex items-center gap-3">
              <div className="p-2 rounded-lg bg-primary/10">
                <Calendar size={16} className="text-primary" />
              </div>
              <div>
                <p className="text-[10px] text-muted uppercase font-bold tracking-tight">Registered On</p>
                <p className="text-xs text-white font-medium">
                  {user.createdAt ? new Date(user.createdAt).toLocaleString(undefined, { 
                    dateStyle: 'full',
                    timeStyle: 'short'
                  }) : 'Not available'}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="modal-footer flex gap-2">
          {!isSelf ? (
            <button 
              onClick={() => onDelete(user.id, user.name)}
              className="btn btn-danger gap-2 flex-1"
            >
              <Trash2 size={14} /> Delete User
            </button>
          ) : (
            <div className="flex-1 text-center py-2 px-3 bg-surfaceHover rounded-lg text-[11px] text-muted italic">
              Your own account cannot be deleted from here.
            </div>
          )}
          <button onClick={onClose} className="btn btn-outline flex-1">Close</button>
        </div>
      </div>
    </div>
  );
};

/* ── Users Page ────────────────────────────────────────────────────────── */
const Users = () => {
  const [users, setUsers] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [selectedUserId, setSelectedUserId] = useState(null);

  const fetchUsers = async () => {
    try {
      const { data } = await api.get('/users');
      setUsers(data);
      setFiltered(data);
    } catch (e) {
      toast.error('Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  useEffect(() => {
    if (!search.trim()) {
      setFiltered(users);
    } else {
      const q = search.toLowerCase();
      setFiltered(users.filter(u =>
        u.name.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q) ||
        u.role.toLowerCase().includes(q)
      ));
    }
  }, [search, users]);

  const deleteUser = async (id, name) => {
    if (!window.confirm(`Are you sure you want to delete user "${name}"?`)) {
      return;
    }

    try {
      await api.delete(`/users/${id}`);
      toast.success(`User "${name}" deleted successfully`);
      setSelectedUserId(null);
      // Update local state instead of re-fetching everything
      setUsers(prev => prev.filter(u => u.id !== id));
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to delete user');
    }
  };

  const adminCount = users.filter(u => u.role === 'ADMIN').length;
  const customerCount = users.filter(u => u.role === 'CUSTOMER').length;

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="page-title flex items-center gap-2">
            <UsersIcon size={22} className="text-primary" />
            User Management
          </h1>
          <p className="page-subtitle">
            {!loading && `${users.length} total · ${adminCount} admin${adminCount !== 1 ? 's' : ''} · ${customerCount} customer${customerCount !== 1 ? 's' : ''}`}
          </p>
        </div>

        {/* Search */}
        <div className="relative w-full sm:w-64">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-muted pointer-events-none" />
          <input
            type="text"
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Search by name, email..."
            className="input-field pl-9 pr-8 text-sm"
          />
          {search && (
            <button onClick={() => setSearch('')} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted hover:text-white">
              <X size={13} />
            </button>
          )}
        </div>
      </div>

      {/* Stats row */}
      {!loading && (
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 animate-slide-up">
          {[
            { label: 'Total Users', value: users.length, color: 'text-white', bg: 'bg-primary/10 border-primary/20' },
            { label: 'Admins', value: adminCount, color: 'text-primary', bg: 'bg-primary/5 border-primary/10' },
            { label: 'Customers', value: customerCount, color: 'text-secondary', bg: 'bg-secondary/5 border-secondary/10' },
          ].map((s, i) => (
            <div key={i} className={`card border ${s.bg} p-4`}>
              <p className={`text-2xl font-bold ${s.color}`}>{s.value}</p>
              <p className="text-xs text-muted mt-0.5">{s.label}</p>
            </div>
          ))}
        </div>
      )}

      {/* User list */}
      {loading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="card animate-pulse flex items-center gap-3 p-5">
              <div className="w-12 h-12 rounded-full bg-surfaceHover flex-shrink-0" />
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-surfaceHover rounded w-3/4" />
                <div className="h-3 bg-surfaceHover rounded w-1/2" />
                <div className="h-4 bg-surfaceHover rounded w-1/4 mt-2" />
              </div>
            </div>
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="empty-state py-12">
          <UsersIcon size={44} className="empty-icon mx-auto mb-3" />
          <h3 className="text-white font-semibold mb-1">No users found</h3>
          <p className="text-muted text-sm">Try a different search term.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((u, idx) => {
            const isAdmin = u.role === 'ADMIN';
            const initials = u.name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
            return (
              <div
                key={u.id}
                onClick={() => setSelectedUserId(u.id)}
                className="card group cursor-pointer hover:border-primary/40 hover:-translate-y-1 transition-all duration-300 animate-fade-in relative overflow-hidden"
                style={{ animationDelay: `${idx * 40}ms` }}
              >
                {/* Decorative background glow */}
                <div className={`absolute -right-4 -top-4 w-20 h-20 rounded-full blur-2xl opacity-0 group-hover:opacity-20 transition-opacity duration-300 ${isAdmin ? 'bg-primary' : 'bg-secondary'}`} />
                
                <div className="flex items-start gap-4 p-1">
                  {/* Avatar */}
                  <div className={`w-12 h-12 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0 shadow-lg ${
                    isAdmin
                      ? 'bg-gradient-to-br from-primary to-primaryHover text-dark'
                      : 'bg-gradient-to-br from-secondary to-secondaryHover text-dark'
                  }`}>
                    {initials}
                  </div>

                  {/* Info */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="font-semibold text-white text-sm truncate group-hover:text-primary transition-colors">{u.name}</h3>
                      <span className={`badge text-[10px] ${isAdmin ? 'role-admin' : 'role-customer'}`}>
                        {isAdmin ? <Shield size={9} /> : <User size={9} />}
                        {isAdmin ? 'Admin' : 'Customer'}
                      </span>
                    </div>
                    <div className="flex items-center gap-1.5 text-xs text-muted mb-1">
                      <Mail size={11} className="opacity-60" />
                      <span className="truncate">{u.email}</span>
                    </div>
                    <div className="flex items-center gap-1.5 text-xs text-muted">
                      <Calendar size={11} className="opacity-60" />
                      <span>{u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}</span>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Detail Modal */}
      {selectedUserId && (
        <UserDetailModal 
          userId={selectedUserId} 
          users={users} 
          onClose={() => setSelectedUserId(null)} 
          onDelete={deleteUser}
        />
      )}
    </div>
  );
};

export default Users;
