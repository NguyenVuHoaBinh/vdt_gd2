package Viettel.backend.service.datahubservice;

import Viettel.backend.config.datahubconfig.DataHubConfigStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Service
public class ConfigGeneratorService {

    private static final String DEFAULT_CONFIG_FILE_NAME = "dynamic_ingestion.yml";

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Generates the YAML configuration content based on the dbType using the appropriate strategy.
     *
     * @param dbParams The database connection parameters.
     * @param dataHubUrl The URL of the DataHub server.
     * @return The YAML configuration content.
     */
    public String createConfigContent(Map<String, String> dbParams, String dataHubUrl) {
        String dbType = dbParams.get("dbType").toLowerCase();
        DataHubConfigStrategy configStrategy = applicationContext.getBean(dbType + "ConfigStrategy", DataHubConfigStrategy.class);
        return configStrategy.createConfigContent(dbParams, dataHubUrl);
    }

    /**
     * Writes the configuration content to a file.
     *
     * @param fileName The name of the file to write to.
     * @param content The content to write.
     */
    public void writeConfigToFile(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write configuration file: " + fileName, e);
        }
    }

    /**
     * A convenience method to create and write the config to a default file.
     *
     * @param dbParams The database connection parameters.
     * @param dataHubUrl The URL of the DataHub server.
     */
    public void createAndWriteConfig(Map<String, String> dbParams, String dataHubUrl) {
        String configContent = createConfigContent(dbParams, dataHubUrl);
        writeConfigToFile(DEFAULT_CONFIG_FILE_NAME, configContent);
    }
}
