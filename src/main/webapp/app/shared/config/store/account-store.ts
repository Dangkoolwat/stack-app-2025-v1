import { defineStore } from 'pinia';

export interface Account {
  id: number;
  login: string;
  langKey: string;
  authorities: string[];
}

export interface AccountStateStorable {
  logon: boolean | null;
  userIdentity: Account | null;
  authenticated: boolean;
  profilesLoaded: boolean;
  ribbonOnProfiles: string;
  activeProfiles: string;
}

export const defaultAccountState: AccountStateStorable = {
  logon: null,
  userIdentity: null,
  authenticated: false,
  profilesLoaded: false,
  ribbonOnProfiles: '',
  activeProfiles: '',
};

export const useAccountStore = defineStore('main', {
  state: (): AccountStateStorable => ({ ...defaultAccountState }),
  getters: {
    account: (state): Account | null => state.userIdentity,
  },
  actions: {
    authenticate(promise: boolean | null) {
      this.logon = promise;
    },
    setAuthentication(identity: Account | null) {
      this.userIdentity = identity;
      this.authenticated = !!identity;
      this.logon = null;
    },
    logout() {
      this.userIdentity = null;
      this.authenticated = false;
      this.logon = null;
    },
    setProfilesLoaded() {
      this.profilesLoaded = true;
    },
    setActiveProfiles(profile: string) {
      this.activeProfiles = profile;
    },
    setRibbonOnProfiles(ribbon: string) {
      this.ribbonOnProfiles = ribbon;
    },
  },
});
