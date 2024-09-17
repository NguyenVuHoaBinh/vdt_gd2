package Viettel.backend.dto;

import java.util.List;

public class ModelResponse {

    private String name;
    private List<String> versions;

    // Getters and Setters for name and versions

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getVersions() {
        return versions;
    }

    public void setVersions(List<String> versions) {
        this.versions = versions;
    }
}
