<template>
  <BToastOrchestrator />
  <div id="app">
    <ribbon></ribbon>
    <div id="app-header">
      <jhi-navbar></jhi-navbar>
    </div>
    <div class="container-fluid">
      <div class="card jh-card">
        <router-view></router-view>
      </div>
      <b-modal
        id="login-page"
        focus="username"
        v-model="loginModalOpen"
        :no-footer="true"
        lazy
        centered
        modal-class="login-modal"
        content-class="border-0 shadow-sm rounded-4"
        header-class="border-0 pb-0"
        body-class="pt-2"
      >
        <template #title>
          <span data-cy="loginTitle" id="login-title">{{ t$('login.title') }}</span>
        </template>
        <login-form v-if="loginModalOpen"></login-form>
      </b-modal>
      <jhi-footer></jhi-footer>
    </div>
  </div>
</template>

<script lang="ts" src="./app.component.ts"></script>
