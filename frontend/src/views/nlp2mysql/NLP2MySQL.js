import React, { useState } from 'react';
import {
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormInput,
  CFormLabel,
  CFormSelect,
  CSpinner,
  CAlert,
  CInputGroup,
  CInputGroupText
} from '@coreui/react';
import axios from 'axios';

const NLP2MySQL = () => {
  const [dbParams, setDbParams] = useState({ host: '', database: '', user: '', password: '' });
  const [model, setModel] = useState('');
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [chatLog, setChatLog] = useState([]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setDbParams({ ...dbParams, [name]: value });
  };

  const connectToDB = async () => {
    setLoading(true);
    try {
      const response = await axios.post('/api/connect', dbParams);
      if (response.data.success) {
        setConnected(true);
        setMessage('Connection successful!');
      } else {
        setMessage('Connection failed!');
      }
    } catch (error) {
      setMessage('Connection error!');
    }
    setLoading(false);
  };

  const handleChatSubmit = async (e) => {
    e.preventDefault();
    const userMessage = e.target.elements.userMessage.value;
    setChatLog([...chatLog, { user: 'user', text: userMessage }]);
    try {
      const response = await axios.post('/api/chat', { message: userMessage, model });
      setChatLog([...chatLog, { user: 'user', text: userMessage }, { user: 'bot', text: response.data.reply }]);
    } catch (error) {
      setChatLog([...chatLog, { user: 'user', text: userMessage }, { user: 'bot', text: 'Error fetching response' }]);
    }
  };

  return (
    <div>
      <h1>MySQL Query</h1>
      <CCard>
        <CCardHeader>Connect to MySQL Database</CCardHeader>
        <CCardBody>
          <CForm>
            <CInputGroup className="mb-3">
              <CInputGroupText>Host IP</CInputGroupText>
              <CFormInput id="host" name="host" value={dbParams.host} onChange={handleInputChange} />
            </CInputGroup>
            <CInputGroup className="mb-3">
              <CInputGroupText>Database Name</CInputGroupText>
              <CFormInput id="database" name="database" value={dbParams.database} onChange={handleInputChange} />
            </CInputGroup>
            <CInputGroup className="mb-3">
              <CInputGroupText>User</CInputGroupText>
              <CFormInput id="user" name="user" value={dbParams.user} onChange={handleInputChange} />
            </CInputGroup>
            <CInputGroup className="mb-3">
              <CInputGroupText>Password</CInputGroupText>
              <CFormInput id="password" name="password" type="password" value={dbParams.password} onChange={handleInputChange} />
            </CInputGroup>
            <CButton color="primary" onClick={connectToDB} disabled={loading}>
              {loading ? <CSpinner size="sm" /> : 'Connect'}
            </CButton>
            {message && <CAlert color={connected ? 'success' : 'danger'}>{message}</CAlert>}
          </CForm>
        </CCardBody>
      </CCard>

      <CCard className="mt-4">
        <CCardHeader>Chatbot</CCardHeader>
        <CCardBody>
          <CForm onSubmit={handleChatSubmit}>
            <CInputGroup className="mb-3">
              <CInputGroupText>LLM Model</CInputGroupText>
              <CFormSelect id="model" value={model} onChange={(e) => setModel(e.target.value)}>
                <option value="" disabled>Select a model</option>
                <option value="model1">Gemini</option>
                <option value="model2">GPT</option>
              </CFormSelect>
            </CInputGroup>
            <CInputGroup className="mb-3">
              <CInputGroupText>Your Message</CInputGroupText>
              <CFormInput id="userMessage" name="userMessage" disabled={!connected || !model} />
            </CInputGroup>
            <CButton type="submit" color="primary" disabled={!connected || !model}>Send</CButton>
          </CForm>
          <div className="chat-log mt-3">
            {chatLog.map((entry, index) => (
              <div key={index} className={`chat-log-entry ${entry.user}`}>
                <span>{entry.user === 'user' ? 'You' : 'Bot'}: </span>{entry.text}
              </div>
            ))}
          </div>
        </CCardBody>
      </CCard>
    </div>
  );
};

export default NLP2MySQL;
