import React from 'react';
import { useContextSelector } from 'use-context-selector';

import { GlobalContext } from '../state/GlobalStateProvider';
import { setFoo } from '../state/GlobalStateReducer';

const ContextPage = () => {
  const foo = useContextSelector(GlobalContext, (v) => v.globalState.foo);
  const dispatch = useContextSelector(GlobalContext, (v) => v.dispatch);
  return (
    <>
      <p>{foo}</p>
      <button
        onClick={() => {
          dispatch(setFoo(foo + 1));
        }}
      >
        Click me!
      </button>
    </>
  );
};

export default ContextPage;
