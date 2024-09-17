package Viettel.backend;

import Viettel.backend.config.datahubconfig.MySQLConfigStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MySQLConfigStrategyTest {

    private MySQLConfigStrategy mysqlConfigStrategy;

    @BeforeEach
    void setUp() {
        mysqlConfigStrategy = new MySQLConfigStrategy();
    }

    @Test
    void testCreateConfigContentWithAllParams() {
        Map<String, String> dbParams = new HashMap<>();
        dbParams.put("host_port", "localhost:3306");
        dbParams.put("database", "testdb");
        dbParams.put("username", "testuser");
        dbParams.put("password", "testpass");
        dbParams.put("include_tables", "true");
        dbParams.put("include_views", "true");
        dbParams.put("profiling_enabled", "true");
        dbParams.put("stateful_ingestion_enabled", "true");

        // Dynamic patterns provided as comma-separated strings
        dbParams.put("database_pattern_allow", "db1,db2");
        dbParams.put("database_pattern_deny", "db3,db4");
        dbParams.put("table_pattern_allow", "table1,table2");
        dbParams.put("table_pattern_deny", "table3,table4");

        String expectedConfig = "source:\n" +
                "  type: mysql\n" +
                "  config:\n" +
                "    host_port: 'localhost:3306'\n" +
                "    database: testdb\n" +
                "    username: testuser\n" +
                "    password: 'testpass'\n" +
                "    include.tables: true\n" +
                "    include.views: true\n" +
                "    profiling.enabled: true\n" +
                "    stateful.ingestion.enabled: true\n" +
                "    database_pattern:\n" +
                "      allow:\n" +
                "        - 'db1'\n" +
                "        - 'db2'\n" +
                "      deny:\n" +
                "        - 'db3'\n" +
                "        - 'db4'\n" +
                "    table_pattern:\n" +
                "      allow:\n" +
                "        - 'table1'\n" +
                "        - 'table2'\n" +
                "      deny:\n" +
                "        - 'table3'\n" +
                "        - 'table4'\n" +
                "sink:\n" +
                "  type: datahub-rest\n" +
                "  config:\n" +
                "    server: http://localhost:8080\n";

        String actualConfig = mysqlConfigStrategy.createConfigContent(dbParams, "http://localhost:8080");

        assertEquals(expectedConfig, actualConfig);
    }

    @Test
    void testCreateConfigContentWithRequiredParamsOnly() {
        Map<String, String> dbParams = new HashMap<>();
        dbParams.put("host_port", "localhost:3306");
        dbParams.put("database", "testdb");
        dbParams.put("username", "testuser");
        dbParams.put("password", "testpass");

        String expectedConfig = "source:\n" +
                "  type: mysql\n" +
                "  config:\n" +
                "    host_port: 'localhost:3306'\n" +
                "    database: testdb\n" +
                "    username: testuser\n" +
                "    password: 'testpass'\n" +
                "sink:\n" +
                "  type: datahub-rest\n" +
                "  config:\n" +
                "    server: http://localhost:8080\n";

        String actualConfig = mysqlConfigStrategy.createConfigContent(dbParams, "http://localhost:8080");

        assertEquals(expectedConfig, actualConfig);
    }

    @Test
    void testCreateConfigContentWithSomeOptionalParams() {
        Map<String, String> dbParams = new HashMap<>();
        dbParams.put("host_port", "localhost:3306");
        dbParams.put("database", "testdb");
        dbParams.put("username", "testuser");
        dbParams.put("password", "testpass");

        // Optional true/false parameters - only include some of them
        dbParams.put("include_tables", "true");
        dbParams.put("stateful_ingestion_enabled", "true");

        // Dynamic patterns - only providing one of each
        dbParams.put("database_pattern_allow", "db1");
        dbParams.put("table_pattern_deny", "table3,table4");

        String expectedConfig = "source:\n" +
                "  type: mysql\n" +
                "  config:\n" +
                "    host_port: 'localhost:3306'\n" +
                "    database: testdb\n" +
                "    username: testuser\n" +
                "    password: 'testpass'\n" +
                "    include.tables: true\n" +
                "    stateful.ingestion.enabled: true\n" +
                "    database_pattern:\n" +
                "      allow:\n" +
                "        - 'db1'\n" +
                "    table_pattern:\n" +
                "      deny:\n" +
                "        - 'table3'\n" +
                "        - 'table4'\n" +
                "sink:\n" +
                "  type: datahub-rest\n" +
                "  config:\n" +
                "    server: http://localhost:8080\n";

        String actualConfig = mysqlConfigStrategy.createConfigContent(dbParams, "http://localhost:8080");

        assertEquals(expectedConfig, actualConfig);
    }

    @Test
    void testCreateConfigContentWithEmptyOptionalParams() {
        Map<String, String> dbParams = new HashMap<>();
        dbParams.put("host_port", "localhost:3306");
        dbParams.put("database", "testdb");
        dbParams.put("username", "testuser");
        dbParams.put("password", "testpass");

        // Empty optional parameters should not appear in the output
        dbParams.put("include_tables", "");
        dbParams.put("stateful_ingestion_enabled", "");

        String expectedConfig = "source:\n" +
                "  type: mysql\n" +
                "  config:\n" +
                "    host_port: 'localhost:3306'\n" +
                "    database: testdb\n" +
                "    username: testuser\n" +
                "    password: 'testpass'\n" +
                "sink:\n" +
                "  type: datahub-rest\n" +
                "  config:\n" +
                "    server: http://localhost:8080\n";

        String actualConfig = mysqlConfigStrategy.createConfigContent(dbParams, "http://localhost:8080");

        assertEquals(expectedConfig, actualConfig);
    }
}
