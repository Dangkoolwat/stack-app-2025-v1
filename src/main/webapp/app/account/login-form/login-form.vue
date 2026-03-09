<template>
  <div class="modal-body">
    <div class="row justify-content-center">
      <div class="col-md-8">
        <n-alert v-if="authenticationError" type="error" data-cy="loginError">
          <span v-html="t$('login.messages.error.authentication')"></span>
        </n-alert>
      </div>
      <div class="col-md-8">
        <n-form @submit.prevent="doLogin()">
          <n-form-item :label="t$('global.form[\'username.label\']')" label-for="username">
            <n-input
              id="username"
              type="text"
              name="username"
              autofocus
              :placeholder="t$('global.form[\'username.placeholder\']')"
              v-model:value="login"
              data-cy="username"
            />
          </n-form-item>
          <n-form-item :label="t$('login.form.password')" label-for="password">
            <n-input
              id="password"
              type="password"
              name="password"
              :placeholder="t$('login.form[\'password.placeholder\']')"
              v-model:value="password"
              data-cy="password"
            />
          </n-form-item>
          <n-checkbox id="rememberMe" name="rememberMe" v-model:checked="rememberMe" checked>
            <span v-text="t$('login.form.rememberme')"></span>
          </n-checkbox>
          <div style="margin-top: 16px">
            <n-button data-cy="submit" type="primary" attr-type="submit" v-text="t$('login.form.button')"></n-button>
          </div>
        </n-form>
        <p></p>
        <div>
          <n-alert type="warning">
            <router-link
              :to="'/account/reset/request'"
              class="alert-link"
              v-text="t$('login.password.forgot')"
              data-cy="forgetYourPasswordSelector"
            ></router-link>
          </n-alert>
        </div>
        <div>
          <n-alert type="warning">
            <span v-text="t$('global.messages.info.register.noaccount')"></span>
            <router-link :to="'/register'" class="alert-link" v-text="t$('global.messages.info.register.link')"></router-link>
          </n-alert>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./login-form.component.ts"></script>
