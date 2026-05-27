package com.example.financialmanagement.model;

public class PersonIncome {
    private String name;
    private double totalIncome;
    private int count;

    public PersonIncome(String name, double totalIncome, int count) {
        this.name = name;
        this.totalIncome = totalIncome;
        this.count = count;
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
}
