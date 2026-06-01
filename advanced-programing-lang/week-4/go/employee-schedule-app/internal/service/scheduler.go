package service

import (
	"employee-schedule-app/internal/domain"
	"employee-schedule-app/internal/repository"
	"sort"
)

type SchedulerService struct {
	repo *repository.MemoryStorage
}

func NewSchedulerService(repo *repository.MemoryStorage) *SchedulerService {
	return &SchedulerService{
		repo: repo,
	}
}

func (s *SchedulerService) GetEmployees() []*domain.Employee {
	return s.repo.GetEmployees()
}

func (s *SchedulerService) GetTargetWeek() domain.WeekWindow {
	return s.repo.GetTargetWeek()
}

func (s *SchedulerService) GetWeeklySchedule() domain.WeeklySchedule {
	return s.repo.GetWeeklySchedule()
}

func (s *SchedulerService) SavePreferences(employeeID string, prefs []domain.Preference) {
	s.repo.SavePreferences(employeeID, prefs)
}

// GenerateSchedule builds assignments deterministically based on sequential fallback routing rules
func (s *SchedulerService) GenerateSchedule() (domain.WeeklySchedule, bool) {
	employees := s.repo.GetEmployees()
	newSched := domain.NewWeeklySchedule()
	capacityExhausted := false

	if len(employees) == 0 {
		return newSched, false
	}

	workDaysCount := make(map[string]int)
	for _, e := range employees {
		workDaysCount[e.ID] = 0
	}

	// --- STEP 1: PREFERENCE & REDIRECTION CONFLICT RESOLUTION ---
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

	sort.Slice(allPrefs, func(i, j int) bool {
		if allPrefs[i].pref.Priority == allPrefs[j].pref.Priority {
			return allPrefs[i].emp.ID < allPrefs[j].emp.ID
		}
		return allPrefs[i].pref.Priority < allPrefs[j].pref.Priority
	})

	for _, fp := range allPrefs {
		emp := fp.emp
		prefDay := fp.pref.Day
		prefShift := fp.pref.Shift

		if workDaysCount[emp.ID] >= 5 {
			continue
		}

		if len(newSched[prefDay][prefShift]) < 2 && !s.isEmployeeWorkingOnDay(newSched, prefDay, emp.ID) {
			newSched[prefDay][prefShift] = append(newSched[prefDay][prefShift], domain.ShiftAssignment{
				EmployeeID:   emp.ID,
				EmployeeName: emp.Name,
			})
			workDaysCount[emp.ID]++
			continue
		}

		assignedSameDay := false
		for _, altShift := range []domain.Shift{domain.Morning, domain.Afternoon, domain.Evening} {
			if altShift == prefShift {
				continue
			}
			if len(newSched[prefDay][altShift]) < 2 && !s.isEmployeeWorkingOnDay(newSched, prefDay, emp.ID) {
				newSched[prefDay][altShift] = append(newSched[prefDay][altShift], domain.ShiftAssignment{
					EmployeeID:   emp.ID,
					EmployeeName: emp.Name,
				})
				workDaysCount[emp.ID]++
				assignedSameDay = true
				break
			}
		}
		if assignedSameDay {
			continue
		}

		nextDay := (prefDay + 1) % 7
		for _, altShift := range []domain.Shift{domain.Morning, domain.Afternoon, domain.Evening} {
			if len(newSched[nextDay][altShift]) < 2 && !s.isEmployeeWorkingOnDay(newSched, nextDay, emp.ID) {
				newSched[nextDay][altShift] = append(newSched[nextDay][altShift], domain.ShiftAssignment{
					EmployeeID:   emp.ID,
					EmployeeName: emp.Name,
				})
				workDaysCount[emp.ID]++
				break
			}
		}
	}

	// --- STEP 2: RESIDUAL DETERMINISTIC FALLBACK FILLING ---
	sort.Slice(employees, func(i, j int) bool {
		return employees[i].ID < employees[j].ID
	})

	for d := domain.Monday; d <= domain.Sunday; d++ {
		for _, shift := range []domain.Shift{domain.Morning, domain.Afternoon, domain.Evening} {
			for len(newSched[d][shift]) < 2 {
				assigned := false
				for _, emp := range employees {
					if workCounts, ok := workDaysCount[emp.ID]; ok && workCounts >= 5 {
						continue
					}
					if s.isEmployeeWorkingOnDay(newSched, d, emp.ID) {
						continue
					}

					newSched[d][shift] = append(newSched[d][shift], domain.ShiftAssignment{
						EmployeeID:   emp.ID,
						EmployeeName: emp.Name,
					})
					workDaysCount[emp.ID]++
					assigned = true
					break
				}

				if !assigned {
					capacityExhausted = true
					break
				}
			}
		}
	}

	s.repo.UpdateSchedule(newSched)
	return newSched, capacityExhausted
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
