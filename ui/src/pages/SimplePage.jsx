import React from 'react';
import { useTranslation } from 'react-i18next';

import { debug } from '../utils';

const SimplePage = () => {
  const { t } = useTranslation();
  debug('foo', 'bar');
  return (
    <div className="page">
      <h1>{t('page1')}</h1>
    </div>
  );
};

export default SimplePage;
