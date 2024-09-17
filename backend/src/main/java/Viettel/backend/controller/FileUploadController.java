package Viettel.backend.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @PostMapping("/upload")
    public Map<String, Object> uploadFiles(@RequestParam("files") MultipartFile[] files,
                                           @RequestParam("sessionId") String sessionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Define the directory where files will be saved, using the session ID
            String uploadDir = System.getProperty("user.dir") + "/uploads/" + sessionId + "/";

            // Create the directory if it doesn't exist
            Path directoryPath = Paths.get(uploadDir);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // Iterate over the files and save each one
            StringBuilder fileNames = new StringBuilder();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Path filePath = directoryPath.resolve(file.getOriginalFilename());
                    logger.info("Saving file to: " + filePath.toAbsolutePath());
                    file.transferTo(filePath.toFile());
                    fileNames.append(file.getOriginalFilename()).append(", ");
                }
            }

            // Remove the last comma and space
            if (fileNames.length() > 0) {
                fileNames.setLength(fileNames.length() - 2);
            }

            // Add folder path and file names to the response
            response.put("success", true);
            response.put("message", "Files uploaded successfully: " + fileNames.toString());
            response.put("folderPath", uploadDir);  // Return the folder path

        } catch (IOException e) {
            logger.error("File upload failed", e);
            response.put("success", false);
            response.put("message", "File upload failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred", e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
        }
        return response;
    }
}
