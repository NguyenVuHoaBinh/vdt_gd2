package Viettel.backend.dto;

import jakarta.validation.constraints.*;

public class LlmConfigDTO {
    // TODO mod, might move to model?
    @NotBlank(message = "No model found")
    private String model;

    @Min(value = 0, message = "Temperature must be within 0 and 1")
    @Max(value = 1, message = "Temperature must be within 0 and 1")
    private double temperature;

    @Min(value = 0, message = "Number of tokens cannot be negative")
    private int maxTokens;
}
