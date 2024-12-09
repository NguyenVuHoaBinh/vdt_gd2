// LangGraphWorkflow.js
import { Graph, StateGraph, Annotation, START, END,MemorySaver } from "@langchain/langgraph/web";

// Define the graph state annotations
const GraphState = Annotation.Root({
  messages: Annotation({
    value: (x, y) => x.concat(y),
    default: () => [],
  }),
  attempts: Annotation({
    value: (x) => x,
    default: () => 0,
  }),
  isValid: Annotation({
    value: (x) => x,
    default: () => false,
  }),
  generatedSQL: Annotation({
    value: (x) => x,
    default: () => '',
  }),
  errorFeedback: Annotation({
    value: (x) => x,
    default: () => '',
  }),
  schemaContext: Annotation({
    value: (x) => x,
    default: () => '',
  }),
  hypotheticalSQL: Annotation({
    value: (x) => x,
    default: () => '',
  }),
  queryResult: Annotation({
    value: (x) => x,
    default: () => null,
  }),
  prompt: Annotation({
    value: (x) => x,
    default: () => '',
  }),
  similarQueries: Annotation({
    value: (x) => x,
    default: () => [],
  })
});

// Initialize the workflow as a StateGraph
const workflow = new StateGraph(GraphState);
const check_schema_uri = 'https://localhost:8888/'
// 1. Input User Query (Handled in React UI)

// 2. Adaptive RAG: Check Schema
workflow.addNode("checkSchema", async (state) => {
  console.log("---CHECK DATABASE SCHEMA---");
  try {
    const response = await fetch(check_schema_uri, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ query: state.userQuery }),
    });

    const result = await response.json();

    if (result.match) {
      state.schemaContext = result.schemaContext;
    } else {
      throw new Error('Schema mismatch: Unable to find a matching schema for the query.');
    }
  } catch (error) {
    console.error('Error in checkSchema:', error);
    throw error;
  }

  return state;
});

// 3. HyDE RAG: Generate Hypothetical SQL and Retrieve Similar Queries
workflow.addNode("generateHypotheticalSQL", async (state) => {
  console.log("---GENERATE HYPOTHETICAL SQL---");
  try {
    const response = await fetch('/api/llm/generateHypotheticalSQL', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ query: state.userQuery }),
    });

    const result = await response.json();
    state.hypotheticalSQL = result.hypotheticalSQL;
  } catch (error) {
    console.error('Error in generateHypotheticalSQL:', error);
    throw error;
  }

  return state;
});

workflow.addNode("retrieveSimilarSQL", async (state) => {
  try {
    const embeddingResponse = await fetch('/api/embedding/generate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ text: state.hypotheticalSQL }),
    });

    const embeddingResult = await embeddingResponse.json();

    const searchResponse = await fetch('/api/search/similarQueries', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ embedding: embeddingResult.embedding }),
    });

    const searchResult = await searchResponse.json();
    state.similarQueries = searchResult.similarQueries;
  } catch (error) {
    console.error('Error in retrieveSimilarSQL:', error);
    throw error;
  }

  return state;
});

// 4. Initial SQL Generation
workflow.addNode("prepareLLMPrompt", async (state) => {
  console.log("---PREPARING LLM PROMPT---");
  
  state.prompt = `
    Schema Context:
    ${state.schemaContext}

    Similar Queries:
    ${state.similarQueries.join('\n')}

    User Query:
    ${state.userQuery}
  `;

  return state;
});

workflow.addNode("generateSQL", async (state) => {
  console.log("---GENERATE INITIAL SQL---");
  try {
    const response = await fetch('/api/llm/generateSQL', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ prompt: state.prompt }),
    });

    const result = await response.json();
    state.generatedSQL = result.sqlQuery;
  } catch (error) {
    console.error('Error in generateSQL:', error);
    throw error;
  }

  return state;
});

// 5. Validation and Corrective RAG Loop
workflow.addNode("validateSQL", async (state) => {
  console.log("---VALIDATE SQL QUERY---");
  try {
    const response = await fetch('/api/sql/validate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ sqlQuery: state.generatedSQL }),
    });

    const result = await response.json();
    state.isValid = result.isValid;
    state.errorFeedback = result.errorFeedback || '';
  } catch (error) {
    console.error('Error in validateSQL:', error);
    throw error;
  }

  return state;
});

workflow.addNode("refineSQL", async (state) => {
  if (state.isValid) return state; // Skip if already valid

  console.log("---REFINING SQL QUERY---");
  try {
    const refinementPrompt = `
      SQL Query:
      ${state.generatedSQL}

      Error Feedback:
      ${state.errorFeedback}

      Please refine the SQL query based on the error feedback and schema context.
    `;

    const response = await fetch('/api/llm/refineSQL', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ prompt: refinementPrompt }),
    });

    const result = await response.json();
    state.generatedSQL = result.sqlQuery;
    state.attempts += 1; // Increment attempt counter
  } catch (error) {
    console.error('Error in refineSQL:', error);
    throw error;
  }

  return state;
});

// 6. Execution
workflow.addNode("executeSQL", async (state) => {
  console.log("---EXECUTE VALIDATED SQL QUERY---");
  try {
    const response = await fetch('/api/sql/execute', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ sqlQuery: state.generatedSQL }),
    });

    const result = await response.json();
    state.queryResult = result.queryResult;
  } catch (error) {
    console.error('Error in executeSQL:', error);
    throw error;
  }

  return state;
});

// 7. Final Result
workflow.addNode("returnResults", (state) => {
  console.log("---RETURNING QUERY RESULTS TO USER---");

  return state;
});

const checkpointer = new MemorySaver();


// Define the workflow edges
workflow.addEdge(START, "checkSchema");
workflow.addEdge("checkSchema", "generateHypotheticalSQL");
workflow.addEdge("generateHypotheticalSQL", "retrieveSimilarSQL");
workflow.addEdge("retrieveSimilarSQL", "prepareLLMPrompt");
workflow.addEdge("prepareLLMPrompt", "generateSQL");
workflow.addEdge("generateSQL", "validateSQL");
workflow.addConditionalEdges("validateSQL", (state) => (state.isValid ? "executeSQL" : "refineSQL"));
workflow.addConditionalEdges("refineSQL", (state) => (state.attempts < 3 ? "validateSQL" : END));
workflow.addEdge("executeSQL", "returnResults");
workflow.addEdge("returnResults", END);

// Compile the workflow into a LangChain Runnable
const app = workflow.compile({ checkpointer });

// Export a function that runs the compiled app
export const runWorkflow = async (initialState) => {
  try {
    const finalState = await app.invoke(initialState, { configurable: { thread_id: "42" } });
    return resultState;
  } catch (error) {
    console.error('Error executing compiled workflow:', error);
    throw error;
  }
};
