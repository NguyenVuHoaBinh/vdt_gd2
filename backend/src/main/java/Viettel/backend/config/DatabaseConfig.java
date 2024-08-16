package Viettel.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DatabaseConfig {

    public DataSource createDataSource(Map<String, String> dbParams) {
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
                dataSource.setUrl("jdbc:sqlserver://" + host + ";databaseName=" + database + ";encrypt=true;trustServerCertificate=true");
                break;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }

        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }
}
