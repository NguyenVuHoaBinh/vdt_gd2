package Viettel.backend.config.database;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;

public interface DataSourceFactory {
    DriverManagerDataSource createDataSource(Map<String, String> dbParams);
}
