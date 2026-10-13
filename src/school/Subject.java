package school;

import java.io.Serializable;

public class Subject implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int lastId = 0;
    private int id;
    private String name;
    private int maxMark;
    private int minMark;

    public Subject(String name, int maxMark, int minMark) {
        this.id = ++lastId;
        this.name = name;
        this.maxMark = maxMark;
        this.minMark = minMark;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getMaxMark() { return maxMark; }
    public int getMinMark() { return minMark; }

    public boolean check_subject(String name) {
        return this.name.equals(name);
    }

    public String toString_detailed() {
        return name + " (marks: " + minMark + "-" + maxMark + ")";
    }

    @Override
    public String toString() {
        return "Subject[id=" + id + ", name='" + name + "']";
    }
}