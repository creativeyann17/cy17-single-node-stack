import axios from 'axios';
import { makeUseAxios } from 'axios-hooks';

const useAxios = makeUseAxios({
  axios: axios.create({
    baseURL: 'https://reqres.in/api',
    timeout: 10000,
    headers: { 'my-header': 'foo' },
  }),
});

// add interceptors etc ...

export default useAxios;
