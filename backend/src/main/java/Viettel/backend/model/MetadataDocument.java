package Viettel.backend.model;

import java.util.Arrays;
import java.util.List;

public class MetadataDocument {
    private String id;
    private List<Integer> chunk;
    private String originalText;
    private int chunkIndex;
    private String parentId;
    private int chunkTokenCount;
    private double[] embedding; // Single embedding as a double array

    // Default constructor
    public MetadataDocument() {
    }

    // Constructor with all fields
    public MetadataDocument(String id, List<Integer> chunk, String originalText, int chunkIndex, String parentId, int chunkTokenCount, double[] embedding) {
        this.id = id;
        this.chunk = chunk;
        this.originalText = originalText;
        this.chunkIndex = chunkIndex;
        this.parentId = parentId;
        this.chunkTokenCount = chunkTokenCount;
        this.embedding = embedding;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Integer> getChunk() {
        return chunk;
    }

    public void setChunk(List<Integer> chunk) {
        this.chunk = chunk;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getChunkTokenCount() {
        return chunkTokenCount;
    }

    public void setChunkTokenCount(int chunkTokenCount) {
        this.chunkTokenCount = chunkTokenCount;
    }

    public double[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(double[] embedding) {
        this.embedding = embedding;
    }

    @Override
    public String toString() {
        return "MetadataDocument{" +
                "id='" + id + '\'' +
                ", chunk=" + chunk +
                ", originalText='" + originalText + '\'' +
                ", chunkIndex=" + chunkIndex +
                ", parentId='" + parentId + '\'' +
                ", chunkTokenCount=" + chunkTokenCount +
                ", embedding=" + Arrays.toString(embedding) +
                '}';
    }
}
