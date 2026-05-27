package com.example.financialmanagement.model;

public class PersonIncome {
    private String name;
    private double totalIncome;
    private int count;
    private String avatar;

    public PersonIncome(String name, double totalIncome, int count) {
        this.name = name;
        this.totalIncome = totalIncome;
        this.count = count;
    }

    public PersonIncome(String name, double totalIncome, int count, String avatar) {
        this.name = name;
        this.totalIncome = totalIncome;
        this.count = count;
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public int getCount() {
        return count;
    }

    public String getAvatar() {
        return avatar;
    }
}
