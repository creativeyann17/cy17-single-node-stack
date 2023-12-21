import i18next from 'i18next';
import LanguageDetector from 'i18next-browser-languagedetector';
import resourcesToBackend from 'i18next-resources-to-backend';
import { initReactI18next } from 'react-i18next';

import { DEBUG } from '../utils';

i18next
  .use(LanguageDetector)
  .use(initReactI18next)
  .use(
    resourcesToBackend((language: string, namespace: string) => {
      if (language === 'dev') return;
      return import(`./locales/${language}/${namespace}.json`);
    }),
  )
  .init({
    debug: DEBUG,
    fallbackLng: {
      zh: ['zh-Hans'],
      ['zh-CN']: ['zh-Hans'],
      ['zh-HK']: ['zh-Hant'],
      ['zh-TW']: ['zh-Hant'],
      'de-CH': ['fr', 'it'],
      default: ['en'],
    },
  });
