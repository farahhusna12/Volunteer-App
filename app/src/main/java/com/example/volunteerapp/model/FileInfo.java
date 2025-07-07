package com.example.volunteerapp.model;

public class FileInfo {
    private int id;
    private String file;

    // Constructor
    public FileInfo(int id, String file) {
        this.id = id;
        this.file = file;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    // Optional: toString method for easy printing
    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", file='" + file + '\'' +
                '}';
    }
}
