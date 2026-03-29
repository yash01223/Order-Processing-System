import { useState, useEffect } from 'react';
import api from '../api/axios';
import { toast } from 'react-toastify';
import { Users as UsersIcon, Shield, User, Search, X, Mail, Hash } from 'lucide-react';

const Users = () => {
  const [users, setUsers] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  useEffect(() => {
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
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
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
                className="card hover:border-primary/20 hover:-translate-y-0.5 transition-all duration-200 animate-fade-in"
                style={{ animationDelay: `${idx * 40}ms` }}
              >
                <div className="flex items-start gap-4">
                  {/* Avatar */}
                  <div className={`w-12 h-12 rounded-full flex items-center justify-center text-sm font-bold flex-shrink-0 ${
                    isAdmin
                      ? 'bg-gradient-to-br from-primary to-primaryHover text-dark'
                      : 'bg-gradient-to-br from-secondary to-secondaryHover text-dark'
                  }`}>
                    {initials}
                  </div>

                  {/* Info */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="font-semibold text-white text-sm truncate">{u.name}</h3>
                      <span className={`badge text-[10px] ${isAdmin ? 'role-admin' : 'role-customer'}`}>
                        {isAdmin ? <Shield size={9} /> : <User size={9} />}
                        {isAdmin ? 'Admin' : 'Customer'}
                      </span>
                    </div>
                    <div className="flex items-center gap-1.5 text-xs text-muted mb-1">
                      <Mail size={11} />
                      <span className="truncate">{u.email}</span>
                    </div>
                    <div className="flex items-center gap-1.5 text-xs text-muted">
                      <Hash size={11} />
                      <span>User #{u.id}</span>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Users;
