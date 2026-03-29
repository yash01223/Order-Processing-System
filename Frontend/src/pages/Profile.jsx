import React from 'react';
import { useAuth } from '../context/AuthContext';
import { User, Mail, Shield, ShieldCheck, Calendar, Key } from 'lucide-react';

const Profile = () => {
  const { user, isAdmin } = useAuth();

  if (!user) return null;

  const infoItems = [
    { label: 'Full Name', value: user.name, icon: <User size={18} className="text-primary" /> },
    { label: 'Email Address', value: user.email, icon: <Mail size={18} className="text-secondary" /> },
    { label: 'Account Role', value: user.role, icon: isAdmin ? <ShieldCheck size={18} className="text-accent" /> : <Shield size={18} className="text-muted" /> },
    { label: 'User ID', value: `#${user.id}`, icon: <Calendar size={18} className="text-muted" /> },
  ];

  return (
    <div className="space-y-6 max-w-2xl mx-auto">
      <header>
        <h1 className="page-title">Your Profile</h1>
        <p className="page-subtitle">Manage your account information and security settings.</p>
      </header>

      <div className="card p-0 overflow-hidden">
        <div className="h-32 bg-gradient-to-r from-primary/20 to-secondary/20 relative">
          <div className="absolute -bottom-10 left-8">
            <div className="w-24 h-24 rounded-2xl bg-surface border-4 border-dark flex items-center justify-center text-3xl font-bold text-primary shadow-xl">
              {user.name.charAt(0).toUpperCase()}
            </div>
          </div>
        </div>
        
        <div className="pt-14 pb-8 px-8">
          <div className="flex justify-between items-start mb-8">
            <div>
              <h2 className="text-2xl font-bold text-white">{user.name}</h2>
              <p className="text-muted">{isAdmin ? 'System Administrator' : 'Valued Customer'}</p>
            </div>
            <button className="btn btn-outline btn-sm gap-2">
              Edit Profile
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {infoItems.map((item, idx) => (
              <div key={idx} className="space-y-1">
                <p className="text-xs font-semibold text-muted uppercase tracking-wider flex items-center gap-2">
                  {item.icon}
                  {item.label}
                </p>
                <div className="p-3 bg-dark rounded-lg border border-surfaceBorder text-white font-medium">
                  {item.value}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="card space-y-4">
        <h3 className="font-bold text-white flex items-center gap-2">
          <Key size={18} className="text-primary" />
          Security Settings
        </h3>
        <p className="text-sm text-muted">Update your password or manage two-factor authentication.</p>
        <div className="flex gap-3">
          <button className="btn btn-primary btn-sm">Change Password</button>
          <button className="btn btn-outline btn-sm">Session History</button>
        </div>
      </div>
    </div>
  );
};

export default Profile;
