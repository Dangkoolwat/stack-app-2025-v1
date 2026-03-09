<template>
  <div class="row justify-content-center">
    <div class="col-8">
      <div v-if="user">
        <h2 class="jh-entity-heading">
          <span v-text="t$('userManagement.detail.title')"></span> [<strong>{{ user.login }}</strong
          >]
        </h2>
        <dl class="row jh-entity-details">
          <dt><span v-text="t$('userManagement.login')"></span></dt>
          <dd>
            <span>{{ user.login }}</span>
            <n-tag v-if="user.activated" type="success" v-text="t$('userManagement.activated')"></n-tag>
            <n-tag v-if="!user.activated" type="error" v-text="t$('userManagement.deactivated')"></n-tag>
          </dd>
          <dt><span v-text="t$('userManagement.firstName')"></span></dt>
          <dd>{{ user.firstName }}</dd>
          <dt><span v-text="t$('userManagement.lastName')"></span></dt>
          <dd>{{ user.lastName }}</dd>
          <dt><span v-text="t$('userManagement.email')"></span></dt>
          <dd>{{ user.email }}</dd>
          <dt><span v-text="t$('userManagement.langKey')"></span></dt>
          <dd>{{ user.langKey }}</dd>
          <dt><span v-text="t$('userManagement.createdBy')"></span></dt>
          <dd>{{ user.createdBy }}</dd>
          <dt><span v-text="t$('userManagement.createdDate')"></span></dt>
          <dd>
            <span v-if="user.createdDate">
              {{ formatDate(user.createdDate) }}
            </span>
          </dd>
          <dt><span v-text="t$('userManagement.lastModifiedBy')"></span></dt>
          <dd>{{ user.lastModifiedBy }}</dd>
          <dt><span v-text="t$('userManagement.lastModifiedDate')"></span></dt>
          <dd>
            <span v-if="user.lastModifiedDate">
              {{ formatDate(user.lastModifiedDate) }}
            </span>
          </dd>
          <dt><span v-text="t$('userManagement.profiles')"></span></dt>
          <dd>
            <ul class="list-unstyled">
              <li v-for="authority of user.authorities" :key="authority">
                <n-tag type="info">{{ authority }}</n-tag>
              </li>
            </ul>
          </dd>
        </dl>
        <router-link custom v-slot="{ navigate }" :to="{ name: 'JhiUser' }">
          <n-button @click="navigate">
            <span v-text="t$('entity.action.back')"></span>
          </n-button>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./user-management-view.component.ts"></script>
