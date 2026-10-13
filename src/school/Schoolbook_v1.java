package school;

import java.util.*;
import java.sql.SQLException;

public class Schoolbook_v1 implements AutoCloseable {
    private DatabaseManager dbManager;
    private List<Student> students = new ArrayList<>();
    private List<Subject> subjects = new ArrayList<>();
    private Map<Student, Map<Subject, List<Integer>>> marks = new HashMap<>();

    // ============ КОНСТРУКТОРЫ ============
    public Schoolbook_v1() {
        this(true);
    }

    public Schoolbook_v1(boolean autoLoad) {
        try {
            dbManager = new DatabaseManager();
            if (autoLoad) {
                loadFromDatabase();
                System.out.println("✅ Журнал загружен из базы данных");
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка подключения к базе данных: " + e.getMessage());
            System.out.println("ℹ️ Создан новый журнал в памяти");
        }
    }

    // ============ МЕТОДЫ ДЛЯ РАБОТЫ С ДАННЫМИ ============
    public void add_student(String name) {
        try {
            if (findStudentByName(name) != null) {
                System.out.println("❌ Этот ученик уже существует!");
                return;
            }
            dbManager.addStudent(name);
            loadStudentsFromDatabase();
            System.out.println("✅ Ученик '" + name + "' добавлен");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка при добавлении ученика: " + e.getMessage());
        }
    }

    public void add_subject(String name, int max_m, int min_m) {
        try {
            if (findSubjectByName(name) != null) {
                System.out.println("❌ Этот предмет уже существует!");
                return;
            }
            dbManager.addSubject(name, max_m, min_m);
            loadSubjectsFromDatabase();
            System.out.println("✅ Предмет '" + name + "' добавлен");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка при добавлении предмета: " + e.getMessage());
        }
    }

    public void set_mark(int mark) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Выберите ученика:");
        for (int i = 0; i < students.size(); i++) {
            System.out.println(i + " - " + students.get(i));
        }

        int st_id = sc.nextInt();
        if (st_id < 0 || st_id >= students.size()) {
            System.out.println("❌ Неверный номер ученика");
            return;
        }

        System.out.println("Выберите предмет:");
        for (int i = 0; i < subjects.size(); i++) {
            System.out.println(i + " - " + subjects.get(i).toString_detailed());
        }

        int sb_id = sc.nextInt();
        if (sb_id < 0 || sb_id >= subjects.size()) {
            System.out.println("❌ Неверный номер предмета");
            return;
        }

        Student student = students.get(st_id);
        Subject subject = subjects.get(sb_id);

        // Проверяем корректность оценки
        int adjustedMark = Math.min(Math.max(subject.getMinMark(), mark), subject.getMaxMark());
        if (adjustedMark != mark) {
            System.out.println("⚠️ Оценка скорректирована к диапазону предмета: " + adjustedMark);
        }

        try {
            dbManager.addMark(student.getId(), subject.getId(), adjustedMark);
            loadMarksFromDatabase();
            System.out.println("✅ Оценка " + adjustedMark + " поставлена ученику " +
                    student.getName() + " по предмету " + subject.getName());
        } catch (SQLException e) {
            System.out.println("❌ Ошибка при добавлении оценки: " + e.getMessage());
        }
    }

    public void print_stats() {
        System.out.println("\n=== СТАТИСТИКА ЖУРНАЛА ===");
        System.out.println("Учеников: " + students.size());
        System.out.println("Предметов: " + subjects.size());

        for (Student student : students) {
            System.out.println("\n" + student.getName() + ":");
            Map<Subject, List<Integer>> studentMarks = marks.get(student);

            if (studentMarks != null && !studentMarks.isEmpty()) {
                for (Map.Entry<Subject, List<Integer>> entry : studentMarks.entrySet()) {
                    List<Integer> grades = entry.getValue();

                    // Вычисляем среднее через цикл
                    double sum = 0;
                    for (Integer grade : grades) {
                        sum += grade;
                    }
                    double average = grades.isEmpty() ? 0 : sum / grades.size();

                    System.out.println("  " + entry.getKey().getName() + ": " +
                            grades + " (среднее: " + String.format("%.2f", average) + ")");
                }
            } else {
                System.out.println("  Нет оценок");
            }
        }
    }

    // ============ МЕТОДЫ ЗАГРУЗКИ ИЗ БАЗЫ ДАННЫХ ============
    private void loadFromDatabase() {
        loadStudentsFromDatabase();
        loadSubjectsFromDatabase();
        loadMarksFromDatabase();
    }

    private void loadStudentsFromDatabase() {
        try {
            students = dbManager.getAllStudents();
        } catch (SQLException e) {
            System.out.println("❌ Ошибка загрузки студентов: " + e.getMessage());
        }
    }

    private void loadSubjectsFromDatabase() {
        try {
            subjects = dbManager.getAllSubjects();
        } catch (SQLException e) {
            System.out.println("❌ Ошибка загрузки предметов: " + e.getMessage());
        }
    }

    private void loadMarksFromDatabase() {
        try {
            Map<Integer, Map<Integer, List<Integer>>> dbMarks = dbManager.getAllMarks();
            marks.clear();

            // Преобразуем ID в объекты
            for (Map.Entry<Integer, Map<Integer, List<Integer>>> studentEntry : dbMarks.entrySet()) {
                Student student = findStudentById(studentEntry.getKey());
                if (student == null) continue;

                Map<Subject, List<Integer>> studentMarks = new HashMap<>();
                for (Map.Entry<Integer, List<Integer>> subjectEntry : studentEntry.getValue().entrySet()) {
                    Subject subject = findSubjectById(subjectEntry.getKey());
                    if (subject != null) {
                        studentMarks.put(subject, subjectEntry.getValue());
                    }
                }
                marks.put(student, studentMarks);
            }
        } catch (SQLException e) {
            System.out.println("❌ Ошибка загрузки оценок: " + e.getMessage());
        }
    }

    // ============ AutoCloseable ИНТЕРФЕЙС ============
    @Override
    public void close() {
        System.out.println("🔄 Закрытие журнала...");
        try {
            if (dbManager != null) {
                dbManager.close();
            }
        } catch (SQLException e) {
            System.out.println("⚠️ Ошибка при закрытии базы данных: " + e.getMessage());
        }
    }

    // ============ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ============
    private Student findStudentByName(String name) {
        try {
            return dbManager.findStudentByName(name);
        } catch (SQLException e) {
            return null;
        }
    }

    private Subject findSubjectByName(String name) {
        try {
            return dbManager.findSubjectByName(name);
        } catch (SQLException e) {
            return null;
        }
    }

    public Student findStudentById(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }
        return null;
    }

    public Subject findSubjectById(int id) {
        for (Subject subject : subjects) {
            if (subject.getId() == id) {
                return subject;
            }
        }
        return null;
    }

    public List<Student> getStudentsWithAverageAbove(double minAverage) {
        List<Student> result = new ArrayList<>();

        for (Student student : students) {
            Map<Subject, List<Integer>> studentMarks = marks.get(student);
            if (studentMarks == null || studentMarks.isEmpty()) {
                continue;
            }

            double totalSum = 0;
            int totalCount = 0;

            for (List<Integer> grades : studentMarks.values()) {
                for (Integer grade : grades) {
                    totalSum += grade;
                    totalCount++;
                }
            }

            double average = totalCount > 0 ? totalSum / totalCount : 0;
            if (average >= minAverage) {
                result.add(student);
            }
        }

        return result;
    }

    public void printTopStudents(int count) {
        // Создаем список пар "студент-средний балл"
        List<Map.Entry<Student, Double>> studentAverages = new ArrayList<>();

        for (Student student : students) {
            Map<Subject, List<Integer>> studentMarks = marks.get(student);
            if (studentMarks == null || studentMarks.isEmpty()) {
                continue;
            }

            double totalSum = 0;
            int totalCount = 0;

            for (List<Integer> grades : studentMarks.values()) {
                for (Integer grade : grades) {
                    totalSum += grade;
                    totalCount++;
                }
            }

            if (totalCount > 0) {
                double average = totalSum / totalCount;
                studentAverages.add(new AbstractMap.SimpleEntry<>(student, average));
            }
        }

        // Сортируем по убыванию среднего балла
        Collections.sort(studentAverages, new Comparator<Map.Entry<Student, Double>>() {
            @Override
            public int compare(Map.Entry<Student, Double> o1, Map.Entry<Student, Double> o2) {
                return Double.compare(o2.getValue(), o1.getValue()); // обратный порядок
            }
        });

        // Выводим топ
        System.out.println("\n=== ТОП-" + Math.min(count, studentAverages.size()) + " УЧЕНИКОВ ===");
        for (int i = 0; i < Math.min(count, studentAverages.size()); i++) {
            Map.Entry<Student, Double> entry = studentAverages.get(i);
            System.out.println((i + 1) + ". " + entry.getKey().getName() +
                    " - средний балл: " + String.format("%.2f", entry.getValue()));
        }
    }

    // ============ ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ДЛЯ УПРАВЛЕНИЯ БАЗОЙ ============
    public void clearDatabase() {
        try {
            dbManager.clearAllData();
            students.clear();
            subjects.clear();
            marks.clear();
            System.out.println("✅ Все данные удалены из базы данных и памяти");
        } catch (SQLException e) {
            System.out.println("❌ Ошибка при очистке базы данных: " + e.getMessage());
        }
    }

    public void refreshData() {
        loadFromDatabase();
        System.out.println("✅ Данные обновлены из базы данных");
    }

    // ============ ГЕТТЕРЫ ============
    public List<Student> getStudents() {
        return new ArrayList<>(students);
    }

    public List<Subject> getSubjects() {
        return new ArrayList<>(subjects);
    }

    // ============ ТЕСТОВЫЙ МЕТОД ============
    public static void main(String[] args) {
        try (Schoolbook_v1 journal = new Schoolbook_v1()) {
            // Добавляем тестовые данные
            journal.add_student("Иван Петров");
            journal.add_student("Мария Сидорова");
            journal.add_student("Алексей Иванов");

            journal.add_subject("Математика", 5, 2);
            journal.add_subject("Физика", 10, 1);
            journal.add_subject("История", 5, 1);

            // Добавляем оценки
            System.out.println("\n--- Добавление оценок ---");
            System.out.println("Добавьте оценку 5 для первого ученика по первому предмету");
            journal.set_mark(5);

            System.out.println("\nДобавьте оценку 4 для второго ученика по второму предмету");
            journal.set_mark(4);

            System.out.println("\nДобавьте оценку 10 для третьего ученика по третьему предмету");
            journal.set_mark(10);

            // Печатаем статистику
            journal.print_stats();

            // Демонстрация новых методов
            journal.printTopStudents(3);

            List<Student> goodStudents = journal.getStudentsWithAverageAbove(4.0);
            System.out.println("\nУченики со средним баллом выше 4.0: " +
                    goodStudents.size() + " человек");

            // Демонстрация обновления данных
            System.out.println("\n--- Тест обновления данных ---");
            journal.refreshData();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\nПрограмма завершена. База данных сохранена.");
    }
}