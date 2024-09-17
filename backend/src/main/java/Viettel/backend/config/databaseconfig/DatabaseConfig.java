package Viettel.backend.config.databaseconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DatabaseConfig {

    @Autowired
    private ApplicationContext applicationContext;

    public DataSource createDataSource(Map<String, String> dbParams) {
        String dbType = dbParams.get("dbType").toLowerCase();
        DataSourceFactory dataSourceFactory = applicationContext.getBean(dbType + "DataSourceFactory", DataSourceFactory.class);
        return dataSourceFactory.createDataSource(dbParams);
    }
}
