package school;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Map;

public class SchoolbookGUI extends JFrame {
    private Schoolbook_v1 schoolbook;
    private JTabbedPane tabbedPane;

    // Компоненты для вкладки "Ученики"
    private JTextField studentNameField;
    private DefaultTableModel studentsTableModel;
    private JTable studentsTable;

    // Компоненты для вкладки "Предметы"
    private JTextField subjectNameField;
    private JTextField subjectMaxField;
    private JTextField subjectMinField;
    private DefaultTableModel subjectsTableModel;
    private JTable subjectsTable;

    // Компоненты для вкладки "Оценки"
    private JComboBox<String> studentCombo;
    private JComboBox<String> subjectCombo;
    private JTextField markField;
    private DefaultTableModel marksTableModel;
    private JTable marksTable;

    // Компоненты для вкладки "Статистика"
    private JTextArea statsArea;
    private JTextField topCountField;
    private JTextField minAverageField;

    public SchoolbookGUI() {
        schoolbook = new Schoolbook_v1();
        
        initUI();
    }

    private void initUI() {
        setTitle("Школьный журнал");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // Создаем панель с вкладками
        tabbedPane = new JTabbedPane();

        // Добавляем вкладки
        tabbedPane.addTab("Ученики", createStudentsPanel());
        tabbedPane.addTab("Предметы", createSubjectsPanel());
        tabbedPane.addTab("Оценки", createMarksPanel());
        tabbedPane.addTab("Статистика", createStatsPanel());

        add(tabbedPane);

        // Обработчик закрытия окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeApplication();
            }
        });

        refreshAllData();
    }

    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель для добавления ученика
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPanel.setBorder(BorderFactory.createTitledBorder("Добавить ученика"));

        addPanel.add(new JLabel("Имя:"));
        studentNameField = new JTextField(20);
        addPanel.add(studentNameField);

        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> addStudent());
        addPanel.add(addButton);

        // Таблица учеников
        studentsTableModel = new DefaultTableModel(new String[]{"ID", "Имя"}, 0);

        studentsTable = new JTable(studentsTableModel);
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Список учеников"));

        panel.add(addPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSubjectsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель для добавления предмета
        JPanel addPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        addPanel.setBorder(BorderFactory.createTitledBorder("Добавить предмет"));

        addPanel.add(new JLabel("Название:"));
        subjectNameField = new JTextField();
        addPanel.add(subjectNameField);

        addPanel.add(new JLabel("Макс. оценка:"));
        subjectMaxField = new JTextField("5");
        addPanel.add(subjectMaxField);

        addPanel.add(new JLabel("Мин. оценка:"));
        subjectMinField = new JTextField("1");
        addPanel.add(subjectMinField);

        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> addSubject());
        addPanel.add(addButton);

        // Таблица предметов
        subjectsTableModel = new DefaultTableModel(new String[]{"ID", "Название", "Мин", "Макс"}, 0);
        subjectsTable = new JTable(subjectsTableModel);
        JScrollPane scrollPane = new JScrollPane(subjectsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Список предметов"));

        panel.add(addPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMarksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель для добавления оценки
        JPanel addPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        addPanel.setBorder(BorderFactory.createTitledBorder("Поставить оценку"));

        addPanel.add(new JLabel("Ученик:"));
        studentCombo = new JComboBox<>();
        addPanel.add(studentCombo);

        addPanel.add(new JLabel("Предмет:"));
        subjectCombo = new JComboBox<>();
        addPanel.add(subjectCombo);

        addPanel.add(new JLabel("Оценка:"));
        markField = new JTextField();
        addPanel.add(markField);

        JButton addButton = new JButton("Поставить оценку");
        addButton.addActionListener(e -> addMark());
        addPanel.add(addButton);

        // Таблица оценок
        marksTableModel = new DefaultTableModel(new String[]{"Ученик", "Предмет", "Оценки"}, 0);
        marksTable = new JTable(marksTableModel);
        JScrollPane scrollPane = new JScrollPane(marksTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Все оценки"));

        JButton refreshButton = new JButton("Обновить");
        refreshButton.addActionListener(e -> refreshMarksTable());

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(refreshButton);

        panel.add(addPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Панель для статистики
        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(statsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Статистика"));

        // Панель управления
        JPanel controlPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Дополнительные функции"));

        controlPanel.add(new JLabel("Топ учеников:"));
        topCountField = new JTextField("3");
        controlPanel.add(topCountField);

        JButton topButton = new JButton("Показать топ");
        topButton.addActionListener(e -> showTopStudents());
        controlPanel.add(topButton);

        controlPanel.add(new JLabel("Средний балл выше:"));
        minAverageField = new JTextField("4.0");
        controlPanel.add(minAverageField);

        JButton averageButton = new JButton("Найти учеников");
        averageButton.addActionListener(e -> showStudentsAboveAverage());
        controlPanel.add(averageButton);

        JButton refreshButton = new JButton("Обновить статистику");
        refreshButton.addActionListener(e -> refreshStats());
        controlPanel.add(refreshButton);

        JButton clearButton = new JButton("Очистить БД");
        clearButton.addActionListener(e -> clearDatabase());
        controlPanel.add(clearButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(controlPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ============ МЕТОДЫ ДЛЯ РАБОТЫ С ДАННЫМИ ============

    private void addStudent() {
        String name = studentNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите имя ученика!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        schoolbook.add_student(name);
        studentNameField.setText("");
        refreshStudentsTable();
    }

    private void addSubject() {
        String name = subjectNameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите название предмета!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int maxMark = Integer.parseInt(subjectMaxField.getText());
            int minMark = Integer.parseInt(subjectMinField.getText());

            if (minMark > maxMark) {
                JOptionPane.showMessageDialog(this, "Минимальная оценка не может быть больше максимальной!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            schoolbook.add_subject(name, maxMark, minMark);
            subjectNameField.setText("");
            subjectMaxField.setText("5");
            subjectMinField.setText("1");
            refreshSubjectsTable();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Введите корректные числа для оценок!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addMark() {
        if (studentCombo.getSelectedIndex() == -1 || subjectCombo.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Нет учеников или предметов!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int mark = Integer.parseInt(markField.getText());

            // Получаем выбранного ученика и предмет
            String studentInfo = (String) studentCombo.getSelectedItem();
            String subjectInfo = (String) subjectCombo.getSelectedItem();

            int studentId = Integer.parseInt(studentInfo.split(" - ")[0]);
            int subjectId = Integer.parseInt(subjectInfo.split(" - ")[0]);

            // Находим предмет, чтобы проверить диапазон
            Subject subject = schoolbook.findSubjectById(subjectId);
            if (subject != null) {
                int adjustedMark = Math.min(Math.max(subject.getMinMark(), mark), subject.getMaxMark());
                if (adjustedMark != mark) {
                    JOptionPane.showMessageDialog(this,
                            "Оценка скорректирована к диапазону предмета: " + adjustedMark,
                            "Предупреждение", JOptionPane.WARNING_MESSAGE);
                }

                // Добавляем оценку через прямой вызов метода dbManager
                try {
                    java.lang.reflect.Field dbManagerField = Schoolbook_v1.class.getDeclaredField("dbManager");
                    dbManagerField.setAccessible(true);
                    DatabaseManager dbManager = (DatabaseManager) dbManagerField.get(schoolbook);
                    dbManager.addMark(studentId, subjectId, adjustedMark);
                    schoolbook.refreshData();

                    markField.setText("");
                    refreshMarksTable();
                    JOptionPane.showMessageDialog(this, "Оценка успешно добавлена!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Ошибка при добавлении оценки: " + e.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Введите корректную оценку!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showTopStudents() {
        try {
            int count = Integer.parseInt(topCountField.getText());
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.PrintStream ps = new java.io.PrintStream(baos);
            java.io.PrintStream old = System.out;
            System.setOut(ps);

            schoolbook.printTopStudents(count);
            System.out.flush();
            System.setOut(old);

            String result = baos.toString();
            statsArea.append("\n" + result + "\n");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Введите корректное число!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showStudentsAboveAverage() {
        try {
            double minAverage = Double.parseDouble(minAverageField.getText());
            List<Student> students = schoolbook.getStudentsWithAverageAbove(minAverage);

            statsArea.append("\n=== Ученики со средним баллом выше " + minAverage + " ===\n");
            if (students.isEmpty()) {
                statsArea.append("Нет таких учеников\n");
            } else {
                for (Student s : students) {
                    statsArea.append("• " + s.getName() + "\n");
                }
            }
            statsArea.append("\n");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Введите корректное число!", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Вы уверены, что хотите очистить всю базу данных?\nЭто действие нельзя отменить!",
                "Подтверждение", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            schoolbook.clearDatabase();
            refreshAllData();
            JOptionPane.showMessageDialog(this, "База данных очищена!", "Успех", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ============ МЕТОДЫ ОБНОВЛЕНИЯ ИНТЕРФЕЙСА ============

    private void refreshAllData() {
        refreshStudentsTable();
        refreshSubjectsTable();
        refreshComboBoxes();
        refreshMarksTable();
        refreshStats();
    }

    private void refreshStudentsTable() {
        studentsTableModel.setRowCount(0);
        List<Student> students = schoolbook.getStudents();
        for (Student s : students) {
            studentsTableModel.addRow(new Object[]{s.getId(), s.getName()});
        }
    }

    private void refreshSubjectsTable() {
        subjectsTableModel.setRowCount(0);
        List<Subject> subjects = schoolbook.getSubjects();
        for (Subject s : subjects) {
            subjectsTableModel.addRow(new Object[]{s.getId(), s.getName(), s.getMinMark(), s.getMaxMark()});
        }
    }

    private void refreshComboBoxes() {
        studentCombo.removeAllItems();
        for (Student s : schoolbook.getStudents()) {
            studentCombo.addItem(s.getId() + " - " + s.getName());
        }

        subjectCombo.removeAllItems();
        for (Subject s : schoolbook.getSubjects()) {
            subjectCombo.addItem(s.getId() + " - " + s.getName() +
                    " (" + s.getMinMark() + "-" + s.getMaxMark() + ")");
        }
    }

    private void refreshMarksTable() {
        marksTableModel.setRowCount(0);

        try {
            java.lang.reflect.Field dbManagerField = Schoolbook_v1.class.getDeclaredField("dbManager");
            dbManagerField.setAccessible(true);
            DatabaseManager dbManager = (DatabaseManager) dbManagerField.get(schoolbook);

            java.lang.reflect.Field marksField = Schoolbook_v1.class.getDeclaredField("marks");
            marksField.setAccessible(true);
            Map<Student, Map<Subject, List<Integer>>> marks =
                    (Map<Student, Map<Subject, List<Integer>>>) marksField.get(schoolbook);

            for (Map.Entry<Student, Map<Subject, List<Integer>>> studentEntry : marks.entrySet()) {
                Student student = studentEntry.getKey();
                for (Map.Entry<Subject, List<Integer>> subjectEntry : studentEntry.getValue().entrySet()) {
                    Subject subject = subjectEntry.getKey();
                    List<Integer> marksList = subjectEntry.getValue();

                    marksTableModel.addRow(new Object[]{
                            student.getName(),
                            subject.getName(),
                            marksList.toString()
                    });
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки рефлексии
        }
    }

    private void refreshStats() {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(baos);
        java.io.PrintStream old = System.out;
        System.setOut(ps);

        schoolbook.print_stats();
        System.out.flush();
        System.setOut(old);

        statsArea.setText(baos.toString());
    }

    private void closeApplication() {
        schoolbook.close();
        dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            SchoolbookGUI gui = new SchoolbookGUI();
            gui.setVisible(true);
        });
    }
}