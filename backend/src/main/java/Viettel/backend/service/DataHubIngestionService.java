package Viettel.backend.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DataHubIngestionService {

    private static final String DATAHUB_URL = "http://localhost:8080";

    public void generateIngestionConfig(Map<String, String> dbParams) {
        String configContent = "source:\n" +
                "  type: " + dbParams.get("dbType") + "\n" +
                "  config:\n" +
                "    username: " + dbParams.get("user") + "\n" +
                "    password: " + dbParams.get("password") + "\n" +
                "    database: " + dbParams.get("database") + "\n" +
                "    host_port: " + dbParams.get("host") + "\n" +
                "sink:\n" +
                "  type: datahub-rest\n" +
                "  config:\n" +
                "    server: http://localhost:8080\n";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("dynamic_ingestion.yml"))) {
            writer.write(configContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public class IngestionResult {
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

    public IngestionResult runIngestionPipeline() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;
        String projectDir = System.getProperty("user.dir");

        // Determine the correct command based on the OS
        if (os.contains("win")) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", Paths.get(projectDir, "run_ingestion.bat").toString());
        } else {
            processBuilder = new ProcessBuilder(Paths.get(projectDir, "run_ingestion.sh").toString());
        }

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        IngestionResult result = new IngestionResult();

        // File to store the output of the ingestion process
        Path outputFile = Paths.get(projectDir, "ingestion_output.log");

        // Use try-with-resources to automatically handle closing resources
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Write the output to the console and to the file
                System.out.println(line);
                writer.write(line);
                writer.newLine(); // Write a new line to the file

                // Check for failures or warnings and add them to the result
                if (line.contains("failure")) {
                    result.getFailures().add(line);
                } else if (line.contains("warning")) {
                    result.getWarnings().add(line);
                }
            }
        }

        // Wait for the process to complete and check for errors
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Ingestion pipeline failed with exit code " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ingestion pipeline interrupted", e);
        }

        return result;
    }

    public String fetchMetadata(String entityUrn) {
        RestTemplate restTemplate = new RestTemplate();
        String url = DATAHUB_URL + "/entities/" + entityUrn;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to fetch metadata: " + response.getStatusCode());
        }
    }
}
