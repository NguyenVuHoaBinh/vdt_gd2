import React, { useState } from 'react';

const Chatbot = ({ isConnected, selectedModel }) => {
  const [message, setMessage] = useState('');
  const [chatHistory, setChatHistory] = useState([]);
  const [file, setFile] = useState(null);

  const handleSend = async () => {
    if (!message) return;

    const newChatHistory = [...chatHistory, { sender: 'user', message }];
    setChatHistory(newChatHistory);

    try {
      const response = await fetch('http://localhost:8080/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message, model: selectedModel }), // Pass selected model
      });

      const result = await response.json();
      setChatHistory([...newChatHistory, { sender: 'bot', message: result.response }]);
    } catch (error) {
      setChatHistory([...newChatHistory, { sender: 'bot', message: 'Error: ' + error.message }]);
    }

    setMessage('');
  };

  const handleFileUpload = async (e) => {
    const uploadedFile = e.target.files[0];
    if (uploadedFile) {
      setFile(uploadedFile);
      const formData = new FormData();
      formData.append('file', uploadedFile);

      try {
        const response = await fetch('http://localhost:8080/upload', {
          method: 'POST',
          body: formData,
        });

        const result = await response.json();
        setChatHistory([...chatHistory, { sender: 'bot', message: result.response }]);
      } catch (error) {
        setChatHistory([...chatHistory, { sender: 'bot', message: 'Error: ' + error.message }]);
      }
    }
  };

  return (
    <div className="chatbot">
      <h2>Chatbot</h2>
      {isConnected ? (
        <>
          <div className="chat-history">
            {chatHistory.map((chat, index) => (
              <div key={index} className={`chat-message ${chat.sender}`}>
                <strong>{chat.sender}:</strong> {chat.message}
              </div>
            ))}
          </div>
          <textarea value={message} onChange={(e) => setMessage(e.target.value)} placeholder="Type your message"></textarea>
          <button onClick={handleSend}>Send</button>
          <input type="file" onChange={handleFileUpload} />
        </>
      ) : (
        <p>Please connect to the database first.</p>
      )}
    </div>
  );
};

export default Chatbot;
