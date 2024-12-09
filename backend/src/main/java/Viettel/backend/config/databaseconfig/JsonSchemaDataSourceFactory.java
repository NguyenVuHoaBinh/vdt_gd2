package Viettel.backend.config.databaseconfig;


import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("jsonschemaDataSourceFactory")
public class JsonSchemaDataSourceFactory implements DataSourceFactory{
    @Override
    public DriverManagerDataSource createDataSource(Map<String, String> dbParams) {
        return null;
    }
}
