package domain

import "time"

// Day represents days of the week as an enumeration
type Day int

// Shift represents daily working intervals
type Shift int

const (
	Monday Day = iota
	Tuesday
	Wednesday
	Thursday
	Friday
	Saturday
	Sunday
)

const (
	Morning Shift = iota
	Afternoon
	Evening
)

// String converts Day enum to readable text
func (d Day) String() string {
	return []string{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}[d]
}

// String converts Shift enum to readable text
func (s Shift) String() string {
	return []string{"Morning", "Afternoon", "Evening"}[s]
}

// Preference maps an employee's desired shift choice and its priority tier
type Preference struct {
	Day      Day
	Shift    Shift
	Priority int // 1 = Primary preference, 2 = Secondary fallback preference
}

// WeekWindow tracks target dates ensuring allocations occur 1 week in advance
type WeekWindow struct {
	StartOfWeek time.Time
}

// GetNextTargetWeek calculates the starting Monday date of the upcoming week
func GetNextTargetWeek() WeekWindow {
	now := time.Now()
	daysUntilNextMonday := int(time.Monday - now.Weekday())
	if daysUntilNextMonday <= 0 {
		daysUntilNextMonday += 7
	}
	nextMonday := now.AddDate(0, 0, daysUntilNextMonday)

	return WeekWindow{
		StartOfWeek: time.Date(nextMonday.Year(), nextMonday.Month(), nextMonday.Day(), 0, 0, 0, 0, now.Location()),
	}
}
