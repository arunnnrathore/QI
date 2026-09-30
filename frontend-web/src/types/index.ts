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
  verified: boolean;
  online: boolean;
  lastSeen: string | null;
  createdAt: string;
}

export interface ConversationSummaryResponse {
  friendId: number;
  friendUsername: string;
  friendFirstName: string;
  friendLastName: string;
  friendProfilePicture: string | null;
  friendOnline: boolean | null;
  friendLastSeen: string | null;
  lastMessageId: number | null;
  lastMessageContent: string | null;
  lastMessageSenderId: number | null;
  lastMessageTimestamp: string | null;
  lastMessageStatus: 'SENT' | 'READ' | null;
  unreadCount: number;
}

export interface ChatMessageResponse {
  id: number;
  senderId: number;
  senderUsername: string;
  senderName: string;
  receiverId: number;
  receiverUsername: string;
  receiverName: string;
  content: string;
  status: 'SENT' | 'READ';
  timestamp: string;
  attachmentId: number | null;
  attachmentFilename: string | null;
  attachmentContentType: string | null;
  attachmentDownloadUrl: string | null;
}
