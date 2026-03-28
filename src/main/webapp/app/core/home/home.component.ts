import axios from 'axios';
import { computed, type ComputedRef, defineComponent, inject, type Ref, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import { useLoginModal } from '@/account/login-modal';

export default defineComponent({
  setup() {
    const { showLogin } = useLoginModal();
    const authenticated = inject<ComputedRef<boolean>>('authenticated');
    const username = inject<ComputedRef<string>>('currentUsername');
    const isInitialized = inject<Ref<boolean>>('isInitialized');
    const store = inject<any>('store');
    const adminSummaryLoading = ref(false);
    const adminHealthCards = ref<
      Array<{ key: string; label: string; value: string; hint: string; tone: 'success' | 'warning' | 'danger' }>
    >([]);
    const adminInfoCards = ref<Array<{ key: string; label: string; value: string; hint: string }>>([]);

    const hasAnyAuthority = (authorities: any) => {
      const userAuthorities = store?.account?.authorities ?? store?.userIdentity?.authorities ?? [];
      if (authorities && userAuthorities) {
        if (typeof authorities === 'string') {
          authorities = [authorities];
        }
        return authorities.some((auth: string) => userAuthorities.includes(auth));
      }
      return false;
    };

    const activeProfiles = computed(() => {
      const profiles = store?.activeProfiles;
      if (Array.isArray(profiles)) {
        return profiles;
      }
      if (typeof profiles === 'string') {
        return profiles
          .split(',')
          .map(profile => profile.trim())
          .filter(Boolean);
      }
      return [];
    });

    const dashboardStats = computed(() => {
      const isAdmin = hasAnyAuthority('ROLE_ADMIN');
      return [
        {
          key: 'role',
          label: 'home.dashboard.stats.role.label',
          value: isAdmin ? 'ADMIN' : 'USER',
          hint: isAdmin ? 'home.dashboard.stats.role.adminHint' : 'home.dashboard.stats.role.userHint',
        },
        {
          key: 'workspace',
          label: 'home.dashboard.stats.workspace.label',
          value: isAdmin ? '6' : '4',
          hint: 'home.dashboard.stats.workspace.hint',
        },
        {
          key: 'system',
          label: 'home.dashboard.stats.system.label',
          value: isAdmin ? '5' : '2',
          hint: 'home.dashboard.stats.system.hint',
        },
        {
          key: 'profile',
          label: 'home.dashboard.stats.profile.label',
          value: activeProfiles.value.includes('prod') ? 'PROD' : 'DEV',
          hint: 'home.dashboard.stats.profile.hint',
        },
      ];
    });

    const quickLinks = computed(() => {
      const links = [
        {
          key: 'boards',
          to: '/board',
          icon: 'file-alt',
          title: 'home.dashboard.quick.boards.title',
          description: 'home.dashboard.quick.boards.desc',
        },
        {
          key: 'create',
          to: '/board/new',
          icon: 'pencil-alt',
          title: 'home.dashboard.quick.create.title',
          description: 'home.dashboard.quick.create.desc',
        },
        {
          key: 'codes',
          to: '/common-code-group',
          icon: 'layer-group',
          title: 'home.dashboard.quick.codes.title',
          description: 'home.dashboard.quick.codes.desc',
        },
        {
          key: 'cleanup',
          to: '/board-resource',
          icon: 'trash-alt',
          title: 'home.dashboard.quick.cleanup.title',
          description: 'home.dashboard.quick.cleanup.desc',
        },
      ];

      if (hasAnyAuthority('ROLE_ADMIN')) {
        links.push(
          {
            key: 'users',
            to: '/admin/user-management',
            icon: 'users',
            title: 'home.dashboard.quick.users.title',
            description: 'home.dashboard.quick.users.desc',
          },
          {
            key: 'health',
            to: '/admin/health',
            icon: 'heart',
            title: 'home.dashboard.quick.health.title',
            description: 'home.dashboard.quick.health.desc',
          },
        );
      }

      return links;
    });

    const attentionItems = computed(() => {
      const items = [
        {
          key: 'nav',
          label: 'home.dashboard.attention.navigation.label',
          value: 'home.dashboard.attention.navigation.value',
        },
        {
          key: 'filters',
          label: 'home.dashboard.attention.filters.label',
          value: 'home.dashboard.attention.filters.value',
        },
        {
          key: 'feedback',
          label: 'home.dashboard.attention.feedback.label',
          value: 'home.dashboard.attention.feedback.value',
        },
      ];

      if (hasAnyAuthority('ROLE_ADMIN')) {
        items.unshift({
          key: 'system',
          label: 'home.dashboard.attention.system.label',
          value: 'home.dashboard.attention.system.value',
        });
      }

      return items;
    });

    const workspaceItems = computed(() => {
      const items = [
        {
          key: 'content',
          label: 'home.dashboard.workspace.content.label',
          value: 'home.dashboard.workspace.content.value',
        },
        {
          key: 'writing',
          label: 'home.dashboard.workspace.writing.label',
          value: 'home.dashboard.workspace.writing.value',
        },
      ];

      if (hasAnyAuthority('ROLE_ADMIN')) {
        items.push(
          {
            key: 'ops',
            label: 'home.dashboard.workspace.ops.label',
            value: 'home.dashboard.workspace.ops.value',
          },
          {
            key: 'account',
            label: 'home.dashboard.workspace.account.label',
            value: 'home.dashboard.workspace.account.value',
          },
        );
      }

      return items;
    });

    const buildTone = (status?: string): 'success' | 'warning' | 'danger' => {
      if (status === 'UP') {
        return 'success';
      }
      if (status === 'UNKNOWN') {
        return 'warning';
      }
      return 'danger';
    };

    const formatBytes = (value?: number): string | null => {
      if (typeof value !== 'number' || Number.isNaN(value)) {
        return null;
      }

      const gb = value / 1024 / 1024 / 1024;
      if (gb >= 1) {
        return `${gb.toFixed(2)} GB`;
      }
      return `${(value / 1024 / 1024).toFixed(2)} MB`;
    };

    const redisMemoryValue = (redis: any): string | null =>
      redis?.details?.used_memory_human ?? redis?.details?.detail?.used_memory_human ?? null;

    const redisMemoryHint = (redis: any): string | null => {
      const maxMemory = redis?.details?.max_memory_human ?? redis?.details?.detail?.max_memory_human;
      const fragmentation = redis?.details?.fragmentation_ratio ?? redis?.details?.detail?.fragmentation_ratio;
      if (maxMemory || fragmentation) {
        return [maxMemory ? `max: ${maxMemory}` : null, fragmentation ? `frag: ${fragmentation}` : null].filter(Boolean).join(' · ');
      }
      return redis?.status ?? null;
    };

    const databaseValue = (db: any): string => db?.details?.database ?? db?.status ?? 'N/A';

    const databaseHint = (db: any): string =>
      db?.details?.validationQuery ? `validation: ${String(db.details.validationQuery)}` : db?.status ?? 'Connection health';

    const diskValue = (disk: any): string => formatBytes(disk?.details?.free) ?? disk?.status ?? 'N/A';

    const diskHint = (disk: any): string => {
      const total = formatBytes(disk?.details?.total);
      if (total) {
        return `total: ${total}`;
      }
      return disk?.status ?? 'Storage health';
    };

    const fetchAdminSummary = async () => {
      if (!authenticated?.value || !hasAnyAuthority('ROLE_ADMIN')) {
        adminHealthCards.value = [];
        adminInfoCards.value = [];
        return;
      }

      adminSummaryLoading.value = true;
      try {
        const [healthRes, infoRes] = await Promise.allSettled([axios.get('management/health'), axios.get('management/info')]);

        if (healthRes.status === 'fulfilled') {
          const components = healthRes.value.data?.components ?? {};
          const db = components.db;
          const redis = components.redisServer ?? components.redis;
          const disk = components.diskSpace;

          adminHealthCards.value = [
            {
              key: 'db',
              label: 'Database',
              value: databaseValue(db),
              hint: databaseHint(db),
              tone: buildTone(db?.status),
            },
            {
              key: 'redis',
              label: 'Redis',
              value: redisMemoryValue(redis) ?? redis?.status ?? 'N/A',
              hint: redisMemoryHint(redis) ?? 'Cache server health',
              tone: buildTone(redis?.status),
            },
            {
              key: 'disk',
              label: 'Disk Space',
              value: diskValue(disk),
              hint: diskHint(disk),
              tone: buildTone(disk?.status),
            },
          ];
        } else {
          adminHealthCards.value = [];
        }

        if (infoRes.status === 'fulfilled') {
          const info = infoRes.value.data ?? {};
          const build = info.build ?? {};

          adminInfoCards.value = [
            {
              key: 'build-version',
              label: 'Build Version',
              value: build.version ?? 'N/A',
              hint: build.time ?? 'Build metadata',
            },
          ];
        } else {
          adminInfoCards.value = [];
        }
      } finally {
        adminSummaryLoading.value = false;
      }
    };

    watch(
      () => [authenticated?.value, store?.account?.authorities, store?.userIdentity?.authorities],
      () => {
        fetchAdminSummary();
      },
      { immediate: true },
    );

    return {
      authenticated,
      username,
      showLogin,
      isInitialized,
      activeProfiles,
      dashboardStats,
      quickLinks,
      attentionItems,
      workspaceItems,
      adminSummaryLoading,
      adminHealthCards,
      adminInfoCards,
      hasAnyAuthority,
      t$: useI18n().t,
    };
  },
});
