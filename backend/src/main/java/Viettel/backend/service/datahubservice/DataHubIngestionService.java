package Viettel.backend.service.datahubservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DataHubIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(DataHubIngestionService.class);
    private static final String DATAHUB_URL = "http://localhost:8080";

    @Autowired
    private ConfigGeneratorService configGeneratorService;

    @Autowired
    private IngestionProcessService ingestionProcessService;

    /**
     * Generates and writes the ingestion configuration file using the appropriate strategy.
     *
     * @param dbParams The database connection parameters.
     */
    public void generateIngestionConfig(Map<String, String> dbParams) {
        try {
            logger.info("Generating ingestion configuration for DataHub.");
            configGeneratorService.createAndWriteConfig(dbParams, DATAHUB_URL);
            logger.info("Ingestion configuration generated and written successfully.");
        } catch (Exception e) {
            logger.error("Failed to generate ingestion configuration for DataHub with parameters: {}", dbParams, e);
            throw new RuntimeException("Error generating ingestion configuration", e);
        }
    }

    /**
     * Runs the ingestion pipeline and returns the result.
     *
     * @return The result of the ingestion process.
     * @throws IOException If there is an error running the ingestion pipeline.
     */
    public IngestionResult runIngestionPipeline() throws IOException {
        logger.info("Starting the ingestion pipeline process.");
        try {
            Process process = ingestionProcessService.startProcess();
            logger.info("Ingestion process started successfully.");

            IngestionResult result = ingestionProcessService.processOutput(process);
            logger.info("Ingestion process completed. Failures: {}, Warnings: {}", result.getFailures().size(), result.getWarnings().size());

            return result;
        } catch (IOException e) {
            logger.error("Error occurred while running the ingestion pipeline.", e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error occurred during ingestion pipeline execution.", e);
            throw new RuntimeException("Unexpected error during ingestion pipeline execution", e);
        }
    }

    public static class IngestionResult {
        private List<String> failures = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public List<String> getFailures() {
            return failures;
        }

        public void setFailures(List<String> failures) {
            this.failures = failures;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }
    }
}
