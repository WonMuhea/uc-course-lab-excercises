package com.schedule.app.domain;

import java.util.ArrayList;
import java.util.List;

public class Employee {
    private final String id;
    private final String name;
    private final List<Preference> preferences;

    public Employee(String id, String name) {
        this.id = id;
        this.name = name;
        this.preferences = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<Preference> getPreferences() { return preferences; }

    public synchronized void setPreferences(List<Preference> newPrefs) {
        this.preferences.clear();
        this.preferences.addAll(newPrefs);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", id, name);
    }
}