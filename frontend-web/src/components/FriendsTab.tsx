import { useState, useEffect } from 'react';
import api from '../services/api';
import type { UserSearchResponse, FriendResponse, FriendRequestResponse } from '../types';

export default function FriendsTab() {
  const [activeSubTab, setActiveSubTab] = useState<'LIST' | 'PENDING' | 'SEARCH'>('LIST');
  const [friends, setFriends] = useState<FriendResponse[]>([]);
  const [pendingRequests, setPendingRequests] = useState<FriendRequestResponse[]>([]);
  const [searchResults, setSearchResults] = useState<UserSearchResponse[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (activeSubTab === 'LIST') fetchFriends();
    if (activeSubTab === 'PENDING') fetchPendingRequests();
  }, [activeSubTab]);

  const fetchFriends = async () => {
    setLoading(true);
    try {
      const res = await api.get('/friends/list');
      setFriends(res.data);
    } catch (err) {
      console.error('Error fetching friends', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchPendingRequests = async () => {
    setLoading(true);
    try {
      const res = await api.get('/friends/pending');
      setPendingRequests(res.data);
    } catch (err) {
      console.error('Error fetching pending requests', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchQuery.trim()) return;
    setLoading(true);
    try {
      const res = await api.get(`/users/search?query=${encodeURIComponent(searchQuery)}`);
      setSearchResults(res.data);
    } catch (err) {
      console.error('Error searching users', err);
    } finally {
      setLoading(false);
    }
  };

  const sendFriendRequest = async (userId: number) => {
    try {
      await api.post(`/friends/request?receiverId=${userId}`);
      alert('Friend request sent!');
    } catch (err: any) {
      alert(err.response?.data?.message || err.response?.data || 'Failed to send request');
    }
  };

  const acceptRequest = async (requestId: number) => {
    try {
      await api.post(`/friends/accept?requestId=${requestId}`);
      fetchPendingRequests();
    } catch (err: any) {
      alert('Failed to accept request');
    }
  };

  const rejectRequest = async (requestId: number) => {
    try {
      await api.post(`/friends/reject?requestId=${requestId}`);
      fetchPendingRequests();
    } catch (err: any) {
      alert('Failed to reject request');
    }
  };

  const unfriend = async (friendId: number) => {
    if (!window.confirm('Are you sure you want to unfriend this user?')) return;
    try {
      await api.delete(`/friends/unfriend?friendId=${friendId}`);
      fetchFriends();
    } catch (err) {
      alert('Failed to unfriend');
    }
  };

  return (
    <div className="flex h-full flex-col bg-white rounded-lg shadow-sm">
      {/* Header Tabs */}
      <div className="flex border-b">
        <button
          className={`flex-1 py-4 text-center font-medium ${activeSubTab === 'LIST' ? 'border-b-2 border-blue-600 text-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveSubTab('LIST')}
        >
          My Friends
        </button>
        <button
          className={`flex-1 py-4 text-center font-medium ${activeSubTab === 'PENDING' ? 'border-b-2 border-blue-600 text-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveSubTab('PENDING')}
        >
          Pending Requests
        </button>
        <button
          className={`flex-1 py-4 text-center font-medium ${activeSubTab === 'SEARCH' ? 'border-b-2 border-blue-600 text-blue-600' : 'text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveSubTab('SEARCH')}
        >
          Find Friends
        </button>
      </div>

      {/* Content Area */}
      <div className="flex-1 overflow-y-auto p-6">
        {loading && <div className="text-center text-gray-500">Loading...</div>}

        {!loading && activeSubTab === 'LIST' && (
          <div className="space-y-4">
            {friends.length === 0 ? (
              <p className="text-center text-gray-500">You don't have any friends yet.</p>
            ) : (
              friends.map(f => (
                <div key={f.id} className="flex items-center justify-between rounded-md border p-4">
                  <div className="flex items-center gap-3">
                    <div className="h-10 w-10 overflow-hidden rounded-full bg-gray-300 flex items-center justify-center text-white font-bold">
                      {f.firstName?.charAt(0) || f.username?.charAt(0)}
                    </div>
                    <div>
                      <p className="font-medium text-gray-800">{f.firstName} {f.lastName}</p>
                      <p className="text-sm text-gray-500">@{f.username}</p>
                    </div>
                  </div>
                  <button onClick={() => unfriend(f.id)} className="text-sm text-red-500 hover:underline">
                    Unfriend
                  </button>
                </div>
              ))
            )}
          </div>
        )}

        {!loading && activeSubTab === 'PENDING' && (
          <div className="space-y-4">
            {pendingRequests.length === 0 ? (
              <p className="text-center text-gray-500">No pending requests.</p>
            ) : (
              pendingRequests.map(req => (
                <div key={req.requestId} className="flex items-center justify-between rounded-md border p-4">
                  <div>
                    <p className="font-medium text-gray-800">{req.senderName}</p>
                    <p className="text-sm text-gray-500">@{req.senderUsername}</p>
                    <p className="text-xs text-gray-400">Sent: {new Date(req.createdAt).toLocaleDateString()}</p>
                  </div>
                  <div className="flex gap-2">
                    <button onClick={() => acceptRequest(req.requestId)} className="rounded bg-green-500 px-3 py-1 text-sm text-white hover:bg-green-600">Accept</button>
                    <button onClick={() => rejectRequest(req.requestId)} className="rounded bg-red-500 px-3 py-1 text-sm text-white hover:bg-red-600">Reject</button>
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {activeSubTab === 'SEARCH' && (
          <div>
            <form onSubmit={handleSearch} className="mb-6 flex gap-2">
              <input
                type="text"
                placeholder="Search users by name or email..."
                className="flex-1 rounded-md border p-2 focus:border-blue-500 focus:outline-none"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
              />
              <button type="submit" className="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700">Search</button>
            </form>
            
            <div className="space-y-4">
              {!loading && searchResults.map(user => (
                <div key={user.id} className="flex items-center justify-between rounded-md border p-4">
                  <div className="flex items-center gap-3">
                    <div className="h-10 w-10 overflow-hidden rounded-full bg-gray-300 flex items-center justify-center text-white font-bold">
                      {user.firstName?.charAt(0) || user.username?.charAt(0)}
                    </div>
                    <div>
                      <p className="font-medium text-gray-800">{user.firstName} {user.lastName}</p>
                      <p className="text-sm text-gray-500">@{user.username}</p>
                    </div>
                  </div>
                  <button onClick={() => sendFriendRequest(user.id)} className="rounded border border-blue-600 px-3 py-1 text-sm text-blue-600 hover:bg-blue-50">
                    Add Friend
                  </button>
                </div>
              ))}
              {!loading && searchResults.length === 0 && searchQuery && (
                <p className="text-center text-gray-500">No users found.</p>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
