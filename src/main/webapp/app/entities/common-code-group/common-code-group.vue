<template>
  <div class="dc-page">
    <section class="dc-page-header">
      <div>
        <h2 id="page-heading" data-cy="CommonCodeGroupHeading" class="dc-page-header__title">
          <span v-text="t$('entities.commonCodeGroup.title')" id="common-code-group-heading"></span>
        </h2>
        <p class="dc-page-header__subtitle" v-text="t$('entities.commonCodeGroup.labels.subtitle')"></p>
      </div>
      <div class="dc-page-actions">
        <button class="btn btn-outline-secondary" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('userManagement.home.refreshListLabel')"></span>
        </button>
        <router-link :to="{ name: 'CommonCodeGroupCreate' }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="commonCodeGroupCreateButton">
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="t$('entities.commonCodeGroup.actions.create')"></span>
          </button>
        </router-link>
      </div>
    </section>

    <section class="dc-panel">
      <div class="dc-panel__body">
        <div class="dc-toolbar">
          <div class="dc-toolbar__group">
            <span class="dc-toolbar__meta" v-text="t$('entities.commonCodeGroup.labels.totalItems', { count: commonCodeGroups?.length || 0 })"></span>
          </div>
        </div>

        <div class="dc-empty-state" v-if="!isFetching && commonCodeGroups && commonCodeGroups.length === 0">
          <div class="dc-empty-state__title" v-text="t$('entities.commonCodeGroup.messages.notFound')"></div>
        </div>

        <div class="dc-table-shell table-responsive" v-if="commonCodeGroups && commonCodeGroups.length > 0">
          <table class="table align-middle" aria-describedby="CommonCodeGroups">
            <thead>
              <tr>
                <th scope="col"><span v-text="t$('entities.commonCodeGroup.form.groupCode')"></span></th>
                <th scope="col"><span v-text="t$('entities.commonCodeGroup.form.groupName')"></span></th>
                <th scope="col"><span v-text="t$('entities.commonCodeGroup.form.displayOrder')"></span></th>
                <th scope="col"><span v-text="t$('entities.commonCodeGroup.form.description')"></span></th>
                <th scope="col"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="commonCodeGroup in commonCodeGroups" :key="commonCodeGroup.groupCode" data-cy="entityTable">
                <td>
                  <router-link :to="{ name: 'CommonCodeGroupView', params: { groupCode: commonCodeGroup.groupCode } }" class="text-decoration-none fw-bold text-primary">
                    {{ commonCodeGroup.groupCode }}
                  </router-link>
                </td>
                <td><span class="fw-semibold">{{ commonCodeGroup.groupName }}</span></td>
                <td>{{ commonCodeGroup.displayOrder }}</td>
                <td><span class="text-muted small">{{ commonCodeGroup.description }}</span></td>
                <td class="text-end">
                  <div class="dc-table-actions">
                    <router-link :to="{ name: 'CommonCodeGroupView', params: { groupCode: commonCodeGroup.groupCode } }" custom v-slot="{ navigate }">
                      <button @click="navigate" class="btn btn-outline-secondary btn-sm dc-btn-compact details" data-cy="entityDetailsButton">
                        <font-awesome-icon icon="eye"></font-awesome-icon>
                        <span class="d-none d-md-inline" v-text="t$('entities.commonCodeGroup.actions.view')"></span>
                      </button>
                    </router-link>
                    <router-link :to="{ name: 'CommonCodeGroupEdit', params: { groupCode: commonCodeGroup.groupCode } }" custom v-slot="{ navigate }">
                      <button @click="navigate" class="btn btn-primary btn-sm dc-btn-compact edit" data-cy="entityEditButton">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                        <span class="d-none d-md-inline" v-text="t$('entities.commonCodeGroup.actions.edit')"></span>
                      </button>
                    </router-link>
                    <b-button
                      v-b-modal.removeEntity
                      variant="danger"
                      class="btn btn-sm dc-btn-compact"
                      data-cy="entityDeleteButton"
                      @click="prepareRemove(commonCodeGroup)"
                    >
                      <font-awesome-icon icon="times"></font-awesome-icon>
                      <span class="d-none d-md-inline" v-text="t$('entities.commonCodeGroup.actions.delete')"></span>
                    </b-button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <b-modal ref="removeEntity" id="removeEntity" :title="t$('entity.delete.title')">
      <div class="modal-body">
        <p id="jhi-delete-commonCodeGroup-heading" v-text="t$('entities.commonCodeGroup.messages.deleteConfirm', { id: removeId })"></p>
      </div>
      <template #footer>
        <div class="dc-modal-actions">
          <button
            type="button"
            class="btn btn-outline-secondary btn-sm"
            v-text="t$('entities.commonCodeGroup.actions.cancel')"
            @click="closeDialog()"
          ></button>
          <button
            type="button"
            class="btn btn-danger btn-sm"
            id="jhi-confirm-delete-commonCodeGroup"
            data-cy="entityConfirmDeleteButton"
            v-text="t$('entities.commonCodeGroup.actions.delete')"
            @click="removeGroup()"
          ></button>
        </div>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./common-code-group.component.ts"></script>
