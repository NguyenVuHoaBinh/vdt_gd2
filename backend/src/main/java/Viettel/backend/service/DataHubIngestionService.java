package Viettel.backend.service;

import Viettel.backend.config.ConfigGenerator;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DataHubIngestionService {

    private static final String DATAHUB_URL = "http://localhost:8080";

    public void generateIngestionConfig(Map<String, String> dbParams) {
        String configContent = ConfigGenerator.createConfigContent(dbParams, DATAHUB_URL);
        ConfigGenerator.writeConfigToFile("dynamic_ingestion.yml", configContent);
    }

    public IngestionResult runIngestionPipeline() throws IOException {
        Process process = IngestionProcessExecutor.startProcess();
        return IngestionProcessExecutor.processOutput(process);
    }

    public static class IngestionResult {
        private List<String> failures = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        // Getters and Setters
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
