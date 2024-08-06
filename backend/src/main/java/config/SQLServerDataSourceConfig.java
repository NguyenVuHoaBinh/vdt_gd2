package config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class SQLServerDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.sqlserver")
    public DataSource sqlServerDataSource() {
        return DataSourceBuilder.create().build();
    }
}
