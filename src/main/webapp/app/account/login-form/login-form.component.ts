import axios from 'axios';
import { type Ref, defineComponent, inject, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import type AccountService from '../account.service';
import { useLoginModal } from '@/account/login-modal';

export default defineComponent({
  compatConfig: { MODE: 3 },
  setup() {
    const authenticationError: Ref<boolean> = ref(false);
    const login: Ref<string> = ref(null);
    const password: Ref<string> = ref(null);
    const rememberMe: Ref<boolean> = ref(false);

    const { hideLogin } = useLoginModal();
    const route = useRoute();
    const router = useRouter();

    const previousState = () => router.go(-1);

    const accountService = inject<AccountService>('accountService');

    const doLogin = async () => {
      const data = `username=${encodeURIComponent(login.value)}&password=${encodeURIComponent(password.value)}&remember-me=${rememberMe.value}&submit=Login`;
      try {
        await axios.post('api/authentication', data, {
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
        });

        authenticationError.value = false;
        hideLogin();
        await accountService.retrieveAccount();
        if (route.path === '/forbidden') {
          previousState();
        }
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
