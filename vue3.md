# vue3.md

## Purpose

This document defines Vue 3 frontend architecture rules for this project.

Frontend stack:

- Vue 3
- Vite
- Pinia
- Vue Router
- Axios
- PrimeVue

The architecture enforces strict separation between logic and presentation.

---

## Frontend Structure

Primary directories:

src/core  
src/themes  
src/views

Responsibilities:

core  
application logic

themes  
visual design

views  
page composition

---

## Import Rules

core must not import Vue components.

themes may use PrimeVue and styling.

views may depend on core and themes.

views must not import PrimeVue directly.

PrimeVue must be wrapped inside theme-level base components.

---

## Theme Independence

Themes must remain replaceable modules.

admin and landing themes must not depend on each other.

Business logic must never depend on theme implementation.

---

## Base Component Rules

All UI elements should use base components defined in the theme layer.

Views must use components like:

BaseButton  
BaseDialog  
BaseTable

PrimeVue components must be wrapped at the theme layer before use.

---

## Auth and API Handling

Auth state belongs in Pinia stores.

Route protection belongs in router guards.

HTTP interceptors handle:

- tokens
- error normalization
- request headers

Backend errors follow RFC7807 and must be normalized centrally.

---

## Frontend Engineering Discussion Rules

When proposing UI architecture or frontend refactoring:

Problem  
Describe the limitation in the current UI or component structure.

Proposal  
Explain the suggested approach.

Alternatives  
Provide alternative approaches.

Trade-offs  
Discuss impacts on:

- component boundaries
- theme independence
- state management
- performance
- maintainability

Risks  
Explain possible UI regressions or theme coupling issues.

Decision Needed  
Indicate when confirmation is required before structural changes.

---

## State Management Rules

Use local state for UI concerns.

Use Pinia for shared application state.

Avoid unnecessary global stores.

Centralize API access.

---

## Verification

Preferred verification order:

1 component tests  
2 lint  
3 build validation  
4 route and auth checks  

If verification cannot run, state what remains unverified.

---

## Frontend Review Checklist

Before finalizing:

- core/themes/views boundaries preserved
- forbidden imports avoided
- theme independence maintained
- auth flow unaffected
- API consumption consistent
