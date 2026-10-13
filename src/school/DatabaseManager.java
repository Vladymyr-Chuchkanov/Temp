package school;

import java.sql.*;
import java.util.*;

public class DatabaseManager implements AutoCloseable {
    private Connection connection;

    public DatabaseManager() throws SQLException {

        connect();
        createTables();
    }

    private void connect() throws SQLException {
        String url = "jdbc:sqlite:schoolbook.db";
        connection = DriverManager.getConnection(url);
        System.out.println("✅ Подключение к базе данных установлено");
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Таблица студентов
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE)");

            // Таблица предметов
            stmt.execute("CREATE TABLE IF NOT EXISTS subjects (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "max_mark INTEGER NOT NULL, " +
                    "min_mark INTEGER NOT NULL)");

            // Таблица оценок
            stmt.execute("CREATE TABLE IF NOT EXISTS marks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "student_id INTEGER NOT NULL, " +
                    "subject_id INTEGER NOT NULL, " +
                    "mark INTEGER NOT NULL, " +
                    "FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE)");

            System.out.println("✅ Таблицы базы данных проверены/созданы");
        }
    }

    // ============ МЕТОДЫ ДЛЯ РАБОТЫ СО СТУДЕНТАМИ ============
    public void addStudent(String name) throws SQLException {
        String sql = "INSERT INTO students (name) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT id, name FROM students ORDER BY id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student student = new Student(rs.getString("name"));
                // Устанавливаем ID через рефлексию, так как поле final
                try {
                    java.lang.reflect.Field idField = Student.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(student, rs.getInt("id"));

                    // Также обновляем lastId
                    java.lang.reflect.Field lastIdField = Student.class.getDeclaredField("lastId");
                    lastIdField.setAccessible(true);
                    int currentLastId = Math.max(lastIdField.getInt(null), rs.getInt("id"));
                    lastIdField.set(null, currentLastId);
                } catch (Exception e) {
                    System.out.println("Ошибка при установке ID: " + e.getMessage());
                }
                students.add(student);
            }
        }
        return students;
    }

    public Student findStudentByName(String name) throws SQLException {
        String sql = "SELECT id, name FROM students WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Student student = new Student(rs.getString("name"));
                try {
                    java.lang.reflect.Field idField = Student.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(student, rs.getInt("id"));
                } catch (Exception e) {
                    System.out.println("Ошибка при установке ID: " + e.getMessage());
                }
                return student;
            }
        }
        return null;
    }

    // ============ МЕТОДЫ ДЛЯ РАБОТЫ С ПРЕДМЕТАМИ ============
    public void addSubject(String name, int maxMark, int minMark) throws SQLException {
        String sql = "INSERT INTO subjects (name, max_mark, min_mark) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, maxMark);
            pstmt.setInt(3, minMark);
            pstmt.executeUpdate();
        }
    }

    public List<Subject> getAllSubjects() throws SQLException {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT id, name, max_mark, min_mark FROM subjects ORDER BY id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Subject subject = new Subject(
                        rs.getString("name"),
                        rs.getInt("max_mark"),
                        rs.getInt("min_mark")
                );

                try {
                    java.lang.reflect.Field idField = Subject.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(subject, rs.getInt("id"));

                    // Также обновляем lastId
                    java.lang.reflect.Field lastIdField = Subject.class.getDeclaredField("lastId");
                    lastIdField.setAccessible(true);
                    int currentLastId = Math.max(lastIdField.getInt(null), rs.getInt("id"));
                    lastIdField.set(null, currentLastId);
                } catch (Exception e) {
                    System.out.println("Ошибка при установке ID: " + e.getMessage());
                }
                subjects.add(subject);
            }
        }
        return subjects;
    }

    public Subject findSubjectByName(String name) throws SQLException {
        String sql = "SELECT id, name, max_mark, min_mark FROM subjects WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Subject subject = new Subject(
                        rs.getString("name"),
                        rs.getInt("max_mark"),
                        rs.getInt("min_mark")
                );

                try {
                    java.lang.reflect.Field idField = Subject.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(subject, rs.getInt("id"));
                } catch (Exception e) {
                    System.out.println("Ошибка при установке ID: " + e.getMessage());
                }
                return subject;
            }
        }
        return null;
    }

    // ============ МЕТОДЫ ДЛЯ РАБОТЫ С ОЦЕНКАМИ ============
    public void addMark(int studentId, int subjectId, int mark) throws SQLException {
        String sql = "INSERT INTO marks (student_id, subject_id, mark) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, subjectId);
            pstmt.setInt(3, mark);
            pstmt.executeUpdate();
        }
    }

    public Map<Integer, Map<Integer, List<Integer>>> getAllMarks() throws SQLException {
        Map<Integer, Map<Integer, List<Integer>>> marks = new HashMap<>();
        String sql = "SELECT student_id, subject_id, mark FROM marks ORDER BY id";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int studentId = rs.getInt("student_id");
                int subjectId = rs.getInt("subject_id");
                int mark = rs.getInt("mark");

                // Добавляем студента в карту если его нет
                if (!marks.containsKey(studentId)) {
                    marks.put(studentId, new HashMap<>());
                }

                // Добавляем предмет в карту студента если его нет
                Map<Integer, List<Integer>> studentMarks = marks.get(studentId);
                if (!studentMarks.containsKey(subjectId)) {
                    studentMarks.put(subjectId, new ArrayList<>());
                }

                // Добавляем оценку
                studentMarks.get(subjectId).add(mark);
            }
        }
        return marks;
    }

    public List<Integer> getStudentMarksForSubject(int studentId, int subjectId) throws SQLException {
        List<Integer> marks = new ArrayList<>();
        String sql = "SELECT mark FROM marks WHERE student_id = ? AND subject_id = ? ORDER BY id";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, subjectId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                marks.add(rs.getInt("mark"));
            }
        }
        return marks;
    }

    // ============ ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ============
    public void clearAllData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM marks");
            stmt.execute("DELETE FROM students");
            stmt.execute("DELETE FROM subjects");
            stmt.execute("DELETE FROM sqlite_sequence");
            System.out.println("✅ Все данные очищены");
        }
    }

    public int getStudentCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM students";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt("count") : 0;
        }
    }

    public int getSubjectCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM subjects";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt("count") : 0;
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("✅ Соединение с базой данных закрыто");
        }
    }

    public Connection getConnection() {
        return connection;
    }
}