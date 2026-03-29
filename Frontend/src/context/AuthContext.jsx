import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { jwtDecode } from 'jwt-decode';
import { useNavigate } from 'react-router-dom';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('name');
    setUser(null);
    navigate('/login');
  }, [navigate]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const decoded = jwtDecode(token);
        if (decoded.exp * 1000 < Date.now()) {
          logout();
        } else {
          // Prefer role from token claims, fallback to stored
          const role = decoded.role || decoded.authorities?.[0] || 'CUSTOMER';
          setUser({
            token,
            role: role.replace('ROLE_', ''),
            id: decoded.userId || decoded.sub,
            name: localStorage.getItem('name') || 'User',
            email: decoded.sub,
          });
        }
      } catch (err) {
        logout();
      }
    }
    setLoading(false);
  }, [logout]);

  /**
   * Called after successful login/register.
   * Stores data returned directly from the AuthResponse DTO:
   * { token, userId, name, email, role }
   */
  const login = (authData) => {
    const { token, name, role } = authData;
    localStorage.setItem('token', token);
    if (name) localStorage.setItem('name', name);
    try {
      const decoded = jwtDecode(token);
      const resolvedRole = (role || decoded.role || decoded.authorities?.[0] || 'CUSTOMER').replace('ROLE_', '');
      setUser({
        token,
        role: resolvedRole,
        id: authData.userId || decoded.userId || decoded.sub,
        name: name || 'User',
        email: authData.email || decoded.sub,
      });
      navigate('/');
    } catch (e) {
      console.error('Invalid token on login', e);
    }
  };

  const isAdmin = user?.role === 'ADMIN';

  return (
    <AuthContext.Provider value={{ user, login, logout, isAdmin, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
