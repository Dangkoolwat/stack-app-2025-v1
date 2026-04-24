# Agent Log - 2026-03-20-fix-websocket-access-denied

## Task Overview
An `AccessDeniedException` was occurring during STOMP `DISCONNECT` messages, especially for anonymous users, due to overly restrictive `WebSocketSecurityConfiguration`.

## Changes Summary
- Files Modified:
    - [WebSocketSecurityConfiguration.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/config/WebSocketSecurityConfiguration.java)
- Modifications:
    - Added `permitAll()` for `CONNECT`, `DISCONNECT`, `UNSUBSCRIBE`, and `OTHER` (heartbeats) STOMP message types.
    - Updated authority check for `/topic/tracker` from `hasRole("ADMIN")` to `hasAuthority(AuthoritiesConstants.ADMIN)`.
    - Added necessary imports.

## Verification Process
1.  Code Review: Identified that any message without a destination (like DISCONNECT) was being denied for non-authenticated users by the `nullDestMatcher().authenticated()` rule.
2.  Implementation: Added explicit permits for STOMP lifecycle message types.
3.  Build Check: Ran `./mvnw compile -DskipTests`. (Success)

## Artifacts Produced
- `docs/backend/agent-log/2026-03-20-fix-websocket-access-denied/implementation_plan.md`
- `docs/backend/agent-log/2026-03-20-fix-websocket-access-denied/walkthrough.md`
- `docs/backend/agent-log/2026-03-20-fix-websocket-access-denied/2026-03-20-fix-websocket-access-denied.md`
