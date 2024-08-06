package model;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {
    public @NotBlank String getMessage() {
        return message;
    }

    public void setMessage(@NotBlank String message) {
        this.message = message;
    }

    public @NotBlank String getModel() {
        return model;
    }

    public void setModel(@NotBlank String model) {
        this.model = model;
    }

    @NotBlank
    private String message;
    @NotBlank
    private String model;

    // Getters and Setters
}

