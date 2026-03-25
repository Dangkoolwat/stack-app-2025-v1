import { type Ref, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';

import CommonCodeService from '../common-code/common-code.service';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  name: 'CommonCodeGroup',
  setup() {
    const { t: t$ } = useI18n();
    const commonCodeService = inject('commonCodeService', () => new CommonCodeService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);

    const commonCodeGroups: Ref<any[]> = ref([]);
    const isFetching = ref(false);
    const removeId = ref<string | null>(null);
    const removeEntity = ref<any>(null);

    const closeDialog = () => {
      removeEntity.value.hide();
    };

    const retrieveAllGroups = async () => {
      isFetching.value = true;
      try {
        const res = await commonCodeService.retrieveGroups();
        commonCodeGroups.value = res.data;
      } catch (err: any) {
        alertService.showHttpError(err.response);
      } finally {
        isFetching.value = false;
      }
    };

    const handleSyncList = () => {
      retrieveAllGroups();
    };

    const prepareRemove = (instance: any) => {
      removeId.value = instance.groupCode;
    };

    const removeGroup = async () => {
      try {
        await commonCodeService.deleteGroup(removeId.value as string);
        alertService.showInfo(t$('entities.commonCodeGroup.messages.deleted', { param: removeId.value }));
        removeId.value = null;
        retrieveAllGroups();
        closeDialog();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    return {
      commonCodeGroups,
      isFetching,
      retrieveAllGroups,
      handleSyncList,
      removeId,
      prepareRemove,
      removeGroup,
      removeEntity,
      closeDialog,
      t$,
    };
  },
  mounted() {
    this.retrieveAllGroups();
  },
});
