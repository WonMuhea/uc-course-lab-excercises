package com.schedule.app.domain;

public enum Shift {
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening");

    private final String label;

    Shift(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}