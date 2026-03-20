import { defineComponent, inject, ref, type Ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import SettingsService from './settings.service';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  name: 'SettingsUpdate',
  setup() {
    const settingsService = inject('settingsService', () => new SettingsService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);
    const { t: t$ } = useI18n();
    const router = useRouter();

    const settings: Ref<any> = ref({});
    const isSaving = ref(false);

    const retrieveSettings = async () => {
      try {
        const res = await settingsService.get();
        settings.value = res;
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const save = async () => {
      isSaving.value = true;
      try {
        await settingsService.update(settings.value);
        alertService.showInfo(t$('stackApp.settings.updated', { param: settings.value.id }));
        router.go(-1);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      } finally {
        isSaving.value = false;
      }
    };

    const previousState = () => {
      router.go(-1);
    };

    onMounted(() => {
      retrieveSettings();
    });

    return {
      settings,
      isSaving,
      save,
      previousState,
      t$,
    };
  },
});
