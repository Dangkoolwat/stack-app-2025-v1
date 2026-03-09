<template>
  <div>
    <h2>
      <span id="user-management-page-heading" v-text="t$('userManagement.home.title')" data-cy="userManagementPageHeading"></span>

      <div class="d-flex justify-content-end">
        <n-button class="mr-2" @click="handleSyncList" :loading="isLoading">
          <span v-text="t$('userManagement.home.refreshListLabel')"></span>
        </n-button>
        <router-link custom v-slot="{ navigate }" :to="{ name: 'JhiUserCreate' }">
          <n-button type="primary" @click="navigate">
            <span v-text="t$('userManagement.home.createLabel')"></span>
          </n-button>
        </router-link>
      </div>
    </h2>

    <n-modal
      v-model:show="showRemoveUserModal"
      preset="dialog"
      :title="t$('entity.delete.title')"
      @positive-click="deleteUser()"
      @negative-click="closeDialog()"
    >
      <p id="jhi-delete-user-heading" v-text="t$('userManagement.delete.question', { login: removeId })"></p>
    </n-modal>

    <div v-show="users && users.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="queryCount" :itemsPerPage="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <n-pagination v-model:page="page" :page-count="Math.ceil(totalItems / itemsPerPage)" @update:page="loadPage(page)" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./user-management.component.ts"></script>
