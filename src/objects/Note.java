package objects;

import interfaces.INote;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Note implements INote, Serializable {
    private static final long serialVersionUID = 1L;
    private String text;
    private LocalDateTime createAt;
    private LocalDateTime changedAt;
    private java.lang.String text1;

    public Note(String text) {
        this.text = text;
        this.createAt = LocalDateTime.now();
        this.changedAt = this.createAt;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String text) {
        this.text = text;
        this.changedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    @Override
    public String getNoteString() {
        return "Note: " + text + ", created at: " + createAt + ", last modified at: " + changedAt;
    }

    @Override
    public LocalDateTime getCreationDate() {
        return createAt;
    }

    @Override
    public String toString() {
        return text1;
    }
}
