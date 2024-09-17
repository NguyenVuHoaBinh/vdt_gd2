// src/main/java/Viettel/backend/model/SchemaDocument.java

package Viettel.backend.AdvanceRAG.model;

public class SchemaDocument {

    private String id;
    private String tableName;
    private String content;

    // Constructors
    public SchemaDocument(String id, String tableName, String content) {
        this.id = id;
        this.tableName = tableName;
        this.content = content;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getTableName() {
        return tableName;
    }

    public String getContent() {
        return content;
    }

    // Optionally, setters if needed
}
