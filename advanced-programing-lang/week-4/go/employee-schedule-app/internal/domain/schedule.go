package domain

// ShiftAssignment pairs specific operations to employee resources
type ShiftAssignment struct {
	EmployeeID   string
	EmployeeName string
}

// DailySchedule matches shifts to active assignments
type DailySchedule map[Shift][]ShiftAssignment

// WeeklySchedule groups comprehensive data across all 7 operational days
type WeeklySchedule map[Day]DailySchedule

// NewWeeklySchedule generates an empty initialized tracking layout matrix
func NewWeeklySchedule() WeeklySchedule {
	ws := make(WeeklySchedule)
	for d := Monday; d <= Sunday; d++ {
		ws[d] = DailySchedule{
			Morning:   make([]ShiftAssignment, 0),
			Afternoon: make([]ShiftAssignment, 0),
			Evening:   make([]ShiftAssignment, 0),
		}
	}
	return ws
}
