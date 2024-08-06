import React, { useState } from 'react';
import {
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormInput,
  CFormSelect,
  CSpinner,
  CAlert,
  CInputGroup,
  CInputGroupText
} from '@coreui/react';
import {
  MessageBox,
  MessageList,
  Input,
  Button as ChatButton
} from 'react-chat-elements';
import 'react-chat-elements/dist/main.css';

const NLP2MySQL = () => {
  const [dbParams, setDbParams] = useState({ host: '', database: '', user: '', password: '', dbType: 'mysql' });
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
      const response = await fetch('http://localhost:8080/connect', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(dbParams),
      });
      const result = await response.json();
      if (result.success) {
        setConnected(true);
        setMessage('Connection successful!');
      } else {
        setMessage(`Connection failed: ${result.message || 'Unknown error'}`);
      }
    } catch (error) {
      setMessage('Connection error!');
    }
    setLoading(false);
  };

  const handleChatSubmit = async (message) => {
    setChatLog([...chatLog, { user: 'user', text: message }]);
    try {
      const response = await fetch('http://localhost:8080/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message, model }),
      });
      const result = await response.json();
      setChatLog([...chatLog, { user: 'user', text: message }, { user: 'bot', text: result.response }]);
    } catch (error) {
      setChatLog([...chatLog, { user: 'user', text: message }, { user: 'bot', text: 'Error fetching response' }]);
    }
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    const formData = new FormData();
    formData.append('file', file);
    try {
      const response = await fetch('http://localhost:8080/upload', {
        method: 'POST',
        body: formData,
      });
      const result = await response.json();
      setMessage(result.message || 'File uploaded successfully');
    } catch (error) {
      setMessage('Error uploading file');
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
            <CInputGroup className="mb-3">
              <CInputGroupText>Database Type</CInputGroupText>
              <CFormSelect id="dbType" name="dbType" value={dbParams.dbType} onChange={handleInputChange}>
                <option value="mysql">MySQL</option>
              </CFormSelect>
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
          <CInputGroup className="mb-3">
            <CInputGroupText>LLM Model</CInputGroupText>
            <CFormSelect id="model" value={model} onChange={(e) => setModel(e.target.value)}>
              <option value="" disabled>Select a model</option>
              <option value="gpt-3">GPT</option>
              <option value="gemini">Gemini</option>
            </CFormSelect>
          </CInputGroup>
          <MessageList
            className='message-list'
            lockable={true}
            toBottomHeight={'100%'}
            dataSource={chatLog.map((entry, index) => ({
              position: entry.user === 'user' ? 'right' : 'left',
              type: 'text',
              text: entry.text,
              date: new Date(),
            }))}
          />
          <Input
            placeholder="Type message here"
            defaultValue=""
            onKeyPress={(e) => {
              if (e.shiftKey && e.charCode === 13) {
                return true;
              }
              if (e.charCode === 13 && !e.shiftKey) {
                handleChatSubmit(e.target.value);
                e.target.value = "";
                return false;
              }
            }}
            rightButtons={
              <ChatButton
                color='white'
                backgroundColor='black'
                text='Send'
                onClick={() => {
                  const input = document.querySelector('.rce-input').value;
                  handleChatSubmit(input);
                  document.querySelector('.rce-input').value = '';
                }}
              />
            }
          />
          <CInputGroup className="mt-3">
            <CInputGroupText>Upload File</CInputGroupText>
            <CFormInput type="file" onChange={handleFileUpload} />
          </CInputGroup>
        </CCardBody>
      </CCard>
    </div>
  );
};

export default NLP2MySQL;
