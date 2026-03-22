import { type Ref, defineComponent, inject, ref } from 'vue';
import { useI18n } from 'vue-i18n';

import { type IBoard } from '@/shared/model/board.model';
import BoardService from './board.service';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  name: 'Board',
  setup() {
    const { t: t$ } = useI18n();
    const boardService = inject('boardService', () => new BoardService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);

    const boards: Ref<IBoard[]> = ref([]);
    const isFetching = ref(false);
    const removeId = ref<number | null>(null);
    const removeEntity = ref<any>(null);

    const itemsPerPage = ref(20);
    const queryCount: Ref<number> = ref(null);
    const page = ref(1);
    const previousPage = ref(1);
    const propOrder = ref('id');
    const reverse = ref(false);
    const totalItems = ref(0);

    const retrieveAllBoards = async () => {
      isFetching.value = true;
      try {
        const paginationQuery = {
          page: page.value - 1,
          size: itemsPerPage.value,
          sort: sort(),
        };
        const res = await boardService.retrieve(paginationQuery);
        boards.value = res.data.content;
        totalItems.value = res.data.totalElements;
        queryCount.value = res.data.totalElements;
      } catch (err: any) {
        alertService.showHttpError(err.response);
      } finally {
        isFetching.value = false;
      }
    };

    const handleSyncList = () => {
      retrieveAllBoards();
    };

    const prepareRemove = (instance: IBoard) => {
      removeId.value = instance.id;
    };

    const closeDialog = () => {
      if (removeEntity.value) {
        removeEntity.value.hide();
      }
    };

    const removeBoard = async () => {
      try {
        await boardService.delete(removeId.value as number);
        const message = t$('entities.board.messages.deleted', { param: removeId.value });
        alertService.showInfo(message);
        removeId.value = null;
        closeDialog();
        retrieveAllBoards();
      } catch (err: any) {
        alertService.showHttpError(err.response);
      }
    };

    const sort = (): string[] => {
      const result = [propOrder.value + ',' + (reverse.value ? 'desc' : 'asc')];
      if (propOrder.value !== 'id') {
        result.push('id');
      }
      return result;
    };

    const loadPage = (p: number) => {
      if (p !== previousPage.value) {
        previousPage.value = p;
        transition();
      }
    };

    const transition = () => {
      retrieveAllBoards();
    };

    const changeOrder = (prop: string) => {
      propOrder.value = prop;
      reverse.value = !reverse.value;
      transition();
    };

    return {
      boards,
      isFetching,
      retrieveAllBoards,
      handleSyncList,
      removeId,
      removeEntity,
      prepareRemove,
      removeBoard,
      closeDialog,
      page,
      previousPage,
      itemsPerPage,
      totalItems,
      queryCount,
      loadPage,
      propOrder,
      reverse,
      changeOrder,
      t$,
    };
  },
  mounted() {
    this.retrieveAllBoards();
  },
});
