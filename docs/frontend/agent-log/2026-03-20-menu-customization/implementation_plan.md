# Entity Menu Customization Plan

Rearrange the items in the entity menu for better logical flow and add descriptive icons to each item.

## Proposed Changes

### [Frontend]

#### [MODIFY] [config.ts](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/app/shared/config/config.ts)
- Import `faLayerGroup`, `faListUl`, `faHashtag`, `faFileAlt` from `@fortawesome/free-solid-svg-icons`.
- Add them to the `library` in `initFortAwesome`.

#### [MODIFY] [entities-menu.vue](file:///Users/sanghyoukjin/daangcoolProject/stack-app-2025-v1/src/main/webapp/app/entities/entities-menu.vue)
- Rearrange items in the following order:
  1. Common Code Group (`/common-code-group`)
  2. Common Code Detail (`/common-code-detail`)
  3. Tag (`/tag`)
  4. Board (`/board`)
- Update icons:
  - Common Code Group: `layer-group`
  - Common Code Detail: `list-ul`
  - Tag: `hashtag`
  - Board: `file-alt`

## Verification Plan

### Manual Verification
- Run the application.
- Open the "Entities" menu.
- Verify the order of items.
- Verify that icons are correctly displayed for each item.
