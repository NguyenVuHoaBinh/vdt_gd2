import React, { useState } from 'react';
import CustomButton from '../common/CustomButton';

const InputForm = ({ onConnect }) => {
  const [host, setHost] = useState('');
  const [database, setDatabase] = useState('');
  const [user, setUser] = useState('');
  const [password, setPassword] = useState('');
  const [dbType, setDbType] = useState('mysql');

  const handleSubmit = (e) => {
    e.preventDefault();
    onConnect({ host, database, user, password, dbType });
  };

  return (
    <div className="input-form">
      <h2>Connect to MySQL</h2>
      <form onSubmit={handleSubmit}>
        <input type="text" value={host} onChange={(e) => setHost(e.target.value)} placeholder="Host IP" required />
        <input type="text" value={database} onChange={(e) => setDatabase(e.target.value)} placeholder="Database name" required />
        <input type="text" value={user} onChange={(e) => setUser(e.target.value)} placeholder="User" required />
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Password" required />
        <select value={dbType} onChange={(e) => setDbType(e.target.value)}>
          <option value="mysql">MySQL</option>
          {/* Add other database types here */}
        </select>
        <CustomButton label="Connect" onClick={handleSubmit} />
      </form>
    </div>
  );
};

export default InputForm;
