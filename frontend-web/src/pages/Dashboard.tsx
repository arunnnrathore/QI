import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

export default function Dashboard() {
  const [user, setUser] = useState<any>(null);
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
      <div className="w-64 border-r bg-white p-4">
        <div className="mb-8 text-2xl font-bold text-blue-600">QI App</div>
        <nav className="space-y-2">
          <a href="#" className="block rounded bg-blue-50 px-3 py-2 text-blue-700">Chats</a>
          <a href="#" className="block rounded px-3 py-2 text-gray-700 hover:bg-gray-50">Friends</a>
          <a href="#" className="block rounded px-3 py-2 text-gray-700 hover:bg-gray-50">Files</a>
        </nav>
        
        <div className="absolute bottom-4 left-4 flex items-center gap-3">
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
            <button onClick={handleLogout} className="text-xs text-red-500 hover:underline">Logout</button>
          </div>
        </div>
      </div>

      {/* Main Content Placeholder */}
      <div className="flex flex-1 flex-col items-center justify-center bg-gray-50 p-8">
        <h1 className="text-3xl font-semibold text-gray-800">Welcome to QI Dashboard!</h1>
        <p className="mt-2 text-gray-600">Select a chat on the left to start messaging.</p>
      </div>
    </div>
  );
}
