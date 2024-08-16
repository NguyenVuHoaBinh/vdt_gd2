package Viettel.backend.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import Viettel.backend.service.DataHubIngestionService;

public class IngestionProcessExecutor {

    public static Process startProcess() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder;
        String projectDir = System.getProperty("user.dir");

        if (os.contains("win")) {
            processBuilder = new ProcessBuilder("cmd.exe", "/c", Paths.get(projectDir, "run_ingestion.bat").toString());
        } else {
            processBuilder = new ProcessBuilder(Paths.get(projectDir, "run_ingestion.sh").toString());
        }

        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

    public static DataHubIngestionService.IngestionResult processOutput(Process process) throws IOException {
        DataHubIngestionService.IngestionResult result = new DataHubIngestionService.IngestionResult();
        Path outputFile = Paths.get(System.getProperty("user.dir"), "ingestion_output.log");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                writer.write(line);
                writer.newLine();

                if (line.contains("failure")) {
                    result.getFailures().add(line);
                } else if (line.contains("warning")) {
                    result.getWarnings().add(line);
                }
            }
        }

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
}
