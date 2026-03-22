<template>
  <div>
    <h2 id="page-heading" data-cy="BoardHeading">
      <span v-text="t$('entities.board.title')" id="board-heading"></span>
      <div class="d-flex justify-content-end">
        <button class="btn btn-info btn-sm me-2" @click="handleSyncList" :disabled="isFetching">
          <font-awesome-icon icon="sync" :spin="isFetching"></font-awesome-icon>
          <span v-text="t$('userManagement.home.refreshListLabel')"></span>
        </button>
        <router-link :to="{ name: 'BoardCreate' }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary btn-sm jh-create-entity" id="jh-create-entity" data-cy="boardCreateButton">
            <font-awesome-icon icon="plus"></font-awesome-icon>
            <span v-text="t$('entities.board.actions.create')"></span>
          </button>
        </router-link>
      </div>
    </h2>
    <br />
    <div class="alert alert-warning" v-if="!isFetching && boards && boards.length === 0">
      <span v-text="t$('entities.board.messages.notFound')"></span>
    </div>
    <div class="table-responsive" v-if="boards && boards.length > 0">
      <table class="table table-striped" aria-describedby="Boards">
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
            <td>{{ board.id }}</td>
            <td>
              <router-link :to="{ name: 'BoardView', params: { id: board.id } }" class="text-decoration-none fw-bold text-primary">
                {{ board.title }}
              </router-link>
            </td>
            <td>{{ board.viewCount }}</td>
            <td>{{ board.notice }}</td>
            <td>{{ board.boardTypeCode }}</td>
            <td>{{ board.createdDate ? $d(board.createdDate, 'short') : '' }}</td>
            <td class="text-end">
              <div class="btn-group">
                <router-link :to="{ name: 'BoardView', params: { id: board.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-info btn-sm details" data-cy="entityDetailsButton">
                    <font-awesome-icon icon="eye"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entities.board.actions.view')"></span>
                  </button>
                </router-link>
                <router-link :to="{ name: 'BoardEdit', params: { id: board.id } }" custom v-slot="{ navigate }">
                  <button @click="navigate" class="btn btn-primary btn-sm edit" data-cy="entityEditButton">
                    <font-awesome-icon icon="pencil-alt"></font-awesome-icon>
                    <span class="d-none d-md-inline" v-text="t$('entities.board.actions.edit')"></span>
                  </button>
                </router-link>
                <b-button
                  v-b-modal.removeEntity
                  variant="danger"
                  class="btn btn-sm"
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
    <b-modal ref="removeEntity" id="removeEntity" :title="t$('entity.delete.title')">
      <div class="modal-body">
        <p id="jhi-delete-board-heading" v-text="t$('entities.board.messages.deleteConfirm', { id: removeId })"></p>
      </div>
      <template #footer>
        <div>
          <button
            type="button"
            class="btn btn-secondary btn-sm"
            v-text="t$('entities.board.actions.cancel')"
            @click="closeDialog()"
          ></button>
          <button
            type="button"
            class="btn btn-primary btn-sm"
            id="jhi-confirm-delete-board"
            data-cy="entityConfirmDeleteButton"
            v-text="t$('entities.board.actions.delete')"
            @click="removeBoard()"
          ></button>
        </div>
      </template>
    </b-modal>
    <div v-show="boards && boards.length > 0">
      <div class="row justify-content-center">
        <jhi-item-count :page="page" :total="totalItems" :items-per-page="itemsPerPage"></jhi-item-count>
      </div>
      <div class="row justify-content-center">
        <b-pagination size="sm" :total-rows="totalItems" v-model="page" :per-page="itemsPerPage" :change="loadPage(page)"></b-pagination>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./board.component.ts"></script>
