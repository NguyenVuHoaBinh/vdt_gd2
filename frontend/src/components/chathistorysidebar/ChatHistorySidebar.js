import React, { useState, useEffect } from 'react';
import { CListGroup, CListGroupItem } from '@coreui/react';

// ChatHistorySidebar component for displaying session history
const ChatHistorySidebar = ({ onSelectSession }) => {
  const [sessions, setSessions] = useState([]);

  useEffect(() => {
    const fetchSessions = async () => {
      const response = await fetch('http://localhost:8080/sessions'); // Replace with your actual backend endpoint
      const data = await response.json();
      setSessions(data);
    };

    fetchSessions();
  }, []);

  return (
    <CListGroup>
      {sessions.map(session => (
        <CListGroupItem key={session.session_id} onClick={() => onSelectSession(session.session_id)}>
          {`Session ${session.session_id}`} - {new Date(session.createdAt).toLocaleString()}
        </CListGroupItem>
      ))}
    </CListGroup>
  );
};

export default ChatHistorySidebar;