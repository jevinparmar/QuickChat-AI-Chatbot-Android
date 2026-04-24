package com.example.quickchataichatbot;

public class UserModel {

    public String username;
    public String email;
    public String phone;

    public UserModel() {}

    public UserModel(String username, String email, String phone) {
        this.username = username;
        this.email = email;
        this.phone = phone;
    }
}