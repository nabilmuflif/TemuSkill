package com.example.temuskill.models;

public class Category {
    private String categoryId;
    private String name;

    public Category() {} // Wajib untuk Firebase

    public Category(String name) {
        this.name = name;
    }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Override toString agar Spinner menampilkan nama, bukan hash object
    @Override
    public String toString() {
        return name;
    }
}