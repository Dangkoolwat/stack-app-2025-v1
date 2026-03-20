<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2
          id="common-code-detail-heading"
          data-cy="CommonCodeDetailCreateUpdateHeading"
          v-text="t$('entities.commonCodeDetail.actions.create')"
        ></h2>
        <div>
          <div class="form-group" v-if="commonCodeDetail.id">
            <label for="id" v-text="t$('entities.commonCodeDetail.form.id')"></label>
            <input type="text" class="form-control" id="id" name="id" v-model="commonCodeDetail.id" readonly />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.commonCodeDetail.form.groupCode')" for="group-code"></label>
            <select
              class="form-control"
              id="group-code"
              data-cy="groupCode"
              name="groupCode"
              v-model="commonCodeDetail.group.groupCode"
              required
              :disabled="isEditing"
            >
              <option v-for="group in commonCodeGroups" :key="group.groupCode" :value="group.groupCode">
                {{ group.groupName }} ({{ group.groupCode }})
              </option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.commonCodeDetail.form.code')" for="detail-code"></label>
            <input
              type="text"
              class="form-control"
              name="code"
              id="detail-code"
              data-cy="code"
              v-model="commonCodeDetail.code"
              required
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.commonCodeDetail.form.name')" for="detail-name"></label>
            <input
              type="text"
              class="form-control"
              name="name"
              id="detail-name"
              data-cy="name"
              v-model="commonCodeDetail.name"
              required
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.commonCodeDetail.form.sortOrder')" for="sort-order"></label>
            <input
              type="number"
              class="form-control"
              name="sortOrder"
              id="sort-order"
              data-cy="sortOrder"
              v-model="commonCodeDetail.sortOrder"
              required
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.commonCodeDetail.form.description')" for="description"></label>
            <input
              type="text"
              class="form-control"
              name="description"
              id="description"
              data-cy="description"
              v-model="commonCodeDetail.description"
            />
          </div>
        </div>
        <div>
          <button type="button" id="cancel-save" data-cy="entityCreateCancelButton" class="btn btn-secondary btn-sm me-2" v-on:click="previousState()">
            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="t$('entities.commonCodeDetail.actions.cancel')"></span>
          </button>
          <button
            type="submit"
            id="save-entity"
            data-cy="entityCreateSaveButton"
            :disabled="isSaving"
            class="btn btn-primary btn-sm"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="t$('entities.commonCodeDetail.actions.save')"></span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts" src="./common-code-detail-update.component.ts"></script>
