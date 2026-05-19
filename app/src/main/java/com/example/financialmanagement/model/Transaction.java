package com.example.financialmanagement.model;

import java.io.Serializable;

public class Transaction implements Serializable {
    public static final String TYPE_INCOME = "income";
    public static final String TYPE_EXPENSE = "expense";

    private long id;
    private String type;
    private double amount;
    private String person;
    private String event;
    private String date;
    private String time;
    private long timestamp;

    public Transaction() {
    }

    public Transaction(long id, String type, double amount, String person, String event, String date, String time, long timestamp) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.person = person;
        this.event = event;
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isIncome() {
        return TYPE_INCOME.equals(type);
    }

    public boolean isExpense() {
        return TYPE_EXPENSE.equals(type);
    }
}
