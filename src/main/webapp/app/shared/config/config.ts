import type { App } from 'vue';
import { type IntlDateTimeFormats, createI18n } from 'vue-i18n';

import { library } from '@fortawesome/fontawesome-svg-core';
import { faArrowLeft } from '@fortawesome/free-solid-svg-icons/faArrowLeft';
import { faAsterisk } from '@fortawesome/free-solid-svg-icons/faAsterisk';
import { faBan } from '@fortawesome/free-solid-svg-icons/faBan';
import { faBars } from '@fortawesome/free-solid-svg-icons/faBars';
import { faBell } from '@fortawesome/free-solid-svg-icons/faBell';
import { faBook } from '@fortawesome/free-solid-svg-icons/faBook';
import { faBolt } from '@fortawesome/free-solid-svg-icons/faBolt';
import { faBullseye } from '@fortawesome/free-solid-svg-icons/faBullseye';
import { faCheck } from '@fortawesome/free-solid-svg-icons/faCheck';
import { faCloud } from '@fortawesome/free-solid-svg-icons/faCloud';


import { faCode } from '@fortawesome/free-solid-svg-icons/faCode';
import { faCogs } from '@fortawesome/free-solid-svg-icons/faCogs';
import { faDatabase } from '@fortawesome/free-solid-svg-icons/faDatabase';
import { faDesktop } from '@fortawesome/free-solid-svg-icons/faDesktop';
import { faExpandArrowsAlt } from '@fortawesome/free-solid-svg-icons/faExpandArrowsAlt';
import { faFileAlt } from '@fortawesome/free-solid-svg-icons/faFileAlt';
import { faHashtag } from '@fortawesome/free-solid-svg-icons/faHashtag';
import { faLayerGroup } from '@fortawesome/free-solid-svg-icons/faLayerGroup';
import { faList } from '@fortawesome/free-solid-svg-icons/faList';
import { faListUl } from '@fortawesome/free-solid-svg-icons/faListUl';
import { faEye } from '@fortawesome/free-solid-svg-icons/faEye';
import { faFlag } from '@fortawesome/free-solid-svg-icons/faFlag';
import { faHeart } from '@fortawesome/free-solid-svg-icons/faHeart';
import { faLock } from '@fortawesome/free-solid-svg-icons/faLock';

import { faPencilAlt } from '@fortawesome/free-solid-svg-icons/faPencilAlt';
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus';
import { faRoad } from '@fortawesome/free-solid-svg-icons/faRoad';

import { faRocket } from '@fortawesome/free-solid-svg-icons/faRocket';
import { faSave } from '@fortawesome/free-solid-svg-icons/faSave';
import { faSearch } from '@fortawesome/free-solid-svg-icons/faSearch';
import { faServer } from '@fortawesome/free-solid-svg-icons/faServer';
import { faShieldAlt } from '@fortawesome/free-solid-svg-icons/faShieldAlt';
import { faSignInAlt } from '@fortawesome/free-solid-svg-icons/faSignInAlt';

import { faSignOutAlt } from '@fortawesome/free-solid-svg-icons/faSignOutAlt';
import { faSort } from '@fortawesome/free-solid-svg-icons/faSort';
import { faSortDown } from '@fortawesome/free-solid-svg-icons/faSortDown';
import { faSortUp } from '@fortawesome/free-solid-svg-icons/faSortUp';
import { faSync } from '@fortawesome/free-solid-svg-icons/faSync';
import { faTachometerAlt } from '@fortawesome/free-solid-svg-icons/faTachometerAlt';
import { faTasks } from '@fortawesome/free-solid-svg-icons/faTasks';
import { faThList } from '@fortawesome/free-solid-svg-icons/faThList';
import { faTimes } from '@fortawesome/free-solid-svg-icons/faTimes';
import { faTimesCircle } from '@fortawesome/free-solid-svg-icons/faTimesCircle';
import { faTrash } from '@fortawesome/free-solid-svg-icons/faTrash';
import { faUser } from '@fortawesome/free-solid-svg-icons/faUser';
import { faUserPlus } from '@fortawesome/free-solid-svg-icons/faUserPlus';
import { faUsers } from '@fortawesome/free-solid-svg-icons/faUsers';
import { faUsersCog } from '@fortawesome/free-solid-svg-icons/faUsersCog';
import { faWrench } from '@fortawesome/free-solid-svg-icons/faWrench';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';

const datetimeFormats: IntlDateTimeFormats = {
  ko: {
    short: { year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric' },
    medium: { year: 'numeric', month: 'short', day: 'numeric', weekday: 'short', hour: 'numeric', minute: 'numeric' },
    long: { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long', hour: 'numeric', minute: 'numeric' },
  },
  en: {
    short: { year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric' },
    medium: { year: 'numeric', month: 'short', day: 'numeric', weekday: 'short', hour: 'numeric', minute: 'numeric' },
    long: { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long', hour: 'numeric', minute: 'numeric' },
  },
  // jhipster-needle-i18n-language-date-time-format - JHipster will add/remove format options in this object
};

export function initFortAwesome(vue: App) {
  vue.component('FontAwesomeIcon', FontAwesomeIcon);

  library.add(
    faArrowLeft,
    faAsterisk,
    faBan,
    faBars,
    faBell,
    faBook,
    faBullseye,
    faCheck,
    faCloud,
    faCogs,
    faDatabase,
    faEye,
    faFlag,
    faBolt,
    faCode,
    faDesktop,
    faExpandArrowsAlt,
    faRocket,
    faServer,
    faShieldAlt,
    faLayerGroup,
    faList,
    faListUl,
    faFileAlt,
    faHashtag,
    faHeart,

    faLock,
    faPencilAlt,
    faPlus,
    faRoad,
    faSave,
    faSearch,
    faSignInAlt,
    faSignOutAlt,
    faSort,
    faSortDown,
    faSortUp,
    faSync,
    faTachometerAlt,
    faTasks,
    faThList,
    faTimes,
    faTimesCircle,
    faTrash,
    faUser,
    faUserPlus,
    faUsers,
    faUsersCog,
    faWrench,
  );
}
export function initI18N(opts: any = {}) {
  return createI18n({
    missingWarn: false,
    fallbackWarn: false,
    legacy: false,
    datetimeFormats,
    silentTranslationWarn: true,
    ...opts,
  });
}
