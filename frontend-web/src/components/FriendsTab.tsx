import { useCallback, useEffect, useState } from 'react';
import axios from 'axios';
import api from '../services/api';
import type { FriendRequestResponse, FriendResponse, UserSearchResponse } from '../types';

type FriendSection = 'LIST' | 'PENDING' | 'SEARCH';

function getErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const data: unknown = error.response?.data;
    if (typeof data === 'string' && data.trim()) return data;
    if (data && typeof data === 'object' && 'message' in data && typeof data.message === 'string') {
      return data.message;
    }
  }
  return fallback;
}

function displayName(firstName?: string, lastName?: string, username?: string): string {
  return [firstName, lastName].filter(Boolean).join(' ') || username || 'QI user';
}

function Avatar({ name, picture }: { name: string; picture?: string | null }) {
  return (
    <div className="flex h-11 w-11 shrink-0 items-center justify-center overflow-hidden rounded-full bg-qi-accent/15 font-semibold text-qi-accent">
      {picture ? <img src={picture} alt="" className="h-full w-full object-cover" /> : name.slice(0, 1).toUpperCase()}
    </div>
  );
}

export default function FriendsTab() {
  const [activeSection, setActiveSection] = useState<FriendSection>('LIST');
  const [friends, setFriends] = useState<FriendResponse[]>([]);
  const [pendingRequests, setPendingRequests] = useState<FriendRequestResponse[]>([]);
  const [searchResults, setSearchResults] = useState<UserSearchResponse[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [workingId, setWorkingId] = useState<number | null>(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  const fetchFriends = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = await api.get<FriendResponse[]>('/friends/list');
      setFriends(response.data);
    } catch (requestError) {
      setError(getErrorMessage(requestError, 'Could not load your friends.'));
    } finally {
      setLoading(false);
    }
  }, []);

  const fetchPendingRequests = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = await api.get<FriendRequestResponse[]>('/friends/pending');
      setPendingRequests(response.data);
    } catch (requestError) {
      setError(getErrorMessage(requestError, 'Could not load pending requests.'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (activeSection === 'LIST') void fetchFriends();
    if (activeSection === 'PENDING') void fetchPendingRequests();
  }, [activeSection, fetchFriends, fetchPendingRequests]);

  const handleSearch = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const query = searchQuery.trim();
    setNotice('');
    setError('');
    if (!query) {
      setSearchResults([]);
      return;
    }

    setLoading(true);
    try {
      const response = await api.get<UserSearchResponse[]>('/users/search', { params: { query } });
      setSearchResults(response.data);
    } catch (requestError) {
      setSearchResults([]);
      setError(getErrorMessage(requestError, 'Could not search users.'));
    } finally {
      setLoading(false);
    }
  };

  const sendFriendRequest = async (user: UserSearchResponse) => {
    setWorkingId(user.id);
    setNotice('');
    setError('');
    try {
      const response = await api.post<string>('/friends/request', null, { params: { receiverId: user.id } });
      const message = response.data;
      setNotice(message);
      if (message?.startsWith('Friend request sent successfully')) {
        setSearchResults((current) => current.filter((result) => result.id !== user.id));
      }
    } catch (requestError) {
      setError(getErrorMessage(requestError, 'Could not send the friend request.'));
    } finally {
      setWorkingId(null);
    }
  };

  const processRequest = async (requestId: number, action: 'accept' | 'reject') => {
    setWorkingId(requestId);
    setNotice('');
    setError('');
    try {
      const response = await api.post<string>(`/friends/${action}`, null, { params: { requestId } });
      setNotice(response.data);
      if (response.data?.includes('successfully')) await fetchPendingRequests();
    } catch (requestError) {
      setError(getErrorMessage(requestError, `Could not ${action} this request.`));
    } finally {
      setWorkingId(null);
    }
  };

  const unfriend = async (friend: FriendResponse) => {
    const name = displayName(friend.firstName, friend.lastName, friend.username);
    if (!window.confirm(`Remove ${name} from your friends?`)) return;
    setWorkingId(friend.id);
    setNotice('');
    setError('');
    try {
      const response = await api.delete<string>('/friends/unfriend', { params: { friendId: friend.id } });
      setNotice(response.data);
      setFriends((current) => current.filter((item) => item.id !== friend.id));
    } catch (requestError) {
      setError(getErrorMessage(requestError, 'Could not remove this friend.'));
    } finally {
      setWorkingId(null);
    }
  };

  const sections: { id: FriendSection; label: string }[] = [
    { id: 'LIST', label: 'Friends' },
    { id: 'PENDING', label: `Requests${pendingRequests.length ? ` · ${pendingRequests.length}` : ''}` },
    { id: 'SEARCH', label: 'Find people' },
  ];

  return (
    <section className="qi-panel overflow-hidden">
      <div className="border-b border-qi-line px-5 py-5 sm:px-7">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h2 className="text-lg font-semibold">Connections</h2>
            <p className="mt-1 text-sm text-qi-secondary">Find people and manage your friend requests.</p>
          </div>
          <span className="rounded-full border border-qi-line px-3 py-1 text-xs text-qi-secondary">{friends.length} friends</span>
        </div>
        <div className="mt-5 flex gap-1 overflow-x-auto" role="tablist" aria-label="Friend sections">
          {sections.map((section) => (
            <button
              key={section.id}
              type="button"
              role="tab"
              aria-selected={activeSection === section.id}
              onClick={() => { setActiveSection(section.id); setError(''); setNotice(''); }}
              className={`whitespace-nowrap rounded-lg px-4 py-2 text-sm font-medium transition ${activeSection === section.id ? 'bg-qi-accent text-qi-background' : 'qi-nav-item'}`}
            >
              {section.label}
            </button>
          ))}
        </div>
      </div>

      <div className="min-h-[360px] p-5 sm:p-7">
        {error && <div className="mb-4 rounded-lg border border-rose-400/20 bg-rose-400/10 px-4 py-3 text-sm text-rose-200" role="alert">{error}</div>}
        {notice && <div className="mb-4 rounded-lg border border-qi-accent/20 bg-qi-accent/10 px-4 py-3 text-sm text-qi-accent" role="status">{notice}</div>}
        {loading && <p className="py-10 text-center text-sm text-qi-secondary" role="status">Loading connections...</p>}

        {!loading && activeSection === 'LIST' && (
          friends.length ? (
            <div className="divide-y divide-qi-line">
              {friends.map((friend) => {
                const name = displayName(friend.firstName, friend.lastName, friend.username);
                return (
                  <div key={friend.id} className="flex items-center gap-3 py-4 first:pt-0 last:pb-0 sm:gap-4">
                    <Avatar name={name} picture={friend.profilePicture} />
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-medium">{name}</p>
                      <p className="truncate text-sm text-qi-secondary">@{friend.username}</p>
                    </div>
                    <button type="button" disabled={workingId === friend.id} onClick={() => void unfriend(friend)} className="qi-button-secondary px-3 py-2 text-sm text-qi-secondary hover:border-rose-300/30 hover:text-rose-200 disabled:opacity-50">
                      {workingId === friend.id ? 'Working…' : 'Remove'}
                    </button>
                  </div>
                );
              })}
            </div>
          ) : <EmptyState title="No friends yet" description="Search for someone you know and send a friend request." action="Find people" onAction={() => setActiveSection('SEARCH')} />
        )}

        {!loading && activeSection === 'PENDING' && (
          pendingRequests.length ? (
            <div className="divide-y divide-qi-line">
              {pendingRequests.map((request) => (
                <div key={request.requestId} className="flex flex-wrap items-center gap-3 py-4 first:pt-0 last:pb-0 sm:gap-4">
                  <Avatar name={request.senderName} />
                  <div className="min-w-0 flex-1">
                    <p className="truncate font-medium">{request.senderName}</p>
                    <p className="truncate text-sm text-qi-secondary">@{request.senderUsername}</p>
                    <p className="mt-1 text-xs text-qi-subtle">Sent {new Date(request.createdAt).toLocaleDateString()}</p>
                  </div>
                  <div className="flex w-full gap-2 sm:w-auto">
                    <button type="button" disabled={workingId === request.requestId} onClick={() => void processRequest(request.requestId, 'accept')} className="qi-button-primary flex-1 px-3 py-2 text-sm disabled:opacity-50 sm:flex-none">Accept</button>
                    <button type="button" disabled={workingId === request.requestId} onClick={() => void processRequest(request.requestId, 'reject')} className="qi-button-secondary flex-1 px-3 py-2 text-sm disabled:opacity-50 sm:flex-none">Decline</button>
                  </div>
                </div>
              ))}
            </div>
          ) : <EmptyState title="You're all caught up" description="New friend requests will show up here." />
        )}

        {activeSection === 'SEARCH' && (
          <div>
            <form onSubmit={(event) => void handleSearch(event)} className="mb-6 flex flex-col gap-2 sm:flex-row">
              <label className="sr-only" htmlFor="friend-search">Search people</label>
              <input id="friend-search" type="search" placeholder="Search by name, username, or email" value={searchQuery} onChange={(event) => setSearchQuery(event.target.value)} className="qi-input min-w-0 flex-1 px-4 py-3 text-sm" />
              <button type="submit" disabled={loading || !searchQuery.trim()} className="qi-button-primary px-5 py-3 text-sm disabled:cursor-not-allowed disabled:opacity-50">Search</button>
            </form>
            {!searchResults.length && searchQuery.trim() && !loading && !error && <p className="py-8 text-center text-sm text-qi-secondary">No people found for “{searchQuery.trim()}”.</p>}
            <div className="divide-y divide-qi-line">
              {searchResults.map((person) => {
                const name = displayName(person.firstName, person.lastName, person.username);
                return (
                  <div key={person.id} className="flex items-center gap-3 py-4 first:pt-0 last:pb-0 sm:gap-4">
                    <Avatar name={name} picture={person.profilePicture} />
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-medium">{name}</p>
                      <p className="truncate text-sm text-qi-secondary">@{person.username}</p>
                      {person.bio && <p className="mt-1 line-clamp-2 text-xs text-qi-subtle">{person.bio}</p>}
                    </div>
                    <button type="button" disabled={workingId === person.id} onClick={() => void sendFriendRequest(person)} className="qi-button-secondary shrink-0 border-qi-accent/30 px-3 py-2 text-sm font-medium text-qi-accent hover:bg-qi-accent/10 disabled:opacity-50">
                      {workingId === person.id ? 'Sending…' : 'Add friend'}
                    </button>
                  </div>
                );
              })}
            </div>
          </div>
        )}
      </div>
    </section>
  );
}

function EmptyState({ title, description, action, onAction }: { title: string; description: string; action?: string; onAction?: () => void }) {
  return (
    <div className="flex min-h-64 flex-col items-center justify-center text-center">
      <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-2xl bg-qi-raised text-xl text-qi-accent">◎</div>
      <h3 className="font-medium">{title}</h3>
      <p className="mt-1 max-w-sm text-sm text-qi-secondary">{description}</p>
      {action && onAction && <button type="button" onClick={onAction} className="qi-button-primary mt-4 px-4 py-2 text-sm">{action}</button>}
    </div>
  );
}
