package Viettel.backend.config.databaseconfig;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;

public interface DataSourceFactory {
    DriverManagerDataSource createDataSource(Map<String, String> dbParams);
}
