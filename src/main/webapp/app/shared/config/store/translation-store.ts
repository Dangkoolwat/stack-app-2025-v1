import { defineStore } from 'pinia';

export interface TranslationState {
  currentLanguage: string;
  languages: Record<string, { name: string }>;
}

export const useTranslationStore = defineStore('translationStore', {
  state: (): TranslationState => ({
    currentLanguage: 'ko',
    languages: {
      ko: { name: '한국어' },
      en: { name: 'English' },
    },
  }),

  getters: {
    getCurrentLanguageName: (state): string => {
      return state.languages[state.currentLanguage]?.name ?? 'Unknown';
    },

    getSupportedLanguages: (state): string[] => {
      return Object.keys(state.languages);
    },
  },

  actions: {
    setCurrentLanguage(lang: string): void {
      if (this.languages[lang]) {
        this.currentLanguage = lang;
        localStorage.setItem('currentLanguage', lang);
      } else {
        console.warn(`Language ${lang} is not supported`);
      }
    },
  },
});

export type TranslationStore = ReturnType<typeof useTranslationStore>;
