package Viettel.backend.config.databaseconfig;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("postgresDataSourceFactory")
public class PostgresDataSourceFactory implements DataSourceFactory {

    @Override
    public DriverManagerDataSource createDataSource(Map<String, String> dbParams) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://" + dbParams.get("host") + "/" + dbParams.get("database"));
        dataSource.setUsername(dbParams.get("user"));
        dataSource.setPassword(dbParams.get("password"));
        return dataSource;
    }
}
