import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';

import Layout, { PrivateRoute } from './components/Layout';

import Auth          from './pages/Auth';
import Overview      from './pages/Overview';
import Products      from './pages/Products';
import Cart          from './pages/Cart';
import Orders        from './pages/Orders';
import Users         from './pages/Users';
import Notifications from './pages/Notifications';
import Profile       from './pages/Profile';

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <Routes>
            {/* Public routes */}
            <Route path="/login"    element={<Auth />} />
            <Route path="/register" element={<Auth />} />

            {/* Protected layout */}
            <Route path="/" element={<Layout />}>
              <Route index element={
                <PrivateRoute><Overview /></PrivateRoute>
              } />
              <Route path="products" element={
                <PrivateRoute><Products /></PrivateRoute>
              } />
              <Route path="cart" element={
                <PrivateRoute><Cart /></PrivateRoute>
              } />
              <Route path="orders" element={
                <PrivateRoute><Orders /></PrivateRoute>
              } />
              <Route path="notifications" element={
                <PrivateRoute><Notifications /></PrivateRoute>
              } />
              <Route path="users" element={
                <PrivateRoute adminOnly><Users /></PrivateRoute>
              } />
              <Route path="profile" element={
                <PrivateRoute><Profile /></PrivateRoute>
              } />
            </Route>

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>

          <ToastContainer
            position="bottom-right"
            theme="dark"
            autoClose={3500}
            hideProgressBar={false}
            newestOnTop
            closeOnClick
            draggable
            pauseOnHover
            toastStyle={{
              background: '#161B22',
              border: '1px solid #30363D',
              borderRadius: '12px',
              color: '#C9D1D9',
              fontSize: '14px',
            }}
          />
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
