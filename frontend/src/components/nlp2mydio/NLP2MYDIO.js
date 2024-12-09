import React, { useState, useEffect, useRef, useCallback } from 'react';
import { CButton, CButtonGroup, CCard, CCardBody, CCardHeader, CSpinner } from '@coreui/react';
import ChatBoxMydio from '../ChatBoxMydio';
import ModalComponent from '../ModalComponent';
import SpeechRecognition, { useSpeechRecognition } from 'react-speech-recognition';
import { FaMicrophone } from 'react-icons/fa';

const NLP2MYDIO = () => {
  const [model, setModel] = useState('gpt-3');
  const [connected, setConnected] = useState(true);
  const [sessionId, setSessionId] = useState(null);
  const [viewMode, setViewMode] = useState('UI'); 
  const [logs, setLogs] = useState([]);
  const [chatLog, setChatLog] = useState([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [modalMessage, setModalMessage] = useState('');
  const [inputValue, setInputValue] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const typingTimeoutRef = useRef(null);

  const {
    transcript,
    listening,
    resetTranscript,
    browserSupportsSpeechRecognition
  } = useSpeechRecognition();

  if (!browserSupportsSpeechRecognition) {
    return <span>Browser doesn't support speech recognition.</span>;
  }

  // Generate a sessionId when component mounts or if not already set
  // Generate a sessionId when component mounts or if not already set
  useEffect(() => {
    if (!sessionId) {
      const newSessionId = generateSessionId();
      setSessionId(newSessionId);
    } else {
      // Auto trigger greeting endpoint when sessionId is set
      const triggerGreeting = async () => {
        try {
          const response = await fetch('https://cd83-58-187-4-197.ngrok-free.app/v2/Mydio_greeting', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sessionId })
          });
          if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
          const result = await response.json();
          setChatLog((prevChatLog) => [
            ...prevChatLog,
            { user: 'bot', text: result.fullResponse }
          ]);
          if (result.audio) {
            console.log('Audio data found, playing audio.');
            const audioBase64 = result.audio;
            const audioBlob = new Blob([new Uint8Array(atob(audioBase64).split('').map(c => c.charCodeAt(0)))], { type: 'audio/wav' });
            const audioUrl = URL.createObjectURL(audioBlob);
            const audio = new Audio(audioUrl);
            audio.play();
            audio.addEventListener('ended', () => {
              URL.revokeObjectURL(audioUrl);
            });
          }
        } catch (error) {
          console.error('Error with greeting API:', error);
        }
      };
      triggerGreeting();
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

  const generateSessionId = () => {
    return `session-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
  };

  const startNewSession = () => {
    const newSessionId = generateSessionId();
    setSessionId(newSessionId);
    localStorage.removeItem(`chatLog-${sessionId}`); // Clear chat log for the old session
    setChatLog([]); // Clear chatLog state
  };

  const callApiEndpoint = async (message, model) => {
    let currentSessionId = sessionId;
    if (!currentSessionId) {
      currentSessionId = generateSessionId();
      setSessionId(currentSessionId);
    }
    console.log('callApiEndpoint called with message and model:', message, model);
    try {
      const response = await fetch('https://cd83-58-187-4-197.ngrok-free.app/v2/Mydio_chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message, model, sessionId: currentSessionId }),
      });
  
      console.log('API response status:', response.status);
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
      const result = await response.json();
      console.log('API response result:', result);
  
      // Prepare messages to add to chatLog
      const newMessages = [];
  
      if (result.fullResponse?.trim()) {
        newMessages.push({ text: result.fullResponse, user: 'bot' });
      }
  
      if (result.sqlQuery?.trim()) {
        newMessages.push({
          text: `**SQL Query:**\n\
          \`\`\`sql\n${result.sqlQuery}\n\
          \`\`\``,
          user: 'bot',
        });
      }
  
      if (Array.isArray(result.queryResult) && result.queryResult.length > 0) {
        const table = generateTable(result.queryResult);
        newMessages.push({ text: `**Result:**\n${table}`, user: 'bot' });
      }
  
      // Add new messages to chatLog
      setChatLog((prevChatLog) => [...prevChatLog, ...newMessages]);
  
      // Check if audio data is available and handle it
      if (result.audio) {
        console.log('Audio data found, playing audio.');
        SpeechRecognition.stopListening();
        const audioBase64 = result.audio;
        const audioBlob = new Blob([new Uint8Array(atob(audioBase64).split('').map(c => c.charCodeAt(0)))], { type: 'audio/wav' });
        const audioUrl = URL.createObjectURL(audioBlob);
        const audio = new Audio(audioUrl);
        audio.play();
        audio.addEventListener('ended', () => {
          URL.revokeObjectURL(audioUrl);
          
        });
      }
  
      // Clear other states as needed based on response properties
      if (result.billInfo) setBillInfo(result.billInfo);
      if (result.productInfo) setProductInfo(result.productInfo);
      if (result.productAdjust) setProductAdjust(result.productAdjust);
      if (result.businessInfo) setBusinessInfo(result.businessInfo);
      if (result.debtInfo) setDebtInfo(result.debtInfo);
      if (result.inactiveInfo) setInactiveInfo(result.inactiveInfo);
      if (result.restockAlertInfo) setRestockAlertInfo(result.restockAlertInfo);
      if (result.trendInfo) setTrendInfo(result.trendInfo);
  
    } catch (error) {
      console.error('Error with API models:', error);
      setChatLog((prevChatLog) => [
        ...prevChatLog,
        { user: 'bot', text: 'Error with API model response' },
      ]);
    }
  };
  

  const handleChatSubmit = useCallback(
    async (message) => {
      if (!connected || !model) {
        alert('Please connect to a model before chatting.');
        return;
      }

      if (isSubmitting) {
        return;
      }

      setIsSubmitting(true);
      const newChatLog = [...chatLog, { user: 'user', text: message }];
      setChatLog(newChatLog);
      setInputValue('');
      resetTranscript();

      try {
        SpeechRecognition.stopListening();
        await callApiEndpoint(message);
      } catch (error) {
        setChatLog((prevChatLog) => [
          ...prevChatLog,
          { user: 'bot', text: 'Error fetching response' },
        ]);
      } finally {
        setIsSubmitting(false);
      }
    },
    [chatLog, model, connected, isSubmitting]
  );

  const handleMicrophoneClick = () => {
    if (listening) {
      SpeechRecognition.stopListening();
    } else {
      SpeechRecognition.startListening({ continuous: true });
    }
  };

  // useEffect to handle new transcript
  useEffect(() => {
    if (listening && transcript) {
      setInputValue(transcript);

      if (typingTimeoutRef.current) {
        clearTimeout(typingTimeoutRef.current);
      }

      typingTimeoutRef.current = setTimeout(() => {
        if (transcript.trim()) {
          handleChatSubmit(transcript);
          SpeechRecognition.stopListening();
        }
      }, 3000);
    }
  }, [listening, transcript, handleChatSubmit]);

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
          <CCard>
            <CCardHeader>
              Chatbot (Session ID: {sessionId || 'N/A'})
              <CButton color="warning" onClick={startNewSession} className="float-end">
                Start New Session
              </CButton>
            </CCardHeader>
            <CCardBody>
              {/* Render ChatBoxMydio component with necessary props */}
              <ChatBoxMydio 
                model={model} 
                sessionId={sessionId} 
                connected={connected} 
                chatLog={chatLog} 
                setChatLog={setChatLog} 
                callApiEndpoint={callApiEndpoint}
              />
              <div style={{ display: 'flex', alignItems: 'center', marginTop: '10px' }}>
                <textarea
                  placeholder="Type message here"
                  value={inputValue}
                  onChange={(e) => {
                    setInputValue(e.target.value);
                    SpeechRecognition.stopListening();
                  }}
                  style={{ width: '100%', height: '60px', padding: '10px', borderRadius: '5px', border: '1px solid #ccc', resize: 'none' }}
                />
                <CButton color="primary" onClick={() => handleChatSubmit(inputValue)} disabled={isSubmitting} style={{ marginLeft: '10px' }}>
                  {isSubmitting ? <CSpinner size="sm" /> : 'Send'}
                </CButton>
                <FaMicrophone
                  onClick={handleMicrophoneClick}
                  style={{ marginLeft: '10px', cursor: 'pointer', color: listening ? 'red' : 'black' }}
                  size={24}
                />
              </div>
            </CCardBody>
          </CCard>
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

export default NLP2MYDIO;
