package Viettel.backend.service.datahubservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DataHubIngestionService {

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
        configGeneratorService.createAndWriteConfig(dbParams, DATAHUB_URL);
    }

    /**
     * Runs the ingestion pipeline and returns the result.
     *
     * @return The result of the ingestion process.
     * @throws IOException If there is an error running the ingestion pipeline.
     */
    public IngestionResult runIngestionPipeline() throws IOException {
        Process process = ingestionProcessService.startProcess();
        return ingestionProcessService.processOutput(process);
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
