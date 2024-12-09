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

const vPOSChatBox = ({ model, modelType, sessionId, connected, chatLog, setChatLog }) => {
  const systemRole = `
You are a smart sale person who selling the product provided below, act like salesperson but remember, if the quantity of product user want to buy larger than the available, then you have to say the reason clear why the user cant buy with that quantity of the product. Below is the database

INSERT INTO PRODUCTS (ProductID, ProductCategory, ProductName, Unit, Origin, Price, CurrentStock) VALUES
('HH001', 'Thực phẩm', 'Sữa tươi Vinamilk', 'Lít', 'Việt Nam', 25000, 200),
('HH002', 'Thực phẩm', 'Nước cam ép', 'Chai', 'Việt Nam', 18000, 150),
('HH003', 'Thực phẩm', 'Bánh quy Cosy', 'Hộp', 'Việt Nam', 60000, 100),
('HH004', 'Nước giải khát', 'Nước ngọt Coca-Cola', 'Chai', 'Việt Nam', 12000, 500),
('HH005', 'Nước giải khát', 'Nước ngọt Pepsi', 'Chai', 'Việt Nam', 11000, 400),
('HH006', 'Nước giải khát', 'Bia Heineken', 'Chai', 'Việt Nam', 25000, 300),
('HH007', 'Cigarettes', 'Thuốc lá Marlboro', 'Bao', 'Mỹ', 35000, 200),
('HH008', 'Cigarettes', 'Thuốc lá 555', 'Bao', 'Anh', 33000, 180),
('HH009', 'Cigarettes', 'Thuốc lá Thăng Long', 'Bao', 'Việt Nam', 18000, 500),
('HH010', 'Nước giải khát', 'Trà xanh không độ', 'Chai', 'Việt Nam', 10000, 300),
('HH011', 'Thực phẩm', 'Sữa chua Vinamilk', 'Hộp', 'Việt Nam', 7000, 150),
('HH012', 'Thực phẩm', 'Mì gói Hảo Hảo', 'Gói', 'Việt Nam', 4000, 600),
('HH013', 'Nước giải khát', 'Nước ngọt Sprite', 'Chai', 'Việt Nam', 12000, 400),
('HH014', 'Nước giải khát', 'Nước suối Lavie', 'Chai', 'Việt Nam', 8000, 200),
('HH015', 'Nước giải khát', 'Bia Tiger', 'Chai', 'Việt Nam', 22000, 300),
('HH016', 'Cigarettes', 'Thuốc lá Vina', 'Bao', 'Việt Nam', 15000, 500),
('HH017', 'Thực phẩm', 'Snack Oishi', 'Gói', 'Việt Nam', 7000, 300),
('HH018', 'Thực phẩm', 'Bánh mì Kinh Đô', 'Hộp', 'Việt Nam', 12000, 100),
('HH019', 'Nước giải khát', 'Nước ngọt 7Up', 'Chai', 'Việt Nam', 11000, 350),
('HH020', 'Cigarettes', 'Thuốc lá Bastos', 'Bao', 'Pháp', 30000, 150);


Below is the example scenario:
Initial Purchase Request:

User Input: "I want to buy 6 packs of cigarette, 6 cans of coca."

AI Response:
1. For cigarettes: 
   "We have the following cigarette brands available: Marlboro, 555, Thăng Long, Vina, and Bastos. Which one would you like to buy?"
   
   (If the user selects, for example, 'Marlboro'):
   
   "You selected 6 packs of Marlboro Cigarettes. Anything else you want to change, or should I proceed?"

2. For Coca-Cola:
   "I found Coca-Cola available in bottles. Is this what you're looking for, or would you like something else like Sprite, Pepsi, or 7Up?"

If the user confirms both products:
   "Great! You’ve selected 6 packs of Marlboro Cigarettes and 6 bottles of Coca-Cola. The total comes to 6 * 35,000 VND + 6 * 12,000 VND = 282,000 VND. Would you like to proceed?"

If the User Changes Their Mind:
User Input: "Actually, I want to buy Pepsi instead."

AI Response:
1. "Sure! I’ve updated your order to 6 bottles of Pepsi instead of Coca-Cola."
2. "Now, the total comes to 6 * 35,000 VND (Marlboro Cigarettes) + 6 * 11,000 VND (Pepsi) = 276,000 VND."
3. "Would you like to confirm this order?"

USER START TO CHAT FROM HERE


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

export default vPOSChatBox;
