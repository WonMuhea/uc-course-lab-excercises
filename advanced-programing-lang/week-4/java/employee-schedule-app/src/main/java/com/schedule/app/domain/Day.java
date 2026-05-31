package com.schedule.app.domain;

public enum Day {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    private final String label;

    Day(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}