<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="board">
        <h2 class="jh-entity-heading" data-cy="boardDetailsHeading">
          <span v-text="t$('entities.board.detail.title')"></span> {{ board.id }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="t$('entities.board.form.title')"></span>
          </dt>
          <dd>
            <span>{{ board.title }}</span>
          </dd>
          <dt>
            <span v-text="t$('entities.board.form.content')"></span>
          </dt>
          <dd>
            <span>{{ board.content }}</span>
          </dd>
          <dt>
            <span v-text="t$('entities.board.form.viewCount')"></span>
          </dt>
          <dd>
            <span>{{ board.viewCount }}</span>
          </dd>
          <dt>
            <span v-text="t$('entities.board.form.notice')"></span>
          </dt>
          <dd>
            <span>{{ board.notice }}</span>
          </dd>
          <dt>
            <span v-text="t$('entities.board.form.boardTypeCode')"></span>
          </dt>
          <dd>
            <span>{{ board.boardTypeCode }}</span>
          </dd>
          <dt>
            <span v-text="t$('entities.board.form.createdBy')"></span>
          </dt>
          <dd>
            <span>{{ board.createdBy }}</span>
          </dd>
          <dt>
            <span v-text="t$('entities.board.form.createdDate')"></span>
          </dt>
          <dd>
            <span>{{ board.createdDate ? $d(board.createdDate, 'long') : '' }}</span>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info btn-sm" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="t$('entities.board.actions.back')"></span>
        </button>
        <router-link v-if="board.id" :to="{ name: 'BoardEdit', params: { id: board.id } }" custom v-slot="{ navigate }">
          <button @click="navigate" class="btn btn-primary btn-sm">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="t$('entities.board.actions.edit')"></span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./board-detail.component.ts"></script>
