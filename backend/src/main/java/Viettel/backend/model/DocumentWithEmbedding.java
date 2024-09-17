package Viettel.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // This will ignore any unknown fields during deserialization
public class DocumentWithEmbedding<T> {

    private String id; // Add an id field
    private T document;
    private float[] embedding;

    // Default constructor required for Jackson deserialization
    public DocumentWithEmbedding() {
    }

    public DocumentWithEmbedding(String id, T document, float[] embedding) {
        this.id = id;
        this.document = document;
        this.embedding = embedding;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public T getDocument() {
        return document;
    }

    public void setDocument(T document) {
        this.document = document;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}
