import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [username, setUsername] = useState('');
  const [isRegistering, setIsRegistering] = useState(false);
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

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    try {
      const response = await api.post('/auth/register', { username, email, password });
      if (response.data === 'User registered successfully!') {
        setIsRegistering(false);
        setError('Account created. Sign in with your new account.');
      } else {
        setError(typeof response.data === 'string' ? response.data : 'Account creation failed. Please try again.');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Account creation failed. Please try again.');
    }
  };

  return (
    <main className="qi-auth-backdrop flex min-h-screen items-center justify-center px-5 py-10 text-qi-primary">
      <div className="w-full max-w-md">
        <div className="mb-8 text-center">
          <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl border border-qi-line bg-qi-raised text-xl font-extrabold tracking-tighter text-qi-accent">QI</div>
          <p className="flex items-center justify-center gap-2 text-xs font-semibold tracking-[0.2em] text-qi-secondary"><span className="h-1.5 w-1.5 rounded-full bg-qi-accent" aria-hidden="true" />QUICK INTELLIGENCE</p>
          <h1 className="mt-3 text-3xl font-bold tracking-tight">A clearer way to connect.</h1>
          <p className="mt-2 text-sm text-qi-secondary">{isRegistering ? 'Create your QI account.' : 'Sign in to your QI workspace.'}</p>
        </div>
        <div className="qi-panel qi-auth-card p-6 sm:p-8">
          <form onSubmit={isRegistering ? handleRegister : handleLogin} className="space-y-5">
          {isRegistering && <div>
            <label htmlFor="username" className="mb-2 block text-sm font-medium text-qi-secondary">Username</label>
            <input
              id="username"
              type="text"
              required
              autoComplete="username"
              className="qi-input w-full px-4 py-3 text-sm"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </div>}
          <div>
            <label htmlFor="email" className="mb-2 block text-sm font-medium text-qi-secondary">Email address</label>
            <input 
              id="email"
              type="email" 
              required
              autoComplete="email"
              className="qi-input w-full px-4 py-3 text-sm"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <div>
            <label htmlFor="password" className="mb-2 block text-sm font-medium text-qi-secondary">Password</label>
            <input 
              id="password"
              type="password" 
              required
              minLength={isRegistering ? 6 : undefined}
              autoComplete={isRegistering ? 'new-password' : 'current-password'}
              className="qi-input w-full px-4 py-3 text-sm"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          {error && <p className={`rounded-lg border px-3 py-2 text-sm ${error.startsWith('Account created.') ? 'border-qi-accent/20 bg-qi-accent/10 text-qi-accent' : 'border-rose-400/20 bg-rose-400/10 text-rose-200'}`} role="status">{error}</p>}
          <button 
            type="submit"
            className="qi-button-primary qi-auth-button w-full py-3 text-sm"
          >
            {isRegistering ? 'Create account' : 'Sign in'}
          </button>
          </form>
        </div>
        <p className="mt-5 text-center text-sm text-qi-secondary">
          {isRegistering ? 'Already have an account?' : 'New to QI?'}{' '}
          <button
            type="button"
            className="font-semibold text-qi-accent transition-colors hover:text-qi-primary focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-qi-accent"
            onClick={() => { setIsRegistering(!isRegistering); setError(''); }}
          >
            {isRegistering ? 'Sign in' : 'Create account'}
          </button>
        </p>
        <p className="mt-6 text-center text-xs text-qi-subtle">Private conversations. Thoughtful connections. Your QI.</p>
      </div>
    </main>
  );
}
