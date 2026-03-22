import { type ComputedRef, defineComponent, inject, type Ref } from 'vue';
import { useI18n } from 'vue-i18n';

import { useLoginModal } from '@/account/login-modal';

export default defineComponent({
  setup() {
    const { showLogin } = useLoginModal();
    const authenticated = inject<ComputedRef<boolean>>('authenticated');
    const username = inject<ComputedRef<string>>('currentUsername');
    const accountService = inject<any>('accountService');
    const isInitialized = inject<Ref<boolean>>('isInitialized');
    const store = inject<any>('store'); // main.ts에서 store도 provide 해야 함

    return {
      authenticated,
      username,
      showLogin,
      isInitialized,
      hasAnyAuthority: (authorities: any) => {
        // store.account에 직접 접근하여 반응성 확보
        const userAuthorities = store?.account?.authorities;
        if (authorities && userAuthorities) {
          if (typeof authorities === 'string') {
            authorities = [authorities];
          }
          return authorities.some((auth: string) => userAuthorities.includes(auth));
        }
        return false;
      },
      t$: useI18n().t,
    };
  },
});
