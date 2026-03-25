import { type Ref, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';

import TagService from './tag.service';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  name: 'Tag',
  setup() {
    const { t: t$ } = useI18n();
    const tagService = inject('tagService', () => new TagService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);

    const tags: Ref<any[]> = ref([]);
    const isFetching = ref(false);
    const removeId = ref<number | null>(null);
    const removeEntity = ref<any>(null);

    const closeDialog = () => {
      removeEntity.value.hide();
    };

    const retrieveAllTags = async () => {
      isFetching.value = true;
      try {
        const res = await tagService.retrieve();
        tags.value = res.data;
      } catch (err: any) {
        alertService.showHttpError(err.response);
      } finally {
        isFetching.value = false;
      }
    };

    const handleSyncList = () => {
      retrieveAllTags();
    };

    const prepareRemove = (instance: any) => {
      removeId.value = instance.id;
    };

    const removeTag = async () => {
      try {
        await tagService.delete(removeId.value as number);
        alertService.showInfo(t$('entities.tag.messages.deleted', { param: removeId.value }));
        removeId.value = null;
        retrieveAllTags();
        closeDialog();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    const undeleteTag = async (id: number) => {
      try {
        await tagService.undelete(id);
        alertService.showInfo(t$('entities.tag.messages.undeleted', { param: id }));
        retrieveAllTags();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    return {
      tags,
      isFetching,
      retrieveAllTags,
      handleSyncList,
      removeId,
      prepareRemove,
      removeTag,
      undeleteTag,
      removeEntity,
      closeDialog,
      t$,
    };
  },
  mounted() {
    this.retrieveAllTags();
  },
});
