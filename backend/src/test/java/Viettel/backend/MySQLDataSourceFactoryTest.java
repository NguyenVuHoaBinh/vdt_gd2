package Viettel.backend;

import Viettel.backend.config.databaseconfig.DataSourceFactory;
import Viettel.backend.config.databaseconfig.MySQLDataSourceFactory;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MySQLDataSourceFactoryTest {

    @Test
    void createDataSource() {
        DataSourceFactory factory = new MySQLDataSourceFactory();
        Map<String, String> dbParams = Map.of(
                "host", "localhost",
                "database", "testdb",
                "user", "testuser",
                "password", "testpass"
        );

        DriverManagerDataSource dataSource = factory.createDataSource(dbParams);

        // Assertions to verify the properties
        assertEquals("jdbc:mysql://localhost/testdb", dataSource.getUrl());
        assertEquals("testuser", dataSource.getUsername());
        assertEquals("testpass", dataSource.getPassword());
    }
}
