package com.example.eduvia;

public class Student {
    private int id;
    private String name;
    private String avatar;
    private String subjects;
    private String className;
    private String status;
    private int activeStaus;

    public Student(int id, String name,String avatar, String subjects, String className, String status,int activeStaus) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.subjects = subjects;
        this.className = className;
        this.status = status;
        this.activeStaus = activeStaus;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public String getAvatar() { return avatar;}

    public String getSubjects() {
        return subjects;
    }

    public String getClassName() {
        return className;
    }

    public String getStatus() {
        return status;
    }

    public int getActiveStaus(){return activeStaus;}

}
