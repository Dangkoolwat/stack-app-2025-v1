<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate @submit.prevent="save()">
        <h2 id="board-heading" data-cy="BoardCreateUpdateHeading" v-text="t$('entities.board.actions.create')"></h2>
        <div>
          <div class="form-group" v-if="board.id">
            <label for="id" v-text="t$('entities.board.form.id')"></label>
            <input type="text" class="form-control" id="id" name="id" v-model="board.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.board.form.title')" for="board-title"></label>
            <input
              type="text"
              class="form-control"
              name="title"
              id="board-title"
              data-cy="title"
              :class="{ 'is-invalid': false }"
              v-model="board.title"
              required
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.board.form.content')" for="board-content"></label>
            <textarea
              class="form-control"
              name="content"
              id="board-content"
              data-cy="content"
              :class="{ 'is-invalid': false }"
              v-model="board.content"
              required
            ></textarea>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.board.form.boardTypeCode')" for="board-type"></label>
            <select class="form-control" id="board-type" data-cy="boardType" name="boardType" v-model="board.boardTypeCode" required>
              <option v-if="!board.boardTypeCode" :value="null"></option>
              <option :value="boardTypeOption.code" v-for="boardTypeOption in boardTypes" :key="boardTypeOption.id">
                {{ boardTypeOption.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.board.form.notice')" for="board-notice"></label>
            <input
              type="checkbox"
              class="form-check"
              name="notice"
              id="board-notice"
              data-cy="notice"
              :class="{ 'is-invalid': false }"
              v-model="board.notice"
            />
          </div>
        </div>
        <div class="mt-3">
          <button
            type="button"
            id="cancel-save"
            data-cy="entityCreateCancelButton"
            class="btn btn-secondary btn-sm me-2"
            @click="previousState()"
          >
            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="t$('entities.board.actions.cancel')"></span>
          </button>
          <button type="submit" id="save-entity" data-cy="entityCreateSaveButton" :disabled="isSaving" class="btn btn-primary btn-sm">
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="t$('entities.board.actions.save')"></span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts" src="./board-update.component.ts"></script>
