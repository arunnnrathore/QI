import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import FriendsTab from '../components/FriendsTab';
import ChatPanel from '../components/ChatPanel';
import { Settings2 } from 'lucide-react';
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
      <div className="flex min-h-screen items-center justify-center bg-qi-background text-qi-secondary" role="status">
        Loading your workspace...
      </div>
    );
  }

  const displayName = [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username;
  const initials = (user.firstName || user.username || 'Q').slice(0, 1).toUpperCase();

  return (
    <div className="flex min-h-screen bg-qi-background text-qi-primary">
      <aside className="flex w-20 shrink-0 flex-col border-r border-qi-line bg-qi-surface px-3 py-5 sm:w-64 sm:px-5">
        <div className="mb-10 flex items-center justify-center gap-3 sm:justify-start">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-qi-accent font-extrabold tracking-tighter text-qi-background">QI</div>
          <div className="hidden sm:block">
            <p className="font-bold tracking-tight">Quick Intelligence</p>
            <p className="text-xs text-qi-secondary">A clearer way to connect</p>
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
              className={`qi-nav-item flex items-center justify-center gap-3 rounded-xl px-3 py-3 text-sm font-medium sm:justify-start ${activeMainTab === tab ? 'qi-nav-item-active' : ''}`}
            >
              <span aria-hidden="true" className="text-lg">{icon}</span>
              <span className="hidden sm:inline">{label}</span>
            </button>
          ))}
        </nav>
        <div className="mt-6 border-t border-qi-line pt-5">
          <div className="flex items-center justify-center gap-3 sm:justify-start">
            <div className="h-10 w-10 shrink-0 overflow-hidden rounded-full bg-qi-raised">
              {user.profilePicture ? (
                <img src={user.profilePicture} alt={`${displayName}'s profile`} className="h-full w-full object-cover" />
              ) : (
                <div className="flex h-full w-full items-center justify-center font-semibold text-qi-accent">{initials}</div>
              )}
            </div>
            <div className="hidden min-w-0 flex-1 sm:block">
              <p className="truncate text-sm font-medium">{displayName}</p>
              <p className="truncate text-xs text-qi-secondary">@{user.username}</p>
            </div>
          </div>
          <details className="group relative mt-4">
            <summary className="qi-nav-item flex cursor-pointer list-none items-center justify-center gap-3 rounded-lg px-3 py-2 text-sm sm:justify-start">
              <Settings2 size={17} aria-hidden="true" />
              <span className="hidden sm:inline">Account & settings</span>
            </summary>
            <div className="absolute bottom-full left-0 z-20 mb-2 w-64 rounded-xl border border-qi-line bg-qi-raised p-4 shadow-2xl">
              <p className="text-xs font-semibold uppercase tracking-wider text-qi-subtle">Signed in as</p>
              <p className="mt-2 truncate text-sm font-semibold">{displayName}</p>
              <p className="mt-1 truncate text-xs text-qi-secondary">{user.email}</p>
              <div className="mt-4 border-t border-qi-line pt-3 text-xs text-qi-subtle">Profile settings</div>
            </div>
          </details>
          <button type="button" onClick={handleLogout} className="qi-nav-item mt-4 w-full rounded-lg px-3 py-2 text-left text-sm">
            <span className="sm:hidden">↪</span><span className="hidden sm:inline">Sign out</span>
          </button>
        </div>
      </aside>

      <main className="min-w-0 flex-1 bg-qi-background p-4 sm:p-8">
        <header className="mb-6 flex items-end justify-between">
          <div>
            <p className="text-xs font-bold tracking-[0.2em] text-qi-accent">QI · QUICK INTELLIGENCE</p>
            <h1 className="mt-1 text-2xl font-semibold tracking-tight sm:text-3xl">{activeMainTab === 'FRIENDS' ? 'Your people' : activeMainTab === 'FILES' ? 'Shared files' : 'Messages'}</h1>
          </div>
          <div className="hidden text-right sm:block">
            <p className="text-sm text-qi-secondary">Welcome back, {user.firstName || user.username}</p>
            <p className="text-xs text-qi-subtle">Your conversations, connections, and files in one place.</p>
          </div>
        </header>
        {activeMainTab === 'CHATS' ? (
          <ChatPanel currentUserId={user.id} />
        ) : activeMainTab === 'FRIENDS' ? (
          <FriendsTab />
        ) : (
          <section className="qi-panel flex min-h-[60vh] items-center justify-center p-8 text-center">
            <div>
              <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-qi-raised text-2xl text-qi-accent">▤</div>
              <h2 className="text-lg font-semibold">Your files will appear here</h2>
              <p className="mx-auto mt-2 max-w-md text-sm leading-6 text-qi-secondary">File sharing is not connected yet. This space is ready for the file manager.</p>
            </div>
          </section>
        )}
      </main>
    </div>
  );
}
