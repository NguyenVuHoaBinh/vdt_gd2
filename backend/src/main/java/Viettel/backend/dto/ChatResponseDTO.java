package Viettel.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatResponseDTO {
    @NotBlank(message = "No response from server")
    private String response;
    private String audio;

    public ChatResponseDTO(String response, String audio) {
        this.response = response;
        this.audio = audio;
    }
}
