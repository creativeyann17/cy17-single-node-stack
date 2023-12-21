import { useCallback } from 'react';

const padding = 13;

const getCurrentTimeFormatted = () => {
  const currentTime = new Date();
  const hours = currentTime.getHours();
  const minutes = currentTime.getMinutes();
  const seconds = currentTime.getSeconds();
  const milliseconds = currentTime.getMilliseconds();
  return `${hours}:${minutes}:${seconds}.${milliseconds}`;
};

const logger = (reducer: any) => {
  const reducerWithLogger = useCallback(
    (state: any, action: any) => {
      const start = Date.now();
      const next = reducer(state, action);
      console.group(
        `%caction %c${action.type} %c@ ${getCurrentTimeFormatted()} (in ${(
          Date.now() - start
        ).toFixed(2)} ms)`,
        'color: grey; font-weight: bold;',
        'color: black; font-weight: bold;',
        'color: grey; font-weight: lighter;',
      );
      console.log(
        '%cprev state:'.padEnd(padding),
        'color: grey; font-weight: bold;',
        state,
      );
      console.log(
        '%caction:'.padEnd(padding),
        'color: #03A9F4; font-weight: bold;',
        action,
      );
      console.log(
        '%cnext state:'.padEnd(padding),
        'color: #4CAF50; font-weight: bold;',
        next,
      );
      console.groupEnd();
      return next;
    },
    [reducer],
  );

  return reducerWithLogger;
};

export default logger;
