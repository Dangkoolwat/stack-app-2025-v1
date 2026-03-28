<template>
  <b-navbar data-cy="navbar" variant="light" data-bs-theme="light" class="dc-navbar">
    <b-navbar-brand class="dc-navbar__brand" b-link to="/">
      <span class="dc-navbar__mark">S</span>
      <span>
        <span class="dc-navbar__title">{{ t$('global.title') }}</span>
        <span class="dc-navbar__version">{{ version }}</span>
      </span>
    </b-navbar-brand>

    <div class="dc-navbar__content">
      <b-navbar-nav class="dc-navbar__nav">
        <b-nav-item to="/" exact class="dc-navbar__item">
          <span>
            <font-awesome-icon icon="home" />
            <span>{{ t$('global.menu.home') }}</span>
          </span>
        </b-nav-item>

        <b-nav-item-dropdown
          :no-size="true"
          end
          id="entity-menu"
          v-if="authenticated"
          active-class="active"
          class="pointer dc-navbar__item"
          data-cy="entity"
        >
          <template #button-content>
            <span class="navbar-dropdown-menu">
              <font-awesome-icon icon="th-list" />
              <span class="no-bold">{{ t$('global.menu.entities.main') }}</span>
            </span>
          </template>
          <entities-menu></entities-menu>
        </b-nav-item-dropdown>

        <b-nav-item-dropdown
          right
          id="admin-menu"
          v-if="hasAnyAuthority('ROLE_ADMIN') && authenticated"
          :class="{ 'router-link-active': subIsActive('/admin') }"
          active-class="active"
          class="pointer dc-navbar__item"
          data-cy="adminMenu"
        >
          <template #button-content>
            <span class="navbar-dropdown-menu">
              <font-awesome-icon icon="users-cog" />
              <span class="no-bold">{{ t$('global.menu.admin.main') }}</span>
            </span>
          </template>
          <b-dropdown-item to="/admin/user-management" active-class="active">
            <font-awesome-icon icon="users" />
            <span>{{ t$('global.menu.admin.userManagement') }}</span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/tracker" active-class="active">
            <font-awesome-icon icon="eye" />
            <span>{{ t$('global.menu.admin.tracker') }}</span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/metrics" active-class="active">
            <font-awesome-icon icon="tachometer-alt" />
            <span>{{ t$('global.menu.admin.metrics') }}</span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/health" active-class="active">
            <font-awesome-icon icon="heart" />
            <span>{{ t$('global.menu.admin.health') }}</span>
          </b-dropdown-item>
          <b-dropdown-item v-if="openAPIEnabled" to="/admin/docs" active-class="active">
            <font-awesome-icon icon="book" />
            <span>{{ t$('global.menu.admin.apidocs') }}</span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/configuration" active-class="active">
            <font-awesome-icon icon="cogs" />
            <span>{{ t$('global.menu.admin.configuration') }}</span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/logs" active-class="active">
            <font-awesome-icon icon="tasks" />
            <span>{{ t$('global.menu.admin.logs') }}</span>
          </b-dropdown-item>
        </b-nav-item-dropdown>

        <b-nav-item-dropdown id="languagesnavBarDropdown" end v-if="languages && Object.keys(languages).length > 1" class="dc-navbar__item">
          <template #button-content>
            <font-awesome-icon icon="flag" />
            <span class="no-bold">{{ t$('global.menu.language') }}</span>
          </template>
          <b-dropdown-item
            v-for="(value, key) in languages"
            :key="`lang-${key}`"
            @click="changeLanguage(key)"
            :class="{ active: isActiveLanguage(key) }"
          >
            {{ value.name }}
          </b-dropdown-item>
        </b-nav-item-dropdown>

        <b-nav-item-dropdown
          right
          id="account-menu"
          :class="{ 'router-link-active': subIsActive('/account') }"
          active-class="active"
          class="pointer dc-navbar__item"
          data-cy="accountMenu"
        >
          <template #button-content>
            <span class="navbar-dropdown-menu">
              <font-awesome-icon icon="user" />
              <span class="no-bold">{{ t$('global.menu.account.main') }}</span>
            </span>
          </template>
          <b-dropdown-item data-cy="settings" to="/account/settings" v-if="authenticated" active-class="active">
            <font-awesome-icon icon="wrench" />
            <span>{{ t$('global.menu.account.settings') }}</span>
          </b-dropdown-item>
          <b-dropdown-item data-cy="passwordItem" to="/account/password" v-if="authenticated" active-class="active">
            <font-awesome-icon icon="lock" />
            <span>{{ t$('global.menu.account.password') }}</span>
          </b-dropdown-item>
          <b-dropdown-item data-cy="logout" v-if="authenticated" @click="logout()" id="logout" active-class="active">
            <font-awesome-icon icon="sign-out-alt" />
            <span>{{ t$('global.menu.account.logout') }}</span>
          </b-dropdown-item>
          <b-dropdown-item data-cy="login" v-if="!authenticated" @click="showLogin()" id="login" active-class="active">
            <font-awesome-icon icon="sign-in-alt" />
            <span>{{ t$('global.menu.account.login') }}</span>
          </b-dropdown-item>
          <b-dropdown-item data-cy="register" to="/register" id="register" v-if="!authenticated" active-class="active">
            <font-awesome-icon icon="user-plus" />
            <span>{{ t$('global.menu.account.register') }}</span>
          </b-dropdown-item>
        </b-nav-item-dropdown>
      </b-navbar-nav>
    </div>
  </b-navbar>
</template>

<script lang="ts" src="./jhi-navbar.component.ts"></script>

<style scoped>
.dc-navbar {
  position: relative;
  z-index: 1041;
  min-height: 72px;
  padding: 12px 20px;
  align-items: flex-start;
  gap: 16px;
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
  backdrop-filter: blur(12px);
}

.dc-navbar__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0;
}

.dc-navbar__mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: #eff6ff;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  border-radius: 14px;
  font-weight: 800;
  font-size: 1rem;
  box-shadow: 0 10px 20px rgba(37, 99, 235, 0.2);
}

.dc-navbar__title {
  display: block;
  color: #111827;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.dc-navbar__version {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  margin-top: 4px;
  padding: 0 8px;
  color: #475569;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  font-size: 0.72rem;
  font-weight: 700;
}

.dc-navbar__content {
  flex: 1;
  min-width: 0;
}

.dc-navbar__nav {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 4px;
}

.dc-navbar__item :deep(.nav-link),
.dc-navbar__item :deep(.dropdown-toggle) {
  min-height: 40px;
  padding: 8px 12px;
  color: #334155;
  border-radius: 12px;
  font-weight: 600;
}

.dc-navbar__item :deep(.nav-link:hover),
.dc-navbar__item :deep(.dropdown-toggle:hover),
.dc-navbar__item.router-link-active :deep(.dropdown-toggle),
.dc-navbar__item :deep(.router-link-active) {
  color: #0f172a;
  background: #eff6ff;
}

@media (max-width: 991px) {
  .dc-navbar {
    padding: 12px 16px;
  }

  .dc-navbar__nav {
    justify-content: flex-start;
  }
}
</style>
