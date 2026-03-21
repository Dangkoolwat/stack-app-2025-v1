import { defineComponent, inject, ref, type Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import { type IBoard } from '@/shared/model/board.model';
import BoardService from './board.service';
import CommonCodeService from '@/entities/common-code/common-code.service'; // Added import for CommonCodeService
import { useAlertService } from '@/shared/alert/alert.service';

export default defineComponent({
  name: 'BoardDetail',
  setup() {
    const { t: t$ } = useI18n();
    const route = useRoute();
    const router = useRouter();

    const boardService = inject('boardService', () => new BoardService(), true);
    const alertService = inject('alertService', () => useAlertService(), true);

    const board: Ref<IBoard> = ref({});

    const retrieveBoard = async (boardId: number) => {
      try {
        const res = await boardService.get(boardId);
        board.value = res;
      } catch (error: any) {
        console.error('Failed to load board types', error); // Modified error handling
      }
    };

    if (route.params?.id) {
      retrieveBoard(Number(route.params.id));
    }

    return {
      alertService,
      board,
      t$,
      previousState: () => router.go(-1),
    };
  },
});
