import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import FriendsTab from '../components/FriendsTab';

export default function Dashboard() {
  const [user, setUser] = useState<any>(null);
  const [activeMainTab, setActiveMainTab] = useState<'CHATS' | 'FRIENDS' | 'FILES'>('CHATS');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await api.get('/auth/me');
        setUser(response.data);
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
    return <div className="flex h-screen items-center justify-center">Loading...</div>;
  }

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar Placeholder */}
      <div className="w-64 border-r bg-white p-4 relative flex flex-col">
        <div className="mb-8 text-2xl font-bold text-blue-600">QI App</div>
        <nav className="space-y-2 flex-1">
          <button 
            onClick={() => setActiveMainTab('CHATS')}
            className={`w-full text-left block rounded px-3 py-2 ${activeMainTab === 'CHATS' ? 'bg-blue-50 text-blue-700' : 'text-gray-700 hover:bg-gray-50'}`}
          >Chats</button>
          <button 
            onClick={() => setActiveMainTab('FRIENDS')}
            className={`w-full text-left block rounded px-3 py-2 ${activeMainTab === 'FRIENDS' ? 'bg-blue-50 text-blue-700' : 'text-gray-700 hover:bg-gray-50'}`}
          >Friends</button>
          <button 
            onClick={() => setActiveMainTab('FILES')}
            className={`w-full text-left block rounded px-3 py-2 ${activeMainTab === 'FILES' ? 'bg-blue-50 text-blue-700' : 'text-gray-700 hover:bg-gray-50'}`}
          >Files</button>
        </nav>
        
        <div className="mt-auto flex items-center gap-3 border-t pt-4">
          <div className="h-10 w-10 overflow-hidden rounded-full bg-gray-300">
            {user.profilePicture ? (
              <img src={user.profilePicture} alt="Profile" className="h-full w-full object-cover" />
            ) : (
              <div className="flex h-full w-full items-center justify-center text-lg font-bold text-white">
                {user.firstName?.charAt(0) || user.username?.charAt(0)}
              </div>
            )}
          </div>
          <div>
            <div className="text-sm font-medium">{user.firstName} {user.lastName}</div>
            <button onClick={handleLogout} className="text-sm font-semibold text-red-500 hover:underline">Logout</button>
          </div>
        </div>
      </div>

      {/* Main Content Placeholder */}
      <div className="flex-1 bg-gray-50 p-6 overflow-hidden">
        {activeMainTab === 'CHATS' && (
          <div className="flex h-full items-center justify-center">
            <div className="text-center">
              <h1 className="text-3xl font-semibold text-gray-800">Welcome to QI Dashboard!</h1>
              <p className="mt-2 text-gray-600">Select a chat on the left to start messaging.</p>
            </div>
          </div>
        )}
        {activeMainTab === 'FRIENDS' && <FriendsTab />}
        {activeMainTab === 'FILES' && (
          <div className="flex h-full items-center justify-center text-gray-500">File Manager Coming Soon...</div>
        )}
      </div>
    </div>
  );
}
