package com.example.volunteerapp.model;

import java.io.Serializable;

public class Participation implements Serializable {

    int participation_id;
    int event_Id;
    int user_Id;
    Event event;
    User user;
    Organizer organizer;

    public int getParticipation_id() {
        return participation_id;
    }

    public void setParticipation_id(int participation_id) {
        this.participation_id = participation_id;
    }

    public int getEvent_Id() {
        return event_Id;
    }

    public void setEvent_Id(int event_Id) {
        this.event_Id = event_Id;
    }

    public int getUser_Id() {
        return user_Id;
    }

    public void setUser_Id(int user_Id) {
        this.user_Id = user_Id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public User getUser() {return user;}

    public void setUser(User user) {this.user = user;}

    public Organizer getOrganizer() {return organizer;}

    public void setOrganizer(Organizer organizer) {this.organizer = organizer;}

}
