# QI_HANDOFF.md

## Purpose
Development handoff for continuing the QI (Quick Intelligence) project in Google Antigravity. Read this before changing code. The repository is the source of truth; inspect existing code before rebuilding anything.

## Project
QI is an AI-based chat and file-sharing application inspired by Telegram.

Planned areas:
- chat/messaging
- large-file sharing
- language translation
- audio translation
- AI features
- authentication
- friend/request system
- web/mobile clients

Suggested structure:
QI/
- backend-java/
- ai-service/
- frontend-web/
- mobile-app/
- database/
- docs/
- design/
- README.md

## Backend stack
- Java
- Spring Boot
- Maven
- PostgreSQL
- Spring Data JPA
- Spring Security
- JWT
- BCrypt
- Postman
- Git/GitHub Desktop

Backend currently runs on:
http://localhost:8081

## Authentication
JWT Bearer authentication is used.

Current-user identity must come from Spring Security:
```java
Authentication authentication;
String email = authentication.getName();
```

Use that email to find the authenticated User.

Do NOT trust client-supplied senderId/userId to identify the authenticated user. For example:
POST /api/friends/request?receiverId=5
uses the JWT user as sender. A senderId query parameter is ignored unless the controller explicitly defines it.

## Friend system
Tables/entities:
- users
- friend_requests
- friends

FriendRequestStatus:
- PENDING
- ACCEPTED
- REJECTED

Endpoints:
- POST /api/friends/request?receiverId={id}
- GET /api/friends/pending
- POST /api/friends/accept?requestId={id}
- POST /api/friends/reject?requestId={id}
- GET /api/friends/list
- DELETE /api/friends/unfriend?friendId={id}

There has also been a temporary:
- POST /api/friends/test

### Send request
Use:
POST /api/friends/request?receiverId=5
Authorization: Bearer <JWT of sender>

Logic:
1. sender = authenticated user
2. receiver = receiverId
3. reject self-request
4. reject if already friends
5. reject if same-direction PENDING request exists
6. reject if opposite-direction PENDING request exists
7. otherwise save PENDING request

### Pending
GET /api/friends/pending
Authorization: Bearer <JWT of receiver>

The JWT determines the receiver. Do not expect ?receiverId= to change the result.

### Accept
POST /api/friends/accept?requestId=<id>
Authorization: Bearer <JWT of receiver>

Only the receiver of that request should accept it. Set status ACCEPTED and create both friendship directions.

### Reject
POST /api/friends/reject?requestId=<id>
Authorization: Bearer <JWT of receiver>

Only the authenticated receiver should reject it. Set status REJECTED.

### Friend list
GET /api/friends/list
Authorization: Bearer <JWT>

Returns the friends of the authenticated user. A query such as ?userId=3 is ignored if not defined by the controller.

### Unfriend
DELETE /api/friends/unfriend?friendId=<id>
Authorization: Bearer <JWT>

Delete both user -> friend and friend -> user rows.

## Important bug already identified
Earlier code used:
```java
findBySenderAndReceiver(sender, receiver)
```
without checking status. That caused historical ACCEPTED/REJECTED requests to block new requests.

Preferred logic:
```java
findBySenderAndReceiverAndStatus(
    sender,
    receiver,
    FriendRequestStatus.PENDING
)
```
and the same check in reverse direction.

Also check:
```java
friendRepository.existsByUserAndFriend(sender, receiver)
```

## Historical database state
At one point friend_requests contained:
id 6: 3 -> 5 ACCEPTED
id 5: 4 -> 3 ACCEPTED
id 4: 5 -> 2 REJECTED
id 3: 4 -> 5 ACCEPTED
id 2: 2 -> 3 ACCEPTED
id 1: 3 -> 2 ACCEPTED

At one point friends contained:
id 3: 3 -> 2
id 4: 2 -> 3
id 7: 4 -> 3
id 8: 3 -> 4
id 9: 3 -> 5
id 10: 5 -> 3

This is historical only. Inspect the live database before relying on it.

## Useful SQL
Requests between users:
```sql
SELECT id, sender_id, receiver_id, status
FROM friend_requests
WHERE (sender_id = 1 AND receiver_id = 5)
   OR (sender_id = 5 AND receiver_id = 1);
```

Friendships between users:
```sql
SELECT id, user_id, friend_id
FROM friends
WHERE (user_id = 1 AND friend_id = 5)
   OR (user_id = 5 AND friend_id = 1);
```

A standalone `WHERE ...` is not a complete SQL statement.

## Testing rules
Always identify the user represented by the JWT first.

A sends to B:
POST /api/friends/request?receiverId=B
Bearer token = A

B checks pending:
GET /api/friends/pending
Bearer token = B

B accepts:
POST /api/friends/accept?requestId=X
Bearer token = B

B rejects:
POST /api/friends/reject?requestId=X
Bearer token = B

User checks own friends:
GET /api/friends/list
Bearer token = that user

Do not use ignored parameters such as senderId/userId/receiverId to switch authenticated identity.

## Debugging
For 401:
- check Authorization header
- check Bearer token validity/expiry
- inspect Spring Security configuration
- confirm endpoint protection
- confirm authenticated user can be loaded

For unexpected "already sent":
1. identify JWT user
2. query for PENDING request
3. check whether users are already friends
4. verify code is not treating ACCEPTED/REJECTED rows as pending

For empty pending list:
- use the intended receiver's JWT.

For identical friend lists with different userId query parameters:
- the endpoint is using JWT identity and ignoring userId.

## Antigravity rules
Before changing code:
1. Read this handoff.
2. Inspect relevant source files.
3. Inspect entities/repositories.
4. Inspect DB when persistence is involved.
5. Preserve existing API contracts unless explicitly asked to change them.
6. Make the smallest safe change.
7. Avoid duplicate classes/imports.
8. Keep JWT as the source of authenticated identity.
9. Build after meaningful backend changes.
10. Report exact build/test failures.
11. Do not silently rewrite unrelated code.

Preferred workflow:
Inspect -> Understand -> Find root cause -> Smallest change -> Build -> Test -> Verify

## Security
Never put real JWTs, passwords, DB credentials, API keys, or .env secrets in this file. Use placeholders such as <JWT>, <DB_PASSWORD>, <API_KEY>.

## First prompt for Antigravity
Read `QI_HANDOFF.md` completely before making changes. Then inspect the existing `backend-java` project and compare it with this handoff. Do not rewrite working functionality. Tell me what is already implemented, what differs from the handoff, and what the current highest-priority backend issue is. Explain your findings before making major changes.

## Source of truth
This handoff summarizes prior work. The actual repository, current configuration, and live database are authoritative if they differ from this document.
