import React, { useEffect, useState } from 'react';
import { CInputGroup, CInputGroupText, CFormSelect } from '@coreui/react';

const ModelSelect = ({ selectedModel, setModel, setModelType, task }) => {
  const [modelOptions, setModelOptions] = useState([]);
  const [modelsLoading, setModelsLoading] = useState(true);

  useEffect(() => {
    const fetchModels = async () => {
      try {
        setModelsLoading(true);
        const response = await fetch(`https://localhost:8888/models/search?tagKey=task&tagValue=${task}`);
        const data = await response.json();
        
        const dynamicOptions = data.map(model => {
          return model.versions.map(version => ({
            value: `${model.name}_v${version}`,
            label: `${model.name} (v${version})`,
            type: 'mlflow' // Mark MLflow models
          }));
        }).flat();
        
        const staticOptions = [
          { value: 'gpt-3', label: 'GPT-3', type: 'api' },
          { value: 'gemini', label: 'Gemini', type: 'api' }
        ];

        setModelOptions([...staticOptions, ...dynamicOptions]);
        setModelsLoading(false);
      } catch (error) {
        console.error('Error fetching models:', error);
        setModelsLoading(false);
      }
    };
    fetchModels();
  }, [task]); // Rerun effect when `task` changes

  const handleModelChange = (e) => {
    const selectedModel = modelOptions.find(option => option.value === e.target.value);
    setModel(selectedModel.value);
    setModelType(selectedModel.type); // Pass model type to parent
  };

  return (
    <CInputGroup className="mb-3">
      <CInputGroupText>LLM Model</CInputGroupText>
      <CFormSelect value={selectedModel} onChange={handleModelChange}>
        <option value="" disabled>Select a model</option>
        {modelsLoading ? <option value="" disabled>Loading models...</option> : modelOptions.map(option => (
          <option key={option.value} value={option.value}>{option.label}</option>
        ))}
      </CFormSelect>
    </CInputGroup>
  );
};

export default ModelSelect;
