import React from 'react';
import { CInputGroup, CInputGroupText, CFormInput } from '@coreui/react';

const InputField = ({ label, name, value, onChange, type = 'text' }) => (
  <CInputGroup className="mb-3">
    <CInputGroupText>{label}</CInputGroupText>
    <CFormInput id={name} name={name} value={value} onChange={onChange} type={type} />
  </CInputGroup>
);

export default InputField;
