package objects;

import interfaces.INote;
import interfaces.IUser;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User implements IUser, Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private LocalDateTime createdAt;
    private List<INote> notes;

    public User(String name) {
        this.name = name;
        this.createdAt = LocalDateTime.now();
        this.notes = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public INote createNote(String text) {
        INote note = new Note(text);
        notes.add(note);
        return note;
    }

    @Override
    public void editNote(int index, String newText) {
        notes.get(index).setText(newText);
    }

    @Override
    public void deleteNote(int index) {
        notes.remove(index);
    }

    @Override
    public List<INote> getNotes() {
        return Collections.unmodifiableList(notes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadNotesFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            notes = (List<INote>) ois.readObject();
        }
    }

    @Override
    public void saveNotesToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(notes);
        }
    }
}
