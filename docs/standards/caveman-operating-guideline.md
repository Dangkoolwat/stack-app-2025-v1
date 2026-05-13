# AI Agent Operating Guideline (v4.3-caveman)

This document defines the behavioral and technical standards for agents operating in this repository, focusing on token efficiency and professional communication.

## 1. Persona & Communication (via Skill)
- **Base Style:** Follow `./.agents/skills/caveman/SKILL.md` strictly.
- **Tone:** Professional, technical, no filler (Lite mode).
- **Tagging:** Maintain ✅ Facts, ⚠️ Uncertain, 💡 Deduction. Do not compress tags.

## 2. Infrastructure & Tools (via MCP)
- **MCP Active:** Use `caveman-shrink` to minimize tool-call overhead.
- **Priority:** Reduce input tokens by 50% using MCP-based tool descriptions.

### 2.1 Shrink Wrapper Safety
`caveman-shrink` reduces transport cost, not review responsibility.

Compressed output may be used for navigation, handoff, and tool-call overhead reduction, but MUST NOT remove failed command names, first meaningful errors, changed file paths, policy triggers, scope deviations, protected-area touches, skipped verification, High-Risk warnings, or release blockers.

For High-Risk work, incidents, failed verification, release decisions, CI/CD decisions, or protected-area changes, compressed summaries are not final evidence. Preserve or request exact output when full context is required.

## 3. Protocol Content vs Lite Mode
Caveman Lite controls wording style, not required protocol content.

DO NOT omit mandatory handshake, policy trigger mapping, validation, incident, work log, or handoff fields for brevity. Keep required content short, but complete.

### 3.1 Communication Protocol (Lite)
- Default to Caveman Skill Lite.
- Keep responses short, direct, and readable.
- In Korean, use polite honorific form.
- Remove greetings, apologies, and filler unless needed for clarity.
- Keep technical facts exact; avoid vague phrasing.
- Use plain statements for security warnings or destructive changes.
