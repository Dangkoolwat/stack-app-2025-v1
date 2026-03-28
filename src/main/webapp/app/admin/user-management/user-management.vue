<template>
  <div class="dc-page">
    <section class="dc-page-header">
      <div>
        <h2 id="user-management-page-heading" data-cy="UserManagementHeading" class="dc-page-header__title">
          {{ t$('userManagement.home.title') }}
        </h2>
        <p class="dc-page-header__subtitle">사용자 상태와 권한 구성을 한 화면에서 확인하고 빠르게 유지보수할 수 있는 운영 화면입니다.</p>
      </div>

      <div class="dc-page-actions">
        <button class="btn btn-outline-secondary" @click="handleSyncList" :disabled="isLoading">
          <font-awesome-icon icon="sync" :spin="isLoading"></font-awesome-icon>
          <span>{{ t$('userManagement.home.refreshListLabel') }}</span>
        </button>
        <router-link custom v-slot="{ navigate }" :to="{ name: 'JhiUserCreate' }">
          <button @click="navigate" class="btn btn-primary jh-create-entity" data-cy="entityCreateButton">
            <font-awesome-icon icon="plus"></font-awesome-icon> <span>{{ t$('userManagement.home.createLabel') }}</span>
          </button>
        </router-link>
      </div>
    </section>

    <section class="dc-panel">
      <div class="dc-panel__body">
        <div class="dc-toolbar">
          <div class="dc-toolbar__group">
            <span class="dc-toolbar__meta">총 {{ totalItems }}명</span>
            <span class="dc-status-badge dc-status-badge--success">활성 {{ activeUserCount }}</span>
            <span class="dc-status-badge dc-status-badge--warning">비활성 {{ inactiveUserCount }}</span>
          </div>
        </div>

        <div class="dc-table-shell table-responsive" v-if="users">
          <table class="table align-middle" aria-describedby="Users">
            <thead>
              <tr>
                <th scope="col" @click="changeOrder('id')">
                  <span>{{ t$('global.field.id') }}</span>
                  <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
                </th>
                <th scope="col" @click="changeOrder('login')">
                  <span>{{ t$('userManagement.login') }}</span>
                  <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'login'"></jhi-sort-indicator>
                </th>
                <th scope="col" @click="changeOrder('email')">
                  <span>{{ t$('userManagement.email') }}</span>
                  <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'email'"></jhi-sort-indicator>
                </th>
                <th scope="col"><span>{{ t$('userManagement.activated') }}</span></th>
                <th scope="col" @click="changeOrder('langKey')">
                  <span>{{ t$('userManagement.langKey') }}</span>
                  <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'langKey'"></jhi-sort-indicator>
                </th>
                <th scope="col">
                  <span>{{ t$('userManagement.profiles') }}</span>
                </th>
                <th scope="col" @click="changeOrder('createdDate')">
                  <span>{{ t$('userManagement.createdDate') }}</span>
                  <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdDate'"></jhi-sort-indicator>
                </th>
                <th scope="col" id="modified-date-sort" @click="changeOrder('lastModifiedDate')">
                  <span>{{ t$('userManagement.lastModifiedDate') }}</span>
                  <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'lastModifiedDate'"></jhi-sort-indicator>
                </th>
                <th scope="col"></th>
              </tr>
            </thead>
            <tbody v-if="users">
              <tr v-for="user in users" :key="user.id" :id="user.login" data-cy="entityTable">
                <td>
                  <span class="text-muted small">{{ user.id }}</span>
                </td>
                <td>
                  <router-link :to="{ name: 'JhiUserView', params: { userId: user.login } }" class="text-decoration-none fw-bold text-primary">
                    {{ user.login }}
                  </router-link>
                  <div class="small text-muted mt-1">{{ user.lastModifiedBy || 'system' }}</div>
                </td>
                <td class="jhi-user-email">
                  <span class="fw-medium">{{ user.email }}</span>
                </td>
                <td>
                  <button class="btn btn-danger btn-sm dc-btn-compact deactivated" @click="setActive(user, true)" v-if="!user.activated">
                    {{ t$('userManagement.deactivated') }}
                  </button>
                  <button
                    class="btn btn-success btn-sm dc-btn-compact"
                    @click="setActive(user, false)"
                    v-if="user.activated"
                    :disabled="username === user.login"
                  >
                    {{ t$('userManagement.activated') }}
                  </button>
                </td>
                <td><span class="dc-chip">{{ user.langKey }}</span></td>
                <td>
                  <div class="d-flex flex-wrap gap-1">
                    <span v-for="authority of user.authorities" :key="authority" class="dc-chip">{{ authority }}</span>
                  </div>
                </td>
                <td><span class="small">{{ formatDate(user.createdDate) }}</span></td>
                <td><span class="small">{{ formatDate(user.lastModifiedDate) }}</span></td>
                <td class="text-end">
                  <div class="dc-table-actions">
                    <router-link :to="{ name: 'JhiUserView', params: { userId: user.login } }" custom v-slot="{ navigate }">
                      <button @click="navigate" class="btn btn-outline-secondary btn-sm dc-btn-compact details" data-cy="entityDetailsButton">
                        <font-awesome-icon icon="eye"></font-awesome-icon>
                        <span class="d-none d-md-inline">{{ t$('entity.action.view') }}</span>
                      </button>
                    </router-link>
                    <router-link :to="{ name: 'JhiUserEdit', params: { userId: user.login } }" custom v-slot="{ navigate }">
                      <button @click="navigate" class="btn btn-primary btn-sm dc-btn-compact edit" data-cy="entityEditButton">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                        <span class="d-none d-md-inline">{{ t$('entity.action.edit') }}</span>
                      </button>
                    </router-link>
                    <b-button
                      @click="prepareRemove(user)"
                      variant="danger"
                      class="btn btn-sm dc-btn-compact"
                      :disabled="username === user.login"
                      data-cy="entityDeleteButton"
                    >
                      <font-awesome-icon icon="times"></font-awesome-icon>
                      <span class="d-none d-md-inline">{{ t$('entity.action.delete') }}</span>
                    </b-button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <b-modal
      ref="removeUser"
      id="removeUser"
      :title="t$('entity.delete.title')"
      data-cy="userManagementDeleteDialogHeading"
    >
      <div class="modal-body">
        <p id="jhi-delete-user-heading">{{ t$('userManagement.delete.question', { login: removeId }) }}</p>
      </div>
      <template #footer>
        <div class="dc-modal-actions">
          <button type="button" class="btn btn-outline-secondary btn-sm" @click="closeDialog()">{{ t$('entity.action.cancel') }}</button>
          <button type="button" class="btn btn-danger btn-sm" id="confirm-delete-user" @click="deleteUser()" data-cy="entityConfirmDeleteButton">
            {{ t$('entity.action.delete') }}
          </button>
        </div>
      </template>
    </b-modal>

    <div class="dc-table-footer" v-show="users?.length > 0">
      <div class="dc-table-footer__count">
        <jhi-item-count :page="page" :total="queryCount" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="dc-pagination">
        <b-pagination size="sm" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./user-management.component.ts"></script>
