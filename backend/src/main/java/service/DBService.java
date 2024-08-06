package service;

import model.DBParams;
import model.ChatRequest;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
public class DBService {

    public boolean connectToDB(DBParams dbParams) {
        try (Connection connection = createDataSource(dbParams).getConnection()) {
            return connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private DataSource createDataSource(DBParams dbParams) {
        String url = buildJdbcUrl(dbParams);
        return DataSourceBuilder.create()
                .url(url)
                .username(dbParams.getUser())
                .password(dbParams.getPassword())
                .driverClassName(getDriverClassName(dbParams.getDatabaseType()))
                .build();
    }

    private String buildJdbcUrl(DBParams dbParams) {
        switch (dbParams.getDatabaseType().toLowerCase()) {
            case "mysql":
                return "jdbc:mysql://" + dbParams.getHost() + "/" + dbParams.getDatabase();
            case "postgresql":
                return "jdbc:postgresql://" + dbParams.getHost() + "/" + dbParams.getDatabase();
            case "sqlserver":
                return "jdbc:sqlserver://" + dbParams.getHost() + ";databaseName=" + dbParams.getDatabase();
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbParams.getDatabaseType());
        }
    }

    private String getDriverClassName(String databaseType) {
        switch (databaseType.toLowerCase()) {
            case "mysql":
                return "com.mysql.cj.jdbc.Driver";
            case "postgresql":
                return "org.postgresql.Driver";
            case "sqlserver":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
    }

    public String chat(ChatRequest chatRequest) {
        // Implement LLM API calls here
        return "Simulated response for: " + chatRequest.getMessage();
    }
}

