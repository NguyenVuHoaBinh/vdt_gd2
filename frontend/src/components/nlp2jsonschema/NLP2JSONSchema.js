import React, { useState, useEffect } from 'react';
import { CButton, CCard, CCardBody, CCardHeader, CSpinner } from '@coreui/react';
import ModalComponent from '../ModalComponent';
import ChatBox from '../ChatBox';
import ModelSelect from '../ModelSelect'; // Import the modified ModelSelect component

const NLP2JSONSchema = () => {
  const [dbParams, setDbParams] = useState({ path: '', dbType: 'jsonschema' });
  const [sessionId, setSessionId] = useState(null);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalMessage, setModalMessage] = useState('');
  const [connected, setConnected] = useState(false);
  const [loading, setLoading] = useState(false);
  const [files, setFiles] = useState([]);
  const [model, setModel] = useState(''); // State for selected model
  const [modelType, setModelType] = useState(''); // State for selected model type

  const apiConnectionEndpoint = 'https://localhost:8888/jsonschema/connect';

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

  const handleFileChange = (e) => {
    setFiles(Array.from(e.target.files));
  };

  const handleModelChange = (selectedModel) => {
    setModel(selectedModel);
  };

  const connectToDB = async (updatedParams = dbParams) => {
    setLoading(true);
    try {
      const response = await fetch(apiConnectionEndpoint, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ ...updatedParams, sessionId }),
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
      console.error('Connection error:', error);
      setModalMessage(`Connection error: ${error.message}`);
    }
    setLoading(false);
    setModalVisible(true);
  };

  const uploadFiles = async () => {
    if (files.length === 0) {
      setModalMessage('Please upload at least one JSON file.');
      setModalVisible(true);
      return;
    }

    if (!model) {
      setModalMessage('Please select a model before proceeding.');
      setModalVisible(true);
      return;
    }

    setLoading(true);
    const formData = new FormData();

    // Correctly append each file to FormData
    files.forEach((file) => {
      formData.append('files', file, file.name);
    });
    formData.append('sessionId', sessionId);

    try {
      const response = await fetch('https://localhost:8888/upload', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        throw new Error(`Server error: ${response.status}`);
      }

      const result = await response.json();

      if (result.success) {
        // Store the path in dbParams and proceed to connect
        setDbParams((prevParams) => {
          const updatedParams = { ...prevParams, path: result.folderPath };
          connectToDB(updatedParams); // Call connectToDB after setting dbParams
          return updatedParams;
        });
        setModalMessage(result.message); // Use the server's success message
      } else {
        setModalMessage(`Upload failed: ${result.message || 'Unknown error'}`);
      }
    } catch (error) {
      console.error('Upload error:', error);
      setModalMessage(`Upload error: ${error.message}`);
    }

    setLoading(false);
    setModalVisible(true);
  };

  return (
    <div>
      <h1>JSON Schema Query</h1>

      {!connected ? (
        <CCard>
          <CCardHeader>Upload JSON Files</CCardHeader>
          <CCardBody>
            <input
              type="file"
              accept=".json"
              multiple
              onChange={handleFileChange}
              style={{ display: 'block', marginBottom: '20px' }}
            />
            {/* Add Model Selection with dynamic task */}
            <ModelSelect 
              selectedModel={model} 
              setModel={handleModelChange} 
              setModelType={setModelType} 
              task="text_to_sql" // Dynamically set the task type
            />

            <CButton color="primary" onClick={uploadFiles} disabled={loading}>
              {loading ? <CSpinner size="sm" /> : 'Upload & Continue'}
            </CButton>
          </CCardBody>
        </CCard>
      ) : (
        <CCard>
          <CCardHeader>
            Chatbot (Session ID: {sessionId || 'N/A'})
            <CButton color="warning" onClick={() => setSessionId(generateSessionId())} className="float-end">
              Start New Session
            </CButton>
          </CCardHeader>
          <CCardBody>
            <ChatBox sessionId={sessionId} connected={connected} dbParams={dbParams} model={model} />
          </CCardBody>
        </CCard>
      )}

      <ModalComponent visible={modalVisible} message={modalMessage} onClose={() => setModalVisible(false)} />
    </div>
  );
};

export default NLP2JSONSchema;
