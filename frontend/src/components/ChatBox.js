import React, { useRef, useState, useCallback, useEffect } from 'react';
import { useReactMediaRecorder } from 'react-media-recorder';
import { Input, Button as ChatButton } from 'react-chat-elements';
import 'react-chat-elements/dist/main.css';
import ReactMarkdown from 'react-markdown';
import rehypeRaw from 'rehype-raw';
import remarkGfm from 'remark-gfm';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { materialDark } from 'react-syntax-highlighter/dist/esm/styles/prism';
import { FaMicrophone } from 'react-icons/fa';

const ChatBox = ({ model, modelType, sessionId, connected, chatLog, setChatLog }) => {
  const systemRole = `
  You are working with a database that contains the following tables and their respective fields, your task is to generate the correct sql command. Follow these instructions:

1. **Schema Overview:**
   - Use only the tables and fields specified in the schema below.
   - The query request may involve words related to the tables or fields, so if they are similar then use them.
    Example:
      Query: day active mon
      Schema: {"Field":"day_active_mon_n1","Type":"TEXT"},{"Field":"day_active_mon_n","Type":"TEXT"},{"Field":"watch_film_duration_mon_n","Type":"TEXT"},{"Field":"watch_film_duration_mon_n1","Type":"TEXT"},{"Field":"watch_film_duration_mon_n2","Type":"TEXT"}]},
      then select day_active_mon_n, day_active_mon_n1
   - If the query request involves tables or fields that are not listed in the schema, return: "No suitable request, entity is not found in the given schema."

2. **Query Construction:**
   - Parse the user request and determine the tables and fields involved.
   - Verify that all mentioned tables and fields exist in the given schema.
   - If all specified tables and fields are found in the schema, generate the SQL query.
   - If any table or field in the request is not present in the schema, return: "No suitable request, entity is not found in the given schema."

**Remember:** Your output must strictly adhere to the schema. If the request cannot be fulfilled with the given schema, return "No suitable request."
Below is the schema for reference:
  `;

  const [inputValue, setInputValue] = useState('');
  const chatRef = useRef(null);

  const { startRecording, stopRecording, mediaBlobUrl, status, clearBlobUrl } = useReactMediaRecorder({
    audio: true,
  });

  // Helper function to generate a table from the query result
  const generateTable = (data) => {
    if (!Array.isArray(data) || data.length === 0) return 'No data available.';

    const headers = Object.keys(data[0]);
    const headerRow = headers.map((header) => `<th>${header}</th>`).join('');
    const rows = data
      .map((row) => {
        const rowData = headers.map((header) => `<td>${row[header]}</td>`).join('');
        return `<tr>${rowData}</tr>`;
      })
      .join('');

    return `<table style="width: 100%; border-collapse: collapse; border: 1px solid black;">
      <thead><tr>${headerRow}</tr></thead>
      <tbody>${rows}</tbody>
    </table>`;
  };

  const handleChatSubmit = useCallback(
    async (message) => {
      if (!connected || !model) {
        alert('Please connect to a model and database before chatting.');
        return;
      }

      // Update the chat log using setChatLog passed from the parent component
      const newChatLog = [...chatLog, { user: 'user', text: message }];
      setChatLog(newChatLog);
      setInputValue('');

      try {
        if (modelType === 'api') {
          await callApiEndpoint(message, model);
        } else if (modelType === 'mlflow') {
          await callMlflowEndpoint(message, model);
        }
      } catch (error) {
        console.error('Error fetching response:', error);
        setChatLog((prevChatLog) => [
          ...prevChatLog,
          { user: 'bot', text: 'Error fetching response' },
        ]);
      }
    },
    [chatLog, model, modelType, sessionId, connected, setChatLog] // Ensure all dependencies are included
  );

  const callApiEndpoint = async (message, model) => {
    try {
      const response = await fetch('https://localhost:8888/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message, model, sessionId, systemRole }),
      });

      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
      const result = await response.json();

      const messages = [];

      if (result.fullResponse?.trim()) {
        messages.push({ text: result.fullResponse, user: 'bot' });
      }

      if (result.sqlQuery?.trim()) {
        messages.push({
          text: `**SQL Query:**\n\`\`\`sql\n${result.sqlQuery}\n\`\`\``,
          user: 'bot',
        });
      }

      if (Array.isArray(result.queryResult) && result.queryResult.length > 0) {
        const table = generateTable(result.queryResult);
        messages.push({ text: `**Result:**\n${table}`, user: 'bot' });
      }

      addMessagesWithDelay(messages, 1000);

    } catch (error) {
      console.error('Error with API models:', error);
      setChatLog((prevChatLog) => [
        ...prevChatLog,
        { user: 'bot', text: 'Error with API model response' },
      ]);
    }
  };

  const addMessagesWithDelay = (messages, delay) => {
    messages.forEach((message, index) => {
      setTimeout(() => {
        setChatLog((prevChatLog) => [...prevChatLog, message]);
      }, index * delay);
    });
  };

  const sendAudioToBackend = async (blobUrl) => {
    try {
      const response = await fetch(blobUrl);
      const audioBlob = await response.blob();
      const formData = new FormData();
      formData.append('file', audioBlob, 'audio.wav');

      const result = await fetch('https://localhost:8888/process-audio', {
        method: 'POST',
        body: formData,
      });

      const data = await result.json();

      if (result.ok) {
        const transcribedText = data.transcription;
        setInputValue(transcribedText);
      } else {
        console.error('Error from backend:', data);
        setChatLog((prevChatLog) => [
          ...prevChatLog,
          { user: 'bot', text: 'Error processing audio' },
        ]);
      }
    } catch (error) {
      console.error('Error sending audio to backend:', error);
      setChatLog((prevChatLog) => [
        ...prevChatLog,
        { user: 'bot', text: 'Error sending audio to backend' },
      ]);
    } finally {
      clearBlobUrl();
    }
  };

  // Scroll to the bottom whenever chatLog updates
  useEffect(() => {
    chatRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatLog]);

  return (
    <div style={styles.chatContainer}>
      <div style={styles.chatMessages}>
        {chatLog.map((entry, index) => (
          <div
            ref={chatRef}
            key={index}
            style={{
              display: 'flex',
              justifyContent: entry.user === 'user' ? 'flex-end' : 'flex-start',
            }}
          >
            <div
              style={{
                maxWidth: '80%',
                padding: '10px',
                background: entry.user === 'user' ? '#d1e7dd' : '#f8d7da',
                borderRadius: '10px',
              }}
            >
              <ReactMarkdown
                rehypePlugins={[rehypeRaw]}
                remarkPlugins={[remarkGfm]}
                components={{
                  code({ node, inline, className, children, ...props }) {
                    const match = /language-(\w+)/.exec(className || '');
                    return !inline && match ? (
                      <SyntaxHighlighter {...props} style={materialDark} language={match[1]} PreTag="div">
                        {String(children).replace(/\n$/, '')}
                      </SyntaxHighlighter>
                    ) : (
                      <code className={className} {...props}>
                        {children}
                      </code>
                    );
                  },
                }}
              >
                {entry.text}
              </ReactMarkdown>
            </div>
          </div>
        ))}
      </div>

      <div style={styles.inputContainer}>
        <Input
          placeholder="Type message here"
          multiline={false}
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          rightButtons={
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <ChatButton
                text="Send"
                onClick={() => {
                  if (inputValue.trim()) {
                    handleChatSubmit(inputValue);
                  }
                }}
              />
              <FaMicrophone
                onClick={() => {
                  if (status === 'recording') {
                    stopRecording();
                  } else {
                    startRecording();
                  }
                }}
                style={{
                  marginLeft: '10px',
                  cursor: 'pointer',
                  color: status === 'recording' ? 'red' : 'black',
                }}
                size={24}
              />
            </div>
          }
        />
      </div>

      {mediaBlobUrl && (
        <div style={{ marginTop: '10px' }}>
          <audio src={mediaBlobUrl} controls />
          <ChatButton text="Send Audio" onClick={() => sendAudioToBackend(mediaBlobUrl)} />
          <ChatButton text="Discard" onClick={() => clearBlobUrl()} style={{ marginLeft: '10px' }} />
        </div>
      )}
    </div>
  );
};

// Styles for the chat box
const styles = {
  chatContainer: {
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    height: '500px',
    border: '1px solid #ccc',
    borderRadius: '10px',
    overflow: 'hidden',
    backgroundColor: '#f4f4f4',
  },
  chatMessages: {
    padding: '10px',
    flex: 1,
    overflowY: 'auto',
    backgroundColor: '#f9f9f9',
  },
  inputContainer: {
    padding: '10px',
    borderTop: '1px solid #ccc',
    backgroundColor: '#fff',
  },
};

export default ChatBox;
