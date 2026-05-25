package main

import (
	"strings"
	"time"
)

// FilterByCategory returns expenses matching a specific category
func FilterByCategory(category string) []Expense {
	var filtered []Expense
	cleanCat := strings.ToLower(strings.TrimSpace(category))
	
	for _, e := range tracker.Expenses {
		if strings.ToLower(e.Category) == cleanCat {
			filtered = append(filtered, e)
		}
	}
	return filtered
}

// FilterByDateRange returns expenses within an inclusive timeframe
func FilterByDateRange(start, end time.Time) []Expense {
	var filtered []Expense
	for _, e := range tracker.Expenses {
		// Checks if date is between start and end (inclusive)
		if (e.Date.After(start) || e.Date.Equal(start)) && (e.Date.Before(end) || e.Date.Equal(end)) {
			filtered = append(filtered, e)
		}
	}
	return filtered
}

// SearchByDescription matches sub-strings within expense descriptions
func SearchByDescription(query string) []Expense {
	var filtered []Expense
	cleanQuery := strings.ToLower(strings.TrimSpace(query))
	
	for _, e := range tracker.Expenses {
		if strings.Contains(strings.ToLower(e.Description), cleanQuery) {
			filtered = append(filtered, e)
		}
	}
	return filtered
}

// GenerateSummary calculates totals across the global expense list
func GenerateSummary(expenses []Expense) SummaryReport {
	var totalOverall float64
	catMap := make(map[string]float64)

	for _, e := range expenses {
		totalOverall += e.Amount
		// Capitalize the first letter for clean summary presentation
		displayCat := strings.Title(strings.ToLower(e.Category))
		catMap[displayCat] += e.Amount
	}

	var catTotals []CategorySum
	for name, sum := range catMap {
		catTotals = append(catTotals, CategorySum{Name: name, Total: sum})
	}

	return SummaryReport{
		TotalOverall:   totalOverall,
		CategoryTotals: catTotals,
	}
}