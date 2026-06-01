package com.schedule.app.repository;

import com.schedule.app.domain.Employee;
import com.schedule.app.domain.Preference;
import com.schedule.app.domain.WeeklySchedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MemoryStorage serves as the in-memory repository layer for the scheduling engine.
 * It encapsulates employee state, preference registries, and generated boards while providing
 * thread-safe access to data across concurrent reads/writes from the application service layer.
 */
public class MemoryStorage {
    
    // Thread-safe map storing registered employees with their unique ID as the lookup key
    private final Map<String, Employee> employees = new ConcurrentHashMap<>();
    
    // Keeps track of the master weekly schedule matrix output allocation
    private WeeklySchedule schedule = new WeeklySchedule();
    
    // Fixed baseline metadata indicating the Monday start date of the active planning horizon
    private final LocalDate targetWeekStart;

    /**
     * Initializes a new storage session and shifts the scheduling target window 
     * automatically to the upcoming Monday.
     */
    public MemoryStorage() {
        this.targetWeekStart = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    /**
     * Registers a unique employee profile into the system memory cache.
     * @param emp The employee object model to store
     * @throws IllegalArgumentException if an employee with the identical ID already exists
     */
    public void registerEmployee(Employee emp) {
        if (employees.containsKey(emp.getId())) {
            throw new IllegalArgumentException("Employee ID already registered.");
        }
        employees.put(emp.getId(), emp);
    }

    /**
     * Retrieves all active employee profiles inside the database pool.
     * @return A thread-safe, backing view Collection of current employee records
     */
    public Collection<Employee> getEmployees() {
        return employees.values();
    }

    /**
     * Updates the collection of preferred work choices assigned to a worker profile.
     * @param id    The unique string identifier of the employee
     * @param prefs The list containing day and shift constraint criteria records
     */
    public void savePreferences(String id, List<Preference> prefs) {
        Employee emp = employees.get(id);
        if (emp != null) {
            emp.setPreferences(prefs);
        }
    }

    /**
     * Thread-safely reads the state copy of the master calendar roster grid.
     * @return The currently held WeeklySchedule entity record
     */
    public synchronized WeeklySchedule getWeeklySchedule() {
        return schedule;
    }

    /**
     * Thread-safely overrides the active master calendar roster layout with an updated matrix plan.
     * @param newSchedule The freshly calculated assignment map output compiled from the service engine
     */
    public synchronized void updateSchedule(WeeklySchedule newSchedule) {
        this.schedule = newSchedule;
    }

    /**
     * Exposes the immutable target scheduling timing benchmark window tracking metrics.
     * @return The LocalDate object marking the target planning week starting Monday
     */
    public LocalDate getTargetWeekStart() {
        return targetWeekStart;
    }
}