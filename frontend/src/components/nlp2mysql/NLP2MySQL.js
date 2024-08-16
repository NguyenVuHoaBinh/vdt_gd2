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
  Input,
  Button as ChatButton
} from 'react-chat-elements';
import 'react-chat-elements/dist/main.css';
import ReactMarkdown from 'react-markdown';
import rehypeRaw from 'rehype-raw';

const NLP2MySQL = () => {
  const [dbParams, setDbParams] = useState({ host: '', database: '', user: '', password: '', dbType: 'mysql' });
  const [model, setModel] = useState('');
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalMessage, setModalMessage] = useState('');
  const [chatLog, setChatLog] = useState([]);
  const [metadata, setMetadata] = useState('');
  const [queryResult, setQueryResult] = useState(null);
  const [sessionId, setSessionId] = useState(null); // State for session ID
  const chatRef = useRef(null);

  // Effect to generate session ID on page load
  useEffect(() => {
    let currentSessionId = sessionId;
    if (!currentSessionId) {
      // If no session ID exists, generate a new one and store it
      currentSessionId = generateSessionId();
      setSessionId(currentSessionId);
    }
  }, []); // Empty dependency array means this effect runs once when the component mounts

  const generateSessionId = () => {
    return `session-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
  };

  const startNewSession = () => {
    const newSessionId = generateSessionId();
    setSessionId(newSessionId);
    setChatLog([]);
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

    if (!sessionId) {
      startNewSession();  // Generate a session ID if none exists
    }

    setLoading(true);
    try {
      const response = await fetch('http://localhost:8888/connect', {
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

    if (!sessionId) {
      startNewSession();  // Generate a session ID if none exists
    }

    const newChatLog = [...chatLog, { user: 'user', text: message }];
    setChatLog(newChatLog);

    try {
      const role = "You are a powerful AI Assistant. You respond only to any request that is related to data for MySQL queries. Your task is to convert their request into SQL queries. The next part is the metadata that help you generate right queries.";
      const response = await fetch('http://localhost:8888/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message, model, systemRole: role, metadata, sessionId }),
      });
      const result = await response.json();

      const table = generateTable(result.queryResult);

      const messages = [
        { text: `${result.fullResponse}`, user: 'bot' },
        { text: `**SQL Query:**\n\`\`\`sql\n${result.sqlQuery}\n\`\`\``, user: 'bot' },
        { text: `**Result:**\n${table}`, user: 'bot' }
      ];

      if (result.imageId) {
        const imageUrl = await fetchImage(result.imageId);
        messages.push({ user: 'bot', text: `<img src="${imageUrl}" alt="Response Image" style="max-width: 100%; height: auto;" />` });
      }

      const addMessagesWithDelay = (messages, delay) => {
        messages.forEach((message, index) => {
          setTimeout(() => {
            setChatLog(prevChatLog => [...prevChatLog, message]);
          }, index * delay);
        });
      };

      addMessagesWithDelay(messages, 1000);

      setQueryResult(result.queryResult);
    } catch (error) {
      setChatLog([...newChatLog, { user: 'bot', text: 'Error fetching response' }]);
    }
  };

  const fetchImage = async (imageId) => {
    try {
      const response = await fetch(`http://localhost:8888/image/${imageId}`, {
        method: 'GET',
      });
      const blob = await response.blob();
      const url = URL.createObjectURL(blob);
      return url;
    } catch (error) {
      console.error("Error fetching image:", error);
      return null;
    }
  };

  const generateTable = (data) => {
    if (!Array.isArray(data) || data.length === 0) return '';

    const headers = Object.keys(data[0]);

    const headerRow = headers.map(header => `<th>${header}</th>`).join('');

    const rows = data.map(row => {
      const rowData = headers.map(header => `<td>${row[header]}</td>`).join('');
      return `<tr>${rowData}</tr>`;
    }).join('');

    return `<table style="width: 100%; border-collapse: collapse; border: 1px solid black;">
      <thead><tr>${headerRow}</tr></thead>
      <tbody>${rows}</tbody>
    </table>`;
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
      const response = await fetch('http://localhost:8888/upload', {
        method: 'POST',
        body: formData,
      });
      const result = await response.json();
      setModalMessage(result.response || 'File uploaded successfully');

      if (result.imageId) {
        const imageUrl = await fetchImage(result.imageId);
        // Add the image to the chat log
        setChatLog(prevChatLog => [
          ...prevChatLog, 
          { user: 'user', text: `<img src="${imageUrl}" alt="Uploaded Image" style="max-width: 100%; height: auto;" />` }
        ]);
      }
    } catch (error) {
      setModalMessage(`Error uploading file: ${error.message}`);
    }
    setModalVisible(true);
  };

  const renderChatMessage = (entry) => {
    return (
      <div
        key={entry.text}
        style={{ marginBottom: '10px', textAlign: entry.user === 'user' ? 'right' : 'left' }}
      >
        <ReactMarkdown rehypePlugins={[rehypeRaw]}>
          {entry.text}
        </ReactMarkdown>
      </div>
    );
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
        <CCardHeader>
          Chatbot (Session ID: {sessionId || 'N/A'})
          <CButton color="warning" onClick={startNewSession} className="float-end">
            Start New Session
          </CButton>
        </CCardHeader>
        <CCardBody>
          <div ref={chatRef} style={{ maxHeight: '300px', overflowY: 'auto' }}>
            {chatLog.map(renderChatMessage)}
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

export default NLP2MySQL;
