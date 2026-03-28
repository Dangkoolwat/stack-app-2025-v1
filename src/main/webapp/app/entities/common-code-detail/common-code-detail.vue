<template>
  <div class="dc-page">
    <section class="dc-page-header">
      <div>
        <h2 id="page-heading" data-cy="CommonCodeDetailHeading" class="dc-page-header__title">
          <span v-text="t$('entities.commonCodeDetail.title')" id="common-code-detail-heading"></span>
        </h2>
        <p class="dc-page-header__subtitle" v-text="t$('entities.commonCodeDetail.labels.subtitle')"></p>
      </div>
      <div class="dc-page-actions">
        <button class="btn btn-outline-secondary" @click="handleSyncList" :disabled="isFetching || !selectedGroupCode">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('userManagement.home.refreshListLabel')"></span>
        </button>
        <router-link
          v-if="selectedGroupCode"
          :to="{ name: 'CommonCodeDetailCreate', query: { groupCode: selectedGroupCode } }"
          custom
          v-slot="{ navigate }"
        >
          <button @click="navigate" class="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="commonCodeDetailCreateButton">
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="t$('entities.commonCodeDetail.actions.create')"></span>
          </button>
        </router-link>
      </div>
    </section>

    <div class="alert alert-info mb-4" v-if="!selectedGroupCode">
      <font-awesome-icon icon="info-circle" class="me-2"></font-awesome-icon>
      <span v-text="t$('entities.commonCodeDetail.messages.selectGroup')"></span>
    </div>

    <section class="dc-panel">
      <div class="dc-panel__header px-4 pt-4">
        <div class="d-flex align-items-center">
          <label class="me-3 fw-bold text-nowrap mb-0" v-text="t$('entities.commonCodeDetail.labels.selectGroupLabel')"></label>
          <select class="form-select form-select-sm dc-select-compact" style="width: 240px" v-model="selectedGroupCode">
            <option :value="null" v-text="t$('entities.commonCodeDetail.labels.selectGroupPlaceholder')"></option>
            <option v-for="group in commonCodeGroups" :key="group.groupCode" :value="group.groupCode">
              {{ group.groupName }} ({{ group.groupCode }})
            </option>
          </select>
        </div>
      </div>

      <div class="dc-panel__body">
        <div class="dc-toolbar" v-if="selectedGroupCode">
          <div class="dc-toolbar__group">
            <span class="dc-toolbar__meta" v-text="t$('entities.commonCodeDetail.labels.totalItems', { count: commonCodeDetails?.length || 0 })"></span>
          </div>
        </div>

        <div class="dc-empty-state" v-if="selectedGroupCode && !isFetching && commonCodeDetails && commonCodeDetails.length === 0">
          <div class="dc-empty-state__title" v-text="t$('entities.commonCodeDetail.messages.notFound')"></div>
        </div>

        <div class="dc-table-shell table-responsive" v-if="commonCodeDetails && commonCodeDetails.length > 0">
          <table class="table align-middle" aria-describedby="CommonCodeDetails">
            <thead>
              <tr>
                <th scope="col"><span v-text="t$('entities.commonCodeDetail.form.id')"></span></th>
                <th scope="col"><span v-text="t$('entities.commonCodeDetail.form.code')"></span></th>
                <th scope="col"><span v-text="t$('entities.commonCodeDetail.form.name')"></span></th>
                <th scope="col"><span v-text="t$('entities.commonCodeDetail.form.sortOrder')"></span></th>
                <th scope="col"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="detail in commonCodeDetails" :key="detail.id" data-cy="entityTable">
                <td><span class="text-muted small">{{ detail.id }}</span></td>
                <td><span class="fw-bold">{{ detail.code }}</span></td>
                <td>{{ detail.name }}</td>
                <td>{{ detail.sortOrder }}</td>
                <td class="text-end">
                  <div class="dc-table-actions">
                    <router-link :to="{ name: 'CommonCodeDetailEdit', params: { id: detail.id } }" custom v-slot="{ navigate }">
                      <button @click="navigate" class="btn btn-primary btn-sm dc-btn-compact edit" data-cy="entityEditButton">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                        <span class="d-none d-md-inline" v-text="t$('entities.commonCodeDetail.actions.edit')"></span>
                      </button>
                    </router-link>
                    <b-button
                      v-b-modal.removeEntity
                      variant="danger"
                      class="btn btn-sm dc-btn-compact"
                      data-cy="entityDeleteButton"
                      @click="prepareRemove(detail)"
                    >
                      <font-awesome-icon icon="times"></font-awesome-icon>
                      <span class="d-none d-md-inline" v-text="t$('entities.commonCodeDetail.actions.delete')"></span>
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
        <p id="jhi-delete-commonCodeDetail-heading" v-text="t$('entities.commonCodeDetail.messages.deleteConfirm', { id: removeId })"></p>
      </div>
      <template #footer>
        <div class="dc-modal-actions">
          <button
            type="button"
            class="btn btn-outline-secondary btn-sm"
            v-text="t$('entities.commonCodeDetail.actions.cancel')"
            @click="closeDialog()"
          ></button>
          <button
            type="button"
            class="btn btn-danger btn-sm"
            id="jhi-confirm-delete-commonCodeDetail"
            data-cy="entityConfirmDeleteButton"
            v-text="t$('entities.commonCodeDetail.actions.delete')"
            @click="removeDetail()"
          ></button>
        </div>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./common-code-detail.component.ts"></script>
