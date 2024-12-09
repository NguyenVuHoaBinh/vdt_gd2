package Viettel.backend;

import Viettel.backend.service.datahubservice.ConfigGeneratorService;
import Viettel.backend.config.datahubconfig.MySQLConfigStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ConfigGeneratorServiceTest {

    @InjectMocks
    private ConfigGeneratorService configGeneratorService;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private MySQLConfigStrategy mysqlConfigStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(applicationContext.getBean("mysqlConfigStrategy", MySQLConfigStrategy.class)).thenReturn(mysqlConfigStrategy);
    }

    @Test
    void testCreateAndWriteConfig() throws IOException {
        Map<String, String> dbParams = new HashMap<>();
        dbParams.put("dbType", "mysql");
        dbParams.put("host_port", "localhost:3306");
        dbParams.put("database", "testdb");
        dbParams.put("username", "testuser");
        dbParams.put("password", "testpass");

        String expectedConfig = "some config content"; // Replace with your actual expected content

        when(mysqlConfigStrategy.createConfigContent(dbParams, "http://localhost:8080")).thenReturn(expectedConfig);

        configGeneratorService.createAndWriteConfig(dbParams, "http://localhost:8080");

        // Verify the file was created and contains the correct content
        try (BufferedReader reader = new BufferedReader(new FileReader("dynamic_ingestion.yml"))) {
            String fileContent = reader.readLine();
            assertEquals(expectedConfig, fileContent);
        }

        // Clean up the file after the test
        Files.deleteIfExists(Paths.get("dynamic_ingestion.yml"));
    }
}
