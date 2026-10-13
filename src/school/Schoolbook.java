package school;

import java.io.*;
import java.util.*;

public class Schoolbook implements Serializable, AutoCloseable {
    private static final long serialVersionUID = 2L;

    private static final int CURRENT_DATA_VERSION = 2;
    private static final String BASE_FILE_NAME = "schoolbook_data";

    private List<Student> students = new ArrayList<>();
    private List<Subject> subjects = new ArrayList<>();
    private Map<Student, Map<Subject, List<Integer>>> marks = new HashMap<>();

    private int dataVersion = CURRENT_DATA_VERSION;

    static {
        loadLastIds();
    }

    // ============ КОНСТРУКТОРЫ ============
    public Schoolbook() {
        this(true);
    }

    public Schoolbook(boolean autoLoad) {
        if (autoLoad) {
            try {
                loadFromFile();
                System.out.println("✅ Журнал загружен из файла (версия: " + dataVersion + ")");
            } catch (Exception e) {
                System.out.println("ℹ️ Не удалось загрузить журнал, создан новый");
            }
        }
    }

    // ============ МЕТОДЫ ДЛЯ РАБОТЫ С ДАННЫМИ ============
    public void add_student(String name) {
        if (findStudentByName(name) != null) {
            System.out.println("❌ Этот ученик уже существует!");
            return;
        }
        students.add(new Student(name));
        System.out.println("✅ Ученик '" + name + "' добавлен");
    }

    public void add_subject(String name, int max_m, int min_m) {
        if (findSubjectByName(name) != null) {
            System.out.println("❌ Этот предмет уже существует!");
            return;
        }
        subjects.add(new Subject(name, max_m, min_m));
        System.out.println("✅ Предмет '" + name + "' добавлен");
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

        // Добавляем оценку через проверки с циклами
        if (!marks.containsKey(student)) {
            marks.put(student, new HashMap<>());
        }

        Map<Subject, List<Integer>> studentMarks = marks.get(student);
        if (!studentMarks.containsKey(subject)) {
            studentMarks.put(subject, new ArrayList<>());
        }

        studentMarks.get(subject).add(adjustedMark);

        System.out.println("✅ Оценка " + adjustedMark + " поставлена ученику " +
                student.getName() + " по предмету " + subject.getName());
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

    // ============ СИСТЕМА СОХРАНЕНИЯ И ВЕРСИОНИРОВАНИЯ ============
    private String getVersionedFileName() {
        return BASE_FILE_NAME + "_v" + CURRENT_DATA_VERSION + ".dat";
    }

    private boolean checkVersionedFileExists(int version) {
        File file = new File(BASE_FILE_NAME + "_v" + version + ".dat");
        return file.exists() && file.length() > 0;
    }

    public void saveToFile() {
        String fileName = getVersionedFileName();

        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(fileName))) {


            oos.writeObject(this);

            System.out.println("✅ Журнал сохранен в файл: " + fileName +
                    " (версия данных: " + dataVersion + ")");

        } catch (IOException e) {
            System.out.println("❌ Ошибка при сохранении: " + e.getMessage());
        }
    }

    public void loadFromFile() throws IOException, ClassNotFoundException {
        if (checkVersionedFileExists(CURRENT_DATA_VERSION)) {
            loadSpecificVersion(CURRENT_DATA_VERSION);
            return;
        }

        // Ищем предыдущие версии
        for (int v = CURRENT_DATA_VERSION - 1; v >= 1; v--) {
            if (checkVersionedFileExists(v)) {
                System.out.println("⚠️ Найдена старая версия данных (v" + v + "). Загружаем...");
                loadSpecificVersion(v);
                migrateFromVersion(v);
                return;
            }
        }

        throw new FileNotFoundException("Файл журнала не найден");
    }

    private void loadSpecificVersion(int version) throws IOException, ClassNotFoundException {
        String fileName = BASE_FILE_NAME + "_v" + version + ".dat";

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(fileName))) {

            Schoolbook loaded = (Schoolbook) ois.readObject();

            this.students = loaded.students;
            this.subjects = loaded.subjects;
            this.marks = loaded.marks;
            this.dataVersion = loaded.dataVersion;

            loadLastIds();

        } catch (InvalidClassException e) {
            System.out.println("⚠️ Версия класса изменилась. Требуется миграция данных.");
            throw e;
        }
    }

    private void migrateFromVersion(int oldVersion) {
        System.out.println("Миграция данных с версии " + oldVersion + " на " + CURRENT_DATA_VERSION);

        if (oldVersion < 2) {
            System.out.println("Добавление ID для существующих записей...");
            // Логика миграции через циклы
            int studentId = 1;
            for (Student student : students) {
                // Если в старой версии не было ID, добавляем
                // (предполагаем, что Student имеет метод setId())
                try {
                    java.lang.reflect.Field idField = Student.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    if (idField.get(student) == null || (int)idField.get(student) == 0) {
                        idField.set(student, studentId++);
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка миграции ID учеников: " + e.getMessage());
                }
            }
        }

        this.dataVersion = CURRENT_DATA_VERSION;
        saveToFile();
    }

    // ============ РАБОТА СО СЧЕТЧИКАМИ ID ============
    private static void saveLastIds() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("schoolbook_ids.dat"))) {

            oos.writeInt(Student.class.getName().hashCode());
            oos.writeInt(Subject.class.getName().hashCode());

        } catch (IOException e) {
            System.out.println("⚠️ Не удалось сохранить счетчики ID");
        }
    }

    private static void loadLastIds() {
        File file = new File("schoolbook_ids.dat");
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {

            int studentHash = ois.readInt();
            int subjectHash = ois.readInt();

        } catch (IOException e) {
            System.out.println("⚠️ Не удалось загрузить счетчики ID");
        }
    }

    // ============ AutoCloseable ИНТЕРФЕЙС ============
    @Override
    public void close() {
        System.out.println("🔄 Автосохранение журнала...");
        try {
            saveToFile();
        } catch (Exception e) {
            System.out.println("⚠️ Не удалось автосохранить: " + e.getMessage());
        }
    }

    // ============ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ (без Stream API) ============
    private Student findStudentByName(String name) {
        for (Student student : students) {
            if (student.check_student(name)) {
                return student;
            }
        }
        return null;
    }

    private Subject findSubjectByName(String name) {
        for (Subject subject : subjects) {
            if (subject.check_subject(name)) {
                return subject;
            }
        }
        return null;
    }

    // Новые методы для работы с данными (хорошие задания для ученика)
    public Student findStudentById(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
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

    // ============ ГЕТТЕРЫ ============
    public List<Student> getStudents() {
        return new ArrayList<>(students);
    }

    public List<Subject> getSubjects() {
        return new ArrayList<>(subjects);
    }

    public int getDataVersion() {
        return dataVersion;
    }

    // ============ ТЕСТОВЫЙ МЕТОД ============
    public static void main(String[] args) {
        try (Schoolbook journal = new Schoolbook()) {
            // Добавляем тестовые данные
            journal.add_student("Иван Петров");
            journal.add_student("Мария Сидорова");
            journal.add_student("Алексей Иванов");

            journal.add_subject("Математика", 5, 2);
            journal.add_subject("Физика", 10, 1);
            journal.add_subject("История", 5, 1);

            journal.set_mark(5);
            journal.set_mark(4);
            journal.set_mark(10);


            // Ручное сохранение
            journal.saveToFile();

            // Печатаем статистику
            journal.print_stats();

            // Демонстрация новых методов
            journal.printTopStudents(3);

            List<Student> goodStudents = journal.getStudentsWithAverageAbove(4.0);
            System.out.println("\nУченики со средним баллом выше 4.0: " +
                    goodStudents.size() + " человек");

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\nПрограмма завершена. Журнал автоматически сохранился.");
    }
}