import React, { Dispatch, ReactNode, useReducer } from 'react';
import { createContext } from 'use-context-selector';

import { DEBUG } from '../utils';
import GlobalStateReducer, { Action, initialState, State } from './GlobalStateReducer';

interface Props {
  children?: ReactNode;
}

interface Context {
  globalState: State;
  dispatch: Dispatch<Action>;
}

export const GlobalContext = createContext<Context>({} as Context);

const GlobalStateProvider = ({ children }: Props) => {
  let reducer = GlobalStateReducer;
  if (DEBUG) {
    // eslint-disable-next-line @typescript-eslint/no-var-requires
    const logger = require('./ReducerLogger');
    reducer = logger(GlobalStateReducer);
  }

  const [globalState, dispatch] = useReducer(reducer, initialState);

  return (
    <GlobalContext.Provider value={{ globalState, dispatch }}>
      {children}
    </GlobalContext.Provider>
  );
};

export default GlobalStateProvider;
