# ✅ Task Completion Checklist

Before considering a task finished, ensure:

1.  **Code Quality**:
    - Run `npm run lint` to ensure no linting errors.
    - Run `npm run prettier:format` to match project styling.
2.  **Verification**:
    - Backend: If logic changed, run `npm run backend:unit:test`.
    - Frontend: If UI/logic changed, run `npm test`.
3.  **Documentation**:
    - Update `AGENT_LOG.md` (or equivalent log file) using the standard template.
    - If a new public symbol is added, verify it shows up in `get_symbols_overview`.
4.  **Security**:
    - Ensure no secrets are hardcoded.
    - Verify authorization checks are in place for new endpoints.
5.  **Clean up**:
    - Remove any temporary scratch files or debug logs.
