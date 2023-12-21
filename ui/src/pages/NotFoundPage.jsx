import React from 'react';
import { useLocation } from 'react-router-dom';

const NotFoundPage = () => {
  const location = useLocation();
  return (
    <div className="page">
      <b>Page not found:</b>
      {' ' + location.pathname}
    </div>
  );
};

export default NotFoundPage;
