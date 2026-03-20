<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="commonCodeGroup">
        <h2 class="jh-entity-heading" data-cy="commonCodeGroupDetailsHeading">
          <span v-text="t$('entities.commonCodeGroup.detail.title')"></span> {{ commonCodeGroup.groupCode }}
        </h2>
        <dl class="row jh-entity-details">
          <dt>
            <span v-text="t$('entities.commonCodeGroup.form.groupCode')"></span>
          </dt>
          <dd>
            <span>{{ commonCodeGroup.groupCode }}</span>
          </dd>
          <dt>
            <span v-text="t$('entities.commonCodeGroup.form.groupName')"></span>
          </dt>
          <dd>
            <span>{{ commonCodeGroup.groupName }}</span>
          </dd>
          <dt>
            <span v-text="t$('entities.commonCodeGroup.form.displayOrder')"></span>
          </dt>
          <dd>
            <span>{{ commonCodeGroup.displayOrder }}</span>
          </dd>
          <dt>
            <span v-text="t$('entities.commonCodeGroup.form.description')"></span>
          </dt>
          <dd>
            <span>{{ commonCodeGroup.description }}</span>
          </dd>
        </dl>
        <button type="submit" @click.prevent="previousState()" class="btn btn-info btn-sm" data-cy="entityDetailsBackButton">
          <font-awesome-icon icon="arrow-left"></font-awesome-icon>&nbsp;<span v-text="t$('entities.commonCodeGroup.actions.cancel')"></span>
        </button>
        <router-link
          v-if="commonCodeGroup.groupCode"
          :to="{ name: 'CommonCodeGroupEdit', params: { groupCode: commonCodeGroup.groupCode } }"
          custom
          v-slot="{ navigate }"
        >
          <button @click="navigate" class="btn btn-primary btn-sm">
            <font-awesome-icon icon="pencil-alt"></font-awesome-icon>&nbsp;<span v-text="t$('entities.commonCodeGroup.actions.edit')"></span>
          </button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./common-code-group-detail.component.ts"></script>
