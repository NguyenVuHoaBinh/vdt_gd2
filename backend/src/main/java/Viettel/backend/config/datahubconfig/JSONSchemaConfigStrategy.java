package Viettel.backend.config.datahubconfig;

import org.springframework.stereotype.Component;

import  java.util.Map;

@Component("jsonschemaConfigStrategy")
public class JSONSchemaConfigStrategy implements DataHubConfigStrategy {

    @Override
    public String createConfigContent(Map<String,String> dbParams, String dataHubUrl){
        StringBuilder configContent = new StringBuilder();
        configContent.append("source:\n")
                .append("  type: json-schema\n")
                .append("  config:\n")
                .append("    path: '").append(dbParams.get("path")).append("'\n")
                .append("    platform: schemaregistry\n");

        configContent.append("sink:\n")
                .append("  type: datahub-rest\n")
                .append("  config:\n")
                .append("    server: ").append(dataHubUrl).append("\n");


        return configContent.toString();
    }
}
