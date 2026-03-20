<template>
  <div>
    <b-dropdown-item to="/settings">
      <font-awesome-icon icon="cogs" />
      <span class="ms-1" v-text="t$('stackApp.settings.home.createOrEditLabel')"></span>
    </b-dropdown-item>
    <b-dropdown-item to="/common-code-group">
      <font-awesome-icon icon="layer-group" />
      <span class="ms-1" v-text="t$('entities.commonCodeGroup.menu')"></span>
    </b-dropdown-item>
    <b-dropdown-item to="/common-code-detail">
      <font-awesome-icon icon="list-ul" />
      <span class="ms-1" v-text="t$('entities.commonCodeDetail.menu')"></span>
    </b-dropdown-item>
    <b-dropdown-item to="/tag">
      <font-awesome-icon icon="hashtag" />
      <span class="ms-1" v-text="t$('entities.tag.menu')"></span>
    </b-dropdown-item>
    <b-dropdown-item to="/board">
      <font-awesome-icon icon="file-alt" />
      <span class="ms-1" v-text="t$('entities.board.menu')"></span>
    </b-dropdown-item>

    <!-- jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here -->




  </div>
</template>

<script lang="ts" src="./entities-menu.component.ts"></script>
