import { type Ref, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import axios from 'axios';

import { useLoginModal } from '@/account/login-modal';
import type AccountService from '../account.service';

export default defineComponent({
  setup() {
    const authenticationError: Ref<boolean> = ref(false);
    const login: Ref<string> = ref('');
    const password: Ref<string> = ref('');
    const rememberMe: Ref<boolean> = ref(false);

    const { hideLogin } = useLoginModal();
    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const accountService = inject<AccountService>('accountService');

    const doLogin = async () => {
      const data = { username: login.value, password: password.value, rememberMe: rememberMe.value };
      try {
        const result = await axios.post('api/authenticate', data);
        const bearerToken = result.headers.authorization;
        let jwt = null;
        if (bearerToken?.startsWith('Bearer ')) {
          jwt = bearerToken.slice(7, bearerToken.length);
        } else if (result.data?.id_token) {
          jwt = result.data.id_token;
        }

        if (jwt) {
          if (rememberMe.value) {
            localStorage.setItem('jhi-authenticationToken', jwt);
            sessionStorage.removeItem('jhi-authenticationToken');
          } else {
            sessionStorage.setItem('jhi-authenticationToken', jwt);
            localStorage.removeItem('jhi-authenticationToken');
          }
        }

        authenticationError.value = false;
        if (accountService) {
          await accountService.retrieveAccount();
        }
        hideLogin();
        
        // 브라우저 새로고침 없이 최적의 경로로 이동 (SPA 방식)
        router.push('/');
      } catch {
        authenticationError.value = true;
      }
    };
    return {
      authenticationError,
      login,
      password,
      rememberMe,
      accountService,
      doLogin,
      t$: useI18n().t,
    };
  },
});
