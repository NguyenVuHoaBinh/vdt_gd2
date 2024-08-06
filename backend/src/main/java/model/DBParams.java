package model;

import jakarta.validation.constraints.NotBlank;

public class DBParams {
    @NotBlank
    private String host;

    public @NotBlank String getHost() {
        return host;
    }

    public void setHost(@NotBlank String host) {
        this.host = host;
    }

    public @NotBlank String getDatabase() {
        return database;
    }

    public void setDatabase(@NotBlank String database) {
        this.database = database;
    }

    public @NotBlank String getUser() {
        return user;
    }

    public void setUser(@NotBlank String user) {
        this.user = user;
    }

    public @NotBlank String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank String password) {
        this.password = password;
    }

    public @NotBlank String getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(@NotBlank String databaseType) {
        this.databaseType = databaseType;
    }

    @NotBlank
    private String database;
    @NotBlank
    private String user;
    @NotBlank
    private String password;
    @NotBlank
    private String databaseType;  // mysql, postgresql, sqlserver

    // Getters and Setters
}
