package Viettel.backend.config;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class ConfigGenerator {

    public static String createConfigContent(Map<String, String> dbParams, String dataHubUrl) {
        return "source:\n" +
                "  type: " + dbParams.get("dbType") + "\n" +
                "  config:\n" +
                "    username: " + dbParams.get("user") + "\n" +
                "    password: " + dbParams.get("password") + "\n" +
                "    database: " + dbParams.get("database") + "\n" +
                "    host_port: " + dbParams.get("host") + "\n" +
                "sink:\n" +
                "  type: datahub-rest\n" +
                "  config:\n" +
                "    server: " + dataHubUrl + "\n";
    }

    public static void writeConfigToFile(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
