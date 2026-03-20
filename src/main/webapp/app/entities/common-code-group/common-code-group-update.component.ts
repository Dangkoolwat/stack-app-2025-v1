import { defineComponent, inject, ref, type Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import CommonCodeService from '../common-code/common-code.service';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  name: 'CommonCodeGroupUpdate',
  setup() {
    const { t: t$ } = useI18n();
    const route = useRoute();
    const router = useRouter();

    const commonCodeService = inject('commonCodeService', () => new CommonCodeService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);

    const commonCodeGroup: Ref<any> = ref({});
    const isSaving = ref(false);
    const isEditing = ref(false);

    const retrieveCommonCodeGroup = async (groupCode: string) => {
      try {
        const res = await commonCodeService.retrieveGroups();
        commonCodeGroup.value = res.data.find((g: any) => g.groupCode === groupCode);

        isEditing.value = true;
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.groupCode) {
      retrieveCommonCodeGroup(route.params.groupCode as string);
    }

    const save = async () => {
      isSaving.value = true;
      try {
        if (isEditing.value) {
          await commonCodeService.updateGroup(commonCodeGroup.value);
          alertService.showInfo(t$('entities.commonCodeGroup.messages.updated', { param: commonCodeGroup.value.groupCode }));
        } else {
          await commonCodeService.createGroup(commonCodeGroup.value);
          alertService.showInfo(t$('entities.commonCodeGroup.messages.created', { param: commonCodeGroup.value.groupCode }));
        }
        router.go(-1);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      } finally {
        isSaving.value = false;
      }
    };

    return {
      commonCodeGroup,
      isSaving,
      isEditing,
      t$,
      save,
      previousState: () => router.go(-1),
    };
  },
});
