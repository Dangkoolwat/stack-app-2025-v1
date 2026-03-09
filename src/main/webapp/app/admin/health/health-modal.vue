<template>
  <div class="modal-body pad">
    <div v-if="currentHealth && currentHealth.details">
      <h5 v-text="t$('health.details.properties')"></h5>
      <n-data-table :columns="columns" :data="healthDetails" :bordered="false" />
    </div>
    <div v-if="currentHealth && currentHealth.error">
      <h4 v-text="t$('health.details.error')"></h4>
      <pre>{{ currentHealth.error }}</pre>
    </div>
  </div>
</template>

<script lang="ts">
import { computed, h, defineComponent } from 'vue';
import { NDataTable, NTag } from 'naive-ui';
import { useI18n } from 'vue-i18n';

export default defineComponent({
  name: 'JhiHealthModal',
  props: {
    currentHealth: Object,
  },
  setup(props) {
    const { t } = useI18n();

    const healthDetails = computed(() => {
      if (!props.currentHealth?.details?.details) return [];
      return Object.entries(props.currentHealth.details.details).map(([key, value]) => ({
        name: key,
        value: value,
      }));
    });

    const columns = [
      { title: t('health.details.name'), key: 'name' },
      { title: t('health.details.value'), key: 'value' },
    ];

    const readableValue = (item: any): string => {
      if (typeof item === 'object') return JSON.stringify(item);
      return String(item);
    };

    return {
      t,
      healthDetails,
      columns,
      readableValue,
    };
  },
});
</script>
