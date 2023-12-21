import './index.css';
import './i18n/config';
import 'tailwindcss/tailwind.css';

import React from 'react';
import ReactDOM from 'react-dom';

import DefaultLayout from './layouts/DefaultLayout';

console.log('VITE_APP_FOO', import.meta.env.VITE_APP_FOO);

ReactDOM.render(
  <React.StrictMode>
    <DefaultLayout />
  </React.StrictMode>,
  document.getElementById('root'),
);
