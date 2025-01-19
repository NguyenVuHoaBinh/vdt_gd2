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

    // TODO nani?
    private String role = "";

    @NotBlank(message = "A llm model is required")
    private String llmModel;

    @NotBlank(message = "An embedding model is required")
    private String embeddingmodel;

    // TODO: more indexes? from FE
    @NotBlank(message = "Choose a field (ie books)")
    private String mydioIndex;

}
