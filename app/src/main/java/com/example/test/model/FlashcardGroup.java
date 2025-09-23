package com.example.test.model;

import com.google.gson.annotations.SerializedName;

public class FlashcardGroup {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("userId")
    private int userId;
    // Getters và Setters
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
    public int getUserId() {
        return userId;
    }
}