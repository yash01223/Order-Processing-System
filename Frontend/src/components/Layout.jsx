import React, { useEffect, useState } from 'react';
import { NavLink, Outlet, Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import api from '../api/axios';
import {
  LayoutDashboard,
  Package,
  ShoppingCart,
  Receipt,
  Users,
  User,
  LogOut,
  Zap,
  Bell,
  ChevronRight,
  Menu,
  X,
} from 'lucide-react';

/* ── Private Route Guard ─────────────────────────────────────────────────── */
export const PrivateRoute = ({ children, adminOnly = false }) => {
  const { user, isAdmin } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (adminOnly && !isAdmin) return <Navigate to="/" replace />;
  return children;
};

/* ── Layout ──────────────────────────────────────────────────────────────── */
const Layout = () => {
  const { user, isAdmin, logout } = useAuth();
  const { cartCount } = useCart();
  const [unreadCount, setUnreadCount] = useState(0);
  const [sidebarOpen, setSidebarOpen] = useState(true);

  if (!user) return <Navigate to="/login" replace />;

  // Fetch notification unread count for customers
  useEffect(() => {
    if (!isAdmin) {
      const fetchUnread = async () => {
        try {
          const { data } = await api.get('/notifications?page=0&size=1');
          setUnreadCount(data.unreadCount || 0);
        } catch (_) {}
      };
      fetchUnread();
      const interval = setInterval(fetchUnread, 30000); // poll every 30s
      return () => clearInterval(interval);
    }
  }, [isAdmin]);

  const navItems = [
    {
      name: 'Overview',
      path: '/',
      icon: <LayoutDashboard size={18} />,
      exact: true,
    },
    {
      name: 'Products',
      path: '/products',
      icon: <Package size={18} />,
    },
    {
      name: 'Cart',
      path: '/cart',
      icon: <ShoppingCart size={18} />,
      customerOnly: true,
      badge: cartCount > 0 ? cartCount : null,
    },
    {
      name: 'Orders',
      path: '/orders',
      icon: <Receipt size={18} />,
    },
    {
      name: 'Notifications',
      path: '/notifications',
      icon: <Bell size={18} />,
      customerOnly: true,
      badge: unreadCount > 0 ? unreadCount : null,
    },
    {
      name: 'Users',
      path: '/users',
      icon: <Users size={18} />,
      adminOnly: true,
    },
    {
      name: 'Profile',
      path: '/profile',
      icon: <User size={18} />,
    },
  ];

  const initials = user.name ? user.name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase() : 'U';

  return (
    <div className="flex h-screen bg-dark overflow-hidden">

      {/* ── Sidebar ────────────────────────────────────────────── */}
      <aside className={`${sidebarOpen ? 'w-60' : 'w-16'} flex-shrink-0 bg-surface border-r border-surfaceBorder flex flex-col transition-all duration-300 z-20`}>

        {/* Logo */}
        <div className="h-14 flex items-center justify-between px-4 border-b border-surfaceBorder flex-shrink-0">
          {sidebarOpen ? (
            <div className="flex items-center gap-2.5 min-w-0">
              <div className="w-8 h-8 rounded-lg bg-primary/10 border border-primary/20 flex items-center justify-center flex-shrink-0">
                <Zap size={16} className="text-primary" />
              </div>
              <div className="min-w-0">
                <p className="text-sm font-bold text-white truncate">OPS Portal</p>
                <p className="text-[10px] text-muted truncate">Order Processing</p>
              </div>
            </div>
          ) : (
            <div className="w-8 h-8 rounded-lg bg-primary/10 border border-primary/20 flex items-center justify-center mx-auto">
              <Zap size={16} className="text-primary" />
            </div>
          )}

          <button
            onClick={() => setSidebarOpen(v => !v)}
            className="btn btn-ghost btn-icon p-1 flex-shrink-0 ml-1"
            title={sidebarOpen ? 'Collapse sidebar' : 'Expand sidebar'}
          >
            {sidebarOpen ? <X size={14} /> : <Menu size={14} />}
          </button>
        </div>

        {/* Nav */}
        <nav className="flex-1 py-3 overflow-y-auto">
          <ul className="space-y-0.5">
            {navItems.map((item) => {
              if (item.adminOnly && !isAdmin) return null;
              if (item.customerOnly && isAdmin) return null;

              return (
                <li key={item.path}>
                  <NavLink
                    to={item.path}
                    end={item.exact}
                    className={({ isActive }) =>
                      `nav-item ${isActive ? 'nav-item-active' : ''} ${!sidebarOpen ? 'justify-center px-0 mx-3' : ''}`
                    }
                    title={!sidebarOpen ? item.name : undefined}
                  >
                    <span className="flex-shrink-0 relative">
                      {item.icon}
                      {item.badge && !sidebarOpen && (
                        <span className="notif-dot">{item.badge > 9 ? '9+' : item.badge}</span>
                      )}
                    </span>
                    {sidebarOpen && (
                      <>
                        <span className="flex-1">{item.name}</span>
                        {item.badge && (
                          <span className="bg-primary/20 text-primary text-xs font-bold rounded-full px-2 py-0.5 min-w-[20px] text-center">
                            {item.badge > 99 ? '99+' : item.badge}
                          </span>
                        )}
                      </>
                    )}
                  </NavLink>
                </li>
              );
            })}
          </ul>
        </nav>

        {/* User footer */}
        <div className={`border-t border-surfaceBorder p-3 flex-shrink-0 ${!sidebarOpen ? 'flex flex-col items-center gap-2' : ''}`}>
          {sidebarOpen ? (
            <div className="flex items-center gap-3 mb-3">
              <div className="w-9 h-9 rounded-full bg-gradient-to-br from-primary to-primaryHover flex items-center justify-center text-dark text-xs font-bold flex-shrink-0">
                {initials}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-semibold text-white truncate">{user.name}</p>
                <p className="text-[11px] text-muted truncate">{isAdmin ? '🛡 Administrator' : '👤 Customer'}</p>
              </div>
            </div>
          ) : (
            <div
              className="w-9 h-9 rounded-full bg-gradient-to-br from-primary to-primaryHover flex items-center justify-center text-dark text-xs font-bold mb-2"
              title={user.name}
            >
              {initials}
            </div>
          )}
          <button
            onClick={logout}
            className={`btn btn-danger ${sidebarOpen ? 'w-full text-xs py-2' : 'btn-icon p-2'}`}
            title="Logout"
          >
            <LogOut size={14} />
            {sidebarOpen && 'Logout'}
          </button>
        </div>
      </aside>

      {/* ── Main Content ────────────────────────────────────────── */}
      <main className="flex-1 overflow-y-auto">
        {/* Top bar */}
        <div className="h-14 border-b border-surfaceBorder bg-surface/50 backdrop-blur-sm flex items-center justify-between px-6 sticky top-0 z-10">
          <div className="flex items-center gap-2 text-muted text-xs">
            {/* Breadcrumb-like hint */}
            <span className="text-primary font-medium">OPS</span>
            <ChevronRight size={12} />
            <span>Dashboard</span>
          </div>
          <div className="flex items-center gap-3">
            {!isAdmin && (
              <NavLink to="/notifications" className="relative btn btn-ghost btn-icon" title="Notifications">
                <Bell size={18} />
                {unreadCount > 0 && (
                  <span className="notif-dot">{unreadCount > 9 ? '9+' : unreadCount}</span>
                )}
              </NavLink>
            )}
            {!isAdmin && (
              <NavLink to="/cart" className="relative btn btn-ghost btn-icon" title="Cart">
                <ShoppingCart size={18} />
                {cartCount > 0 && (
                  <span className="notif-dot">{cartCount > 9 ? '9+' : cartCount}</span>
                )}
              </NavLink>
            )}
            <div className="w-8 h-8 rounded-full bg-gradient-to-br from-primary to-primaryHover flex items-center justify-center text-dark text-xs font-bold">
              {initials}
            </div>
          </div>
        </div>

        {/* Page content */}
        <div className="p-6 animate-fade-in">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default Layout;
