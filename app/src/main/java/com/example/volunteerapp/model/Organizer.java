package com.example.volunteerapp.model;

import java.io.Serializable;

public class Organizer implements Serializable {
    private int organizer_id;
    private int user_id;
    private String organizer_name;
    User user;

    public Organizer(int organizer_id, int user_id, String organizer_name) {
        this.organizer_id = organizer_id;
        this.user_id = user_id;
        this.organizer_name = organizer_name;
    }

    public int getOrganizer_id() {
        return organizer_id;
    }

    public void setOrganizer_id(int organizer_id) {
        this.organizer_id = organizer_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getOrganizer_name() {
        return organizer_name;
    }

    public void setOrganizer_name(String organizer_name) {
        this.organizer_name = organizer_name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
