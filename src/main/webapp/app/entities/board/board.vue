<template>
  <div class="dc-page">
    <section class="dc-page-header">
      <div>
        <h2 id="page-heading" data-cy="BoardHeading" class="dc-page-header__title">
          <span v-text="t$('entities.board.title')" id="board-heading"></span>
        </h2>
        <p class="dc-page-header__subtitle" v-text="t$('entities.board.labels.subtitle')"></p>
      </div>
      <div class="dc-page-actions">
        <button class="btn btn-outline-secondary" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('userManagement.home.refreshListLabel')"></span>
        </button>
        <router-link :to="{ name: 'BoardCreate' }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="boardCreateButton">
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="t$('entities.board.actions.create')"></span>
          </button>
        </router-link>
      </div>
    </section>

    <section class="dc-panel">
      <div class="dc-panel__body">
        <div class="dc-toolbar">
          <div class="dc-toolbar__group">
            <span class="dc-toolbar__meta" v-text="t$('entities.board.labels.totalItems', { count: totalItems })"></span>
            <span class="dc-toolbar__meta" v-text="t$('entities.board.labels.sortOrder', { order: propOrder })"></span>
          </div>
          <div class="dc-toolbar__group">
            <span class="dc-status-badge dc-status-badge--success" v-text="t$('entities.board.labels.liveList')"></span>
          </div>
        </div>

        <div class="dc-empty-state" v-if="!isFetching && boards && boards.length === 0">
          <div class="dc-empty-state__title" v-text="t$('entities.board.messages.notFound')"></div>
          <p class="mb-0 mt-2 text-muted" v-text="t$('entities.board.labels.emptySubtitle')"></p>
        </div>

        <div class="dc-table-shell table-responsive" v-if="boards && boards.length > 0">
          <table class="table align-middle" aria-describedby="Boards">
            <thead>
              <tr>
                <th scope="col" @click="changeOrder('id')">
                  <span v-text="t$('entities.board.form.id')"></span>
                  <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'id'"></jhi-sort-indicator>
                </th>
                <th scope="col" @click="changeOrder('title')">
                  <span v-text="t$('entities.board.form.title')"></span>
                  <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'title'"></jhi-sort-indicator>
                </th>
                <th scope="col" @click="changeOrder('viewCount')">
                  <span v-text="t$('entities.board.form.viewCount')"></span>
                  <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'viewCount'"></jhi-sort-indicator>
                </th>
                <th scope="col" @click="changeOrder('notice')">
                  <span v-text="t$('entities.board.form.notice')"></span>
                  <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'notice'"></jhi-sort-indicator>
                </th>
                <th scope="col" @click="changeOrder('boardTypeCode')">
                  <span v-text="t$('entities.board.form.boardTypeCode')"></span>
                  <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'boardTypeCode'"></jhi-sort-indicator>
                </th>
                <th scope="col" @click="changeOrder('createdDate')">
                  <span v-text="t$('entities.board.form.createdDate')"></span>
                  <jhi-sort-indicator :current-order="propOrder" :reverse="reverse" :field-name="'createdDate'"></jhi-sort-indicator>
                </th>
                <th scope="col"></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="board in boards" :key="board.id" data-cy="entityTable">
                <td>
                  <span class="text-muted small">{{ board.id }}</span>
                </td>
                <td>
                  <router-link :to="{ name: 'BoardView', params: { id: board.id } }" class="text-decoration-none fw-bold text-primary">
                    {{ board.title }}
                  </router-link>
                  <div class="small text-muted mt-1">{{ t$('entities.board.labels.createdDateLabel') }} {{ board.createdDate ? $d(board.createdDate, 'short') : '-' }}</div>
                </td>
                <td>
                  <span class="fw-semibold">{{ board.viewCount }}</span>
                </td>
                <td>
                  <span :class="board.notice ? 'dc-status-badge dc-status-badge--warning' : 'dc-status-badge'">
                    {{ board.notice ? t$('entities.board.labels.statusNotice') : t$('entities.board.labels.statusNormal') }}
                  </span>
                </td>
                <td>
                  <span class="dc-chip">{{ board.boardTypeCode || '-' }}</span>
                </td>
                <td>{{ board.createdDate ? $d(board.createdDate, 'short') : '-' }}</td>
                <td class="text-end">
                  <div class="dc-table-actions">
                    <router-link :to="{ name: 'BoardView', params: { id: board.id } }" custom v-slot="{ navigate }">
                      <button
                        @click="navigate"
                        class="btn btn-outline-secondary btn-sm dc-btn-compact details"
                        data-cy="entityDetailsButton"
                      >
                        <font-awesome-icon icon="eye"></font-awesome-icon>
                        <span class="d-none d-md-inline" v-text="t$('entities.board.actions.view')"></span>
                      </button>
                    </router-link>
                    <router-link :to="{ name: 'BoardEdit', params: { id: board.id } }" custom v-slot="{ navigate }">
                      <button @click="navigate" class="btn btn-primary btn-sm dc-btn-compact edit" data-cy="entityEditButton">
                        <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                        <span class="d-none d-md-inline" v-text="t$('entities.board.actions.edit')"></span>
                      </button>
                    </router-link>
                    <b-button
                      v-b-modal.removeEntity
                      variant="danger"
                      class="btn btn-sm dc-btn-compact"
                      data-cy="entityDeleteButton"
                      @click="prepareRemove(board)"
                    >
                      <font-awesome-icon icon="times"></font-awesome-icon>
                      <span class="d-none d-md-inline" v-text="t$('entities.board.actions.delete')"></span>
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
        <p id="jhi-delete-board-heading" v-text="t$('entities.board.messages.deleteConfirm', { id: removeId })"></p>
      </div>
      <template #footer>
        <div class="dc-modal-actions">
          <button
            type="button"
            class="btn btn-outline-secondary btn-sm"
            v-text="t$('entities.board.actions.cancel')"
            @click="closeDialog()"
          ></button>
          <button
            type="button"
            class="btn btn-danger btn-sm"
            id="jhi-confirm-delete-board"
            data-cy="entityConfirmDeleteButton"
            v-text="t$('entities.board.actions.delete')"
            @click="removeBoard()"
          ></button>
        </div>
      </template>
    </b-modal>
    <div class="dc-table-footer" v-show="boards && boards.length > 0">
      <div class="dc-table-footer__count">
        <jhi-item-count :page="page" :total="totalItems" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="dc-pagination">
        <b-pagination size="sm" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./board.component.ts"></script>
