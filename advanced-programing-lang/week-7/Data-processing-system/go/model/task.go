package model

// Task encapsulates data processed through channels by worker goroutines.
type Task struct {
	ID   int
	Data string
}
