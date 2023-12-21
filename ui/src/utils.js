import isEmpty from 'lodash/isEmpty';

export const DEBUG = import.meta.env.DEV;

export const debug = (message, ...args) => {
  if (DEBUG) {
    if (!isEmpty(args)) {
      console.log(message, args);
    } else {
      console.log(message);
    }
  }
};
