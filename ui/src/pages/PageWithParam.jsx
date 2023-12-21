import React from 'react';
import { useParams } from 'react-router-dom';

const NotFoundPage = () => {
  const { id } = useParams();
  return (
    <div className="page">
      <h1>Page with param:{' ' + id}</h1>
    </div>
  );
};

export default NotFoundPage;
