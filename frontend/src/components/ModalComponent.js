import React from 'react';
import { CModal, CModalBody, CModalFooter, CModalHeader, CModalTitle, CButton } from '@coreui/react';

const ModalComponent = ({ visible, message, onClose }) => (
  <CModal visible={visible} onClose={onClose}>
    <CModalHeader>
      <CModalTitle>Notification</CModalTitle>
    </CModalHeader>
    <CModalBody>{message}</CModalBody>
    <CModalFooter>
      <CButton color="secondary" onClick={onClose}>Close</CButton>
    </CModalFooter>
  </CModal>
);

export default ModalComponent;
