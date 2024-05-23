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

/**
 * Главный класс приложения для заметок, отвечающий за графический интерфейс и взаимодействие с пользователем.
 */
public class NoteApplication extends JFrame {
    private final INoteManager noteManager; // Менеджер заметок, управляющий пользователями и заметками
    private IUser currentUser; // Текущий пользователь, работающий с приложением
    private JComboBox<String> userComboBox; // Выпадающий список для выбора пользователя
    private JList<INote> notesList; // Список заметок, отображаемый для текущего пользователя
    private JTextPane noteTextArea; // Текстовое поле для просмотра и редактирования заметки
    private JScrollPane listScrollPane; // Панель прокрутки для списка заметок
    private JScrollPane textScrollPane; // Панель прокрутки для текстового поля заметки
    private JToolBar toolbar; // Панель инструментов с кнопками действий
    private JPanel listPanel; // Панель, содержащая список пользователей и заметок

    /**
     * Слушатель событий изменения текста в поле заметки, обновляющий модель данных при редактировании.
     */
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

        /**
         * Обновляет текст выбранной заметки в модели данных.
         */
        private void updateSelectedNote() {
            int selectedIndex = notesList.getSelectedIndex();
            if (selectedIndex >= 0) {
                INote selectedNote = currentUser.getNotes().get(selectedIndex);
                selectedNote.setText(noteTextArea.getText());
            }
        }
    };

    /**
     * Конструктор класса NoteApplication. Инициализирует приложение, создает интерфейс и загружает начальные данные.
     * @param noteManager Менеджер заметок для управления данными приложения.
     */
    public NoteApplication(INoteManager noteManager) {
        super("Note Application"); // Устанавливает заголовок окна приложения
        this.noteManager = noteManager; // Сохраняет переданный менеджер заметок

        createInitialData(); // Создает начальные данные пользователя и заметки
        createApplicationLayout(); // Формирует графический интерфейс приложения
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Завершает приложение при закрытии окна
        pack(); // Устанавливает размер окна под содержимое
        setLocationRelativeTo(null); // Центрирует окно на экране
        setVisible(true); // Делает окно видимым
    }

/**
 * Создает начальные данные для приложения: пользователя по умолчанию и первую заметку.
 */
private void createInitialData() {
    currentUser = noteManager.createUser("Default User"); // Создает пользователя "Default User"
    currentUser.createNote("Initial note"); // Создает заметку "Initial note" для пользователя
}

    /**
     * Создает и размещает элементы графического интерфейса приложения.
     */
    private void createApplicationLayout() {
        createUserComboBox(); // Создает выпадающий список пользователей
        createNotesList(); // Создает список заметок
        createNoteTextArea(); // Создает текстовое поле для заметок
        createToolbar(); // Создает панель инструментов

        JPanel mainPanel = new JPanel(new BorderLayout()); // Создает главную панель с компоновщиком BorderLayout
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Устанавливает отступы для главной панели

        listPanel = new JPanel(new BorderLayout()); // Создает панель для списка пользователей и заметок
        listPanel.add(userComboBox, BorderLayout.NORTH); // Добавляет выпадающий список пользователей сверху
        listPanel.add(listScrollPane, BorderLayout.CENTER); // Добавляет список заметок по центру

        mainPanel.add(listPanel, BorderLayout.WEST); // Добавляет панель списка пользователей и заметок слева
        mainPanel.add(textScrollPane, BorderLayout.CENTER); // Добавляет панель текстового поля заметки по центру

        add(toolbar, BorderLayout.NORTH); // Добавляет панель инструментов сверху
        add(mainPanel, BorderLayout.CENTER); // Добавляет главную панель по центру окна

        setPreferredSize(new Dimension(1280, 960)); // Устанавливает предпочтительный размер окна
    }

    /**
     * Создает выпадающий список с именами пользователей, получая данные из менеджера заметок.
     */
    private void createUserComboBox() {
        List<IUser> users = noteManager.getUsers(); // Получает список пользователей из менеджера заметок
        String[] userNames = users.stream()
                .map(IUser::getName) // Преобразует список пользователей в массив имен
                .toArray(String[]::new);
        userComboBox = new JComboBox<>(userNames); // Создает выпадающий список с именами пользователей
        userComboBox.addActionListener(e -> { // Добавляет слушатель событий для обработки выбора пользователя
            String selectedUserName = (String) userComboBox.getSelectedItem(); // Получает имя выбранного пользователя
            currentUser = users.stream()
                    .filter(user -> user.getName().equals(selectedUserName)) // Находит пользователя по имени
                    .findFirst()
                    .orElse(currentUser); // Устанавливает текущего пользователя или оставляет прежнего, если не найден
            updateNotesList(); // Обновляет список заметок для выбранного пользователя
        });
    }

    /**
     * Создает список заметок для текущего пользователя, получая данные из модели.
     */
    private void createNotesList() {
        notesList = new JList<>(); // Создает список заметок
        notesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Разрешает выбор только одной заметки
        notesList.addListSelectionListener(e -> { // Добавляет слушатель событий для обработки выбора заметки
            int selectedIndex = notesList.getSelectedIndex(); // Получает индекс выбранной заметки
            if (selectedIndex >= 0) {
                INote selectedNote = currentUser.getNotes().get(selectedIndex); // Получает выбранную заметку
                noteTextArea.setText(selectedNote.getText()); // Устанавливает текст заметки в текстовое поле
            }
        });
        listScrollPane = createScrollPane(notesList); // Создает панель прокрутки для списка заметок
    }

    /**
     * Создает текстовое поле для просмотра и редактирования заметок, поддерживающее HTML-форматирование.
     */
    private void createNoteTextArea() {
        noteTextArea = new JTextPane(); // Создает текстовое поле
        noteTextArea.setEditorKit(new HTMLEditorKit()); // Устанавливает редактор, поддерживающий HTML
        noteTextArea.setContentType("text/html"); // Устанавливает тип содержимого как HTML
        noteTextArea.setDocument(new HTMLDocument()); // Устанавливает документ HTML для текстового поля
        noteTextArea.getDocument().addDocumentListener(noteTextAreaListener); // Добавляет слушатель изменений текста

        textScrollPane = createScrollPane(noteTextArea); // Создает панель прокрутки для текстового поля
    }

    /**
     * Создает панель инструментов с кнопками для основных действий с заметками.
     */
    private void createToolbar() {
        toolbar = new JToolBar(); // Создает панель инструментов

        JButton newButton = new JButton("New"); // Создает кнопку "New"
        newButton.addActionListener(e -> createNewNote()); // Добавляет действие по нажатию - создание новой заметки
        toolbar.add(newButton); // Добавляет кнопку на панель инструментов

        JButton editButton = new JButton("Edit"); // Создает кнопку "Edit"
        editButton.addActionListener(e -> editSelectedNote()); // Добавляет действие по нажатию - редактирование заметки
        toolbar.add(editButton); // Добавляет кнопку на панель инструментов

        JButton deleteButton = new JButton("Delete"); // Создает кнопку "Delete"
        deleteButton.addActionListener(e -> deleteSelectedNote()); // Добавляет действие по нажатию - удаление заметки
        toolbar.add(deleteButton); // Добавляет кнопку на панель инструментов

        JButton saveButton = new JButton("Save"); // Создает кнопку "Save"
        saveButton.addActionListener(e -> saveNotes()); // Добавляет действие по нажатию - сохранение заметок
        toolbar.add(saveButton); // Добавляет кнопку на панель инструментов

        JButton loadButton = new JButton("Load"); // Создает кнопку "Load"
        loadButton.addActionListener(e -> loadNotes()); // Добавляет действие по нажатию - загрузку заметок
        toolbar.add(loadButton); // Добавляет кнопку на панель инструментов

        JButton boldButton = new JButton("B"); // Создает кнопку "B" (жирный шрифт)
        boldButton.addActionListener(e -> wrapSelectedText("<span style=\"font-weight: bold;\">", "</span>")); // Добавляет действие по нажатию - выделение жирным
        toolbar.add(boldButton); // Добавляет кнопку на панель инструментов

        JButton italicButton = new JButton("I"); // Создает кнопку "I" (курсив)
        italicButton.addActionListener(e -> wrapSelectedText("<span style=\"font-style: italic;\">", "</span>")); // Добавляет действие по нажатию - выделение курсивом
        toolbar.add(italicButton); // Добавляет кнопку на панель инструментов

        JComboBox<String> fontComboBox = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()); // Создает выпадающий список шрифтов
        JComboBox<Integer> fontSizeComboBox = new JComboBox<>(new Integer[]{8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24}); // Создает выпадающий список размеров шрифта

        fontComboBox.addActionListener(e -> { // Добавляет слушатель событий для выбора шрифта
            String selectedFont = (String) fontComboBox.getSelectedItem(); // Получает выбранный шрифт
            Integer selectedSize = (Integer) fontSizeComboBox.getSelectedItem(); // Получает выбранный размер шрифта
            if (selectedFont != null && selectedSize != null) {
                changeFontStyle("font-family", selectedFont); // Изменяет шрифт текста
                changeFontStyle("font-size", selectedSize + "pt"); // Изменяет размер шрифта текста
            }
        });
        toolbar.add(fontComboBox); // Добавляет выпадающий список шрифтов на панель инструментов

        JButton imageButton = new JButton("Image"); // Создает кнопку "Image"
        imageButton.addActionListener(e -> insertImage()); // Добавляет действие по нажатию - вставка изображения
        toolbar.add(imageButton); // Добавляет кнопку на панель инструментов

        fontSizeComboBox.addActionListener(e -> { // Добавляет слушатель событий для выбора размера шрифта
            Integer selectedSize = (Integer) fontSizeComboBox.getSelectedItem(); // Получает выбранный размер шрифта
            if (selectedSize != null) {
                changeFontStyle("font-size", selectedSize + "pt"); // Изменяет размер шрифта текста
            }
        });
        toolbar.add(fontSizeComboBox); // Добавляет выпадающий список размеров шрифта на панель инструментов
    }

    /**
     * Оборачивает выделенный текст в HTML-теги, добавляя или удаляя стили жирного шрифта или курсива.
     * @param startTag Начальный HTML-тег для оборачивания.
     * @param endTag Конечный HTML-тег для оборачивания.
     */
    private void wrapSelectedText(String startTag, String endTag) {
        try {
            int start = noteTextArea.getSelectionStart(); // Получает начальную позицию выделения
            int end = noteTextArea.getSelectionEnd(); // Получает конечную позицию выделения
            if (start != end) { // Проверяет, есть ли выделенный текст
                HTMLDocument doc = (HTMLDocument) noteTextArea.getDocument(); // Получает HTML-документ текстового поля
                String selectedText = doc.getText(start, end - start); // Получает выделенный текст

                boolean hasStartTag = selectedText.contains(startTag); // Проверяет, содержится ли начальный тег в выделении
                boolean hasEndTag = selectedText.contains(endTag); // Проверяет, содержится ли конечный тег в выделении

                if (hasStartTag && hasEndTag) { // Если оба тега есть, то удаляет их
                    selectedText = selectedText.replaceAll(startTag, "");
                    selectedText = selectedText.replaceAll(endTag, "");
                    doc.remove(start, end - start);
                    doc.insertString(start, selectedText, null);
                } else if (!hasStartTag && !hasEndTag) { // Если тегов нет, то добавляет оба
                    doc.insertString(end, endTag, null);
                    doc.insertString(start, startTag, null);
                } else { // Если один тег есть, то удаляет/добавляет нужный
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

    /**
     * Изменяет стиль шрифта выделенного текста, добавляя или обновляя HTML-атрибуты стиля.
     * @param styleAttribute Название атрибута стиля (например, "font-family" или "font-size").
     * @param styleValue Значение атрибута стиля (например, "Arial" или "12pt").
     */
    private void changeFontStyle(String styleAttribute, String styleValue) {
        try {
            int start = noteTextArea.getSelectionStart(); // Получает начальную позицию выделения
            int end = noteTextArea.getSelectionEnd(); // Получает конечную позицию выделения
            if (start != end) { // Проверяет, есть ли выделенный текст
                HTMLDocument doc = (HTMLDocument) noteTextArea.getDocument(); // Получает HTML-документ текстового поля
                Element root = doc.getDefaultRootElement(); // Получает корневой элемент документа
                Element startElement = root.getElement(root.getElementIndex(start)); // Получает элемент, содержащий начало выделения
                Element endElement = root.getElement(root.getElementIndex(end - 1)); // Получает элемент, содержащий конец выделения

                for (int i = startElement.getElementIndex(start); i <= endElement.getElementIndex(end - 1); i++) {
                    Element element = startElement.getElement(i); // Получает текущий элемент текста
                    String existingStyle = element.getAttributes().getAttribute(HTML.Tag.SPAN) != null ?
                            element.getAttributes().getAttribute(HTML.Tag.SPAN).toString() : ""; // Получает текущий стиль элемента

                    if (existingStyle.isEmpty()) { // Если стиль пустой, создает новый стиль
                        existingStyle = "style=\"" + styleAttribute + ":" + styleValue + ";\"";
                    } else { // Иначе обновляет существующий стиль
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

                    String finalExistingStyle = existingStyle; // Создает финальную копию стиля

                    doc.setCharacterAttributes(element.getStartOffset(), element.getEndOffset() - element.getStartOffset(),
                            new SimpleAttributeSet() {{
                                addAttribute(HTML.Tag.SPAN, finalExistingStyle); // Применяет стиль к элементу
                            }},
                            false
                    );
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Открывает диалоговое окно для выбора изображения и вставляет его в текстовое поле заметки.
     */
    private void insertImage() {
        JFileChooser fileChooser = new JFileChooser(); // Создает диалоговое окно выбора файла
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // Разрешает выбирать только файлы
        int result = fileChooser.showOpenDialog(this); // Показывает диалоговое окно
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String base64Image = Base64.getEncoder().encodeToString(Files.readAllBytes(selectedFile.toPath()));
                String mimeType = Files.probeContentType(selectedFile.toPath()); // Получаем MIME тип файла
                String imageTag = "<img src='data:" + mimeType + ";base64," + base64Image + "'/>";
                noteTextArea.getDocument().insertString(noteTextArea.getCaretPosition(), imageTag, null);
            } catch (IOException | BadLocationException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Создает новую заметку с текстом, введенным пользователем, и добавляет ее в список заметок.
     */
    private void createNewNote() {
        String newNoteText = JOptionPane.showInputDialog(this, "Enter new note text:");
        if (newNoteText != null && !newNoteText.isEmpty()) {
            String htmlText = Jsoup.parse(newNoteText).html();
            INote newNote = currentUser.createNote(htmlText);
            updateNotesList();
            notesList.setSelectedValue(newNote, true);
        }
    }


    /**
     * Создает панель прокрутки для переданного компонента.
     * @param component Компонент, для которого создается панель прокрутки.
     * @return Панель прокрутки с заданным компонентом.
     */
    private JScrollPane createScrollPane(Component component) {
        JScrollPane scrollPane = new JScrollPane(component); // Создает панель прокрутки
        scrollPane.setPreferredSize(new Dimension(300, 400)); // Устанавливает предпочтительный размер панели
        return scrollPane; // Возвращает созданную панель прокрутки
    }

    /**
     * Редактирует текст выбранной заметки, запрашивая новый текст у пользователя.
     */
    private void editSelectedNote() {
        int selectedIndex = notesList.getSelectedIndex();
        if (selectedIndex >= 0) {
            INote selectedNote = currentUser.getNotes().get(selectedIndex);
            String oldText = selectedNote.getText();
            String newText = JOptionPane.showInputDialog(this, "Enter new note text:", oldText);
            if (newText != null && !newText.isEmpty()) {
                // Преобразуем текст в HTML, сохраняя существующие теги
                String htmlText = Jsoup.parse(newText).html();
                selectedNote.setText(htmlText);
                updateNotesList();
                notesList.setSelectedValue(selectedNote, true);
            }
        }
    }


    /**
     * Удаляет выбранную заметку из списка заметок текущего пользователя.
     */
    private void deleteSelectedNote() {
        int selectedIndex = notesList.getSelectedIndex(); // Получает индекс выбранной заметки
        if (selectedIndex >= 0) { // Проверяет, выбрана ли заметка
            currentUser.deleteNote(selectedIndex); // Удаляет заметку из модели данных
            updateNotesList(); // Обновляет список заметок
        }
    }

/**
 * Сохраняет заметки текущего пользователя в файл, выбранный пользователем в диалоговом окне.
 */
private void saveNotes() {
    JFileChooser fileChooser = new JFileChooser(); // Создает диалоговое окно выбора файла
    int result = fileChooser.showSaveDialog(this); // Показывает диалоговое окно для сохранения файла
    if (result == JFileChooser.APPROVE_OPTION) { // Если пользователь выбрал файл
        File selectedFile = fileChooser.getSelectedFile(); // Получает выбранный файл
        String filename = selectedFile.getAbsolutePath(); // Получает абсолютный путь к файлу
        try {
            currentUser.saveNotesToFile(filename); // Сохраняет заметки в файл
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving notes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Выводит сообщение об ошибке сохранения
        }
    }
}

    /**
     * Загружает заметки для текущего пользователя из файла, выбранного пользователем в диалоговом окне.
     */
    private void loadNotes() {
        JFileChooser fileChooser = new JFileChooser(); // Создает диалоговое окно выбора файла
        int result = fileChooser.showOpenDialog(this); // Показывает диалоговое окно для открытия файла
        if (result == JFileChooser.APPROVE_OPTION) { // Если пользователь выбрал файл
            File selectedFile = fileChooser.getSelectedFile(); // Получает выбранный файл
            String filename = selectedFile.getAbsolutePath(); // Получает абсолютный путь к файлу
            try {
                currentUser.loadNotesFromFile(filename); // Загружает заметки из файла
                updateNotesList(); // Обновляет список заметок
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Error loading notes: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Выводит сообщение об ошибке загрузки
            }
        }
    }

    /**
     * Обновляет список заметок, отображаемый в интерфейсе, получая данные от текущего пользователя.
     */
    private void updateNotesList() {
        List<INote> notes = currentUser.getNotes().stream()
                .sorted(Comparator.comparing(INote::getCreationDate).reversed())
                .collect(Collectors.toList());
        notesList.setListData(notes.toArray(new INote[0]));

        int selectedIndex = notesList.getSelectedIndex();
        if (selectedIndex >= 0) {
            INote selectedNote = currentUser.getNotes().get(selectedIndex);
            try {

                HTMLDocument doc = (HTMLDocument) noteTextArea.getDocument();
                doc.setInnerHTML(doc.getDefaultRootElement(), selectedNote.getText());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Точка входа в приложение. Создает менеджер заметок и запускает графический интерфейс.
     * @param args Аргументы командной строки (не используются).
     */
    public static void main(String[] args) {
        INoteManager noteManager = new NoteManager(); // Создает менеджер заметок
        SwingUtilities.invokeLater(() -> new NoteApplication(noteManager)); // Запускает графический интерфейс в потоке обработки событий Swing
    }
}
