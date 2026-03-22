// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.common with an alias.
import { computed, createApp, onMounted, provide, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import { createPinia, storeToRefs } from 'pinia';

import AccountService from '@/account/account.service';
import { useLoginModal } from '@/account/login-modal';
import TranslationService from '@/locale/translation.service';
import { setupAxiosInterceptors } from '@/shared/config/axios-interceptor';
import { initFortAwesome, initI18N } from '@/shared/config/config';
import { initBootstrapVue } from '@/shared/config/config-bootstrap-vue';
import JhiItemCount from '@/shared/jhi-item-count.vue';
import JhiSortIndicator from '@/shared/sort/jhi-sort-indicator.vue';
import { useStore, useTranslationStore } from '@/store';

import { useTrackerService } from './admin/tracker/tracker.service';
import App from './app.vue';
import router from './router';

import '../content/scss/global.scss';
import '../content/scss/vendor.scss';

const pinia = createPinia();

import BoardService from '@/entities/board/board.service';
import CommonCodeService from '@/entities/common-code/common-code.service';
import SettingsService from '@/entities/settings/settings.service';
import TagService from '@/entities/tag/tag.service';

// jhipster-needle-add-entity-service-to-main-import - JHipster will import entities services here

const i18n = initI18N();

const app = createApp({
  components: { App },
  setup() {
    const { hideLogin, showLogin } = useLoginModal();
    const store = useStore();
    const accountService = new AccountService(store);
    const i18n = useI18n();
    const translationStore = useTranslationStore();
    const translationService = new TranslationService(i18n);

    const changeLanguage = async (newLanguage: string) => {
      const messages = i18n.getLocaleMessage(newLanguage) as any;
      if (i18n.locale.value !== newLanguage || !messages || Object.keys(messages).length === 0) {
        await translationService.refreshTranslation(newLanguage);
        translationService.setLocale(newLanguage);
        translationStore.setCurrentLanguage(newLanguage);
      }
    };

    provide('currentLanguage', i18n.locale);
    provide('changeLanguage', changeLanguage);

    const isInitialized = ref(false);
    provide('isInitialized', isInitialized);

    watch(
      () => store.account,
      async value => {
        if (value && !translationService.getLocalStoreLanguage()) {
          await changeLanguage(value.langKey);
        }
      },
    );

    watch(
      () => translationStore.currentLanguage,
      value => {
        translationService.setLocale(value);
      },
    );

    const init = async () => {
      try {
        const lang = [translationService.getLocalStoreLanguage(), store.account?.langKey, navigator.language, 'ko'].find(
          lng => lng && translationService.isLanguageSupported(lng),
        );
        if (lang) {
          await changeLanguage(lang);
        }
      } catch (e) {
        console.error('Initialization error:', e);
      } finally {
        isInitialized.value = true;
      }
    };

    init();

    router.beforeResolve(async (to, from, next) => {
      // Make sure login modal is closed
      hideLogin();

      if (!store.authenticated) {
        await accountService.update();
      }
      if (to.meta?.authorities && to.meta.authorities.length > 0) {
        const value = await accountService.hasAnyAuthorityAndCheckAuth(to.meta.authorities);
        if (!value) {
          if (from.path !== '/forbidden') {
            next({ path: '/forbidden' });
            return;
          }
        }
      }
      next();
    });

    setupAxiosInterceptors(
      error => {
        const url = error.response?.config?.url;
        const status = error.status || error.response?.status;
        if (status === 401) {
          // Store logged out state.
          store.logout();
          if (!url.endsWith('api/account') && !url.endsWith('api/authenticate')) {
            // Ask for a new authentication
            showLogin();
            return;
          }
        }
        return Promise.reject(error);
      },
      error => {
        return Promise.reject(error);
      },
    );

    const { authenticated } = storeToRefs(store);
    provide('authenticated', authenticated as any);
    provide(
      'currentUsername',
      computed(() => store.account?.login),
    );

    provide('translationService', translationService);
    provide('accountService', accountService);
    provide('store', store);
    provide('boardService', new BoardService());
    provide('commonCodeService', new CommonCodeService());
    provide('tagService', new TagService());
    provide('settingsService', new SettingsService());
    // jhipster-needle-add-entity-service-to-main - JHipster will add entities services here

    provide('trackerService', useTrackerService({ authenticated: authenticated as any }));
  },
  template: '<App/>',
});

initFortAwesome(app);

initBootstrapVue(app);

app.component('JhiItemCount', JhiItemCount).component('JhiSortIndicator', JhiSortIndicator).use(router).use(pinia).use(i18n).mount('#app');
