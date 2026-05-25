package main

import (
	"encoding/json"
	"os"
)

const jsonFile = "expenses.json"

var tracker Tracker

func loadData() {
	file, err := os.ReadFile(jsonFile)
	if err != nil {
		// File doesn't exist or can't be read; initialize clean state
		tracker = Tracker{Expenses: []Expense{}, NextID: 1}
		return
	}
	_ = json.Unmarshal(file, &tracker)
}

func saveData() {
	data, _ := json.MarshalIndent(tracker, "", "  ")
	_ = os.WriteFile(jsonFile, data, 0644)
}