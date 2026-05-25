package main

import (
	"bufio"
	"fmt"
	"os"
	"strconv"
	"strings"
	"time"
)

var reader = bufio.NewReader(os.Stdin)

func main() {
	loadData()

	for {
		renderMainMenu()
		fmt.Print("Enter your choice (1-8): ")
		
		choice, _ := reader.ReadString('\n')
		choice = strings.TrimSpace(choice)

		switch choice {
		case "1":
			menuAddExpense()
		case "2":
			menuViewAllExpenses()
		case "3":
			menuFilterByCategory()
		case "4":
			menuFilterByDateRange()
		case "5":
			menuSearchByDescription()
		case "6":
			menuShowSummaryReport()
		case "7":
			menuRemoveExpense()
		case "8":
			fmt.Println("\nExiting application. Goodbye!")
			return
		default:
			fmt.Println("\nInvalid selection. Please choose a number from 1 to 8.")
			waitForEnter()
		}
	}
}

// --- UI RENDERING FUNCTIONS ---

func renderMainMenu() {
	fmt.Print("\033[H\033[2J") 
	fmt.Println("+----------------------------------+")
	fmt.Println("|            MAIN MENU             |")
	fmt.Println("+----------------------------------+")
	fmt.Println("|  1. Add Expense                  |")
	fmt.Println("|  2. View All Expenses            |")
	fmt.Println("|  3. Filter by Category           |")
	fmt.Println("|  4. Filter by Date Range         |")
	fmt.Println("|  5. Search by Description        |")
	fmt.Println("|  6. Show Summary Report          |")
	fmt.Println("|  7. Remove Expense               |")
	fmt.Println("|  8. Exit                         |")
	fmt.Println("+----------------------------------+")
}

func printTable(expenses []Expense) {
	if len(expenses) == 0 {
		fmt.Println("\n[No transactions recorded or matching criteria]")
		return
	}
	fmt.Println("\n------------------------------------------------------------------------")
	fmt.Printf("%-4s | %-12s | %-15s | %-10s | %-20s\n", "ID", "Date", "Category", "Amount", "Description")
	fmt.Println("------------------------------------------------------------------------")
	for _, e := range expenses {
		fmt.Printf("%-4d | %-12s | %-15s | $%-9.2f | %-20s\n",
			e.ID, e.Date.Format("2006-01-02"), strings.ToUpper(e.Category), e.Amount, e.Description)
	}
	fmt.Println("------------------------------------------------------------------------")
}

// --- MENU HANDLERS ---

func menuAddExpense() {
	fmt.Println("\n>>> ADD NEW EXPENSE <<<")
	
	fmt.Print("Enter Date (YYYY-MM-DD) [or press Enter for today]: ")
	dateStr, _ := reader.ReadString('\n')
	dateStr = strings.TrimSpace(dateStr)
	var date time.Time
	var err error
	if dateStr == "" {
		date = time.Now()
	} else {
		date, err = time.Parse("2006-01-02", dateStr)
		if err != nil {
			fmt.Println("Error: Invalid date format.")
			waitForEnter()
			return
		}
	}

	fmt.Print("Enter Amount ($): ")
	amtStr, _ := reader.ReadString('\n')
	amount, err := strconv.ParseFloat(strings.TrimSpace(amtStr), 64)
	if err != nil {
		fmt.Println("Error: Invalid numeric amount.")
		waitForEnter()
		return
	}

	fmt.Print("Enter Category: ")
	catStr, _ := reader.ReadString('\n')
	category := strings.TrimSpace(catStr)

	fmt.Print("Enter Description: ")
	descStr, _ := reader.ReadString('\n')
	description := strings.TrimSpace(descStr)

	newExpense := Expense{
		ID:          tracker.NextID,
		Date:        date,
		Amount:      amount,
		Category:    category,
		Description: description,
	}
	
	tracker.Expenses = append(tracker.Expenses, newExpense)
	tracker.NextID++
	saveData()
	fmt.Println("\nExpense added successfully!")
	waitForEnter()
}

func menuViewAllExpenses() {
	fmt.Println("\n>>> ALL EXPENSES <<<")
	printTable(tracker.Expenses)
	waitForEnter()
}

func menuFilterByCategory() {
	fmt.Println("\n>>> FILTER BY CATEGORY <<<")
	fmt.Print("Enter category name: ")
	cat, _ := reader.ReadString('\n')
	results := FilterByCategory(cat)
	printTable(results)
	waitForEnter()
}

func menuFilterByDateRange() {
	fmt.Println("\n>>> FILTER BY DATE RANGE <<<")
	fmt.Print("Enter start date (YYYY-MM-DD): ")
	startStr, _ := reader.ReadString('\n')
	start, err1 := time.Parse("2006-01-02", strings.TrimSpace(startStr))

	fmt.Print("Enter end date (YYYY-MM-DD): ")
	endStr, _ := reader.ReadString('\n')
	end, err2 := time.Parse("2006-01-02", strings.TrimSpace(endStr))

	if err1 != nil || err2 != nil {
		fmt.Println("Error: One or both dates are in an incorrect layout format.")
		waitForEnter()
		return
	}

	results := FilterByDateRange(start, end)
	printTable(results)
	waitForEnter()
}

func menuSearchByDescription() {
	fmt.Println("\n>>> SEARCH BY DESCRIPTION <<<")
	fmt.Print("Enter search keywords: ")
	query, _ := reader.ReadString('\n')
	results := SearchByDescription(query)
	printTable(results)
	waitForEnter()
}

func menuShowSummaryReport() {
	fmt.Println("\n>>> SUMMARY REPORT <<<")
	report := GenerateSummary(tracker.Expenses)
	
	fmt.Printf("\nOverall Total Expenses: $%.2f\n", report.TotalOverall)
	fmt.Println("\nBreakdown by Category:")
	if len(report.CategoryTotals) == 0 {
		fmt.Println("  No active dynamic categories recorded.")
	} else {
		for _, item := range report.CategoryTotals {
			fmt.Printf("  - %-15s: $%.2f\n", item.Name, item.Total)
		}
	}
	waitForEnter()
}

func menuRemoveExpense() {
	fmt.Println("\n>>> REMOVE EXPENSE <<<")
	
	// Added clear notice to guide the user to read the ID beforehand
	fmt.Println("ℹ️ Note: To delete an expense, you must provide its unique ID.")
	fmt.Println("   If you don't know the ID, please return to the Main Menu and use")
	fmt.Println("   options 2, 3, 4, or 5 to search and find the ID first.")
	fmt.Println("-----------------------------------------------------------------")
	
	fmt.Print("Enter the ID of the expense to delete (or press Enter to cancel): ")
	idStr, _ := reader.ReadString('\n')
	idStr = strings.TrimSpace(idStr)
	
	if idStr == "" {
		fmt.Println("\nDeletion canceled.")
		waitForEnter()
		return
	}

	id, err := strconv.Atoi(idStr)
	if err != nil {
		fmt.Println("Error: Invalid numerical ID format.")
		waitForEnter()
		return
	}

	removed := false
	for i, e := range tracker.Expenses {
		if e.ID == id {
			tracker.Expenses = append(tracker.Expenses[:i], tracker.Expenses[i+1:]...)
			removed = true
			break
		}
	}

	if removed {
		saveData()
		fmt.Println("\nExpense record removed successfully.")
	} else {
		fmt.Println("\nError: ID record not found.")
	}
	waitForEnter()
}

func waitForEnter() {
	fmt.Print("\nPress [Enter] to return to the Main Menu...")
	_, _ = reader.ReadString('\n')
}