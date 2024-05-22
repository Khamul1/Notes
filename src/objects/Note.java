package objects;

import interfaces.INote;
import interfaces.IUser;

import java.io.*;
import java.time.LocalDateTime;

public class Note implements INote, Serializable {
    private static final long serialVersionUID = 1L;

    private String text;
    private LocalDateTime createAt;
    private LocalDateTime changedAt;

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

    public void save(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }
    }

    public static Note load(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (Note) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            // Handle the exception appropriately
            return null;
        }
    }

    @Override
    public String getNoteString() {
        return "Note: " + text + ", created at: " + createAt + ", last modified at: " + changedAt;
    }
}