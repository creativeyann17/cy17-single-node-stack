import useAxios from 'axios-hooks';
import React from 'react';

const HelloPage = () => {
  const [{ data, loading, error }] = useAxios('/api/v1/hello');
  return (
    <div className="page">
      <h1>{loading ? 'loading...' : error ? error.message : data}</h1>
    </div>
  );
};

export default HelloPage;
