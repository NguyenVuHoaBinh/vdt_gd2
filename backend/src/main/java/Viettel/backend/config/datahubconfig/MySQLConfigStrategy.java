package Viettel.backend.config.datahubconfig;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component("mysqlConfigStrategy")
public class MySQLConfigStrategy implements DataHubConfigStrategy {

    @Override
    public String createConfigContent(Map<String, String> dbParams, String dataHubUrl) {
        validateRequiredParams(dbParams);

        StringBuilder configContent = new StringBuilder();
        configContent.append("source:\n")
                .append("  type: mysql\n")
                .append("  config:\n")
                .append("    host_port: '").append(dbParams.get("host")).append("'\n")
                .append("    database: ").append(dbParams.get("database")).append("\n")
                .append("    username: ").append(dbParams.get("user")).append("\n")
                .append("    password: '").append(dbParams.get("password")).append("'\n");

        // Handle dynamic true/false parameters (optional)
        appendBooleanParam(configContent, "include_tables", dbParams);
        appendBooleanParam(configContent, "include_views", dbParams);
        appendBooleanParam(configContent, "profiling_enabled", dbParams);
        appendBooleanParam(configContent, "profile_table_level_only", dbParams);
        appendBooleanParam(configContent, "stateful_ingestion_enabled", dbParams);

        // Handle dynamic patterns (optional)
        appendPatternConfig(configContent, "database_pattern", dbParams);
        appendPatternConfig(configContent, "view_pattern", dbParams);
        appendPatternConfig(configContent, "table_pattern", dbParams);
        appendPatternConfig(configContent, "schema_pattern", dbParams);

        configContent.append("sink:\n")
                .append("  type: datahub-rest\n")
                .append("  config:\n")
                .append("    server: ").append(dataHubUrl).append("\n");

        return configContent.toString();
    }

    private void validateRequiredParams(Map<String, String> dbParams) {
        if (!dbParams.containsKey("host") || !dbParams.containsKey("database") ||
                !dbParams.containsKey("user") || !dbParams.containsKey("password")) {
            throw new IllegalArgumentException("Missing required database parameters: host_port, database, username, or password.");
        }
    }

    private void appendBooleanParam(StringBuilder configContent, String paramName, Map<String, String> dbParams) {
        String paramValue = dbParams.get(paramName);
        if (paramValue != null && !paramValue.isEmpty()) {
            configContent.append("    ").append(paramName.replace("_", ".")).append(": ").append(paramValue).append("\n");
        }
    }

    private void appendPatternConfig(StringBuilder configContent, String patternType, Map<String, String> dbParams) {
        String allowListKey = patternType + "_allow";
        String denyListKey = patternType + "_deny";

        String[] allowItems = dbParams.getOrDefault(allowListKey, "").split(",");
        String[] denyItems = dbParams.getOrDefault(denyListKey, "").split(",");

        boolean hasAllow = allowItems.length > 0 && !allowItems[0].isEmpty();
        boolean hasDeny = denyItems.length > 0 && !denyItems[0].isEmpty();

        if (hasAllow || hasDeny) {
            configContent.append("    ").append(patternType).append(":\n");
            if (hasAllow) {
                configContent.append("      allow:\n");
                for (String allowItem : allowItems) {
                    configContent.append("        - '").append(allowItem.trim()).append("'\n");
                }
            }
            if (hasDeny) {
                configContent.append("      deny:\n");
                for (String denyItem : denyItems) {
                    configContent.append("        - '").append(denyItem.trim()).append("'\n");
                }
            }
        }
    }
}
