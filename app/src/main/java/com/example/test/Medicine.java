package com.example.test;

import java.io.Serializable;

public class Medicine implements Serializable {
    private String name;
    private String dosage;
    private long reminderTime; // В миллисекундах (время в день)

    public Medicine(String name, String dosage, long reminderTime) {
        this.name = name;
        this.dosage = dosage;
        this.reminderTime = reminderTime;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public long getReminderTime() { return reminderTime; }
    public void setReminderTime(long reminderTime) { this.reminderTime = reminderTime; }
}