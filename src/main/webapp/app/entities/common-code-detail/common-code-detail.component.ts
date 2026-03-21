import { type Ref, defineComponent, inject, ref, watch, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute } from 'vue-router';

import CommonCodeService from '../common-code/common-code.service';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  name: 'CommonCodeDetail',
  setup() {
    const { t: t$ } = useI18n();
    const route = useRoute();
    const commonCodeService = inject('commonCodeService', () => new CommonCodeService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);

    const commonCodeDetails: Ref<any[]> = ref([]);
    const commonCodeGroups: Ref<any[]> = ref([]);
    const selectedGroupCode = ref<string | null>((route.query?.groupCode as string) || null);
    const isFetching = ref(false);
    const removeId = ref<number | null>(null);

    const retrieveGroups = async () => {
      try {
        const res = await commonCodeService.retrieveGroups();
        commonCodeGroups.value = res.data;
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    const retrieveDetails = async () => {
      if (!selectedGroupCode.value) {
        commonCodeDetails.value = [];
        return;
      }
      isFetching.value = true;
      try {
        const res = await commonCodeService.retrieveDetailsByGroup(selectedGroupCode.value as string);
        commonCodeDetails.value = res.data;
      } catch (err: any) {
        alertService.showHttpError(err.response);
      } finally {
        isFetching.value = false;
      }
    };

    watch(selectedGroupCode, () => {
      retrieveDetails();
    });

    const handleSyncList = () => {
      retrieveDetails();
    };

    const prepareRemove = (instance: any) => {
      removeId.value = instance.id;
    };

    const removeDetail = async () => {
      try {
        await commonCodeService.deleteDetail(removeId.value as number);
        alertService.showInfo(t$('entities.commonCodeDetail.messages.deleted', { param: removeId.value }));
        removeId.value = null;
        retrieveDetails();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    onMounted(() => {
      retrieveGroups();
      retrieveDetails();
    });

    return {
      commonCodeDetails,
      commonCodeGroups,
      selectedGroupCode,
      isFetching,
      handleSyncList,
      removeId,
      prepareRemove,
      removeDetail,
      t$,
    };
  },
});
