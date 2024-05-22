package interfaces;

import java.time.LocalDateTime;

public interface INote {
    String getText();
    void setText(String text);
    String getNoteString();
    LocalDateTime getCreationDate();
}
