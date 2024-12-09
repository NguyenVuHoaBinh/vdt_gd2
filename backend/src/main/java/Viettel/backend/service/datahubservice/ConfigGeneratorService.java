package Viettel.backend.service.datahubservice;

import Viettel.backend.config.datahubconfig.DataHubConfigStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Service
public class ConfigGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigGeneratorService.class);
    private static final String DEFAULT_CONFIG_FILE_NAME = "dynamic_ingestion.yml";

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Generates the YAML configuration content based on the dbType using the appropriate strategy.
     *
     * @param dbParams   The database connection parameters.
     * @param dataHubUrl The URL of the DataHub server.
     * @return The YAML configuration content.
     */
    public String createConfigContent(Map<String, String> dbParams, String dataHubUrl) {
        String dbType = dbParams.get("dbType").toLowerCase();
        logger.info("Creating configuration content for dbType: {}", dbType);

        try {
            DataHubConfigStrategy configStrategy = applicationContext.getBean(dbType + "ConfigStrategy", DataHubConfigStrategy.class);
            String configContent = configStrategy.createConfigContent(dbParams, dataHubUrl);
            logger.info("Configuration content generated successfully for dbType: {}", dbType);
            return configContent;
        } catch (Exception e) {
            logger.error("Failed to create configuration content for dbType: {}", dbType, e);
            throw new RuntimeException("Error creating configuration content for dbType: " + dbType, e);
        }
    }

    /**
     * Writes the configuration content to a file.
     *
     * @param fileName The name of the file to write to.
     * @param content  The content to write.
     */
    public void writeConfigToFile(String fileName, String content) {
        logger.info("Writing configuration content to file: {}", fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
            logger.info("Configuration content written successfully to file: {}", fileName);
        } catch (IOException e) {
            logger.error("Failed to write configuration file: {}", fileName, e);
            throw new RuntimeException("Failed to write configuration file: " + fileName, e);
        }
    }

    /**
     * A convenience method to create and write the config to a default file.
     *
     * @param dbParams   The database connection parameters.
     * @param dataHubUrl The URL of the DataHub server.
     */
    public void createAndWriteConfig(Map<String, String> dbParams, String dataHubUrl) {
        logger.info("Creating and writing configuration file using default file name: {}", DEFAULT_CONFIG_FILE_NAME);
        try {
            String configContent = createConfigContent(dbParams, dataHubUrl);
            writeConfigToFile(DEFAULT_CONFIG_FILE_NAME, configContent);
            logger.info("Configuration file created and written successfully to: {}", DEFAULT_CONFIG_FILE_NAME);
        } catch (Exception e) {
            logger.error("Failed to create and write configuration file: {}", DEFAULT_CONFIG_FILE_NAME, e);
            throw new RuntimeException("Failed to create and write configuration file: " + DEFAULT_CONFIG_FILE_NAME, e);
        }
    }
}
