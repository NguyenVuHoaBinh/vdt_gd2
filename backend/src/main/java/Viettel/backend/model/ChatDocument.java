package Viettel.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

// TODO: fix: this is still <testing> mode
@Getter
public class ChatDocument {
    @NotBlank
    String sessionId; // ???

    String text;

    @JsonIgnore
    @Setter
    byte[] embedding;

    public ChatDocument(String human, String assistant) { // ini?
        sessionId = "123123132";  //test
        setText(human, assistant);
    }

//    <FOR TESTING>
//    public ChatDocument(String text) {
//        sessionId = "123123131";
//        this.text = text;
//    }

    public void setText(String assistant, String human) {
        this.text = String.format("Human: %s\nAssistant: %s", assistant, human);
    }

}
