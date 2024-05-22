package objects;

import interfaces.INote;
import interfaces.INoteManager;
import interfaces.IUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteManager implements INoteManager {
    private Map<String, IUser> users;

    public NoteManager() {
        this.users = new HashMap<>();
    }

    @Override
    public IUser createUser(String name) {
        IUser user = new User(name);
        users.put(name, user);
        return user;
    }

    @Override
    public void createNote(String userName, String noteText) {
        IUser user = users.get(userName);
        if (user != null) {
            user.createNote(noteText);
        }
    }

    @Override
    public void editNote(String username, int noteIndex, String newText) {
        IUser user = users.get(username);
        if (user != null) {
            user.editNote(noteIndex, newText);
        }
    }

    @Override
    public void deleteNote(String username, int noteIndex) {
        IUser user = users.get(username);
        if (user != null) {
            user.deleteNote(noteIndex);
        }
    }

    @Override
    public List<INote> getNotes(String username) {
        IUser user = users.get(username);
        if (user != null) {
            return user.getNotes();
        }
        return null;
    }

    @Override
    public List<IUser> getUsers() {
        return new ArrayList<>(users.values());
    }
}
