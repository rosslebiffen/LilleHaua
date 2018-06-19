package com.example.jonet.lillehaua.Model;


public class User {
    private String Name;
    private String Password;
    private String Phone;
    private String Admin;


    public User() {

    }

    public User(String name, String password) {
        Name = name;
        Password = password;
        Admin = "false";

    }



    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getAdmin() {
        return Admin;
    }

    public void setAdmin(String admin) {
        Admin = admin;
    }
}
