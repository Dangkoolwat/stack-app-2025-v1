import { defineComponent, inject, ref, type Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import CommonCodeService from '../common-code/common-code.service';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  name: 'CommonCodeGroupDetail',
  setup() {
    const { t: t$ } = useI18n();
    const route = useRoute();
    const router = useRouter();

    const commonCodeService = inject('commonCodeService', () => new CommonCodeService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);

    const commonCodeGroup: Ref<any> = ref({});

    const retrieveCommonCodeGroup = async (groupCode: string) => {
      try {
        const res = await commonCodeService.retrieveGroups();
        commonCodeGroup.value = res.data.find((g: any) => g.groupCode === groupCode);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    if (route.params?.groupCode) {
      retrieveCommonCodeGroup(route.params.groupCode as string);
    }

    return {
      alertService,
      commonCodeGroup,
      t$,
      previousState: () => router.go(-1),
    };
  },
});
