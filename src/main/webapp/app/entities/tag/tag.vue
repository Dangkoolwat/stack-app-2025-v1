<template>
  <div class="dc-page">
    <section class="dc-page-header">
      <div>
        <h2 id="page-heading" data-cy="TagHeading" class="dc-page-header__title">
          <span v-text="t$('entities.tag.title')" id="tag-heading"></span>
        </h2>
        <p class="dc-page-header__subtitle" v-text="t$('entities.tag.labels.subtitle')"></p>
      </div>
      <div class="dc-page-actions">
        <button class="btn btn-outline-secondary" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('userManagement.home.refreshListLabel')"></span>
        </button>
      </div>
    </section>

    <section class="dc-panel">
      <div class="dc-panel__body">
        <div class="dc-toolbar">
          <div class="dc-toolbar__group">
            <span class="dc-toolbar__meta" v-text="t$('entities.tag.labels.totalItems', { count: tags?.length || 0 })"></span>
          </div>
        </div>

        <div class="dc-empty-state" v-if="!isFetching && tags && tags.length === 0">
          <div class="dc-empty-state__title" v-text="t$('entities.tag.messages.notFound')"></div>
        </div>

        <div class="dc-table-shell table-responsive" v-if="tags && tags.length > 0">
          <table class="table align-middle" aria-describedby="Tags">
            <thead>
              <tr>
                <th scope="col" style="width: 80px"><span v-text="t$('entities.tag.form.id')"></span></th>
                <th scope="col"><span v-text="t$('entities.tag.form.name')"></span></th>
                <th scope="col" style="width: 150px"><span v-text="t$('entities.tag.form.usageCount')"></span></th>
                <th scope="col"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="tag in tags" :key="tag.id" data-cy="entityTable">
                <td><span class="text-muted small">{{ tag.id }}</span></td>
                <td><span class="dc-chip dc-chip--primary">{{ tag.name }}</span></td>
                <td><span class="fw-semibold">{{ tag.usageCount }}</span></td>
                <td class="text-end">
                  <div class="dc-table-actions">
                    <b-button
                      v-b-modal.removeEntity
                      variant="danger"
                      class="btn btn-sm dc-btn-compact"
                      data-cy="entityDeleteButton"
                      @click="prepareRemove(tag)"
                    >
                      <font-awesome-icon icon="times"></font-awesome-icon>
                      <span class="d-none d-md-inline" v-text="t$('entities.tag.actions.delete')"></span>
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
        <p id="jhi-delete-tag-heading" v-text="t$('entities.tag.messages.deleteConfirm', { id: removeId })"></p>
      </div>
      <template #footer>
        <div class="dc-modal-actions">
          <button
            type="button"
            class="btn btn-outline-secondary btn-sm"
            v-text="t$('entities.tag.actions.cancel')"
            @click="closeDialog()"
          ></button>
          <button
            type="button"
            class="btn btn-danger btn-sm"
            id="jhi-confirm-delete-tag"
            data-cy="entityConfirmDeleteButton"
            v-text="t$('entities.tag.actions.delete')"
            @click="removeTag()"
          ></button>
        </div>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./tag.component.ts"></script>
