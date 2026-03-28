import axios from 'axios';
import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { ref } from 'vue';

import { createTestingPinia } from '@pinia/testing';
import { flushPromises, shallowMount } from '@vue/test-utils';

import { useLoginModal } from '@/account/login-modal';

import Home from './home.vue';

vitest.mock('axios', () => ({
  default: {
    get: vitest.fn(),
  },
}));

type HomeComponentType = InstanceType<typeof Home>;

describe('Home', () => {
  let home: HomeComponentType;
  let authenticated;
  let currentUsername;
  let login: ReturnType<typeof useLoginModal>;
  let isInitialized;
  let store;

  beforeEach(() => {
    authenticated = ref(false);
    currentUsername = ref('');
    isInitialized = ref(true);
    store = {
      account: { authorities: [] },
      activeProfiles: [],
    };
    const wrapper = shallowMount(Home, {
      global: {
        plugins: [createTestingPinia()],
        stubs: {
          'router-link': true,
          'font-awesome-icon': true,
        },
        provide: {
          authenticated,
          currentUsername,
          isInitialized,
          store,
        },
      },
    });
    home = wrapper.vm;
    login = useLoginModal();
  });

  it('should not have user data set', () => {
    expect(home.authenticated).toBeFalsy();
    expect(home.username).toBe('');
  });

  it('should have user data set after authentication', () => {
    authenticated.value = true;
    currentUsername.value = 'test';

    expect(home.authenticated).toBeTruthy();
    expect(home.username).toBe('test');
  });

  it('should use login service', () => {
    home.showLogin();
    expect(login.showLogin).toHaveBeenCalled();
  });

  it('should map admin health cards from health details', async () => {
    authenticated.value = true;
    currentUsername.value = 'admin';
    store.account.authorities = ['ROLE_ADMIN'];
    store.userIdentity = { authorities: ['ROLE_ADMIN'] };

    vitest.mocked(axios.get).mockImplementation((url: string) => {
      if (url === 'management/health') {
        return Promise.resolve({
          data: {
            components: {
              db: { status: 'UP', details: { database: 'Oracle', validationQuery: 'isValid()' } },
              redisServer: { status: 'UP', details: { used_memory_human: '1.65M', max_memory_human: '0B' } },
              diskSpace: { status: 'UP', details: { free: 2147483648, total: 4294967296 } },
            },
          },
        });
      }

      return Promise.resolve({
        data: {
          build: {
            version: '2.0.0',
            time: '2026-03-28T00:00:00Z',
          },
        },
      });
    });

    const wrapper = shallowMount(Home, {
      global: {
        plugins: [createTestingPinia()],
        stubs: {
          'router-link': true,
          'font-awesome-icon': true,
        },
        provide: {
          authenticated,
          currentUsername,
          isInitialized,
          store,
        },
      },
    });

    await flushPromises();

    expect(wrapper.vm.adminHealthCards).toEqual([
      expect.objectContaining({ key: 'db', value: 'Oracle', hint: 'validation: isValid()' }),
      expect.objectContaining({ key: 'redis', value: '1.65M', hint: 'max: 0B' }),
      expect.objectContaining({ key: 'disk', value: '2.00 GB', hint: 'total: 4.00 GB' }),
    ]);
  });
});
