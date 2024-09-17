package Viettel.backend.config.datahubconfig;

import java.util.Map;

public interface DataHubConfigStrategy {
    String createConfigContent(Map<String, String> dbParams, String dataHubUrl);
}
