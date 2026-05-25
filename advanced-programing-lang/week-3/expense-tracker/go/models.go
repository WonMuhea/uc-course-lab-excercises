package main

import "time"

type Expense struct {
	ID          int       `json:"id"`
	Date        time.Time `json:"date"`
	Amount      float64   `json:"amount"`
	Category    string    `json:"category"`
	Description string    `json:"description"`
}

type Tracker struct {
	Expenses []Expense `json:"expenses"`
	NextID   int       `json:"next_id"`
}

type CategorySum struct {
	Name  string
	Total float64
}

type SummaryReport struct {
	TotalOverall   float64
	CategoryTotals []CategorySum
}