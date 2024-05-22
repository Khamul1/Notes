package objects;

import interfaces.INote;
import interfaces.IUser;

import javax.swing.*;
import java.awt.event.ActionEvent;
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
    public void createNote(String text) {
        Note note = new Note(text);
        notes.add(note);
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

    @Override
    public void loadNotesFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            List<INote> loadedNotes = (List<INote>) ois.readObject();
            notes.addAll(loadedNotes);
        } catch (IOException | ClassNotFoundException e) {
            // Обработка ошибок чтения или преобразования объекта
            System.err.println("Ошибка при загрузке заметок из файла: " + e.getMessage());
        }
    }

    @Override
    public void saveNotesToFile(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(notes);
        } catch (IOException e) {
            // Обработка ошибок записи
            System.err.println("Ошибка при сохранении заметок в файл: " + e.getMessage());
        }
    }
}
