<template>
  <div id="app">
    <ribbon></ribbon>
    <div id="app-header">
      <jhi-navbar></jhi-navbar>
    </div>
    <div class="container-fluid">
      <div class="card jh-card">
        <router-view></router-view>
      </div>
      <n-modal v-model:show="loginModalOpen" preset="card" :style="{ width: '450px' }" :title="t$('login.title')" :mask-closable="false">
        <login-form></login-form>
      </n-modal>

      <jhi-footer></jhi-footer>
    </div>
  </div>
</template>

<script lang="ts" src="./app.component.ts"></script>
