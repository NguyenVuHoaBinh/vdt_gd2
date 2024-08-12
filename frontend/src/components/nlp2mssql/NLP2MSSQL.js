import React, { useState, useEffect, useRef } from 'react';
import {
  CButton,
  CCard,
  CCardBody,
  CCardHeader,
  CForm,
  CFormInput,
  CFormSelect,
  CSpinner,
  CInputGroup,
  CInputGroupText,
  CModal,
  CModalBody,
  CModalFooter,
  CModalHeader,
  CModalTitle
} from '@coreui/react';
import {
  MessageBox,
  MessageList,
  Input,
  Button as ChatButton
} from 'react-chat-elements';
import 'react-chat-elements/dist/main.css';

const NLP2MSSQL = () => {
  const [dbParams, setDbParams] = useState({ host: '', database: '', user: '', password: '', dbType: 'mssql' });
  const [model, setModel] = useState('');
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalMessage, setModalMessage] = useState('');
  const [chatLog, setChatLog] = useState([]);
  const [metadata, setMetadata] = useState('');
  const [queryResult, setQueryResult] = useState(null);
  const chatRef = useRef(null);

  useEffect(() => {
    // Scroll to the bottom when chat log updates
    if (chatRef.current) {
      chatRef.current.scrollTop = chatRef.current.scrollHeight;
    }
  }, [chatLog]);

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
    try {
      const response = await fetch('http://localhost:8089/connect', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(dbParams),
      });
      const result = await response.json();
      
      if (result.success) {
        setConnected(true);
        let message = 'Connection established!';
        setMetadata(result.metadata);
        
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

  const handleChatSubmit = async (message) => {
    if (!model) {
      setModalMessage('Please select an LLM model before sending a message.');
      setModalVisible(true);
      return;
    }

    if (!connected) {
      setModalMessage('Please connect to the database before sending a message.');
      setModalVisible(true);
      return;
    }

    const newChatLog = [...chatLog, { user: 'user', text: message }];
    setChatLog(newChatLog);

    try {
      const role = "You are a powerful AI Assistant. You respond only to any request that is related to data for Microsoft SQL Server queries. Your task is to convert their request into SQL queries. The next part is the metadata that help you generate right queries.";
      const response = await fetch('http://localhost:8089/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message, model, role, metadata }),
      });
      const result = await response.json();

      // Extract the query result and the SQL query from the response
      setChatLog([...newChatLog, { 
        user: 'bot', 
        text:   `${result.fullResponse}\n\n
                SQL Query: ${result.sqlQuery}\n\n
                Result:\n${JSON.stringify(result.queryResult, null, 2)}` }]);
      setQueryResult(result.queryResult);
    } catch (error) {
      setChatLog([...newChatLog, { user: 'bot', text: 'Error fetching response' }]);
    }
  };

  const handleFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) {
      setModalMessage('No file selected.');
      setModalVisible(true);
      return;
    }

    const formData = new FormData();
    formData.append('file', file);
    try {
      const response = await fetch('http://localhost:8089/upload', {
        method: 'POST',
        body: formData,
      });
      const result = await response.json();
      setModalMessage(result.response || 'File uploaded successfully');
    } catch (error) {
      setModalMessage(`Error uploading file: ${error.message}`);
    }
    setModalVisible(true);
  };

  return (
    <div>
      <h1>MSSQL Query</h1>
      <CCard>
        <CCardHeader>Connect to MSSQL Database</CCardHeader>
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
                <option value="mssql">sqlserver</option>
              </CFormSelect>
            </CInputGroup>
            <CInputGroup className="mb-3">
              <CInputGroupText>LLM Model</CInputGroupText>
              <CFormSelect id="model" value={model} onChange={(e) => setModel(e.target.value)}>
                <option value="" disabled>Select a model</option>
                <option value="gpt-3">GPT-3</option>
                <option value="gemini">Gemini</option>
              </CFormSelect>
            </CInputGroup>
            <CButton color="primary" onClick={connectToDB} disabled={loading}>
              {loading ? <CSpinner size="sm" /> : 'Connect'}
            </CButton>
          </CForm>
        </CCardBody>
      </CCard>

      <CCard className="mt-4">
        <CCardHeader>Chatbot</CCardHeader>
        <CCardBody>
          <div ref={chatRef} style={{ maxHeight: '300px', overflowY: 'auto' }}>
            <MessageList
              className="message-list"
              lockable={true}
              toBottomHeight={'100%'}
              dataSource={chatLog.map((entry, index) => ({
                position: entry.user === 'user' ? 'right' : 'left',
                type: 'text',
                text: entry.text,
                date: new Date(),
              }))}
            />
          </div>
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

      <CModal visible={modalVisible} onClose={() => setModalVisible(false)}>
        <CModalHeader>
          <CModalTitle>Notification</CModalTitle>
        </CModalHeader>
        <CModalBody>
          {modalMessage}
        </CModalBody>
        <CModalFooter>
          <CButton color="secondary" onClick={() => setModalVisible(false)}>Close</CButton>
        </CModalFooter>
      </CModal>
    </div>
  );
};

export default NLP2MSSQL;
