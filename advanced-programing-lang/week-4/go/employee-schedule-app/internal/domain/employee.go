package domain

// Employee encapsulates core profile attributes and shift preferences
type Employee struct {
	ID          string
	Name        string
	Preferences []Preference
}

// NewEmployee instantiates a baseline employee model
func NewEmployee(id, name string) *Employee {
	return &Employee{
		ID:          id,
		Name:        name,
		Preferences: make([]Preference, 0),
	}
}
