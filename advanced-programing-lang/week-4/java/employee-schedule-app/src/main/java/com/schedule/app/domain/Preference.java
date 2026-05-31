package com.schedule.app.domain;

public record Preference(Day day, Shift shift, int priority) {}