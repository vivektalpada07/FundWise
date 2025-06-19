package com.example.fundwiseapp.models;

public class Lesson {
    private String id;
    private String title;
    private String content;
    private String category;

    private String author;
    private String role;
    private String ownerId;
    private long timestamp;

    // Required empty constructor for Firebase
    public Lesson() {
    }

    // Full constructor for creating lesson objects
    public Lesson(String id, String title, String content, String category,
                  String author, String role, String ownerId, long timestamp) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;

        this.author = author;
        this.role = role;
        this.ownerId = ownerId;
        this.timestamp = timestamp;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}