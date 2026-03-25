import { defineComponent, inject, ref, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import BoardResourceManagementService from './board-resource-management.service';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  name: 'BoardResourceManagement',
  setup() {
    const boardResourceManagementService = new BoardResourceManagementService();
    const alertService = inject('alertService', () => useAlertService(), true);
    const { t: t$ } = useI18n();

    const resourceType = ref('uploads'); // 'uploads', 'tags', 'comments'
    const resources = ref<any[]>([]);
    const selectedIds = ref<number[]>([]);
    const isFetching = ref(false);
    const removeModal = ref<any>(null);

    const loadAll = async () => {
      isFetching.value = true;
      try {
        const res = await boardResourceManagementService.retrieve(resourceType.value);
        resources.value = res.data;
        selectedIds.value = [];
      } catch (err: any) {
        alertService.showHttpError(err.response);
      } finally {
        isFetching.value = false;
      }
    };

    onMounted(() => {
      loadAll();
    });

    const changeType = () => {
      loadAll();
    };

    const toggleSelectAll = (event: Event) => {
      const checked = (event.target as HTMLInputElement).checked;
      if (checked) {
        selectedIds.value = resources.value.map(r => r.id);
      } else {
        selectedIds.value = [];
      }
    };

    const prepareRemove = () => {
      if (selectedIds.value.length === 0) return;
      if (removeModal.value) {
        removeModal.value.show();
      }
    };

    const closeDialog = () => {
      if (removeModal.value) {
        removeModal.value.hide();
      }
    };

    const removeResources = async () => {
      try {
        await boardResourceManagementService.deleteTokens(resourceType.value, selectedIds.value);
        alertService.showInfo(t$('stackApp.boardResourceManagement.home.messages.deleted', { count: selectedIds.value.length }));
        closeDialog();
        loadAll();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    return {
      resourceType,
      resources,
      selectedIds,
      isFetching,
      loadAll,
      changeType,
      toggleSelectAll,
      prepareRemove,
      removeModal,
      closeDialog,
      removeResources,
      t$,
    };
  },
});
