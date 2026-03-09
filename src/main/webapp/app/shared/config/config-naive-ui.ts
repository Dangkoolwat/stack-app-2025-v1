import type { App } from 'vue';
import naive from 'naive-ui';

export function initNaiveUI(app: App): void {
  app.use(naive);
}
