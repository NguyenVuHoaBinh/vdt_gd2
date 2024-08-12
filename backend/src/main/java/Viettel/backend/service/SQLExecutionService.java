package Viettel.backend.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Service
public class SQLExecutionService {

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public List<Map<String, Object>> executeQuery(String sql, Map<String, String> dbParams) {
        try {
            setDataSource(createDataSource(dbParams));
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
        }
    }

    private DataSource createDataSource(Map<String, String> dbParams) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        String dbType = dbParams.get("dbType");
        String host = dbParams.get("host");
        String database = dbParams.get("database");
        String username = dbParams.get("user");
        String password = dbParams.get("password");

        switch (dbType.toLowerCase()) {
            case "mysql":
                dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
                dataSource.setUrl("jdbc:mysql://" + host + "/" + database);
                break;
            case "postgres":
                dataSource.setDriverClassName("org.postgresql.Driver");
                dataSource.setUrl("jdbc:postgresql://" + host + "/" + database);
                break;
            case "mssql":
                dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                dataSource.setUrl("jdbc:sqlserver://" + host + ";databaseName=" + database+";encrypt=true;trustServerCertificate=true");
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }

        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }
}
