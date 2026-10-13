package school;

import java.io.Serializable;

public class Student implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int lastId = 0;
    private int id;
    private String name;

    public Student(String name) {
        this.id = ++lastId;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean check_student(String name) {
        return this.name.equals(name);
    }

    @Override
    public String toString() {
        return "Student[id=" + id + ", name='" + name + "']";
    }
}