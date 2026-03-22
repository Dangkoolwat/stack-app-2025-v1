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
            <span class="fw-bold fs-5">{{ board.title }}</span>
          </dd>
          <dt>
            <span v-text="t$('entities.board.form.content')"></span>
          </dt>
          <dd>
            <!-- Toast UI Viewer가 마크다운을 HTML로 렌더링하는 영역 -->
            <div ref="viewerRef" class="border rounded p-3 bg-white"></div>
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
            <span>{{ board.notice ? '✅ Yes' : 'No' }}</span>
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

        <!-- 첨부파일 목록 -->
        <div v-if="board.uploads && board.uploads.length > 0" class="mb-4">
          <h5 class="fw-bold mb-3">
            <font-awesome-icon icon="paperclip"></font-awesome-icon>
            첨부파일 ({{ board.uploads.length }})
          </h5>
          <ul class="list-group">
            <li
              v-for="file in board.uploads"
              :key="file.id"
              class="list-group-item d-flex justify-content-between align-items-center"
            >
              <div>
                <font-awesome-icon :icon="getFileIcon(file.sourceFilename || file.name || '')" class="text-secondary me-2"></font-awesome-icon>
                <span class="fw-semibold">{{ file.sourceFilename || file.name || 'file' }}</span>
                <span class="badge bg-secondary ms-2">{{ getFileExtension(file.sourceFilename || file.name || '') }}</span>
                <small class="text-muted ms-2">{{ formatBytes(file.fileSize || file.size || 0) }}</small>
              </div>
              <a
                :href="`/api/uploads/${file.id}/download`"
                class="btn btn-outline-primary btn-sm"
                target="_blank"
                download
              >
                <font-awesome-icon icon="download"></font-awesome-icon> 다운로드
              </a>
            </li>
          </ul>
        </div>

        <!-- 태그 표시 -->
        <div v-if="board.tags && board.tags.length > 0" class="mb-4">
          <span
            v-for="tag in board.tags"
            :key="tag"
            class="badge bg-info text-dark me-1 mb-1"
          >
            #{{ tag }}
          </span>
        </div>

        <button type="submit" @click.prevent="previousState()" class="btn btn-info btn-sm me-2" data-cy="entityDetailsBackButton">
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
