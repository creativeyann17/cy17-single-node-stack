import useAxios from 'axios-hooks';
import React from 'react';

import { default as useCustomAxios } from '../apis/CustomAPI';
import { debug } from '../utils';

const AxiosPage = () => {
  const [{ data, loading, error }, refetch] = useAxios(
    'https://reqres.in/api/users?delay=2',
  );
  const [anotherCall, anotherCallRefetch] = useCustomAxios('/users?delay=1');

  debug('axios - default', data, loading, error);
  debug('axios - 1', anotherCall);

  return (
    <>
      <div>
        {loading && <p>Loading...</p>}
        {error && <p>{error}</p>}
        <button onClick={refetch}>refetch</button>
        <pre>{JSON.stringify(data, null, 2)}</pre>
      </div>

      <div>
        {anotherCall.loading && <p>Loading1...</p>}
        {anotherCall.error && <p>{anotherCall.error}</p>}
        <button onClick={anotherCallRefetch}>refetch</button>
        <pre>{JSON.stringify(anotherCall.data, null, 2)}</pre>
      </div>
    </>
  );
};

export default AxiosPage;
