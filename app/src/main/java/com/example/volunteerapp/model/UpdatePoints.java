package com.example.volunteerapp.model;

public class UpdatePoints {

    private int points;
    private int id;


    public int getPoints() {
        return points;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public UpdatePoints(int id, int points) {
        this.id=id;
        this.points = points;

    }
}
