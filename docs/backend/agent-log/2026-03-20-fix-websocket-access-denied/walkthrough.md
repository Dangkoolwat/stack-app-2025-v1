# Walkthrough - WebSocket Access Denied for DISCONNECT Fixed

The WebSocket connection was failing at termination (DISCONNECT) with `AccessDeniedException` for anonymous users. This was because the security configuration didn't explicitly permit these core STOMP maintenance messages.

## Changes

### [WebSocketSecurityConfiguration.java](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/java/com/daangcool/stack/config/WebSocketSecurityConfiguration.java)
- Corrected the `@Bean` return type to `AuthorizationManager<Message<?>>`.
- Added `.build()` call to properly instantiate the authorization manager.
- Explicitly permitted `CONNECT`, `DISCONNECT`, `UNSUBSCRIBE`, and `OTHER` for all users.
- Added `nullDestMatcher().permitAll()` as an additional safeguard for messages without destinations.
- Updated `hasRole("ADMIN")` to `hasAuthority(AuthoritiesConstants.ADMIN)`.
- Added necessary imports (`Message`, `AuthorizationManager`, `SimpMessageType`).

```java
        messages
            .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.DISCONNECT, SimpMessageType.UNSUBSCRIBE, SimpMessageType.OTHER).permitAll()
            .simpDestMatchers("/topic/tracker").hasAuthority(AuthoritiesConstants.ADMIN)
            .simpDestMatchers("/topic/").authenticated()
// ...
```

## Side Effects & Risks
- Security Impact: No negative security impact. These message types (`CONNECT`, `DISCONNECT`, etc.) are part of the STOMP protocol management and don't bypass security for actual business data destinations. Any `MESSAGE` or `SUBSCRIBE` to actual topics (`/topic/`) still requires authentication.
- Data Flow: Prevents unnecessary exceptions in the logs and ensures clean WebSocket session closure.

## Verification
- Build: Successfully compiled with `./mvnw compile`.
- Logic: Anonymous disconnects (matching the reported log) are now covered by `permitAll()`.
