<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" role="form" novalidate v-on:submit.prevent="save()">
        <h2 id="common-code-group-heading" data-cy="CommonCodeGroupCreateUpdateHeading" v-text="t$('entities.commonCodeGroup.actions.create')"></h2>
        <div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.commonCodeGroup.form.groupCode')" for="group-code"></label>
            <input
              type="text"
              class="form-control"
              name="groupCode"
              id="group-code"
              data-cy="groupCode"
              v-model="commonCodeGroup.groupCode"
              :readonly="isEditing"
              required
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.commonCodeGroup.form.groupName')" for="group-name"></label>
            <input
              type="text"
              class="form-control"
              name="groupName"
              id="group-name"
              data-cy="groupName"
              v-model="commonCodeGroup.groupName"
              required
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.commonCodeGroup.form.displayOrder')" for="display-order"></label>
            <input
              type="number"
              class="form-control"
              name="displayOrder"
              id="display-order"
              data-cy="displayOrder"
              v-model="commonCodeGroup.displayOrder"
              required
            />
          </div>
          <div class="form-group">
            <label class="form-control-label" v-text="t$('entities.commonCodeGroup.form.description')" for="description"></label>
            <input
              type="text"
              class="form-control"
              name="description"
              id="description"
              data-cy="description"
              v-model="commonCodeGroup.description"
            />
          </div>
        </div>
        <div>
          <button type="button" id="cancel-save" data-cy="entityCreateCancelButton" class="btn btn-secondary btn-sm me-2" v-on:click="previousState()">
            <font-awesome-icon icon="ban"></font-awesome-icon>&nbsp;<span v-text="t$('entities.commonCodeGroup.actions.cancel')"></span>
          </button>
          <button
            type="submit"
            id="save-entity"
            data-cy="entityCreateSaveButton"
            :disabled="isSaving"
            class="btn btn-primary btn-sm"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="t$('entities.commonCodeGroup.actions.save')"></span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts" src="./common-code-group-update.component.ts"></script>
