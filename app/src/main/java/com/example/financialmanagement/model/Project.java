package com.example.financialmanagement.model;

import java.io.Serializable;

public class Project implements Serializable {
    private long id;
    private String name;
    private long createdAt;

    public Project() {
    }

    public Project(long id, String name, long createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
