import React from 'react';

const CustomButton = ({ onClick, label }) => {
  return <button onClick={onClick}>{label}</button>;
};

export default CustomButton;
