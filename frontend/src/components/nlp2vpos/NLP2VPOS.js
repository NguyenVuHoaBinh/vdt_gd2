import React, { useState, useEffect } from 'react';
import { CButton, CButtonGroup, CCard, CCardBody, CCardHeader, CSpinner } from '@coreui/react';
import InputField from '../InputField';
import ModalComponent from '../ModalComponent';
import ChatBox from '../ChatBox';
import ModelSelect from '../ModelSelect'; 

const NLP2VPOS = () => {
  const [dbParams, setDbParams] = useState({ host: '', database: '', user: '', password: '', dbType: 'postgres' });
  const [model, setModel] = useState('');
  const [modelType, setModelType] = useState('');
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalMessage, setModalMessage] = useState('');
  const [sessionId, setSessionId] = useState(null);
  const [viewMode, setViewMode] = useState('UI'); 
  const [logs, setLogs] = useState([]);
  const [chatLog, setChatLog] = useState([]);

  const apiConnectionEndpoint = 'https://localhost:8888/v2/vPOS_connect';

  // Generate a sessionId when component mounts or if not already set
  useEffect(() => {
    if (!sessionId) {
      const newSessionId = generateSessionId();
      setSessionId(newSessionId);
    }
  }, [sessionId]);

  // Initialize chatLog from localStorage when sessionId is available
  useEffect(() => {
    if (sessionId) {
      const savedChatLog = localStorage.getItem(`chatLog-${sessionId}`);
      if (savedChatLog) {
        setChatLog(JSON.parse(savedChatLog));
      }
    }
  }, [sessionId]);

  // Persist chatLog to localStorage when it changes
  useEffect(() => {
    if (sessionId) {
      localStorage.setItem(`chatLog-${sessionId}`, JSON.stringify(chatLog));
    }
  }, [chatLog, sessionId]);

  // // Initialize WebSocket for logs
  // useEffect(() => {
  //   const socket = new WebSocket('wss://localhost:8888/logs'); // Update with your WebSocket URL

  //   socket.onmessage = (event) => {
  //     const newLog = event.data;
  //     setLogs((prevLogs) => [...prevLogs, newLog]); // Accumulate logs
  //   };

  //   // Clean up WebSocket connection
  //   return () => socket.close();
  // }, []);

  const generateSessionId = () => {
    return `session-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
  };

  const startNewSession = () => {
    const newSessionId = generateSessionId();
    setSessionId(newSessionId);
    localStorage.removeItem(`chatLog-${sessionId}`); // Clear chat log for the old session
    setChatLog([]); // Clear chatLog state
    setConnected(false); // Reset connection status
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setDbParams({ ...dbParams, [name]: value });
  };

  const connectToDB = async () => {
    if (!model) {
      setModalMessage('Please select an LLM model before connecting.');
      setModalVisible(true);
      return;
    }

    setLoading(true);

    // Construct payload for debugging
    const payload = { ...dbParams, sessionId };
    console.log("Payload for connectToDB:", payload); // Debugging: Check your payload here

    try {
      const response = await fetch(apiConnectionEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
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

  return (
    <div>
      <h1>vPOS</h1>

      <div style={{ marginBottom: '20px' }}>
        {/* Button Group for view selection */}
        <CButtonGroup role="group" aria-label="View selection">
          <CButton 
            color={viewMode === 'UI' ? 'primary' : 'secondary'} 
            onClick={() => setViewMode('UI')}
          >
            UI
          </CButton>
          <CButton 
            color={viewMode === 'API UI' ? 'primary' : 'secondary'} 
            onClick={() => setViewMode('API UI')}
          >
            API UI
          </CButton>
          <CButton 
            color={viewMode === 'Log UI' ? 'primary' : 'secondary'} 
            onClick={() => setViewMode('Log UI')}
          >
            Log UI
          </CButton>
        </CButtonGroup>
      </div>

      {viewMode === 'UI' && (
        <div>
          {!connected ? (
            <CCard>
              <CCardHeader>Ban Hang khong cham</CCardHeader>
              <CCardBody>
                <InputField label="Host IP" name="host" value={dbParams.host} onChange={handleInputChange} />
                <InputField label="Database Name" name="database" value={dbParams.database} onChange={handleInputChange} />
                <InputField label="User" name="user" value={dbParams.user} onChange={handleInputChange} />
                <InputField label="Password" name="password" type="password" value={dbParams.password} onChange={handleInputChange} />
                
                <ModelSelect 
                  selectedModel={model} 
                  setModel={setModel} 
                  setModelType={setModelType} 
                  task="text_to_sql"
                />
                
                <CButton color="primary" onClick={connectToDB} disabled={loading}>
                  {loading ? <CSpinner size="sm" /> : 'Connect'}
                </CButton>
              </CCardBody>
            </CCard>
          ) : (
            <CCard>
              <CCardHeader>
                Chatbot (Session ID: {sessionId || 'N/A'})
                <CButton color="warning" onClick={startNewSession} className="float-end">
                  Start New Session
                </CButton>
              </CCardHeader>
              <CCardBody>
                {/* Render ChatBox component with necessary props */}
                <ChatBox 
                  model={model} 
                  modelType={modelType} 
                  sessionId={sessionId} 
                  connected={connected} 
                  chatLog={chatLog} 
                  setChatLog={setChatLog} 
                />
              </CCardBody>
            </CCard>
          )}
        </div>
      )}

      {viewMode === 'API UI' && (
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
  "dbType": "postgres"
}`}
              </pre>
              <button onClick={() => navigator.clipboard.writeText(`{
  "host": "required",
  "database": "required",
  "user": "required",
  "password": "required",
  "dbType": "postgres"
}`)}>Copy</button>
            </div>
          </CCardBody>
        </CCard>
      )}

      {viewMode === 'Log UI' && (
        <CCard>
          <CCardHeader>Log UI</CCardHeader>
          <CCardBody style={{ maxHeight: '400px', overflowY: 'scroll' }}>
            {logs.length > 0 ? (
              logs.map((log, index) => <div key={index}>{log}</div>)
            ) : (
              <p>No logs available.</p>
            )}
          </CCardBody>
        </CCard>
      )}

      <ModalComponent visible={modalVisible} message={modalMessage} onClose={() => setModalVisible(false)} />
    </div>
  );
};

export default NLP2VPOS;
