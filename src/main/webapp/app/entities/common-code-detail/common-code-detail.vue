<template>
  <div>
    <h2 id="page-heading" data-cy="CommonCodeDetailHeading">
      <span v-text="t$('entities.commonCodeDetail.title')" id="common-code-detail-heading"></span>
      <div class="d-flex justify-content-end">
        <select class="form-control me-2" style="width: 200px" v-model="selectedGroupCode">
          <option :value="null">-- Select Group --</option>
          <option
            v-for="group in commonCodeGroups"
            :key="group.groupCode"
            :value="group.groupCode"
          >
            {{ group.groupName }} ({{ group.groupCode }})
          </option>
        </select>
        <button class="btn btn-info btn-sm me-2" @click="handleSyncList" :disabled="isFetching || !selectedGroupCode">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('userManagement.home.refreshListLabel')"></span>
        </button>
        <router-link
          v-if="selectedGroupCode"
          :to="{ name: 'CommonCodeDetailCreate', query: { groupCode: selectedGroupCode } }"
          custom
          v-slot="{ navigate }"
        >
          <button @click="navigate" class="btn btn-primary btn-sm jh-create-entity" id="jh-create-entity" data-cy="commonCodeDetailCreateButton">
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="t$('entities.commonCodeDetail.actions.create')"></span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && commonCodeDetails && commonCodeDetails.length === 0">
      <span v-text="t$('entities.commonCodeDetail.messages.notFound')"></span>
    </div>
    <div class="table-responsive" v-if="commonCodeDetails && commonCodeDetails.length > 0">
      <table class="table table-striped" aria-describedby="CommonCodeDetails">
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
            <td>{{ detail.id }}</td>
            <td>{{ detail.code }}</td>
            <td>{{ detail.name }}</td>
            <td>{{ detail.sortOrder }}</td>
            <td class="text-end">
              <div class="btn-group">
                <router-link
                  :to="{ name: 'CommonCodeDetailEdit', params: { id: detail.id } }"
                  custom
                  v-slot="{ navigate }"
                >
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entities.commonCodeDetail.actions.edit')"></span>
                  </button>
                </router-link>
                <b-button
                  v-b-modal.removeEntity
                  variant="danger"
                  class="btn btn-sm"
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
    <b-modal ref="removeEntity" id="removeEntity" :title="t$('entity.delete.title')" @ok="removeDetail()">
      <div class="modal-body">
        <p id="jhi-delete-commonCodeDetail-heading" v-text="t$('entities.commonCodeDetail.messages.deleteConfirm', { id: removeId })"></p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary btn-sm me-2" v-text="t$('entities.commonCodeDetail.actions.cancel')" @click="removeDetail()"></button>
          <button
            type="button"
            class="btn btn-primary btn-sm"
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
