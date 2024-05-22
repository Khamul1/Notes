package interfaces;

import java.util.List;

public interface IUser {
    String getName();
    void createNote(String text);
    void editNote(int index, String newText);
    void deleteNote(int index);
    List<INote> getNotes();
    void saveNotesToFile(String filename);
    void loadNotesFromFile(String filename);
}