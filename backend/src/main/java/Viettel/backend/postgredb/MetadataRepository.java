package Viettel.backend.postgredb;

import Viettel.backend.model.PostgreSQLMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MetadataRepository {

    private final JdbcTemplate jdbcTemplate;

    public MetadataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveMetadata(PostgreSQLMetadata metadata) {
        String sql = "INSERT INTO metadata (database_name, schema_details, db_type, created_at) VALUES (?, ?::jsonb, ?, ?)";
        jdbcTemplate.update(
                sql,
                metadata.getDatabaseName(),
                metadata.getSchemaDetails(),
                metadata.getDbType(),
                metadata.getCreatedAt()
        );
    }
}