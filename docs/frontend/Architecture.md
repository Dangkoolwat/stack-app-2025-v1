# Frontend Architecture Notes

Document role: reference-only architecture notes for frontend contributors.
This file provides orientation for the frontend structure, but it does not override `AGENTS.md`, `docs/standards/`, `docs/workflow/`, or `docs/operations/`.

## Core Principles
- Maintainability First
- Clear Separation of Concerns
- Backend is source of truth

---

## Architecture

### Views
- UI composition only

### Core
- Business logic
- Auth / routing / guards

### Themes
- Reusable UI components (Base components)

---

## Recommended Patterns

Prefer:
- Small, focused components
- Explicit props over implicit behavior
- Composition over inheritance

Avoid:
- Over-componentization
- Logic inside views
- Global state overuse

---

## State Management

Default:
- Local state first

Use Pinia ONLY when:
- State is shared across multiple views
- Persistent or global concern

---

## Maintainability

- Keep components readable
- Avoid deep nesting
- Extract reusable UI into Themes

---

## Security

- Do NOT trust client-only validation
- Always rely on backend for auth/authorization
- Handle errors consistently

---

## Change Playbook

### 1. New Screen

Steps:
1. Define UI structure
2. Use Base components
3. Connect to API
4. Handle loading/error states

---

### 2. State Change

- Evaluate:
  - Local vs Global
- Prefer local unless clearly shared

---

### 3. API Integration

- Use centralized API layer
- Standardize error handling
- Avoid direct fetch in components

---

## Preferred Defaults

- Base components first
- Minimal global state
- Clear UI/data separation

---

## Self-Check Before Merge

- [ ] Component responsibility clear
- [ ] No unnecessary global state
- [ ] API error handled
- [ ] UI consistent with design system

---

## Golden Rule

 "Simple UI structure beats clever abstraction."
