export interface UserSearchResponse {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  profilePicture: string | null;
  bio: string | null;
}

export interface FriendResponse {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  profilePicture: string | null;
}

export interface FriendRequestResponse {
  requestId: number;
  senderId: number;
  senderUsername: string;
  senderName: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  createdAt: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  profilePicture: string | null;
  bio: string | null;
  preferredLanguage: string | null;
  status: string;
  createdAt: string;
}
