package Viettel.backend.config.datahubconfig;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PostgresConfigStrategy implements DataHubConfigStrategy {

    @Override
    public String createConfigContent(Map<String, String> dbParams, String dataHubUrl) {
        StringBuilder config = new StringBuilder();

        config.append("source:\n");
        config.append("  type: postgres\n");
        config.append("  config:\n");

        // Required parameters
        config.append("    host_port: '").append(dbParams.get("host")).append("'\n");
        config.append("    database: ").append(dbParams.get("database")).append("\n");
        config.append("    username: ").append(dbParams.get("user")).append("\n");
        config.append("    password: '").append(dbParams.get("password")).append("'\n");

        // Optional boolean parameters
        addOptionalBooleanParam(config, "include.tables", dbParams.get("include_tables"));
        addOptionalBooleanParam(config, "include.views", dbParams.get("include_views"));
        addOptionalBooleanParam(config, "profiling.enabled", dbParams.get("profiling_enabled"));
        addOptionalBooleanParam(config, "stateful.ingestion.enabled", dbParams.get("stateful_ingestion_enabled"));

        // Dynamic patterns
        addPattern(config, "database_pattern", dbParams.get("database_pattern_allow"), dbParams.get("database_pattern_deny"));
        addPattern(config, "table_pattern", dbParams.get("table_pattern_allow"), dbParams.get("table_pattern_deny"));

        // Sink configuration
        config.append("sink:\n");
        config.append("  type: datahub-rest\n");
        config.append("  config:\n");
        config.append("    server: ").append(dataHubUrl).append("\n");

        return config.toString();
    }

    private void addOptionalBooleanParam(StringBuilder config, String paramName, String paramValue) {
        if (paramValue != null && !paramValue.isEmpty()) {
            config.append("    ").append(paramName).append(": ").append(paramValue).append("\n");
        }
    }

    private void addPattern(StringBuilder config, String patternName, String allowPatterns, String denyPatterns) {
        boolean hasAllow = allowPatterns != null && !allowPatterns.isEmpty();
        boolean hasDeny = denyPatterns != null && !denyPatterns.isEmpty();

        if (hasAllow || hasDeny) {
            config.append("    ").append(patternName).append(":\n");
            if (hasAllow) {
                config.append("      allow:\n");
                for (String pattern : allowPatterns.split(",")) {
                    config.append("        - '").append(pattern.trim()).append("'\n");
                }
            }
            if (hasDeny) {
                config.append("      deny:\n");
                for (String pattern : denyPatterns.split(",")) {
                    config.append("        - '").append(pattern.trim()).append("'\n");
                }
            }
        }
    }
}
