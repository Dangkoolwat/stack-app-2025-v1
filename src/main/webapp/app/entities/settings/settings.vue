<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <form name="editForm" novalidate v-on:submit.prevent="save()">
        <h2 id="stackApp.settings.home.createOrEditLabel" data-cy="SettingsCreateUpdateHeading" v-text="t$('stackApp.settings.home.createOrEditLabel')"></h2>
        <div>
          <div class="form-group" v-if="settings.id">
            <label for="id" v-text="t$('global.field.id')"></label>
            <input type="text" class="form-control" id="id" name="id" v-model="settings.id" readonly />
          </div>

          <div class="form-group mb-3">
            <label class="form-control-label" v-text="t$('stackApp.settings.tokenValiditySeconds')" for="settings-tokenValiditySeconds"></label>
            <input
              type="number"
              class="form-control"
              name="tokenValiditySeconds"
              id="settings-tokenValiditySeconds"
              data-cy="tokenValiditySeconds"
              v-model.number="settings.tokenValiditySeconds"
              required
            />
          </div>

          <div class="form-group mb-3">
            <label class="form-control-label" v-text="t$('stackApp.settings.tokenValiditySecondsForRememberMe')" for="settings-tokenValiditySecondsForRememberMe"></label>
            <input
              type="number"
              class="form-control"
              name="tokenValiditySecondsForRememberMe"
              id="settings-tokenValiditySecondsForRememberMe"
              data-cy="tokenValiditySecondsForRememberMe"
              v-model.number="settings.tokenValiditySecondsForRememberMe"
              required
            />
          </div>

          <div class="form-group mb-3">
            <label class="form-control-label" v-text="t$('stackApp.settings.loginMaxFailureAttempts')" for="settings-loginMaxFailureAttempts"></label>
            <input
              type="number"
              class="form-control"
              name="loginMaxFailureAttempts"
              id="settings-loginMaxFailureAttempts"
              data-cy="loginMaxFailureAttempts"
              v-model.number="settings.loginMaxFailureAttempts"
              required
            />
          </div>

          <div class="form-group mb-3">
            <label class="form-control-label" v-text="t$('stackApp.settings.description')" for="settings-description"></label>
            <input
              type="text"
              class="form-control"
              name="description"
              id="settings-description"
              data-cy="description"
              v-model="settings.description"
            />
          </div>
        </div>
        <div>
          <button
            type="submit"
            id="save-entity"
            data-cy="entityCreateSaveButton"
            :disabled="isSaving"
            class="btn btn-primary btn-sm"
          >
            <font-awesome-icon icon="save"></font-awesome-icon>&nbsp;<span v-text="t$('entity.action.save')"></span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts" src="./settings.component.ts"></script>
