package com.schedule.app.repository;

import com.schedule.app.domain.Employee;
import com.schedule.app.domain.Preference;
import com.schedule.app.domain.WeeklySchedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryStorage {
    private final Map<String, Employee> employees = new ConcurrentHashMap<>();
    private WeeklySchedule schedule = new WeeklySchedule();
    private final LocalDate targetWeekStart;

    public MemoryStorage() {
        this.targetWeekStart = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    public void registerEmployee(Employee emp) {
        if (employees.containsKey(emp.getId())) {
            throw new IllegalArgumentException("Employee ID already registered.");
        }
        employees.put(emp.getId(), emp);
    }

    public Collection<Employee> getEmployees() {
        return employees.values();
    }

    public void savePreferences(String id, List<Preference> prefs) {
        Employee emp = employees.get(id);
        if (emp != null) {
            emp.setPreferences(prefs);
        }
    }

    public synchronized WeeklySchedule getWeeklySchedule() {
        return schedule;
    }

    public synchronized void updateSchedule(WeeklySchedule newSchedule) {
        this.schedule = newSchedule;
    }

    public LocalDate getTargetWeekStart() {
        return targetWeekStart;
    }
}