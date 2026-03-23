import { defineComponent, inject, ref, type Ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import CommonCodeService from '../common-code/common-code.service';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  name: 'CommonCodeDetailUpdate',
  setup() {
    const { t: t$ } = useI18n();
    const route = useRoute();
    const router = useRouter();

    const commonCodeService = inject('commonCodeService', () => new CommonCodeService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);

    const commonCodeDetail: Ref<any> = ref({ group: {} });
    const commonCodeGroups: Ref<any[]> = ref([]);
    const isSaving = ref(false);
    const isEditing = ref(false);

    const retrieveGroups = async () => {
      try {
        const res = await commonCodeService.retrieveGroups();
        commonCodeGroups.value = res.data;
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    const retrieveDetail = async (id: number) => {
      try {
        const res = await commonCodeService.getDetail(id);
        commonCodeDetail.value = res;
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    onMounted(async () => {
      await retrieveGroups();
      if (route.params?.id) {
        await retrieveDetail(Number(route.params.id));
        isEditing.value = true;
      } else if (route.query?.groupCode) {
        commonCodeDetail.value.group.groupCode = route.query.groupCode;
      }
    });

    const save = async () => {
      isSaving.value = true;
      try {
        if (isEditing.value) {
          await commonCodeService.updateDetail(commonCodeDetail.value);
          alertService.showInfo(t$('entities.commonCodeDetail.messages.updated', { param: commonCodeDetail.value.id }));
        } else {
          await commonCodeService.createDetail(commonCodeDetail.value);
          alertService.showInfo(t$('entities.commonCodeDetail.messages.created', { param: commonCodeDetail.value.code }));
        }
        goBack();
      } catch (error: any) {
        alertService.showHttpError(error.response);
      } finally {
        isSaving.value = false;
      }
    };

    const goBack = () => {
      if (commonCodeDetail.value.group?.groupCode) {
        router.push({ name: 'CommonCodeDetail', query: { groupCode: commonCodeDetail.value.group.groupCode } });
      } else {
        router.push({ name: 'CommonCodeDetail' });
      }
    };

    return {
      commonCodeDetail,
      commonCodeGroups,
      isSaving,
      isEditing,
      t$,
      save,
      previousState: () => goBack(),
    };
  },
});
