package repository

import (
	"employee-schedule-app/internal/domain"
	"errors"
	"sync"
)

var ErrEmployeeExists = errors.New("employee ID already registered")

// MemoryStorage implements an in-memory, thread-safe data access layer
type MemoryStorage struct {
	mu         sync.RWMutex
	employees  map[string]*domain.Employee
	schedule   domain.WeeklySchedule
	targetWeek domain.WeekWindow
}

func NewMemoryStorage() *MemoryStorage {
	return &MemoryStorage{
		employees:  make(map[string]*domain.Employee),
		schedule:   domain.NewWeeklySchedule(),
		targetWeek: domain.GetNextTargetWeek(),
	}
}

// RegisterEmployee stores a new employee record if the ID is unique
func (m *MemoryStorage) RegisterEmployee(emp *domain.Employee) error {
	m.mu.Lock()
	defer m.mu.Unlock()

	if _, exists := m.employees[emp.ID]; exists {
		return ErrEmployeeExists
	}
	m.employees[emp.ID] = emp
	return nil
}

// GetEmployees returns all currently registered records
func (m *MemoryStorage) GetEmployees() []*domain.Employee {
	m.mu.RLock()
	defer m.mu.RUnlock()

	list := make([]*domain.Employee, 0, len(m.employees))
	for _, emp := range m.employees {
		list = append(list, emp)
	}
	return list
}

// SavePreferences updates preferences for a specific employee ID
func (m *MemoryStorage) SavePreferences(id string, prefs []domain.Preference) {
	m.mu.Lock()
	defer m.mu.Unlock()

	if emp, exists := m.employees[id]; exists {
		emp.Preferences = prefs
	}
}

func (m *MemoryStorage) GetWeeklySchedule() domain.WeeklySchedule {
	m.mu.RLock()
	defer m.mu.RUnlock()
	return m.schedule
}

func (m *MemoryStorage) UpdateSchedule(sched domain.WeeklySchedule) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.schedule = sched
}

func (m *MemoryStorage) GetTargetWeek() domain.WeekWindow {
	return m.targetWeek
}
