<template>
  <div>
    <h2 id="page-heading" data-cy="BoardResourceManagementHeading">
      <span>{{ t$('stackApp.boardResourceManagement.home.title') }}</span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info mr-2" @click="loadAll" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span>{{ t$('stackApp.boardResourceManagement.home.refreshOptions') }}</span>
        </button>
      </div>
    </h2>

    <br />

    <!-- 안내 메시지 -->
    <div class="alert alert-warning">
      <font-awesome-icon icon="exclamation-triangle"></font-awesome-icon>
      <span v-html="t$('stackApp.boardResourceManagement.home.warningMessage')"></span>
    </div>

    <div class="row align-items-center mb-3">
      <div class="col-md-4">
        <label for="resource-type" class="mr-2 font-weight-bold">{{ t$('stackApp.boardResourceManagement.home.searchTarget') }}</label>
        <select id="resource-type" class="form-control" v-model="resourceType" @change="changeType">
          <option value="uploads">{{ t$('stackApp.boardResourceManagement.home.options.uploads') }}</option>
          <option value="tags">{{ t$('stackApp.boardResourceManagement.home.options.tags') }}</option>
          <option value="comments">{{ t$('stackApp.boardResourceManagement.home.options.comments') }}</option>
        </select>
      </div>
      <div class="col-md-8 text-right d-flex justify-content-end align-items-end">
        <button class="btn btn-danger" :disabled="selectedIds.length === 0" @click="prepareRemove">
          <font-awesome-icon icon="trash"></font-awesome-icon> <span>{{ t$('stackApp.boardResourceManagement.home.deleteSelected') }}</span>
        </button>
      </div>
    </div>

    <div class="table-responsive" v-if="resources && resources.length > 0">
      <table class="table table-striped" aria-describedby="page-heading">
        <thead>
          <tr>
            <th scope="col"><input type="checkbox" @change="toggleSelectAll" :checked="selectedIds.length === resources.length" /></th>
            <th scope="col">
              <span>{{ t$('stackApp.boardResourceManagement.home.table.id') }}</span>
            </th>
            <th scope="col">
              <span>{{ t$('stackApp.boardResourceManagement.home.table.details') }}</span>
            </th>
            <th scope="col">
              <span>{{ t$('stackApp.boardResourceManagement.home.table.date') }}</span>
            </th>
            <th scope="col">
              <span>{{ t$('stackApp.boardResourceManagement.home.table.status') }}</span>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="resource in resources" :key="resource.id">
            <td><input type="checkbox" :value="resource.id" v-model="selectedIds" /></td>
            <td>{{ resource.id }}</td>
            <td>
              <span v-if="resourceType === 'uploads'"
                >{{ resource.sourceFilename
                }}{{ t$('stackApp.boardResourceManagement.home.detailsMap.uploads', { size: Math.round(resource.fileSize / 1024) }) }}</span
              >
              <span v-else-if="resourceType === 'tags'"
                >{{ resource.name }}{{ t$('stackApp.boardResourceManagement.home.detailsMap.tags', { count: resource.usageCount }) }}</span
              >
              <span v-else-if="resourceType === 'comments'">{{ resource.content }}</span>
            </td>
            <td>{{ resource.createdDate || resource.created_date || 'N/A' }}</td>
            <td>
              <span class="badge bg-danger" v-if="resource.deleted">{{
                t$('stackApp.boardResourceManagement.home.statusMap.softDeleted')
              }}</span>
              <span class="badge bg-warning" v-else>{{ t$('stackApp.boardResourceManagement.home.statusMap.orphaned') }}</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-else class="alert alert-info" v-show="!isFetching">
      <span>{{ t$('stackApp.boardResourceManagement.home.noData') }}</span>
    </div>

    <b-modal ref="removeModal" id="removeModal" :title="t$('stackApp.boardResourceManagement.home.modal.title')">
      <div class="modal-body">
        <p v-html="t$('stackApp.boardResourceManagement.home.modal.body1', { count: selectedIds.length })"></p>
        <p style="color: red; font-weight: bold">{{ t$('stackApp.boardResourceManagement.home.modal.body2') }}</p>
      </div>
      <template #footer>
        <div>
          <button type="button" class="btn btn-secondary" @click="closeDialog()">
            {{ t$('stackApp.boardResourceManagement.home.modal.cancelText') }}
          </button>
          <button type="button" class="btn btn-danger" @click="removeResources()">
            {{ t$('stackApp.boardResourceManagement.home.modal.okText') }}
          </button>
        </div>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./board-resource-management.component.ts"></script>
