import interfaces.INote;
import interfaces.INoteManager;
import interfaces.IUser;
import objects.NoteManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class NoteApplication extends JFrame implements ActionListener {
    private INoteManager noteManager;
    private IUser currentUser;
    private JList<String> notesList;
    private JTextArea noteTextArea;
    private JMenuItem newNoteItem;
    private JMenuItem saveItem;
    private JMenuItem loadItem;

    public NoteApplication(INoteManager noteManager) {
        super("Note Application");
        this.noteManager = noteManager;
        this.currentUser = noteManager.createUser("Default User");

        // Создание меню
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        newNoteItem = new JMenuItem("New Note");
        newNoteItem.addActionListener(this);
        saveItem = new JMenuItem("Save");
        saveItem.addActionListener(this);
        loadItem = new JMenuItem("Load");
        loadItem.addActionListener(this);
        fileMenu.add(newNoteItem);
        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        // Создание списка заметок
        notesList = new JList<>();
        notesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notesList.addListSelectionListener(e -> {
            int selectedIndex = notesList.getSelectedIndex();
            if (selectedIndex >= 0) {
                INote selectedNote = currentUser.getNotes().get(selectedIndex);
                noteTextArea.setText(selectedNote.getText());
            }
        });
        JScrollPane listScrollPane = new JScrollPane(notesList);
        listScrollPane.setPreferredSize(new Dimension(200, 200));

        // Создание текстовой области для заметки
        noteTextArea = new JTextArea();
        JScrollPane textScrollPane = new JScrollPane(noteTextArea);
        textScrollPane.setPreferredSize(new Dimension(200, 200));

        // Создание интерфейса приложения
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(listScrollPane);
        panel.add(textScrollPane);
        add(panel);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == newNoteItem) {
            String newNoteText = JOptionPane.showInputDialog(this, "Enter new note text:");
            if (newNoteText != null && !newNoteText.isEmpty()) {
                currentUser.createNote(newNoteText);
                updateNotesList();
            }
        } else {
            handleSaveAndLoadActions(e);
        }
    }

    private void handleSaveAndLoadActions(ActionEvent e) {
        if (e.getSource() == saveItem) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filename = selectedFile.getAbsolutePath();
                currentUser.saveNotesToFile(filename);
            }
        } else if (e.getSource() == loadItem) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String filename = selectedFile.getAbsolutePath();
                currentUser.loadNotesFromFile(filename);
                updateNotesList();
            }
        }
    }

    private void updateNotesList() {
        List<String> noteTexts = currentUser.getNotes().stream()
                .map(INote::getText)
                .collect(Collectors.toList());
        notesList.setListData(noteTexts.toArray(new String[0]));
    }

    public static void main(String[] args) {
        // Создание экземпляра NoteManager
        INoteManager noteManager = new NoteManager(); // Пример, замените на вашу реализацию

        // Создание и отображение главного окна приложения
        SwingUtilities.invokeLater(() -> {
            NoteApplication app = new NoteApplication(noteManager);
        });
    }
}