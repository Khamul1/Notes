package interfaces;

import java.util.List;

public interface INoteManager {
    IUser createUser(String name);
    void createNote(String userName, String noteText);
    void editNote(String username, int noteIndex, String newText);
    void deleteNote(String username, int noteIndex);
    List<INote> getNotes(String username);
    List<IUser> getUsers();
}
