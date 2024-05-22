import interfaces.INote;
import interfaces.INoteManager;
import interfaces.IUser;
import objects.Note;
import objects.NoteManager;
import objects.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NoteApplication extends JFrame {
    private final INoteManager noteManager;
    private IUser currentUser;
    private JComboBox<String> userComboBox;
    private JList<INote> notesList;
    private JTextPane noteTextArea; // Используем JTextPane

    public NoteApplication(INoteManager noteManager) {
        super("Note Application");
        this.noteManager = noteManager;

        createInitialData();
        createApplicationLayout();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createInitialData() {
        currentUser = noteManager.createUser("Default User");
        currentUser.createNote("Initial note");
    }

    private void createApplicationLayout() {
        createUserComboBox();
        createNotesList();
        createNoteTextArea();
        createToolbar();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(userComboBox, BorderLayout.NORTH);
        listPanel.add(new JScrollPane(notesList), BorderLayout.CENTER);
        mainPanel.add(listPanel, BorderLayout.WEST);

        mainPanel.add(new JScrollPane(noteTextArea), BorderLayout.CENTER);
        add(mainPanel);

        setPreferredSize(new Dimension(1280, 960));
    }

    private void createUserComboBox() {
        List<IUser> users = noteManager.getUsers();
        String[] userNames = users.stream()
                .map(IUser::getName)
                .toArray(String[]::new);
        userComboBox = new JComboBox<>(userNames);
        userComboBox.addActionListener(e -> {
            String selectedUserName = (String) userComboBox.getSelectedItem();
            currentUser = users.stream()
                    .filter(user -> user.getName().equals(selectedUserName))
                    .findFirst()
                    .orElse(currentUser);
            updateNotesList();
        });
    }

    private void createNotesList() {
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
        listScrollPane.setPreferredSize(new Dimension(300, 400));
    }

    private void createNoteTextArea() {
        noteTextArea = new JTextPane(); // Инициализируем JTextPane
        noteTextArea.setContentType("text/html"); // Устанавливаем тип контента HTML
        noteTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSelectedNote();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSelectedNote();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSelectedNote();
            }

            private void updateSelectedNote() {
                int selectedIndex = notesList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    INote selectedNote = currentUser.getNotes().get(selectedIndex);
                    selectedNote.setText(noteTextArea.getText());
                }
            }
        });
        JScrollPane textScrollPane = new JScrollPane(noteTextArea);
        textScrollPane.setPreferredSize(new Dimension(500, 400));
    }

    private void createToolbar() {
        JToolBar toolbar = new JToolBar();

        // Кнопки для работы с заметками
        JButton newButton = new JButton("New");
        newButton.addActionListener(e -> createNewNote());
        toolbar.add(newButton);

        JButton editButton = new JButton("Edit");
        editButton.addActionListener(e -> editSelectedNote());
        toolbar.add(editButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> deleteSelectedNote());
        toolbar.add(deleteButton);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveNotes());
        toolbar.add(saveButton);

        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(e -> loadNotes());
        toolbar.add(loadButton);

        // Кнопки форматирования текста
        JButton boldButton = new JButton("B");
        boldButton.addActionListener(e -> wrapSelectedText("<b>", "</b>"));
        toolbar.add(boldButton);

        JButton italicButton = new JButton("I");
        italicButton.addActionListener(e -> wrapSelectedText("<i>", "</i>"));
        toolbar.add(italicButton);

        // Выбор шрифта
        JComboBox<String> fontComboBox = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontComboBox.addActionListener(e -> {
            String selectedFont = (String) fontComboBox.getSelectedItem();
            if (selectedFont != null) {
                changeFontStyle("font-family: " + selectedFont + ";");
            }
        });
        toolbar.add(fontComboBox);

        // Выбор размера шрифта
        JComboBox<Integer> fontSizeComboBox = new JComboBox<>(new Integer[]{8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24});
        fontSizeComboBox.addActionListener(e -> {
            Integer selectedSize = (Integer) fontSizeComboBox.getSelectedItem();
            if (selectedSize != null) {
                changeFontStyle("font-size: " + selectedSize + "pt;");
            }
        });
        toolbar.add(fontSizeComboBox);

        // Кнопка вставки изображения
        JButton imageButton = new JButton("Image");
        imageButton.addActionListener(e -> insertImage());
        toolbar.add(imageButton);

        add(toolbar, BorderLayout.NORTH);
    }

    // Метод для выделения текста тегами
    private void wrapSelectedText(String startTag, String endTag) {
        try {
            int start = noteTextArea.getSelectionStart();
            int end = noteTextArea.getSelectionEnd();
            String selectedText = noteTextArea.getDocument().getText(start, end - start);
            noteTextArea.getDocument().insertString(end, endTag, null);
            noteTextArea.getDocument().insertString(start, startTag, null);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    // Метод для изменения стиля текста
    private void changeFontStyle(String style) {
        try {
            int start = noteTextArea.getSelectionStart();
            int end = noteTextArea.getSelectionEnd();
            String selectedText = noteTextArea.getDocument().getText(start, end - start);
            noteTextArea.getDocument().insertString(end, "</span>", null);
            noteTextArea.getDocument().insertString(start, "<span style=\"" + style + "\">", null);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    // Метод для вставки изображения
    private void insertImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Получаем base64 представление изображения
                String base64Image = Base64.getEncoder().encodeToString(Files.readAllBytes(selectedFile.toPath()));
                // Вставляем изображение в формате HTML
                noteTextArea.getDocument().insertString(noteTextArea.getCaretPosition(), "<img src='data:image/png;base64," + base64Image + "'/>", null);
            } catch (IOException | BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void createNewNote() {
        String newNoteText = JOptionPane.showInputDialog(this, "Enter new note text:");
        if (newNoteText != null && !newNoteText.isEmpty()) {
            INote newNote = currentUser.createNote(newNoteText);
            updateNotesList();
            notesList.setSelectedValue(newNote, true);
        }
    }

    private void editSelectedNote() {
        int selectedIndex = notesList.getSelectedIndex();
        if (selectedIndex >= 0) {
            INote selectedNote = currentUser.getNotes().get(selectedIndex);
            String newText = JOptionPane.showInputDialog(this, "Enter new note text:", selectedNote.getText());
            if (newText != null && !newText.isEmpty()) {
                selectedNote.setText(newText);
                updateNotesList();
                notesList.setSelectedValue(selectedNote, true);
            }
        }
    }

    private void deleteSelectedNote() {
        int selectedIndex = notesList.getSelectedIndex();
        if (selectedIndex >= 0) {
            currentUser.deleteNote(selectedIndex);
            updateNotesList();
        }
    }

    private void saveNotes() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filename = selectedFile.getAbsolutePath();
            try {
                currentUser.saveNotesToFile(filename);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving notes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadNotes() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filename = selectedFile.getAbsolutePath();
            try {
                currentUser.loadNotesFromFile(filename);
                updateNotesList();
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Error loading notes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateNotesList() {
        List<INote> notes = currentUser.getNotes().stream()
                .sorted(Comparator.comparing(INote::getCreationDate).reversed())
                .collect(Collectors.toList());
        notesList.setListData(notes.toArray(new INote[0]));
    }

    public static void main(String[] args) {
        INoteManager noteManager = new NoteManager();
        SwingUtilities.invokeLater(() -> new NoteApplication(noteManager));
    }
}
