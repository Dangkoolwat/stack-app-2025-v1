<template>
  <div>
    <h2 id="page-heading" data-cy="TagHeading">
      <span v-text="t$('entities.tag.title')" id="tag-heading"></span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info btn-sm me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('userManagement.home.refreshListLabel')"></span>
        </button>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && tags && tags.length === 0">
      <span v-text="t$('entities.tag.messages.notFound')"></span>
    </div>
    <div class="table-responsive" v-if="tags && tags.length > 0">
      <table class="table table-striped" aria-describedby="Tags">
        <thead>
          <tr>
            <th scope="col"><span v-text="t$('entities.tag.form.id')"></span></th>
            <th scope="col"><span v-text="t$('entities.tag.form.name')"></span></th>
            <th scope="col"><span v-text="t$('entities.tag.form.usageCount')"></span></th>
            <th scope="col"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="tag in tags" :key="tag.id" data-cy="entityTable">
            <td>{{ tag.id }}</td>
            <td>{{ tag.name }}</td>
            <td>{{ tag.usageCount }}</td>
            <td class="text-end">
              <div class="btn-group">
                <b-button
                  v-b-modal.removeEntity
                  variant="danger"
                  class="btn btn-sm"
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
    <b-modal ref="removeEntity" id="removeEntity" :title="t$('entity.delete.title')" @ok="removeTag()">
      <div class="modal-body">
        <p id="jhi-delete-tag-heading" v-text="t$('entities.tag.messages.deleteConfirm', { id: removeId })"></p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary btn-sm me-2" v-text="t$('entities.tag.actions.cancel')" @click="removeTag()"></button>
          <button
            type="button"
            class="btn btn-primary btn-sm"
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
