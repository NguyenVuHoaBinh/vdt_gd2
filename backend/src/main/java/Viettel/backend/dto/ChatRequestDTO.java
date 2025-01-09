package Viettel.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequestDTO {
    @NotBlank(message = "No session id found")
    private String sessionId;

    @NotBlank(message = "No message from user")
    private String message;

    private String systemRole = "";

    @NotBlank(message = "A model is required")
    private String model;

}
