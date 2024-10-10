package Viettel.backend.service.datahubservice;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class IngestionProcessService {

    private static final Logger logger = LoggerFactory.getLogger(IngestionProcessService.class);
    private static final String OUTPUT_LOG_FILE = "ingestion_output.log";

    /**
     * Starts the ingestion process based on the operating system.
     *
     * @return The started Process.
     * @throws IOException If there is an error starting the process.
     */
    public Process startProcess() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String projectDir = System.getProperty("user.dir");

        logger.info("Starting ingestion process on OS: {}", os);
        ProcessBuilder processBuilder;

        if (os.contains("win")) {
            String command = Paths.get(projectDir, "run_ingestion.bat").toString();
            processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
            logger.debug("Using Windows command: {}", command);
        } else {
            String command = Paths.get(projectDir, "run_ingestion.sh").toString();
            processBuilder = new ProcessBuilder(command);
            logger.debug("Using Unix/Linux command: {}", command);
        }

        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            logger.info("Ingestion process started successfully.");
            return process;
        } catch (IOException e) {
            logger.error("Failed to start ingestion process.", e);
            throw e;
        }
    }

    /**
     * Processes the output of the ingestion process and logs failures and warnings.
     *
     * @param process The Process to be monitored.
     * @return An IngestionResult containing details of the ingestion process.
     * @throws IOException If there is an error processing the output.
     */
    public DataHubIngestionService.IngestionResult processOutput(Process process) throws IOException {
        DataHubIngestionService.IngestionResult result = new DataHubIngestionService.IngestionResult();
        Path outputFile = Paths.get(System.getProperty("user.dir"), OUTPUT_LOG_FILE);

        logger.info("Processing output of the ingestion process. Output will be written to: {}", outputFile);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("Ingestion process output: {}", line);
                writer.write(line);
                writer.newLine();

                if (line.contains("failure")) {
                    result.getFailures().add(line);
                    logger.warn("Failure detected: {}", line);
                } else if (line.contains("warning")) {
                    result.getWarnings().add(line);
                    logger.warn("Warning detected: {}", line);
                }
            }
        } catch (IOException e) {
            logger.error("Error processing the output of the ingestion process.", e);
            throw e;
        }

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                logger.error("Ingestion pipeline failed with exit code {}", exitCode);
                throw new RuntimeException("Ingestion pipeline failed with exit code " + exitCode);
            }
            logger.info("Ingestion process completed successfully with exit code 0.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Ingestion pipeline was interrupted.", e);
            throw new RuntimeException("Ingestion pipeline interrupted", e);
        }

        return result;
    }
}
