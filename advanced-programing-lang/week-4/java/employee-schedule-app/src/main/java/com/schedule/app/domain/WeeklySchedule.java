package com.schedule.app.domain;

import java.util.*;

public class WeeklySchedule {
    public record ShiftAssignment(String employeeId, String employeeName) {}

    private final Map<Day, Map<Shift, List<ShiftAssignment>>> scheduleMatrix;

    public WeeklySchedule() {
        this.scheduleMatrix = new EnumMap<>(Day.class);
        for (Day day : Day.values()) {
            Map<Shift, List<ShiftAssignment>> dailyShifts = new EnumMap<>(Shift.class);
            for (Shift shift : Shift.values()) {
                dailyShifts.put(shift, new ArrayList<>());
            }
            this.scheduleMatrix.put(day, dailyShifts);
        }
    }

    public void assign(Day day, Shift shift, Employee employee) {
        scheduleMatrix.get(day).get(shift).add(new ShiftAssignment(employee.getId(), employee.getName()));
    }

    public List<ShiftAssignment> getAssignments(Day day, Shift shift) {
        return scheduleMatrix.get(day).get(shift);
    }
}