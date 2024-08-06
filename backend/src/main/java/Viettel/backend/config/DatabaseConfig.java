package Viettel.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource() {
        // Default datasource set to MySQL
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/your_database");
        dataSource.setUsername("your_username");
        dataSource.setPassword("your_password");
        return dataSource;
    }

    public DataSource createDataSource(Map<String, String> dbParams) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String dbType = dbParams.get("dbType");
        switch (dbType) {
            case "mysql":
                dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
                dataSource.setUrl("jdbc:mysql://" + dbParams.get("host") + "/" + dbParams.get("database"));
                break;
            // Add other cases for different databases here
        }
        dataSource.setUsername(dbParams.get("user"));
        dataSource.setPassword(dbParams.get("password"));
        return dataSource;
    }
}
