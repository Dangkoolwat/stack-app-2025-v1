import { defineComponent, inject } from 'vue';
import { useI18n } from 'vue-i18n';

import HealthService from './health.service';

export default defineComponent({
  name: 'JhiHealthModal',
  props: {
    currentHealth: {
      type: Object,
      default: () => ({}),
    },
  },
  setup() {
    const healthService = inject('healthService', () => new HealthService(), true);

    return {
      healthService,
      t$: useI18n().t,
    };
  },
  methods: {
    baseName(name: string): any {
      return this.healthService.getBaseName(name);
    },
    subSystemName(name: string): any {
      return this.healthService.getSubSystemName(name);
    },
    isObjectValue(value: any): boolean {
      return value !== null && typeof value === 'object' && !Array.isArray(value);
    },
    shouldUnwrapDetailEntry(key: string, value: any): boolean {
      return (key === 'detail' || key === 'details') && this.isObjectValue(value);
    },
    entryList(value: any): Array<[string, any]> {
      if (!this.isObjectValue(value)) {
        return [];
      }
      return Object.entries(value);
    },
    detailEntries(): Array<[string, any]> {
      if (!this.currentHealth?.details || !this.isObjectValue(this.currentHealth.details)) {
        return [];
      }

      return Object.entries(this.currentHealth.details).flatMap(([key, value]) => {
        if (this.shouldUnwrapDetailEntry(key, value)) {
          return Object.entries(value);
        }
        return [[key, value]];
      });
    },
    readableValue(value: any): string {
      if (this.currentHealth.name === 'diskSpace') {
        // Should display storage space in a human readable unit
        const val = value / 1073741824;
        if (val > 1) {
          // Value
          return `${val.toFixed(2)} GB`;
        }
        return `${(value / 1048576).toFixed(2)} MB`;
      }

      if (typeof value === 'object') {
        return JSON.stringify(value);
      }
      return value.toString();
    },
  },
});
