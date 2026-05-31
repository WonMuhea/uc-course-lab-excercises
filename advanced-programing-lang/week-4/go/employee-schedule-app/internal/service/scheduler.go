package service

import (
	"employee-schedule-app/internal/domain"
	"employee-schedule-app/internal/repository"
	"math/rand"
	"sort"
	"time"
)

// SchedulerService orchestrates automated generation and validates constraints
type SchedulerService struct {
	repo *repository.MemoryStorage
	rng  *rand.Rand
}

func NewSchedulerService(repo *repository.MemoryStorage) *SchedulerService {
	return &SchedulerService{
		repo: repo,
		rng:  rand.New(rand.NewSource(time.Now().UnixNano())),
	}
}

// GenerateSchedule builds assignments according to preference ranks and labor limits.
// Returns the compiled schedule and a boolean indicating if any shifts remain uncovered due to staff limits.
func (s *SchedulerService) GenerateSchedule() (domain.WeeklySchedule, bool) {
	employees := s.repo.GetEmployees()
	newSched := domain.NewWeeklySchedule()
	capacityExhausted := false

	if len(employees) == 0 {
		return newSched, false
	}

	// Tracks total assigned days per employee across the week
	workDaysCount := make(map[string]int)
	for _, e := range employees {
		workDaysCount[e.ID] = 0
	}

	// Step 1: Flatten and sort preferred choices by priority rank
	type flatPref struct {
		emp  *domain.Employee
		pref domain.Preference
	}
	var allPrefs []flatPref
	for _, emp := range employees {
		for _, p := range emp.Preferences {
			allPrefs = append(allPrefs, flatPref{emp: emp, pref: p})
		}
	}

	// Sort so primary choices (Priority 1) are processed before fallback selections
	sort.Slice(allPrefs, func(i, j int) bool {
		return allPrefs[i].pref.Priority < allPrefs[j].pref.Priority
	})

	// Step 2: Assign shifts based on preferences and daily rules
	for _, fp := range allPrefs {
		day := fp.pref.Day
		shift := fp.pref.Shift
		emp := fp.emp

		// Skip if employee has hit the maximum 5-day weekly limit
		if workDaysCount[emp.ID] >= 5 {
			continue
		}
		// Skip if employee is already working another shift on this day
		if s.isEmployeeWorkingOnDay(newSched, day, emp.ID) {
			continue
		}

		newSched[day][shift] = append(newSched[day][shift], domain.ShiftAssignment{
			EmployeeID:   emp.ID,
			EmployeeName: emp.Name,
		})
		workDaysCount[emp.ID]++
	}

	// Step 3: Enforce the minimum capacity rule (at least 2 employees per shift per day)
	for d := domain.Monday; d <= domain.Sunday; d++ {
		for _, shift := range []domain.Shift{domain.Morning, domain.Afternoon, domain.Evening} {
			for len(newSched[d][shift]) < 2 {
				assigned := s.fillShiftWithFallback(newSched, d, shift, employees, workDaysCount)
				if !assigned {
					// Mark true if shifts are short-staffed because everyone reached their 5-day limit
					capacityExhausted = true
					break
				}
			}
		}
	}

	s.repo.UpdateSchedule(newSched)
	return newSched, capacityExhausted
}

// fillShiftWithFallback finds available staff using a randomized shuffle for fair distribution
func (s *SchedulerService) fillShiftWithFallback(sched domain.WeeklySchedule, day domain.Day, shift domain.Shift, employees []*domain.Employee, workCounts map[string]int) bool {
	shuffledEmps := make([]*domain.Employee, len(employees))
	copy(shuffledEmps, employees)
	s.rng.Shuffle(len(shuffledEmps), func(i, j int) {
		shuffledEmps[i], shuffledEmps[j] = shuffledEmps[j], shuffledEmps[i]
	})

	for _, emp := range shuffledEmps {
		if workCounts[emp.ID] >= 5 {
			continue // Employee has reached the 5-day cap
		}
		if s.isEmployeeWorkingOnDay(sched, day, emp.ID) {
			continue // Employee is already working today
		}

		sched[day][shift] = append(sched[day][shift], domain.ShiftAssignment{
			EmployeeID:   emp.ID,
			EmployeeName: emp.Name,
		})
		workCounts[emp.ID]++
		return true // Assignment successful
	}
	return false // No available employees fit the constraints
}

func (s *SchedulerService) isEmployeeWorkingOnDay(sched domain.WeeklySchedule, day domain.Day, empID string) bool {
	for _, assignments := range sched[day] {
		for _, assign := range assignments {
			if assign.EmployeeID == empID {
				return true
			}
		}
	}
	return false
}
