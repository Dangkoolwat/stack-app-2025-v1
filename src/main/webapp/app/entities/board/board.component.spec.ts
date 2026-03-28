import { beforeEach, describe, expect, it, vitest } from 'vitest';

import { shallowMount } from '@vue/test-utils';

import Board from './board.vue';

describe('Board Component', () => {
  let boardService;
  let alertService;

  beforeEach(() => {
    boardService = {
      retrieve: vitest.fn().mockResolvedValue({
        data: {
          content: [{ id: 1, title: 'sample' }],
          page: {
            totalElements: 23,
            totalPages: 2,
            number: 0,
            size: 20,
          },
        },
      }),
    };

    alertService = {
      showHttpError: vitest.fn(),
      showInfo: vitest.fn(),
    };
  });

  it('should read total items from nested page payload', async () => {
    const wrapper = shallowMount(Board, {
      global: {
        stubs: {
          'router-link': true,
          'font-awesome-icon': true,
          'b-button': true,
          'b-modal': true,
          'b-pagination': true,
          'jhi-sort-indicator': true,
          'jhi-item-count': true,
        },
        mocks: {
          t$: (key: string) => key,
          $d: (value: string) => value,
        },
        provide: {
          boardService,
          alertService,
        },
      },
    });

    await vitest.waitFor(() => {
      expect(wrapper.vm.totalItems).toBe(23);
    });

    expect(wrapper.vm.boards).toHaveLength(1);
    expect(boardService.retrieve).toHaveBeenCalled();
  });
});
