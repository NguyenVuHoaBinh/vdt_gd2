package Viettel.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import Viettel.backend.config.databaseconfig.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Service
public class SQLExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(SQLExecutionService.class);
    private final DatabaseConfig databaseConfig;
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SQLExecutionService(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        logger.info("DataSource has been set successfully.");
    }

    public List<Map<String, Object>> executeQuery(String sql, Map<String, String> dbParams) {
        try {
            logger.info("Executing SQL query.");
            logger.debug("SQL Query: {}", sql);
            logger.debug("Database parameters: {}", dbParams);

            setDataSource(databaseConfig.createDataSource(dbParams));
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);

            logger.info("Query executed successfully. Retrieved {} rows.", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Failed to execute query: {}", sql, e);
            throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
        }
    }

    public int executeUpdate(String sql, Map<String, String> dbParams) {
        try {
            logger.info("Executing SQL update.");
            logger.debug("SQL Update Query: {}", sql);
            logger.debug("Database parameters: {}", dbParams);

            // Set the data source
            setDataSource(databaseConfig.createDataSource(dbParams));

            // Execute the update and return the number of affected rows
            int rowsAffected = jdbcTemplate.update(sql);

            logger.info("Update executed successfully. Affected {} rows.", rowsAffected);
            return 1;
        } catch (Exception e) {
            logger.error("Failed to execute update: {}", sql, e);
            throw new RuntimeException("Failed to execute update: " + e.getMessage(), e);
        }
    }

}
