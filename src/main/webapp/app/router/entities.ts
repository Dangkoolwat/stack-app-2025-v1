const Entities = () => import('@/entities/entities.vue');

// jhipster-needle-add-entity-to-router-import - JHipster will import entities to the router here

export default {
  path: '/',
  component: Entities,
  children: [
    {
      path: 'board',
      name: 'Board',
      component: () => import('@/entities/board/board.vue'),
    },
    {
      path: 'board/new',
      name: 'BoardCreate',
      component: () => import('@/entities/board/board-update.vue'),
    },
    {
      path: 'board/:id/edit',
      name: 'BoardEdit',
      component: () => import('@/entities/board/board-update.vue'),
    },
    {
      path: 'board/:id/view',
      name: 'BoardView',
      component: () => import('@/entities/board/board-detail.vue'),
    },
    {
      path: 'common-code-group',
      name: 'CommonCodeGroup',
      component: () => import('@/entities/common-code-group/common-code-group.vue'),
    },
    {
      path: 'common-code-group/new',
      name: 'CommonCodeGroupCreate',
      component: () => import('@/entities/common-code-group/common-code-group-update.vue'),
    },
    {
      path: 'common-code-group/:groupCode/edit',
      name: 'CommonCodeGroupEdit',
      component: () => import('@/entities/common-code-group/common-code-group-update.vue'),
    },
    {
      path: 'common-code-group/:groupCode/view',
      name: 'CommonCodeGroupView',
      component: () => import('@/entities/common-code-group/common-code-group-detail.vue'),
    },
    {
      path: 'tag',
      name: 'Tag',
      component: () => import('@/entities/tag/tag.vue'),
    },
    {
      path: 'common-code-detail',
      name: 'CommonCodeDetail',
      component: () => import('@/entities/common-code-detail/common-code-detail.vue'),
    },
    {
      path: 'common-code-detail/new',
      name: 'CommonCodeDetailCreate',
      component: () => import('@/entities/common-code-detail/common-code-detail-update.vue'),
    },
    {
      path: 'common-code-detail/:id/edit',
      name: 'CommonCodeDetailEdit',
      component: () => import('@/entities/common-code-detail/common-code-detail-update.vue'),
    },
    {
      path: 'settings',
      name: 'Settings',
      component: () => import('@/entities/settings/settings.vue'),
    },

    // jhipster-needle-add-entity-to-router - JHipster will add entities to the router here
  ],
};
