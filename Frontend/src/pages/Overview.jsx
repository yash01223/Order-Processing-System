import { useAuth } from '../context/AuthContext';
import AdminOverview from '../components/AdminOverview';
import CustomerOverview from '../components/CustomerOverview';
import { Shield, User } from 'lucide-react';

const Overview = () => {
  const { user, isAdmin } = useAuth();

  const getGreeting = () => {
    const hour = new Date().getHours();
    if (hour < 12) return 'Good morning';
    if (hour < 18) return 'Good afternoon';
    return 'Good evening';
  };

  return (
    <div className="space-y-5">
      {/* Page Header */}
      <header className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">
            {getGreeting()}, <span className="text-gradient-primary">{user.name}</span> 👋
          </h1>
          <p className="text-sm text-muted mt-0.5">
            {isAdmin
              ? 'Here is a live overview of your order processing platform.'
              : 'Here is a summary of your recent activity.'}
          </p>
        </div>
        <div className={`badge ${isAdmin ? 'role-admin' : 'role-customer'} gap-1.5`}>
          {isAdmin ? <Shield size={12} /> : <User size={12} />}
          {isAdmin ? 'Admin' : 'Customer'}
        </div>
      </header>

      {/* Role-based dashboard */}
      {isAdmin ? <AdminOverview /> : <CustomerOverview />}
    </div>
  );
};

export default Overview;
