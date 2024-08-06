import React, { useState } from 'react';

const ModelSelector = ({ onSelectModel }) => {
  const [selectedModel, setSelectedModel] = useState('');

  const handleSelect = (e) => {
    const model = e.target.value;
    setSelectedModel(model);
    onSelectModel(model);
  };

  return (
    <div className="model-selector">
      <h2>Select LLM Model</h2>
      <select value={selectedModel} onChange={handleSelect}>
        <option value="">Select a model</option>
        <option value="gpt-3">GPT-3</option>
        <option value="gemini">Gemini 1.5 Flash</option>
        {/* Add other models here */}
      </select>
    </div>
  );
};

export default ModelSelector;
