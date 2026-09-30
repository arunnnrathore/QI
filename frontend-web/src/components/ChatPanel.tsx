import { useCallback, useEffect, useRef, useState } from 'react';
import axios from 'axios';
import { Client } from '@stomp/stompjs';
import { Search } from 'lucide-react';
import api from '../services/api';
import type { ChatMessageResponse, ConversationSummaryResponse } from '../types';

function getErrorMessage(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const data: unknown = error.response?.data;
    if (typeof data === 'string' && data.trim()) return data;
    if (data && typeof data === 'object' && 'message' in data && typeof data.message === 'string') return data.message;
  }
  return fallback;
}

function friendName(conversation: ConversationSummaryResponse): string {
  return [conversation.friendFirstName, conversation.friendLastName].filter(Boolean).join(' ') || conversation.friendUsername;
}

function formatTime(value: string | null): string {
  if (!value) return '';
  return new Date(value).toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
}

function formatDay(value: string): string {
  return new Date(value).toLocaleDateString([], { weekday: 'long', month: 'long', day: 'numeric' });
}

export default function ChatPanel({ currentUserId }: { currentUserId: number }) {
  const [conversations, setConversations] = useState<ConversationSummaryResponse[]>([]);
  const [selectedFriendId, setSelectedFriendId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessageResponse[]>([]);
  const [loadingConversations, setLoadingConversations] = useState(true);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('');
  const [draft, setDraft] = useState('');
  const [socketState, setSocketState] = useState<'connecting' | 'connected' | 'disconnected'>('connecting');
  const [mobileChatOpen, setMobileChatOpen] = useState(false);
  const [client, setClient] = useState<Client | null>(null);
  const selectedFriendIdRef = useRef<number | null>(null);

  const loadConversations = useCallback(async () => {
    setLoadingConversations(true);
    setError('');
    try {
      const response = await api.get<ConversationSummaryResponse[]>('/messages/conversations');
      setConversations(response.data);
      setSelectedFriendId((selected) => selected && response.data.some((item) => item.friendId === selected)
        ? selected
        : response.data[0]?.friendId ?? null);
    } catch (requestError) {
      setError(getErrorMessage(requestError, 'Could not load your conversations.'));
    } finally {
      setLoadingConversations(false);
    }
  }, []);

  const loadHistory = useCallback(async (friendId: number) => {
    setLoadingHistory(true);
    setError('');
    try {
      const response = await api.get<ChatMessageResponse[]>('/messages/history', { params: { friendId } });
      setMessages(response.data);
      setConversations((items) => items.map((item) => item.friendId === friendId ? { ...item, unreadCount: 0 } : item));
    } catch (requestError) {
      setMessages([]);
      setError(getErrorMessage(requestError, 'Could not load this conversation.'));
    } finally {
      setLoadingHistory(false);
    }
  }, []);

  useEffect(() => { void loadConversations(); }, [loadConversations]);
  useEffect(() => {
    selectedFriendIdRef.current = selectedFriendId;
    if (selectedFriendId !== null) void loadHistory(selectedFriendId);
    else setMessages([]);
  }, [selectedFriendId, loadHistory]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      setSocketState('disconnected');
      return;
    }

    const configuredApiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8081';
    const socketUrl = `${configuredApiUrl.replace(/^http/, 'ws').replace(/\/$/, '')}/ws`;
    const stompClient = new Client({
      brokerURL: socketUrl,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 4000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        setSocketState('connected');
        stompClient.subscribe('/user/queue/messages', (frame) => {
          try {
            const message = JSON.parse(frame.body) as ChatMessageResponse;
            const otherUserId = message.senderId === currentUserId ? message.receiverId : message.senderId;
            const preview = message.content || (message.attachmentFilename ? `Attachment: ${message.attachmentFilename}` : '');

            setConversations((items) => items.map((item) => item.friendId === otherUserId ? {
              ...item,
              lastMessageId: message.id,
              lastMessageContent: preview,
              lastMessageSenderId: message.senderId,
              lastMessageTimestamp: message.timestamp,
              lastMessageStatus: message.status,
              unreadCount: message.senderId !== currentUserId && selectedFriendIdRef.current === otherUserId ? 0 : item.unreadCount + (message.senderId !== currentUserId ? 1 : 0),
            } : item).sort((a, b) => (b.lastMessageTimestamp || '').localeCompare(a.lastMessageTimestamp || '')));

            if (selectedFriendIdRef.current === otherUserId) {
              if (message.senderId !== currentUserId) {
                void loadHistory(otherUserId);
              } else {
                setMessages((items) => items.some((item) => item.id === message.id) ? items : [...items, message]);
              }
            }
          } catch {
            setError('Received a message in an unsupported format.');
          }
        });
      },
      onWebSocketClose: () => setSocketState('disconnected'),
      onWebSocketError: () => setSocketState('disconnected'),
      onStompError: (frame) => {
        setSocketState('disconnected');
        setError(frame.headers.message || 'Live messaging connection was rejected.');
      },
    });

    setClient(stompClient);
    stompClient.activate();
    return () => {
      setClient(null);
      void stompClient.deactivate();
    };
  }, [currentUserId, loadHistory]);

  const sendMessage = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const content = draft.trim();
    if (!content || selectedFriendId === null || !client?.connected) return;

    setError('');
    try {
      client.publish({
        destination: '/app/chat.send',
        body: JSON.stringify({ receiverId: selectedFriendId, content, attachmentId: null }),
      });
      setDraft('');
    } catch {
      setError('Message could not be sent. Check your connection and try again.');
    }
  };

  const filteredConversations = conversations.filter((item) => {
    const query = filter.trim().toLowerCase();
    return !query || friendName(item).toLowerCase().includes(query) || item.friendUsername.toLowerCase().includes(query);
  });
  const selectedConversation = conversations.find((item) => item.friendId === selectedFriendId) ?? null;

  return (
    <section className="flex h-[calc(100vh-9rem)] min-h-[480px] overflow-hidden rounded-2xl border border-white/10 bg-slate-900/70 shadow-xl shadow-black/10">
      <aside className={`${mobileChatOpen ? 'hidden' : 'flex'} w-full max-w-sm shrink-0 flex-col border-r border-white/10 sm:flex sm:w-[19rem]`}>
        <div className="border-b border-white/10 p-4">
          <div className="mb-4 flex items-center justify-between">
            <div>
              <h2 className="font-semibold">Conversations</h2>
              <p className="mt-1 text-xs text-slate-500">{conversations.length} friends</p>
            </div>
            <button type="button" onClick={() => void loadConversations()} aria-label="Refresh conversations" className="rounded-lg px-3 py-2 text-xs text-slate-400 transition hover:bg-white/5 hover:text-white">Refresh</button>
          </div>
          <label className="flex items-center gap-2 rounded-lg border border-white/10 bg-slate-950/70 px-3 py-2 text-slate-500 focus-within:border-cyan-300/50">
            <Search size={16} aria-hidden="true" />
            <span className="sr-only">Filter conversations</span>
            <input value={filter} onChange={(event) => setFilter(event.target.value)} placeholder="Find a conversation" className="min-w-0 flex-1 bg-transparent text-sm text-white outline-none placeholder:text-slate-500" />
          </label>
        </div>
        <div className="flex-1 overflow-y-auto">
          {loadingConversations ? (
            <p className="p-5 text-sm text-slate-400" role="status">Loading conversations...</p>
          ) : filteredConversations.length ? filteredConversations.map((conversation) => {
            const name = friendName(conversation);
            const active = conversation.friendId === selectedFriendId;
            return (
              <button key={conversation.friendId} type="button" onClick={() => { setSelectedFriendId(conversation.friendId); setMobileChatOpen(true); }} aria-current={active ? 'true' : undefined} className={`flex w-full items-center gap-3 border-b border-white/5 px-4 py-4 text-left transition ${active ? 'bg-cyan-300/10' : 'hover:bg-white/[0.04]'}`}>
                <div className="relative h-11 w-11 shrink-0 overflow-hidden rounded-full bg-slate-700">
                  {conversation.friendProfilePicture ? <img src={conversation.friendProfilePicture} alt="" className="h-full w-full object-cover" /> : <div className="flex h-full w-full items-center justify-center font-semibold text-cyan-200">{name.slice(0, 1).toUpperCase()}</div>}
                  {conversation.friendOnline && <span className="absolute bottom-0 right-0 h-3 w-3 rounded-full border-2 border-slate-900 bg-emerald-400" aria-label="Online" />}
                </div>
                <div className="min-w-0 flex-1">
                  <div className="flex items-center justify-between gap-2">
                    <span className="truncate text-sm font-medium">{name}</span>
                    <span className="shrink-0 text-[11px] text-slate-500">{formatTime(conversation.lastMessageTimestamp)}</span>
                  </div>
                  <div className="mt-1 flex items-center justify-between gap-2">
                    <span className="truncate text-xs text-slate-400">{conversation.lastMessageContent || 'Start a conversation'}</span>
                    {conversation.unreadCount > 0 && <span className="flex h-5 min-w-5 items-center justify-center rounded-full bg-cyan-300 px-1.5 text-[10px] font-bold text-slate-950">{conversation.unreadCount}</span>}
                  </div>
                </div>
              </button>
            );
          }) : !error && <p className="px-5 py-8 text-center text-sm text-slate-500">{conversations.length ? 'No matches found.' : 'Add friends to start a conversation.'}</p>}
        </div>
      </aside>

      <div className={`${mobileChatOpen ? 'flex' : 'hidden sm:flex'} min-w-0 flex-1 flex-col`}>
        {selectedConversation ? (
          <>
            <header className="flex items-center gap-3 border-b border-white/10 px-4 py-4 sm:px-5">
              <button type="button" onClick={() => setMobileChatOpen(false)} className="rounded-lg px-2 py-1 text-sm text-slate-400 hover:bg-white/5 sm:hidden" aria-label="Back to conversations">←</button>
              <div className="flex h-10 w-10 items-center justify-center overflow-hidden rounded-full bg-slate-700 font-semibold text-cyan-200">
                {selectedConversation.friendProfilePicture ? <img src={selectedConversation.friendProfilePicture} alt="" className="h-full w-full object-cover" /> : friendName(selectedConversation).slice(0, 1).toUpperCase()}
              </div>
              <div>
                <h3 className="text-sm font-semibold">{friendName(selectedConversation)}</h3>
                <p className="mt-0.5 text-xs text-slate-400">@{selectedConversation.friendUsername}{selectedConversation.friendOnline ? ' · Online' : ''} · {socketState === 'connected' ? 'Live' : socketState === 'connecting' ? 'Connecting…' : 'Offline'}</p>
              </div>
            </header>
            {error && <p className="m-4 rounded-lg bg-rose-400/10 p-3 text-sm text-rose-200" role="alert">{error}</p>}
            <div className="flex-1 space-y-4 overflow-y-auto p-5" aria-live="polite">
              {loadingHistory ? <p className="text-center text-sm text-slate-500" role="status">Loading messages...</p> : messages.length ? messages.map((message, index) => {
                const ownMessage = message.senderId === currentUserId;
                const previousMessage = messages[index - 1];
                const showDay = !previousMessage || new Date(previousMessage.timestamp).toDateString() !== new Date(message.timestamp).toDateString();
                return (
                  <div key={message.id}>
                    {showDay && <p className="mb-4 text-center text-[11px] text-slate-500">{formatDay(message.timestamp)}</p>}
                    <div className={`flex ${ownMessage ? 'justify-end' : 'justify-start'}`}>
                      <article className={`max-w-[82%] rounded-2xl px-4 py-3 ${ownMessage ? 'rounded-br-sm bg-cyan-300 text-slate-950' : 'rounded-bl-sm bg-white/5 text-slate-100'}`}>
                        <p className="whitespace-pre-wrap break-words text-sm leading-6">{message.content || (message.attachmentFilename ? `Attachment: ${message.attachmentFilename}` : '')}</p>
                        <p className={`mt-1 text-right text-[10px] ${ownMessage ? 'text-slate-700' : 'text-slate-500'}`}>{formatTime(message.timestamp)}{ownMessage && message.status === 'READ' ? ' · Read' : ''}</p>
                      </article>
                    </div>
                  </div>
                );
              }) : <div className="flex h-full flex-col items-center justify-center text-center"><p className="font-medium text-slate-300">No messages yet</p><p className="mt-1 text-sm text-slate-500">Send a message to start the conversation.</p></div>}
            </div>
            <form onSubmit={sendMessage} className="flex items-end gap-3 border-t border-white/10 p-3 sm:p-4">
              <label className="sr-only" htmlFor="chat-message">Write a message</label>
              <textarea id="chat-message" rows={1} maxLength={4000} value={draft} onChange={(event) => setDraft(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); event.currentTarget.form?.requestSubmit(); } }} placeholder={socketState === 'connected' ? 'Write a message…' : 'Connecting to live chat…'} disabled={socketState !== 'connected'} className="max-h-32 min-h-11 flex-1 resize-y rounded-xl border border-white/10 bg-slate-950/70 px-4 py-3 text-sm text-white outline-none placeholder:text-slate-500 focus:border-cyan-300/50 disabled:opacity-50" />
              <button type="submit" disabled={socketState !== 'connected' || !draft.trim()} className="h-11 shrink-0 rounded-xl bg-cyan-300 px-4 text-sm font-semibold text-slate-950 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:opacity-50">Send</button>
            </form>
          </>
        ) : (
          <div className="flex flex-1 flex-col items-center justify-center p-8 text-center">
            <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-white/5 text-2xl text-cyan-300">◌</div>
            <h3 className="font-medium">Select a conversation</h3>
            <p className="mt-1 text-sm text-slate-500">Choose a friend from the list to view your messages.</p>
            {error && <p className="mt-4 text-sm text-rose-200" role="alert">{error}</p>}
          </div>
        )}
      </div>
    </section>
  );
}
