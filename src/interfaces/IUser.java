package interfaces;

import java.io.IOException;
import java.util.List;

public interface IUser {
    String getName();
    INote createNote(String text);
    void editNote(int index, String newText);
    void deleteNote(int index);
    List<INote> getNotes();
    void saveNotesToFile(String filename) throws IOException;
    void loadNotesFromFile(String filename) throws IOException, ClassNotFoundException;
}
