package com.schedule.app.service;

import com.schedule.app.domain.*;
import com.schedule.app.repository.MemoryStorage;

import java.time.LocalDate;
import java.util.*;

public class SchedulerService {
    private final MemoryStorage storage;

    public SchedulerService(MemoryStorage storage) {
        this.storage = storage;
    }

    // --- SERVICE ARCHITECTURE PROXY METHOD ENDPOINTS ---

    public List<Employee> getEmployees() {
        return new ArrayList<>(storage.getEmployees());
    }

    public void registerEmployee(Employee emp) {
        storage.registerEmployee(emp);
    }

    public void savePreferences(String id, List<Preference> prefs) {
        storage.savePreferences(id, prefs);
    }

    public WeeklySchedule getWeeklySchedule() {
        return storage.getWeeklySchedule();
    }

    public LocalDate getTargetWeekStart() {
        return storage.getTargetWeekStart();
    }

    public SchedulingResult generateSchedule() {
        List<Employee> employeeList = new ArrayList<>(storage.getEmployees());
        WeeklySchedule newSched = new WeeklySchedule();
        boolean capacityExhausted = false;

        if (employeeList.isEmpty()) {
            return new SchedulingResult(newSched, false);
        }

        Map<String, Integer> workDaysCount = new HashMap<>();
        for (Employee e : employeeList) {
            workDaysCount.put(e.getId(), 0);
        }

        // Step 1: Flatten and sort preferred choices by priority tier
        record FlatPref(Employee employee, Preference preference) {}
        List<FlatPref> allPrefs = new ArrayList<>();
        for (Employee emp : employeeList) {
            for (Preference p : emp.getPreferences()) {
                allPrefs.add(new FlatPref(emp, p));
            }
        }
        allPrefs.sort(Comparator.comparingInt(p -> p.preference().priority()));

        // Step 2: Assign shifts based on choice trends
        for (FlatPref fp : allPrefs) {
            Day day = fp.preference().day();
            Shift shift = fp.preference().shift();
            Employee emp = fp.employee();

            if (workDaysCount.get(emp.getId()) >= 5) continue;
            if (isEmployeeWorkingOnDay(newSched, day, emp.getId())) continue;

            newSched.assign(day, shift, emp);
            workDaysCount.put(emp.getId(), workDaysCount.get(emp.getId()) + 1);
        }

        // Step 3: Enforce shift minimum capacities (Minimum 2 employees) deterministically
        for (Day day : Day.values()) {
            for (Shift shift : Shift.values()) {
                while (newSched.getAssignments(day, shift).size() < 2) {
                    boolean assigned = fillShiftWithDeterministicFallback(newSched, day, shift, employeeList, workDaysCount);
                    if (!assigned) {
                        capacityExhausted = true;
                        break;
                    }
                }
            }
        }

        storage.updateSchedule(newSched);
        return new SchedulingResult(newSched, capacityExhausted);
    }

    /**
     * FIXED: Replaced random shuffling with a strict, predictable fallback order based on Employee ID.
     */
    private boolean fillShiftWithDeterministicFallback(WeeklySchedule sched, Day day, Shift shift, 
                                                        List<Employee> employees, Map<String, Integer> workCounts) {
        // Create a copy and sort alphabetically/numerically by ID to ensure repeatable results
        List<Employee> sortedEmps = new ArrayList<>(employees);
        sortedEmps.sort(Comparator.comparing(Employee::getId));

        for (Employee emp : sortedEmps) {
            if (workCounts.get(emp.getId()) >= 5) continue;
            if (isEmployeeWorkingOnDay(sched, day, emp.getId())) continue;

            sched.assign(day, shift, emp);
            workCounts.put(emp.getId(), workCounts.get(emp.getId()) + 1);
            return true;
        }
        return false;
    }

    private boolean isEmployeeWorkingOnDay(WeeklySchedule sched, Day day, String empId) {
        for (Shift shift : Shift.values()) {
            for (WeeklySchedule.ShiftAssignment assign : sched.getAssignments(day, shift)) {
                if (assign.employeeId().equals(empId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public record SchedulingResult(WeeklySchedule schedule, boolean capacityExhausted) {}
}