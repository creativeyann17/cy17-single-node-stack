export const initialState = { foo: 0 };

export interface State {
  foo: number;
}

export interface Action {
  type: string;
  value: number;
}

export const SET_FOO_ACTION = 'set_foo';

export const setFoo = (value: number) => ({
  type: SET_FOO_ACTION,
  value,
});

function GlobalStateReducer(state: State, action: Action) {
  switch (action.type) {
    case SET_FOO_ACTION:
      return {
        ...state,
        foo: action.value,
      };
    default:
      throw new Error('Unknown action ' + action.type);
  }
}

export default GlobalStateReducer;
