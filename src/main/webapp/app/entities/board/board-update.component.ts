import { defineComponent, inject, ref, type Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import { type IBoard, Board } from '@/shared/model/board.model';
import BoardService from './board.service';
import CommonCodeService from '../common-code/common-code.service';
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  name: 'BoardUpdate',
  setup() {
    const { t: t$ } = useI18n();
    const route = useRoute();
    const router = useRouter();

    const boardService = inject('boardService', () => new BoardService(), true);
    const commonCodeService = inject('commonCodeService', () => new CommonCodeService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);

    const board: Ref<IBoard> = ref(new Board());
    const boardTypes: Ref<any[]> = ref([]);
    const isSaving = ref(false);

    const retrieveBoard = async (boardId: number) => {
      try {
        const res = await boardService.get(boardId);
        board.value = res;
      } catch (error: any) {
        alertService.showHttpError(error.response);
      }
    };

    const retrieveBoardTypes = async () => {
      try {
        const res = await commonCodeService.retrieveDetailsByGroup('BOARD_TYPE');
        boardTypes.value = res.data;
      } catch (error: any) {
        console.error('Failed to load board types', error);
      }
    };

    if (route.params?.id) {
      retrieveBoard(Number(route.params.id));
    }
    retrieveBoardTypes();

    const save = async () => {
      isSaving.value = true;
      try {
        if (board.value.id) {
          await boardService.update(board.value);
          alertService.showInfo(t$('entities.board.messages.updated', { param: board.value.id }));
        } else {
          const res = await boardService.create(board.value);
          board.value = res;
          alertService.showInfo(t$('entities.board.messages.created', { param: res.id }));
        }
        router.go(-1);
      } catch (error: any) {
        alertService.showHttpError(error.response);
      } finally {
        isSaving.value = false;
      }
    };


    return {
      board,
      boardTypes,
      isSaving,
      t$,
      save,
      previousState: () => router.go(-1),
    };
  },
});
