package com.example.volunteerapp.model;

public class AdminUpdateUser {

    private int id;
    private String firstName;
    private String lastName;

    private String Username;
    private String Email;
    private String Image;

    public AdminUpdateUser(int id, String firstName, String lastName, String username, String email, String Image) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.Username = username;
        this.Email = email;
        this.Image = Image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}
