# Task: Fix Login Cache Error

- [ ] Diagnosing the issue [/]
    - [x] Review `AuthenticateController.java`
    - [x] Review `AuthenticateControllerIT.java`
    - [x] Review `UserRepository.java`
    - [x] Review `CacheConfiguration.java`
    - [x] Review `JacksonConfiguration.java`
    - [ ] Analyze `JsonJacksonCodec` behavior
- [x] Fix Redisson Caching Issue
    - [x] Option 1: Configure `JsonJacksonCodec` with application `ObjectMapper`
    - [x] Propose clearing Redis (`FLUSHALL`) if it's due to stale data
- [ ] Verify Login Flow
    - [x] Run `AuthenticateControllerIT` (Fixed secondary config issue or verified locally)
    - [ ] Verify manual login if possible
