import React, { useState, useEffect } from 'react';
import { CButton, CCard, CCardBody, CCardHeader, CSpinner } from '@coreui/react';
import Switch from "react-switch";
import InputField from '../InputField';
import ModalComponent from '../ModalComponent';
import ChatBox from '../ChatBox';
import ModelSelect from '../ModelSelect';

const NLP2MSSQL = () => {
  const [dbParams, setDbParams] = useState({ host: '', database: '', user: '', password: '', dbType: 'mssql' });
  const [model, setModel] = useState('');
  const [modelType, setModelType] = useState('');
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalMessage, setModalMessage] = useState('');
  const [sessionId, setSessionId] = useState(null);
  const [apiView, setApiView] = useState(false);  // Initialize apiView state

  const apiConnectionEndpoint = 'https://localhost:8888/connect';

  useEffect(() => {
    let currentSessionId = sessionId;
    if (!currentSessionId) {
      currentSessionId = generateSessionId();
      setSessionId(currentSessionId);
    }
  }, [sessionId]);

  const generateSessionId = () => {
    return `session-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
  };

  const startNewSession = () => {
    const newSessionId = generateSessionId();
    setSessionId(newSessionId);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setDbParams({ ...dbParams, [name]: value });
  };

  const handleModelChange = (e) => {
    const selectedModel = modelOptions.find(option => option.value === e.target.value);
    setModel(selectedModel.value);
    setModelType(selectedModel.type);
  };

  const connectToDB = async () => {
    if (!model) {
      setModalMessage('Please select an LLM model before connecting.');
      setModalVisible(true);
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(apiConnectionEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ ...dbParams, sessionId }),
      });
      const result = await response.json();

      if (result.success) {
        setConnected(true);
        let message = 'Connection established!';
        if (result.ingestionResult) {
          message += `\nIngestion Result: ${result.ingestionResult}`;
        }
        setModalMessage(message);
      } else {
        setModalMessage(`Connection failed: ${result.message || 'Unknown error'}`);
      }
    } catch (error) {
      setModalMessage(`Connection error: ${error.message}`);
    }
    setLoading(false);
    setModalVisible(true);
  };

  const handleSwitchChange = () => {
    setApiView(!apiView);  // Toggle apiView state
  };

  return (
    <div>
      <h1>MSSQL Query</h1>
      <div style={{ marginBottom: '20px' }}>
        <label>
          <span style={{ marginRight: '10px' }}>UI</span>
          <Switch onChange={handleSwitchChange} checked={apiView} uncheckedIcon={false} checkedIcon={false} />
          <span style={{ marginLeft: '10px' }}>API UI</span>
        </label>
      </div>

      {!connected ? (
        <div>
          {!apiView ? (
            <CCard>
              <CCardHeader>Connect to MSSQL Database</CCardHeader>
              <CCardBody>
                <InputField label="Host IP" name="host" value={dbParams.host} onChange={handleInputChange} />
                <InputField label="Database Name" name="database" value={dbParams.database} onChange={handleInputChange} />
                <InputField label="User" name="user" value={dbParams.user} onChange={handleInputChange} />
                <InputField label="Password" name="password" type="password" value={dbParams.password} onChange={handleInputChange} />
                <ModelSelect selectedModel={model} setModel={setModel} setModelType={setModelType} />
                <CButton color="primary" onClick={connectToDB} disabled={loading}>
                  {loading ? <CSpinner size="sm" /> : 'Connect'}
                </CButton>
              </CCardBody>
            </CCard>
          ) : (
            <CCard>
              <CCardHeader>API UI - Connection</CCardHeader>
              <CCardBody>
                <h4>API Connection Endpoint</h4>
                <div style={{ padding: '10px', backgroundColor: '#f1f1f1', borderRadius: '5px' }}>
                  <code>https://localhost:8888/connect</code>
                  <button onClick={() => navigator.clipboard.writeText('https://localhost:8888/connect')}>Copy</button>
                </div>

                <h4 className="mt-3">Request Body for Connection</h4>
                <div style={{ padding: '10px', backgroundColor: '#f1f1f1', borderRadius: '5px' }}>
                  <pre>
                    {`{
  "host": "required",
  "database": "required",
  "user": "required",
  "password": "required",
  "dbType": "mssql"
}`}
                  </pre>
                  <button onClick={() => navigator.clipboard.writeText(`{
  "host": "required",
  "database": "required",
  "user": "required",
  "password": "required",
  "dbType": "mssql"
}`)}>Copy</button>
                </div>
              </CCardBody>
            </CCard>
          )}
        </div>
      ) : (
        // Display Chat UI when connected
        !apiView ? (
          <CCard>
            <CCardHeader>
              Chatbot (Session ID: {sessionId || 'N/A'})
              <CButton color="warning" onClick={startNewSession} className="float-end">
                Start New Session
              </CButton>
            </CCardHeader>
            <CCardBody>
              <ChatBox model={model} modelType={modelType} sessionId={sessionId} connected={connected} />
            </CCardBody>
          </CCard>
        ) : (
          <CCard>
            <CCardHeader>API UI - Chat</CCardHeader>
            <CCardBody>
              <h4>API Chat Endpoint</h4>
              <div style={{ padding: '10px', backgroundColor: '#f1f1f1', borderRadius: '5px' }}>
                <code>https://localhost:8888/chat</code>
                <button onClick={() => navigator.clipboard.writeText('https://localhost:8888/chat')}>Copy</button>
              </div>

              <h4 className="mt-3">Request Body for Chat</h4>
              <div style={{ padding: '10px', backgroundColor: '#f1f1f1', borderRadius: '5px' }}>
                <pre>
                  {`{
  "message": "your_message",
  "model": "${model}",
  "sessionId": "${sessionId}"
}`}
                </pre>
                <button onClick={() => navigator.clipboard.writeText(`{
  "message": "your_message",
  "model": "${model}",
  "sessionId": "${sessionId}"
}`)}>Copy</button>
              </div>
            </CCardBody>
          </CCard>
        )
      )}

      <ModalComponent visible={modalVisible} message={modalMessage} onClose={() => setModalVisible(false)} />
    </div>
  );
};

export default NLP2MSSQL;