// src/main/java/Viettel/backend/model/DocumentWithEmbedding.java

package Viettel.backend.AdvanceRAG.model;

public class DocumentWithEmbedding<T> {

    private T document;
    private float[] embedding;

    // Constructors
    public DocumentWithEmbedding(T document, float[] embedding) {
        this.document = document;
        this.embedding = embedding;
    }

    // Getters and setters
    public T getDocument() {
        return document;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    // Optionally, setters if needed
}
