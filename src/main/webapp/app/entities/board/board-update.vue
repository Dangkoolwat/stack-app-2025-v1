<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate @submit.prevent="save()">
        <h2 id="board-heading" data-cy="BoardCreateUpdateHeading">
          <span v-if="isNew" v-text="t$('entities.board.actions.create')"></span>
          <span v-else>{{ t$('entities.board.actions.edit') }} #{{ board.id }}</span>
        </h2>
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
          <!-- 에디터 영역 (Toast UI) -->
          <div class="form-group mb-4">
            <label class="form-control-label fw-bold" v-text="t$('entities.board.form.content')"></label>
            <div ref="editorRef" id="board-content" class="border rounded" style="min-height: 400px"></div>
            <!-- 실제 내용은 board.content에 바인딩됩니다 -->
          </div>

          <!-- 태그 입력 영역 -->
          <div class="form-group mb-4" @keydown.enter.prevent>
            <label class="form-control-label fw-bold" for="board-tags">Tags</label>
            <div class="d-flex align-items-center justify-content-center mt-2">
              <b-form-tags
                v-model="tags"
                id="board-tags"
                placeholder="태그를 입력하고 엔터를 누르세요 (최대 10개)"
                :limit="10"
                remove-on-delete
                class="w-100"
              ></b-form-tags>
            </div>
          </div>

          <!-- 다중 첨부파일 드래그 앤 드롭 업로드 영역 -->
          <div class="form-group mb-4">
            <label class="form-control-label fw-bold">첨부 파일</label>
            <div
              class="dropzone mt-2 p-4 border rounded text-center position-relative"
              @dragover.prevent="dragover = true"
              @dragleave.prevent="dragover = false"
              @drop.prevent="handleDrop"
              :class="{ 'bg-light border-primary': dragover, 'border-secondary': !dragover }"
              style="border-style: dashed !important; border-width: 2px !important; cursor: pointer;"
              @click="triggerFileInput"
            >
              <font-awesome-icon icon="cloud-upload-alt" class="text-muted fa-2x mb-2"></font-awesome-icon>
              <p class="mb-0 text-muted">파일을 여기에 드래그하거나 클릭하여 업로드하세요</p>
              <small class="text-danger">최대 5개, 각 파일 10MB 이하 제한</small>
              <input type="file" multiple class="d-none" ref="fileInput" @change="handleFileSelect" />
            </div>

            <!-- 업로드된 파일 리스트 -->
            <ul class="list-group mt-3" v-if="uploads.length > 0">
              <li class="list-group-item d-flex justify-content-between align-items-center" v-for="(file, index) in uploads" :key="file.id">
                <div>
                  <font-awesome-icon icon="file" class="text-secondary me-2"></font-awesome-icon>
                  <a :href="`/api/uploads/${file.id}/download`" target="_blank" class="text-decoration-none text-dark">{{ file.name }}</a>
                  <small class="text-muted ms-2">({{ formatBytes(file.size) }})</small>
                </div>
                <button type="button" class="btn btn-outline-danger btn-sm" @click="removeUpload(file, index)" title="파일 삭제">
                  <font-awesome-icon icon="trash"></font-awesome-icon>
                </button>
              </li>
            </ul>
          </div>
          
          <div class="row">
            <div class="col-md-6 form-group">
              <label class="form-control-label fw-bold" v-text="t$('entities.board.form.boardTypeCode')" for="board-type"></label>
              <select class="form-select" id="board-type" data-cy="boardType" name="boardType" v-model="board.boardTypeCode" required>
                <option v-if="!board.boardTypeCode" :value="null"></option>
                <option :value="boardTypeOption.code" v-for="boardTypeOption in boardTypes" :key="boardTypeOption.id">
                  {{ boardTypeOption.name }}
                </option>
              </select>
            </div>
            <div class="col-md-6 form-group d-flex align-items-end mb-3">
              <div class="form-check form-switch ms-2">
                <input
                  type="checkbox"
                  class="form-check-input"
                  name="notice"
                  id="board-notice"
                  data-cy="notice"
                  v-model="board.notice"
                />
                <label class="form-check-label fw-bold" v-text="t$('entities.board.form.notice')" for="board-notice"></label>
              </div>
            </div>
          </div>
        </div>
        <hr class="mt-4 mb-3" />
        <div class="d-flex justify-content-end">
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
