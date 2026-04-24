# Implementation Plan - Fix WebSocket Access Denied for DISCONNECT

## Problem
An `org.springframework.security.access.AccessDeniedException` was occurring during WebSocket session termination (DISCONNECT). This happened because the `WebSocketSecurityConfiguration` was too restrictive, requiring authentication for messages without a destination (`nullDestMatcher`), which includes common STOMP maintenance messages like `DISCONNECT`, `HEARTBEAT`, and `CONNECT` (for initial handshake).

## Proposed Changes
1.  Modify `WebSocketSecurityConfiguration.java`:
    *   Change the `@Bean` return type to `AuthorizationManager<Message<?>>` and ensure `.build()` is called. Previously it was erroneously returning a `Builder` which likely caused Spring Security to fall back to default deny-all rules.
    *   Explicitly permit `CONNECT`, `DISCONNECT`, `UNSUBSCRIBE`, and `OTHER` (includes heartbeats) message types for all users.
    *   Add `nullDestMatcher().permitAll()` for maintenance messages without destinations.
    *   Use `AuthoritiesConstants` for role/authority checks.
2.  Ensure proper imports for `AuthoritiesConstants` and `SimpMessageType`.

## Verification Plan
1.  Compile Check: Run `./mvnw compile` to ensure no syntax errors and correct constant usage.
2.  Logic Review: Verify that `DISCONNECT` and `UNSUBSCRIBE` are now allowed for anonymous users, matching the log feedback where an `AnonymousAuthenticationToken` was denied.
