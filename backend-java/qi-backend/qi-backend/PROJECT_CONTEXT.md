# QI — Project Context

## Project
QI (Quick Intelligence) is an AI-based messaging and file-sharing application similar to Telegram.

## Backend
- Java
- Spring Boot
- PostgreSQL
- Spring Security
- JWT authentication
- BCrypt
- Maven
- Tomcat
- Postman for API testing

## Current backend
Backend runs on:
http://localhost:8081

## Authentication
The application uses JWT Bearer authentication.

The logged-in user is determined from:
`Authentication authentication`
`authentication.getName()`
which returns the user's email.

Do NOT trust senderId supplied by the client for friend requests.

## Friend System

Entities:
- User
- FriendRequest
- Friend

Tables:
- users
- friend_requests
- friends

## FriendRequest
Fields:
- id
- sender
- receiver
- status
- createdAt

Statuses:
- PENDING
- ACCEPTED
- REJECTED

## Friend Request API

- `POST /api/friends/request?receiverId={id}`: The sender is determined from the authenticated JWT user.
- `GET /api/friends/pending`: Returns pending requests for the authenticated user.
- `POST /api/friends/accept?requestId={id}`: Accepts a request as the authenticated receiver.
- `POST /api/friends/reject?requestId={id}`: Rejects a request as the authenticated receiver.
- `GET /api/friends/list`: Returns friends of authenticated user.
- `DELETE /api/friends/unfriend?friendId={id}`: Removes friendship in both directions.

## Important Friend Request Logic

When sending a request:
1. Get sender from authenticated email.
2. Get receiver by receiverId.
3. Prevent sending to yourself.
4. Check whether they are already friends.
5. Check whether a PENDING request already exists sender → receiver.
6. Check whether a PENDING request already exists receiver → sender.
7. Otherwise create a PENDING request.

Old ACCEPTED or REJECTED requests should not prevent a new request.

## Friend Acceptance

When a request is accepted:
- Request status becomes ACCEPTED.
- Create sender → receiver friendship.
- Create receiver → sender friendship.
- Avoid duplicate friendship rows.

## Current debugging lesson

Do not use `senderId` from the request URL to determine the sender.
The authenticated JWT determines the sender.
Example:
`POST /api/friends/request?receiverId=5`
If the JWT belongs to user 1:
User 1 → User 5

## Current database verification

`friend_requests` currently contains examples such as:
- 3 → 5 ACCEPTED
- 4 → 3 ACCEPTED
- 5 → 2 REJECTED
- 4 → 5 ACCEPTED
- 2 → 3 ACCEPTED
- 3 → 2 ACCEPTED

The `friends` table stores friendships in both directions.

## Important current issue that was fixed

The previous `sendFriendRequest()` implementation used:
`findBySenderAndReceiver(sender, receiver)`
This detected ANY previous request, including ACCEPTED and REJECTED requests.

It should instead use:
```java
findBySenderAndReceiverAndStatus(
    sender,
    receiver,
    FriendRequestStatus.PENDING
)
```
and similarly for the reverse direction.

## Development rule

Before changing code:
1. Inspect the existing code.
2. Inspect the database/schema if relevant.
3. Reproduce the problem.
4. Explain the cause.
5. Make the smallest correct change.
6. Build the project.
7. Test the affected API using Postman.
8. Verify the database state.

Do not rewrite working code unnecessarily.
