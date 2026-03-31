import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axios';
import { Zap, Mail, Lock, User, ChevronRight, Eye, EyeOff, Shield, RefreshCw } from 'lucide-react';

const Auth = () => {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({ name: '', email: '', password: '', role: 'CUSTOMER' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);
  const [showOtp, setShowOtp] = useState(false);
  const [otpArray, setOtpArray] = useState(['', '', '', '', '', '']); // 6-digit OTP
  const [timeLeft, setTimeLeft] = useState(300); // 5 minutes in seconds
  const [success, setSuccess] = useState('');
  const otpFieldRefs = useRef([]);
  const { login } = useAuth();

  // Timer Logic
  useEffect(() => {
    let timer;
    if (showOtp && timeLeft > 0) {
      timer = setInterval(() => {
        setTimeLeft(prev => prev - 1);
      }, 1000);
    } else if (timeLeft === 0) {
      clearInterval(timer);
    }
    return () => clearInterval(timer);
  }, [showOtp, timeLeft]);

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  const handleChange = (e) => setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const handleToggle = () => {
    setIsLogin(!isLogin);
    setError('');
    setSuccess('');
    setShowOtp(false);
    setOtpArray(['', '', '', '', '', '']);
    setTimeLeft(300);
    setFormData({ name: '', email: '', password: '', role: 'CUSTOMER' });
  };

  const handleResendOtp = async () => {
    if (timeLeft > 0) return; // Prevent resend until timer hits 0
    setError('');
    setSuccess('');
    try {
      await api.post('/auth/resend-otp', { email: formData.email });
      setSuccess('A new OTP has been sent to your email.');
      setOtpArray(['', '', '', '', '', '']);
      setTimeLeft(300); // Reset timer
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to resend OTP.');
    }
  };

  const handleOtpChange = (index, value) => {
    if (!/^\d*$/.test(value)) return; // Only digits

    const newOtpArray = [...otpArray];
    newOtpArray[index] = value.slice(-1); // Take only last digit
    setOtpArray(newOtpArray);

    // Auto-focus next box
    if (value && index < 5) {
      otpFieldRefs.current[index + 1].focus();
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otpArray[index] && index > 0) {
      // Auto-focus previous box on backspace if current is empty
      otpFieldRefs.current[index - 1].focus();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pasteData = e.clipboardData.getData('text').slice(0, 6);
    if (!/^\d+$/.test(pasteData)) return;

    const newOtpArray = [...otpArray];
    pasteData.split('').forEach((char, i) => {
      if (i < 6) newOtpArray[i] = char;
    });
    setOtpArray(newOtpArray);
    
    // Focus last filled box or next empty
    const nextIndex = Math.min(pasteData.length, 5);
    otpFieldRefs.current[nextIndex].focus();
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
      } else if (!showOtp) {
        // Step 1: Register (Trigger OTP)
        await api.post('/auth/register', {
          name: formData.name,
          email: formData.email,
          password: formData.password,
          role: formData.role,
        });
        setShowOtp(true);
        setSuccess(' Please check your email for the OTP.');
      } else {
        // Step 2: Verify OTP
        const fullOtp = otpArray.join('');
        if (fullOtp.length < 6) {
          setError('Please enter the full 6-digit code.');
          setLoading(false);
          return;
        }

        const { data } = await api.post('/auth/verify-otp', {
          email: formData.email,
          otp: fullOtp,
        });
        login(data); // Final login after verification
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

          {/* Messages */}
          {error && (
            <div className="mb-5 p-3.5 bg-error/10 border border-error/25 text-error text-sm rounded-lg flex items-start gap-2 animate-fade-in">
              <span className="mt-0.5 flex-shrink-0">⚠</span>
              <span>{error}</span>
            </div>
          )}

          {success && (
            <div className="mb-5 p-3.5 bg-success/10 border border-success/25 text-success text-sm rounded-lg flex items-start gap-2 animate-fade-in">
              <span className="mt-0.5 flex-shrink-0">✓</span>
              <span>{success}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {!isLogin && !showOtp && (
              <>
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
              </>
            )}

            {!isLogin && showOtp && (
              <div className="form-group animate-fade-in">
                <div className="flex justify-between items-end mb-2">
                  <label className="label mb-0">Verification OTP</label>
                  <span className={`text-xs font-mono ${timeLeft < 30 ? 'text-error animate-pulse' : 'text-muted'}`}>
                    Expires in {formatTime(timeLeft)}
                  </span>
                </div>
                
                <div className="flex justify-between gap-2 mb-4" onPaste={handlePaste}>
                  {otpArray.map((digit, i) => (
                    <input
                      key={i}
                      ref={el => otpFieldRefs.current[i] = el}
                      type="text"
                      inputMode="numeric"
                      maxLength={1}
                      className="w-12 h-14 bg-dark/60 border border-surfaceBorder rounded-xl text-center text-xl font-bold text-white focus:border-primary focus:ring-1 focus:ring-primary/20 transition-all outline-none"
                      value={digit}
                      onChange={(e) => handleOtpChange(i, e.target.value)}
                      onKeyDown={(e) => handleKeyDown(i, e)}
                    />
                  ))}
                </div>

                <div className="flex justify-center">
                  <button 
                    type="button"
                    onClick={handleResendOtp}
                    disabled={timeLeft > 0}
                    className={`flex items-center gap-1.5 text-sm transition-all duration-300 ${
                      timeLeft > 0 
                        ? 'text-white/10 cursor-not-allowed font-medium' 
                        : 'text-primary hover:text-primaryLight hover:underline font-bold animate-fade-in'
                    }`}
                  >
                    <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
                    Resend New OTP
                  </button>
                </div>
              </div>
            )}

            {(!showOtp || isLogin) && (
              <>
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
              </>
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
                  {isLogin ? 'Sign In' : (showOtp ? 'Verify OTP' : (timeLeft === 0 ? 'Code Expired' : 'Create Account'))}
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
