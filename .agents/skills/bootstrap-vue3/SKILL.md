---
name: bootstrap-vue3
description: Concise guide for BootstrapVue Next (Bootstrap 5 + Vue 3) as used in this project.
---

# BootstrapVue Next (Slim)

## 1. Project Context
This project uses **BootstrapVue Next** with **Bootstrap 5**.
Components are globally registered in `src/main/webapp/app/shared/config/config-bootstrap-vue.ts`.

## 2. Commonly Used Components
- **Layout**: `BContainer`, `BRow`, `BCol`, `BNavbar`, `BNavbarNav`, `BNavbarBrand`, `BNavbarToggle`.
- **Forms**: `BForm`, `BFormGroup`, `BFormInput`, `BFormCheckbox`, `BFormTags`, `BPagination`.
- **UI Elements**: `BButton`, `BBadge`, `BAlert`, `BProgress`, `BProgressBar`, `BCard`, `BCollapse`.
- **Feedback**: `BModal`, `BDropdown`, `BDropdownItem`, `BNavItem`, `BNavItemDropdown`.
- **Directives**: `v-b-modal`, `v-b-tooltip`.

## 3. Basic Usage Example
```vue
<template>
  <b-form @submit.prevent="onSubmit">
    <b-form-group label="Email Address:" label-for="email-input">
      <b-form-input id="email-input" v-model="form.email" type="email" required />
    </b-form-group>
    <b-button type="submit" variant="primary">Submit</b-button>
  </b-form>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
const form = reactive({ email: '' });
const onSubmit = () => { /* ... */ };
</script>
```

## 4. Best Practices
- ✅ **Utility Classes**: Use Bootstrap 5 utility classes (`d-flex`, `gap-2`, `p-3`, `text-center`) for layout.
- ✅ **Composition API**: Always use `<script setup lang="ts">`.
- ✅ **Variant Consistency**: Use standard variants (`primary`, `secondary`, `success`, `danger`, `warning`, `info`).
- ❌ **Avoid Raw CSS**: Use Bootstrap utilities or Themes layer for styling.
- ❌ **Direct DOM Manipulation**: Always use Vue reactivity or refs.
