import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    try {
      const response = await api.post('/auth/login', { email, password });
      const token = response.data;
      if (token && typeof token === 'string' && !token.includes('Invalid')) {
        localStorage.setItem('token', token);
        navigate('/dashboard');
      } else {
        setError('Invalid credentials');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed. Please try again.');
    }
  };

  return (
    <main className="qi-auth-backdrop flex min-h-screen items-center justify-center px-5 py-10 text-qi-primary">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-qi-accent text-xl font-extrabold tracking-tighter text-qi-background shadow-lg shadow-qi-accent/15">QI</div>
          <p className="text-xs font-bold tracking-[0.24em] text-qi-accent">QUICK INTELLIGENCE</p>
          <h1 className="mt-3 text-3xl font-bold tracking-tight">A clearer way to connect.</h1>
          <p className="mt-2 text-sm text-qi-secondary">Sign in to your QI workspace.</p>
        </div>
        <div className="qi-panel p-6 sm:p-8">
          <form onSubmit={handleLogin} className="space-y-5">
          <div>
            <label className="mb-2 block text-sm font-medium text-qi-secondary">Email address</label>
            <input 
              type="email" 
              required
              autoComplete="email"
              className="qi-input w-full px-4 py-3 text-sm"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <div>
            <label className="mb-2 block text-sm font-medium text-qi-secondary">Password</label>
            <input 
              type="password" 
              required
              autoComplete="current-password"
              className="qi-input w-full px-4 py-3 text-sm"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          {error && <p className="rounded-lg border border-rose-400/20 bg-rose-400/10 px-3 py-2 text-sm text-rose-200" role="alert">{error}</p>}
          <button 
            type="submit"
            className="qi-button-primary w-full py-3 text-sm"
          >
            Sign in
          </button>
          </form>
        </div>
        <p className="mt-6 text-center text-xs text-qi-subtle">Private conversations. Thoughtful connections. Your QI.</p>
      </div>
    </main>
  );
}
