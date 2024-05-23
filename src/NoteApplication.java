import interfaces.INote;
import interfaces.INoteManager;
import interfaces.IUser;
import objects.Note;
import objects.NoteManager;
import objects.User;
import org.jsoup.Jsoup;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
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
    private JTextPane noteTextArea;
    private JScrollPane listScrollPane;
    private JScrollPane textScrollPane;
    private JToolBar toolbar;
    private JPanel listPanel;

    // DocumentListener как поле класса
    private final DocumentListener noteTextAreaListener = new DocumentListener() {
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
    };

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

        listPanel = new JPanel(new BorderLayout());
        listPanel.add(userComboBox, BorderLayout.NORTH);
        listPanel.add(listScrollPane, BorderLayout.CENTER);

        mainPanel.add(listPanel, BorderLayout.WEST);
        mainPanel.add(textScrollPane, BorderLayout.CENTER);

        add(toolbar, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

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
        listScrollPane = createScrollPane(notesList);
    }

    private void createNoteTextArea() {
        noteTextArea = new JTextPane();
        noteTextArea.setEditorKit(new HTMLEditorKit());
        noteTextArea.setContentType("text/html");
        noteTextArea.setDocument(new HTMLDocument());
        noteTextArea.getDocument().addDocumentListener(noteTextAreaListener);

        textScrollPane = createScrollPane(noteTextArea);
    }

    private void createToolbar() {
        toolbar = new JToolBar();

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

        JButton boldButton = new JButton("B");
        boldButton.addActionListener(e -> wrapSelectedText("<span style=\"font-weight: bold;\">", "</span>"));
        toolbar.add(boldButton);

        JButton italicButton = new JButton("I");
        italicButton.addActionListener(e -> wrapSelectedText("<span style=\"font-style: italic;\">", "</span>"));
        toolbar.add(italicButton);

        JComboBox<String> fontComboBox = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        JComboBox<Integer> fontSizeComboBox = new JComboBox<>(new Integer[]{8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24});

        fontComboBox.addActionListener(e -> {
            String selectedFont = (String) fontComboBox.getSelectedItem();
            Integer selectedSize = (Integer) fontSizeComboBox.getSelectedItem();
            if (selectedFont != null && selectedSize != null) {
                changeFontStyle("font-family", selectedFont);
                changeFontStyle("font-size", selectedSize + "pt");
            }
        });
        toolbar.add(fontComboBox);

        JButton imageButton = new JButton("Image");
        imageButton.addActionListener(e -> insertImage());
        toolbar.add(imageButton);

        fontSizeComboBox.addActionListener(e -> {
            Integer selectedSize = (Integer) fontSizeComboBox.getSelectedItem();
            if (selectedSize != null) {
                changeFontStyle("font-size", selectedSize + "pt");
            }
        });
        toolbar.add(fontSizeComboBox);
    }

    private void wrapSelectedText(String startTag, String endTag) {
        try {
            int start = noteTextArea.getSelectionStart();
            int end = noteTextArea.getSelectionEnd();
            if (start != end) {
                HTMLDocument doc = (HTMLDocument) noteTextArea.getDocument();
                String selectedText = doc.getText(start, end - start);


                boolean hasStartTag = selectedText.contains(startTag);
                boolean hasEndTag = selectedText.contains(endTag);

                if (hasStartTag && hasEndTag) {
                    selectedText = selectedText.replaceAll(startTag, "");
                    selectedText = selectedText.replaceAll(endTag, "");
                    doc.remove(start, end - start);
                    doc.insertString(start, selectedText, null);
                } else if (!hasStartTag && !hasEndTag) {
                    doc.insertString(end, endTag, null);
                    doc.insertString(start, startTag, null);
                } else {
                    if (hasStartTag) {
                        doc.remove(start, startTag.length());
                    } else {
                        doc.insertString(end, endTag, null);
                    }
                }
            }
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }


    private void changeFontStyle(String styleAttribute, String styleValue) {
        try {
            int start = noteTextArea.getSelectionStart();
            int end = noteTextArea.getSelectionEnd();
            if (start != end) {
                HTMLDocument doc = (HTMLDocument) noteTextArea.getDocument();
                Element root = doc.getDefaultRootElement();
                Element startElement = root.getElement(root.getElementIndex(start));
                Element endElement = root.getElement(root.getElementIndex(end - 1));

                for (int i = startElement.getElementIndex(start); i <= endElement.getElementIndex(end - 1); i++) {
                    Element element = startElement.getElement(i);
                    String existingStyle = element.getAttributes().getAttribute(HTML.Tag.SPAN) != null ?
                            element.getAttributes().getAttribute(HTML.Tag.SPAN).toString() : "";
                    if (existingStyle.isEmpty()) {
                        existingStyle = "style=\"" + styleAttribute + ":" + styleValue + ";\"";
                    } else {
                        if (!existingStyle.contains(styleAttribute)) {
                            existingStyle = existingStyle.substring(0, existingStyle.length() - 1) + ";" + styleAttribute + ":" + styleValue + "\"";
                        } else {
                            String[] styles = existingStyle.split(";");
                            StringBuilder newStyle = new StringBuilder("style=\"");
                            for (String style : styles) {
                                if (style.contains(styleAttribute)) {
                                    style = styleAttribute + ":" + styleValue;
                                }
                                newStyle.append(style).append(";");
                            }
                            existingStyle = newStyle.substring(0, newStyle.length() - 1) + "\"";
                        }
                    }

                    String finalExistingStyle = existingStyle;

                    doc.setCharacterAttributes(element.getStartOffset(), element.getEndOffset() - element.getStartOffset(),
                            new SimpleAttributeSet() {{
                                addAttribute(HTML.Tag.SPAN, finalExistingStyle); // Используем копию
                            }},
                            false
                    );
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void insertImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String base64Image = Base64.getEncoder().encodeToString(Files.readAllBytes(selectedFile.toPath()));
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

    private JScrollPane createScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        return scrollPane;
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
