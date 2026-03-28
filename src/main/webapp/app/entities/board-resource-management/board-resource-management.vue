<template>
  <div class="dc-page">
    <section class="dc-page-header">
      <div>
        <h2 id="page-heading" data-cy="BoardResourceManagementHeading" class="dc-page-header__title">
          <span>{{ t$('stackApp.boardResourceManagement.home.title') }}</span>
        </h2>
        <p class="dc-page-header__subtitle">사용되지 않거나 소프트 삭제된 리소스를 전수 조사하여 완전히 제거함으로써 시스템 저장 공간을 최적화합니다.</p>
      </div>

      <div class="dc-page-actions">
        <button class="btn btn-outline-secondary" @click="loadAll" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span>{{ t$('stackApp.boardResourceManagement.home.refreshOptions') }}</span>
        </button>
      </div>
    </section>

    <!-- 안내 메시지 -->
    <div class="alert alert-warning mb-4">
      <font-awesome-icon icon="exclamation-triangle" class="me-2"></font-awesome-icon>
      <span v-html="t$('stackApp.boardResourceManagement.home.warningMessage')"></span>
    </div>

    <section class="dc-panel">
      <div class="dc-panel__header px-4 pt-4">
        <div class="row align-items-center">
          <div class="col-md-5 d-flex align-items-center">
            <label for="resource-type" class="me-3 fw-bold text-nowrap mb-0">{{ t$('stackApp.boardResourceManagement.home.searchTarget') }}</label>
            <select id="resource-type" class="form-select form-select-sm dc-select-compact" v-model="resourceType" @change="changeType">
              <option value="uploads">{{ t$('stackApp.boardResourceManagement.home.options.uploads') }}</option>
              <option value="tags">{{ t$('stackApp.boardResourceManagement.home.options.tags') }}</option>
              <option value="comments">{{ t$('stackApp.boardResourceManagement.home.options.comments') }}</option>
            </select>
          </div>
          <div class="col-md-7 text-end d-flex justify-content-end gap-2">
            <button class="btn btn-danger btn-sm px-3" :disabled="selectedIds.length === 0" @click="prepareRemove">
              <font-awesome-icon icon="trash" class="me-1"></font-awesome-icon> <span>{{ t$('stackApp.boardResourceManagement.home.deleteSelected') }}</span>
            </button>
          </div>
        </div>
      </div>

      <div class="dc-panel__body">
        <div class="dc-toolbar">
          <div class="dc-toolbar__group">
            <span class="dc-toolbar__meta">총 {{ resources?.length || 0 }}건 선택됨: {{ selectedIds.length }}건</span>
          </div>
        </div>

        <div class="dc-table-shell table-responsive" v-if="resources && resources.length > 0">
          <table class="table align-middle" aria-describedby="page-heading">
            <thead>
              <tr>
                <th scope="col" style="width: 40px">
                  <input type="checkbox" class="form-check-input" @change="toggleSelectAll" :checked="selectedIds.length === resources.length" />
                </th>
                <th scope="col" style="width: 80px">
                  <span>{{ t$('stackApp.boardResourceManagement.home.table.id') }}</span>
                </th>
                <th scope="col">
                  <span>{{ t$('stackApp.boardResourceManagement.home.table.details') }}</span>
                </th>
                <th scope="col" style="width: 180px">
                  <span>{{ t$('stackApp.boardResourceManagement.home.table.date') }}</span>
                </th>
                <th scope="col" style="width: 120px">
                  <span>{{ t$('stackApp.boardResourceManagement.home.table.status') }}</span>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="resource in resources" :key="resource.id">
                <td><input type="checkbox" class="form-check-input" :value="resource.id" v-model="selectedIds" /></td>
                <td><span class="text-muted small">{{ resource.id }}</span></td>
                <td>
                  <span v-if="resourceType === 'uploads'" class="fw-semibold">
                    {{ resource.sourceFilename }}{{ t$('stackApp.boardResourceManagement.home.detailsMap.uploads', { size: Math.round(resource.fileSize / 1024) }) }}
                  </span>
                  <span v-else-if="resourceType === 'tags'" class="fw-semibold">
                    {{ resource.name }}{{ t$('stackApp.boardResourceManagement.home.detailsMap.tags', { count: resource.usageCount }) }}
                  </span>
                  <span v-else-if="resourceType === 'comments'" class="small text-muted">{{ resource.content }}</span>
                </td>
                <td><span class="small">{{ resource.createdDate || resource.created_date || 'N/A' }}</span></td>
                <td>
                  <span class="dc-status-badge dc-status-badge--danger" v-if="resource.deleted">{{
                    t$('stackApp.boardResourceManagement.home.statusMap.softDeleted')
                  }}</span>
                  <span class="dc-status-badge dc-status-badge--warning" v-else>{{ t$('stackApp.boardResourceManagement.home.statusMap.orphaned') }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="dc-empty-state" v-show="!isFetching">
          <div class="dc-empty-state__title">{{ t$('stackApp.boardResourceManagement.home.noData') }}</div>
        </div>
      </div>
    </section>

    <b-modal ref="removeModal" id="removeModal" :title="t$('stackApp.boardResourceManagement.home.modal.title')">
      <div class="modal-body">
        <p v-html="t$('stackApp.boardResourceManagement.home.modal.body1', { count: selectedIds.length })"></p>
        <p class="text-danger fw-bold mb-0">{{ t$('stackApp.boardResourceManagement.home.modal.body2') }}</p>
      </div>
      <template #footer>
        <div class="dc-modal-actions">
          <button type="button" class="btn btn-outline-secondary btn-sm" @click="closeDialog()">
            {{ t$('stackApp.boardResourceManagement.home.modal.cancelText') }}
          </button>
          <button type="button" class="btn btn-danger btn-sm" @click="removeResources()">
            {{ t$('stackApp.boardResourceManagement.home.modal.okText') }}
          </button>
        </div>
      </template>
    </b-modal>
  </div>
</template>

<script lang="ts" src="./board-resource-management.component.ts"></script>
