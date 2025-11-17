package com.example.test;  // Измени на свой пакет, если отличается

import java.io.Serializable;

public class Medicine implements Serializable {
    private String name;
    private String dosage;
    private long reminderTime;

    public Medicine() {}  // Для Gson

    public Medicine(String name, String dosage, long reminderTime) {
        this.name = name;
        this.dosage = dosage;
        this.reminderTime = reminderTime;
    }

    // Геттеры/сеттеры
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public long getReminderTime() { return reminderTime; }
    public void setReminderTime(long reminderTime) { this.reminderTime = reminderTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medicine medicine = (Medicine) o;
        return name != null ? name.equals(medicine.name) : medicine.name == null
                && (dosage != null ? dosage.equals(medicine.dosage) : medicine.dosage == null);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (dosage != null ? dosage.hashCode() : 0);
        return result;
    }
}