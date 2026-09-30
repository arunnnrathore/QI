import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import FriendsTab from '../components/FriendsTab';
import type { UserResponse } from '../types';

export default function Dashboard() {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [activeMainTab, setActiveMainTab] = useState<'CHATS' | 'FRIENDS' | 'FILES'>('CHATS');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await api.get('/auth/me');
        setUser(response.data as UserResponse);
      } catch (err) {
        console.error('Failed to fetch profile', err);
        localStorage.removeItem('token');
        navigate('/login');
      }
    };
    fetchProfile();
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  if (!user) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-950 text-slate-300" role="status">
        Loading your workspace...
      </div>
    );
  }

  const displayName = [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username;
  const initials = (user.firstName || user.username || 'Q').slice(0, 1).toUpperCase();

  return (
    <div className="flex min-h-screen bg-slate-950 text-slate-100">
      <aside className="flex w-20 shrink-0 flex-col border-r border-white/10 bg-slate-900 px-3 py-5 sm:w-64 sm:px-5">
        <div className="mb-10 flex items-center justify-center gap-3 sm:justify-start">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-cyan-400 font-black text-slate-950">Q</div>
          <div className="hidden sm:block">
            <p className="font-bold tracking-wide">QI</p>
            <p className="text-xs text-slate-400">Your workspace</p>
          </div>
        </div>
        <nav aria-label="Main navigation" className="flex flex-1 flex-col gap-2">
          {([
            ['CHATS', 'Chats', '◌'],
            ['FRIENDS', 'Friends', '◎'],
            ['FILES', 'Files', '▤'],
          ] as const).map(([tab, label, icon]) => (
            <button
              key={tab}
              type="button"
              onClick={() => setActiveMainTab(tab)}
              aria-current={activeMainTab === tab ? 'page' : undefined}
              className={`flex items-center justify-center gap-3 rounded-xl px-3 py-3 text-sm font-medium transition sm:justify-start ${activeMainTab === tab ? 'bg-cyan-400/15 text-cyan-300' : 'text-slate-400 hover:bg-white/5 hover:text-white'}`}
            >
              <span aria-hidden="true" className="text-lg">{icon}</span>
              <span className="hidden sm:inline">{label}</span>
            </button>
          ))}
        </nav>
        <div className="mt-6 border-t border-white/10 pt-5">
          <div className="flex items-center justify-center gap-3 sm:justify-start">
            <div className="h-10 w-10 shrink-0 overflow-hidden rounded-full bg-slate-700">
              {user.profilePicture ? (
                <img src={user.profilePicture} alt={`${displayName}'s profile`} className="h-full w-full object-cover" />
              ) : (
                <div className="flex h-full w-full items-center justify-center font-semibold text-cyan-200">{initials}</div>
              )}
            </div>
            <div className="hidden min-w-0 flex-1 sm:block">
              <p className="truncate text-sm font-medium">{displayName}</p>
              <p className="truncate text-xs text-slate-400">@{user.username}</p>
            </div>
          </div>
          <button type="button" onClick={handleLogout} className="mt-4 w-full rounded-lg px-3 py-2 text-left text-sm text-slate-400 transition hover:bg-white/5 hover:text-white">
            <span className="sm:hidden">↪</span><span className="hidden sm:inline">Sign out</span>
          </button>
        </div>
      </aside>

      <main className="min-w-0 flex-1 p-4 sm:p-8">
        <header className="mb-6 flex items-end justify-between">
          <div>
            <p className="text-sm font-medium text-cyan-300">QUICK INTELLIGENCE</p>
            <h1 className="mt-1 text-2xl font-semibold tracking-tight sm:text-3xl">{activeMainTab === 'FRIENDS' ? 'Your people' : activeMainTab === 'FILES' ? 'Shared files' : 'Messages'}</h1>
          </div>
          <div className="hidden text-right sm:block">
            <p className="text-sm text-slate-300">Welcome back, {user.firstName || user.username}</p>
            <p className="text-xs text-slate-500">Your conversations, connections, and files in one place.</p>
          </div>
        </header>
        {activeMainTab === 'FRIENDS' ? (
          <FriendsTab />
        ) : (
          <section className="flex min-h-[60vh] items-center justify-center rounded-2xl border border-white/10 bg-slate-900/70 p-8 text-center">
            <div>
              <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-white/5 text-2xl text-cyan-300">{activeMainTab === 'CHATS' ? '◌' : '▤'}</div>
              <h2 className="text-lg font-semibold">{activeMainTab === 'CHATS' ? 'Your conversations will appear here' : 'Your files will appear here'}</h2>
              <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-slate-400">{activeMainTab === 'CHATS' ? 'Messaging is the next integration step. Add friends to get your workspace ready.' : 'File sharing is not connected yet. This space is ready for the file manager.'}</p>
              {activeMainTab === 'CHATS' && <button type="button" onClick={() => setActiveMainTab('FRIENDS')} className="mt-5 rounded-lg bg-cyan-300 px-4 py-2 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200">Find people</button>}
            </div>
          </section>
        )}
      </main>
    </div>
  );
}
