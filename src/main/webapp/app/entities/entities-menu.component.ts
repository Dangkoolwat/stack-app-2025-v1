import { defineComponent, inject } from 'vue';
import { useI18n } from 'vue-i18n';

export default defineComponent({
  name: 'EntitiesMenu',
  setup() {
    const i18n = useI18n();
    const store = inject<any>('store');

    return {
      t$: i18n.t,
      hasAnyAuthority: (authorities: any) => {
        const userAuthorities = store?.account?.authorities ?? store?.userIdentity?.authorities ?? [];
        if (authorities && userAuthorities) {
          if (typeof authorities === 'string') {
            authorities = [authorities];
          }
          return authorities.some((auth: string) => userAuthorities.includes(auth));
        }
        return false;
      },
    };
  },
});
