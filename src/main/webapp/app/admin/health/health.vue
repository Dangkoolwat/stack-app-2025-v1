<template>
  <div>
    <h2>
      <span id="health-page-heading" v-text="t$('health.title')" data-cy="healthPageHeading"></span>
      <n-button class="float-right" @click="refresh()" :loading="updatingHealth">
        <span v-text="t$('health[\'refresh.button\']')"></span>
      </n-button>
    </h2>

    <n-data-table :columns="columns" :data="healthData" :bordered="false" />

    <n-modal
      v-model:show="showModal"
      preset="card"
      :style="{ width: '600px' }"
      :title="currentHealth ? t$('health.indicator.' + baseName(currentHealth.name)) + ' ' + subSystemName(currentHealth.name) : ''"
    >
      <health-modal :current-health="currentHealth"></health-modal>
    </n-modal>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, computed, h } from 'vue';
import { useI18n } from 'vue-i18n';
import { NButton, NTag } from 'naive-ui';
import HealthService from './health.service';
import JhiHealthModal from './health-modal.vue';

export default defineComponent({
  name: 'JhiHealth',
  components: {
    'health-modal': JhiHealthModal,
  },
  setup() {
    const { t } = useI18n();
    const healthService = new HealthService();

    const healthData = ref<any[]>([]);
    const currentHealth = ref<any>(null);
    const updatingHealth = ref(false);
    const showModal = ref(false);

    const columns = computed(() => [
      { title: t('health.table.service'), key: 'name' },
      {
        title: t('health.table.status'),
        key: 'status',
        render: (row: any) => {
          const type = row.status === 'UP' ? 'success' : row.status === 'UNKNOWN' ? 'warning' : 'error';
          return h(NTag, { type, size: 'small' }, { default: () => t('health.status.' + row.status) });
        },
      },
      {
        title: t('health.details.details'),
        key: 'actions',
        render: (row: any) => (row.details || row.error ? h('a', { class: 'hand', onClick: () => showHealth(row) }, '👁') : ''),
      },
    ]);

    const baseName = (name: string): string => {
      return healthService.getBaseName(name);
    };

    const subSystemName = (name: string): string => {
      return healthService.getSubSystemName(name);
    };

    const getBadgeClass = (statusState: string): string => {
      if (statusState === 'UP') return 'success';
      return 'error';
    };

    const refresh = () => {
      updatingHealth.value = true;
      healthService
        .checkHealth()
        .then(res => {
          healthData.value = healthService.transformHealthData(res.data);
          updatingHealth.value = false;
        })
        .catch(error => {
          if (error.status === 503) {
            healthData.value = healthService.transformHealthData(error.error);
          }
          updatingHealth.value = false;
        });
    };

    const showHealth = (health: any) => {
      currentHealth.value = health;
      showModal.value = true;
    };

    return {
      t,
      healthData,
      currentHealth,
      updatingHealth,
      showModal,
      columns,
      baseName,
      subSystemName,
      getBadgeClass,
      refresh,
      showHealth,
    };
  },
  mounted() {
    this.refresh();
  },
});
</script>
