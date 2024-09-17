package Viettel.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import Viettel.backend.config.databaseconfig.DatabaseConfig;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Service
public class SQLExecutionService {

    private final DatabaseConfig databaseConfig;
    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SQLExecutionService(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<Map<String, Object>> executeQuery(String sql, Map<String, String> dbParams) {
        try {
            setDataSource(databaseConfig.createDataSource(dbParams));
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
        }
    }
}
