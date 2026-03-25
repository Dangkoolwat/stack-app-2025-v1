<template>
  <div>
    <h2 id="page-heading" data-cy="CommonCodeGroupHeading">
      <span v-text="t$('entities.commonCodeGroup.title')" id="common-code-group-heading"></span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info btn-sm me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('userManagement.home.refreshListLabel')"></span>
        </button>
        <router-link :to="{ name: 'CommonCodeGroupCreate' }" custom v-slot="{ navigate }">
          <button
            @click="navigate"
            class="btn btn-primary btn-sm jh-create-entity"
            id="jh-create-entity"
            data-cy="commonCodeGroupCreateButton"
          >
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="t$('entities.commonCodeGroup.actions.create')"></span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && commonCodeGroups && commonCodeGroups.length === 0">
      <span v-text="t$('entities.commonCodeGroup.messages.notFound')"></span>
    </div>
    <div class="table-responsive" v-if="commonCodeGroups && commonCodeGroups.length > 0">
      <table class="table table-striped" aria-describedby="CommonCodeGroups">
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
              <router-link :to="{ name: 'CommonCodeGroupView', params: { groupCode: commonCodeGroup.groupCode } }">{{
                commonCodeGroup.groupCode
              }}</router-link>
            </td>
            <td>{{ commonCodeGroup.groupName }}</td>
            <td>{{ commonCodeGroup.displayOrder }}</td>
            <td>{{ commonCodeGroup.description }}</td>
            <td class="text-end">
              <div class="btn-group">
                <router-link
                  :to="{ name: 'CommonCodeGroupView', params: { groupCode: commonCodeGroup.groupCode } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entities.commonCodeGroup.actions.view')"></span>
                  </button>
                </router-link>
                <router-link
                  :to="{ name: 'CommonCodeGroupEdit', params: { groupCode: commonCodeGroup.groupCode } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entities.commonCodeGroup.actions.edit')"></span>
                  </button>
                </router-link>
                <b-button
                  v-b-modal.removeEntity
                  variant="danger"
                  class="btn btn-sm"
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
    <b-modal ref="removeEntity" id="removeEntity" :title="t$('entity.delete.title')">
      <div class="modal-body">
        <p id="jhi-delete-commonCodeGroup-heading" v-text="t$('entities.commonCodeGroup.messages.deleteConfirm', { id: removeId })"></p>
      </div>
      <template #footer>
        <div>
          <button
            type="button"
            class="btn btn-secondary btn-sm"
            v-text="t$('entities.commonCodeGroup.actions.cancel')"
            @click="closeDialog()"
          ></button>
          <button
            type="button"
            class="btn btn-primary btn-sm"
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
