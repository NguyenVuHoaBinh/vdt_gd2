package Viettel.backend.model;

import java.time.LocalDateTime;

public class PostgreSQLMetadata {
    private int id;
    private String databaseName;
    private String dbType;
    private String schemaDetails; // JSONB field as a String
    private LocalDateTime createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getSchemaDetails() {
        return schemaDetails;
    }

    public void setSchemaDetails(String schemaDetails) {
        this.schemaDetails = schemaDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
