<template>
  <n-layout-header bordered class="jh-navbar">
    <div class="navbar-brand logo">
      <router-link to="/">
        <span class="logo-img"></span>
        <span class="navbar-title" v-text="t$('global.title')"></span>
        <span class="navbar-version">{{ version }}</span>
      </router-link>
    </div>

    <div class="navbar-menu">
      <router-link to="/" exact>
        <span v-text="t$('global.menu.home')"></span>
      </router-link>

      <n-dropdown v-if="authenticated" :options="entityMenuOptions" @select="handleMenuSelect">
        <n-button text>
          <span v-text="t$('global.menu.entities.main')"></span>
        </n-button>
      </n-dropdown>

      <n-dropdown v-if="hasAnyAuthority('ROLE_ADMIN') && authenticated" :options="adminMenuOptions" @select="handleMenuSelect">
        <n-button text>
          <span v-text="t$('global.menu.admin.main')"></span>
        </n-button>
      </n-dropdown>

      <n-dropdown v-if="languages && Object.keys(languages).length > 1" :options="languageMenuOptions" @select="handleLanguageChange">
        <n-button text>
          <span v-text="t$('global.menu.language')"></span>
        </n-button>
      </n-dropdown>

      <n-dropdown :options="accountMenuOptions" @select="handleAccountMenuSelect">
        <n-button text>
          <span v-text="t$('global.menu.account.main')"></span>
        </n-button>
      </n-dropdown>
    </div>
  </n-layout-header>
</template>

<script lang="ts" src="./jhi-navbar.component.ts"></script>

<style scoped>
.jh-navbar {
  display: flex;
  align-items: center;
  padding: 0.2em 1em;
  background-color: #353d47;
}

.navbar-version {
  font-size: 0.65em;
  color: #ccc;
}

.navbar-brand.logo {
  padding: 0 7px;
}

.navbar-brand a {
  display: flex;
  align-items: center;
  text-decoration: none;
}

.logo .logo-img {
  height: 45px;
  display: inline-block;
  vertical-align: middle;
  width: 45px;
}

.logo-img {
  height: 100%;
  background: url('/content/images/logo-jhipster.png') no-repeat center center;
  background-size: contain;
  width: 100%;
  filter: drop-shadow(0 0 0.05rem white);
  margin: 0 5px;
}

.navbar-title {
  display: inline-block;
  color: white;
  font-weight: 400;
}

.navbar-menu {
  display: flex;
  gap: 1rem;
  margin-left: auto;
  align-items: center;
}

.navbar-menu a {
  color: #ccc;
  text-decoration: none;
  padding: 0.5em;
}

.navbar-menu a:hover,
.navbar-menu a.router-link-active {
  color: white;
}
</style>
