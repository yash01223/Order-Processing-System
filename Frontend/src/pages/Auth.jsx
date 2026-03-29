import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';
import { Zap, Mail, Lock, User, ChevronRight, Eye, EyeOff, Shield } from 'lucide-react';

const Auth = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({ name: '', email: '', password: '', role: 'CUSTOMER' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);
  const { login } = useAuth();

  const handleChange = (e) => setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const handleToggle = () => {
    setIsLogin(!isLogin);
    setError('');
    setFormData({ name: '', email: '', password: '', role: 'CUSTOMER' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isLogin) {
        // POST /api/auth/login → { token, userId, name, email, role }
        const { data } = await api.post('/auth/login', {
          email: formData.email,
          password: formData.password,
        });
        login(data); // pass full AuthResponse
      } else {
        // POST /api/auth/register (201 Created, returns AuthResponse)
        const { data } = await api.post('/auth/register', {
          name: formData.name,
          email: formData.email,
          password: formData.password,
          role: formData.role,
        });
        login(data); // auto-login after register
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Authentication failed. Please check your credentials.';
      setError(typeof msg === 'string' ? msg : 'An error occurred. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-dark flex items-center justify-center p-4 relative overflow-hidden">
      {/* Background glow effects */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[600px] h-[600px] bg-primary/5 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-0 right-1/4 w-[400px] h-[400px] bg-secondary/5 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md relative z-10 animate-slide-up">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-primary/10 border border-primary/20 mb-4">
            <Zap size={28} className="text-primary" />
          </div>
          <h1 className="text-3xl font-bold text-gradient-primary tracking-tight">OPS Portal</h1>
          <p className="text-muted text-sm mt-1">Order Processing System</p>
        </div>

        {/* Card */}
        <div className="card-glass p-8">
          {/* Tabs */}
          <div className="flex bg-dark/60 rounded-xl p-1 mb-7 border border-surfaceBorder">
            <button
              onClick={() => isLogin || handleToggle()}
              className={`flex-1 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200 ${
                isLogin ? 'bg-primary text-dark shadow-md' : 'text-muted hover:text-white'
              }`}
            >
              Sign In
            </button>
            <button
              onClick={() => !isLogin || handleToggle()}
              className={`flex-1 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200 ${
                !isLogin ? 'bg-primary text-dark shadow-md' : 'text-muted hover:text-white'
              }`}
            >
              Register
            </button>
          </div>

          {/* Error */}
          {error && (
            <div className="mb-5 p-3.5 bg-error/10 border border-error/25 text-error text-sm rounded-lg flex items-start gap-2 animate-fade-in">
              <span className="mt-0.5 flex-shrink-0">⚠</span>
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {!isLogin && (
              <div className="form-group animate-fade-in">
                <label className="label">Full Name</label>
                <div className="relative">
                  <User size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted pointer-events-none" />
                  <input
                    name="name"
                    type="text"
                    required
                    autoComplete="name"
                    placeholder="Your full name"
                    className="input-field pl-10"
                    value={formData.name}
                    onChange={handleChange}
                  />
                </div>
              </div>
            )}

            <div className="form-group">
              <label className="label">Email Address</label>
              <div className="relative">
                <Mail size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted pointer-events-none" />
                <input
                  name="email"
                  type="email"
                  required
                  autoComplete="email"
                  placeholder="you@example.com"
                  className="input-field pl-10"
                  value={formData.email}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="label">Password</label>
              <div className="relative">
                <Lock size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-muted pointer-events-none" />
                <input
                  name="password"
                  type={showPass ? 'text' : 'password'}
                  required
                  minLength={6}
                  autoComplete={isLogin ? 'current-password' : 'new-password'}
                  placeholder="Min. 6 characters"
                  className="input-field pl-10 pr-10"
                  value={formData.password}
                  onChange={handleChange}
                />
                <button
                  type="button"
                  onClick={() => setShowPass(v => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted hover:text-white transition-colors"
                >
                  {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>

            {!isLogin && (
              <div className="form-group animate-fade-in">
                <label className="label">Account Type</label>
                <div className="grid grid-cols-2 gap-3">
                  {['CUSTOMER', 'ADMIN'].map((r) => (
                    <button
                      key={r}
                      type="button"
                      onClick={() => setFormData(prev => ({ ...prev, role: r }))}
                      className={`flex items-center gap-2 px-4 py-2.5 rounded-lg border text-sm font-medium transition-all duration-200 ${
                        formData.role === r
                          ? 'border-primary bg-primary/10 text-primary'
                          : 'border-surfaceBorder text-muted hover:border-muted hover:text-white'
                      }`}
                    >
                      {r === 'ADMIN' ? <Shield size={16} /> : <User size={16} />}
                      {r === 'CUSTOMER' ? 'Customer' : 'Admin'}
                    </button>
                  ))}
                </div>
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="btn btn-primary w-full py-3 mt-2 text-sm font-semibold"
            >
              {loading ? (
                <span className="spinner spinner-sm" />
              ) : (
                <>
                  {isLogin ? 'Sign In' : 'Create Account'}
                  <ChevronRight size={16} />
                </>
              )}
            </button>
          </form>
        </div>

        <p className="text-center text-sm text-muted mt-5">
          {isLogin ? "Don't have an account? " : "Already have an account? "}
          <button onClick={handleToggle} className="text-primary hover:underline font-medium">
            {isLogin ? 'Register here' : 'Sign in here'}
          </button>
        </p>
      </div>
    </div>
  );
};

export default Auth;
