import { type ComputedRef, inject, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { RxStomp, RxStompState } from '@stomp/rx-stomp';
import { map } from 'rxjs';
import SockJS from 'sockjs-client';

const DESTINATION_TRACKER = '/topic/tracker';
const DESTINATION_ACTIVITY = '/topic/activity';

export const useTrackerService = ({ stomp, authenticated }: { stomp?: RxStomp; authenticated?: ComputedRef<boolean> } = {}) => {
  const router = useRouter();
  authenticated = authenticated ?? inject('authenticated');
  const trackerService = new TrackerService({ stomp });

  router.afterEach(to => trackerService.sendActivity(to.fullPath));

  watch(trackerService.status, value => {
    if (value === 'open') {
      trackerService.sendActivity(router.currentRoute.value.fullPath);
    }
  });

  watch(
    authenticated,
    (value, prevValue) => {
      if (value === prevValue) return;
      if (value) {
        trackerService.connect();
      } else {
        trackerService.disconnect();
      }
    },
    { immediate: true },
  );
  return trackerService;
};

export default class TrackerService {
  status = ref<'open' | 'connecting' | 'closing' | 'closed'>('closed');
  private rxStomp: RxStomp;

  constructor({ stomp }: { stomp?: RxStomp }) {
    this.stomp = stomp ?? new RxStomp();
  }

  get stomp() {
    return this.rxStomp;
  }

  set stomp(rxStomp) {
    this.rxStomp = rxStomp;
    this.rxStomp.configure({
      debug: (msg: string): void => {
        console.log(new Date(), msg);
      },
    });

    this.rxStomp.connectionState$.subscribe(state => {
      switch (state) {
        case RxStompState.CONNECTING:
          this.status.value = 'connecting';
          return;
        case RxStompState.OPEN:
          this.status.value = 'open';
          return;
        case RxStompState.CLOSING:
          this.status.value = 'closing';
          return;
        case RxStompState.CLOSED:
          this.status.value = 'closed';
      }
    });
  }

  connect(): void {
    this.updateCredentials();
    this.rxStomp.activate();
  }

  async disconnect(): Promise<void> {
    await this.rxStomp.deactivate();
  }

  private getAuthToken() {
    const authToken = localStorage.getItem('jhi-authenticationToken') || sessionStorage.getItem('jhi-authenticationToken');
    return authToken;
  }

  private buildUrl(): string {
    // building absolute path so that websocket doesn't fail when deploying with a context path
    const loc = window.location;
    const baseHref = document.querySelector('base')?.getAttribute('href');
    const wsUrl = SERVER_WS_URL.startsWith('/') ? `${loc.protocol}//${loc.host}${baseHref ?? '/'}${SERVER_WS_URL.substring(1)}/tracker` : `${SERVER_WS_URL}/tracker`;
    const url = wsUrl.replace(/\/+/g, '/').replace('http:/', 'http://').replace('https:/', 'https://');
    const authToken = this.getAuthToken();
    if (authToken) {
      return `${url}?access_token=${authToken}`;
    }
    return url;
  }

  private updateCredentials(): void {
    this.rxStomp.configure({
      webSocketFactory: () => {
        return new SockJS(this.buildUrl());
      },
    });
  }

  sendActivity(page: string): void {
    this.rxStomp.publish({
      destination: DESTINATION_ACTIVITY,
      body: JSON.stringify({ page }),
    });
  }

  subscribe(observer) {
    return this.rxStomp
      .watch(DESTINATION_TRACKER)
      .pipe(map(imessage => JSON.parse(imessage.body)))
      .subscribe(observer);
  }
}
