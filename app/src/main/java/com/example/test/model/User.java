package com.example.test.model;

public class User {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private String speciField;
    private String englishLevel;

    public User() {
    }
    public User(int id, String email, String name, String phone, String speciField, String avatar, String englishLevel) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.speciField = speciField;
        this.avatar = avatar;
        this.englishLevel = englishLevel;
    }
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
//    public String getAvatar() {
//        return avatar;
//    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public String getAvt() {
        return avatar;
    }
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpeciField() {
        return speciField;
    }

    public void setSpeciField(String speciField) {
        this.speciField = speciField;
    }
    public String getEnglishLevel() {
        return englishLevel;
    }

    public void setEnglishLevel(String englishLevel) {
        this.englishLevel = englishLevel;
    }
}
