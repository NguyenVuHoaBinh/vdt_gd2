package config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class PostgreSQLDataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.postgresql")
    public DataSource postgreSQLDataSource() {
        return DataSourceBuilder.create().build();
    }
}
