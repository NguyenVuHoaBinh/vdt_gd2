package Viettel.backend.service;

import Viettel.backend.model.PostgreSQLMetadata;
import Viettel.backend.postgredb.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MetadataService {

    @Autowired
    private MetadataRepository metadataRepository;

    public void saveMetadata(String databaseName, String schemaDetails, String dbType) {
        PostgreSQLMetadata metadata = new PostgreSQLMetadata();
        metadata.setDatabaseName(databaseName);
        metadata.setSchemaDetails(schemaDetails); // JSON in String format
        metadata.setDbType(dbType);
        metadata.setCreatedAt(LocalDateTime.now());

        metadataRepository.saveMetadata(metadata);
    }
}
