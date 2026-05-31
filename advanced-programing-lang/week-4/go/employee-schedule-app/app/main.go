package main

import (
	"fmt"
	"strings"

	"fyne.io/fyne/v2"
	"fyne.io/fyne/v2/app"
	"fyne.io/fyne/v2/container"
	"fyne.io/fyne/v2/dialog"
	"fyne.io/fyne/v2/widget"

	"employee-schedule-app/internal/domain"
	"employee-schedule-app/internal/repository"
	"employee-schedule-app/internal/service"
)

type AppUI struct {
	storage       *repository.MemoryStorage
	scheduler     *service.SchedulerService
	window        fyne.Window
	rightSpace    *fyne.Container // Container for updating the right-side workspace dynamically
	calendarGrid  *fyne.Container // Grid container holding the calendar view items
	footnoteLabel *widget.Label   // Italics footnote for unassigned shift warnings

	// Main element required for dynamic form sync
	empDropdown *widget.Select
}

func main() {
	a := app.New()
	w := a.NewWindow("Employee Management Dashboard")
	w.Resize(fyne.NewSize(1150, 720))

	storage := repository.NewMemoryStorage()
	scheduler := service.NewSchedulerService(storage)

	ui := &AppUI{
		storage:       storage,
		scheduler:     scheduler,
		window:        w,
		rightSpace:    container.NewMax(),
		calendarGrid:  container.NewGridWithColumns(2),
		footnoteLabel: widget.NewLabel(""),
	}

	ui.footnoteLabel.TextStyle = fyne.TextStyle{Italic: true}
	ui.footnoteLabel.Hide() // Hidden by default

	w.SetContent(ui.buildSidebarNavigationLayout())
	w.ShowAndRun()
}

// buildSidebarNavigationLayout builds a split-screen layout with side-navigation links on the left
func (ui *AppUI) buildSidebarNavigationLayout() fyne.CanvasObject {
	ui.rightSpace.Objects = []fyne.CanvasObject{ui.buildRegistrationScreen()}

	navRegisterBtn := widget.NewButton("Employee Registration", func() {
		ui.rightSpace.Objects = []fyne.CanvasObject{ui.buildRegistrationScreen()}
		ui.rightSpace.Refresh()
	})
	navPrefBtn := widget.NewButton("Shift Selection", func() {
		ui.rightSpace.Objects = []fyne.CanvasObject{ui.buildPreferencesScreen()}
		ui.rightSpace.Refresh()
	})
	navSchedBtn := widget.NewButton("Weekly Schedule Board", func() {
		ui.rightSpace.Objects = []fyne.CanvasObject{ui.buildScheduleScreen()}
		ui.rightSpace.Refresh()
	})

	sidebarContainer := container.NewVBox(
		widget.NewLabelWithStyle(" NAVIGATION MENU", fyne.TextAlignLeading, fyne.TextStyle{Bold: true}),
		widget.NewSeparator(),
		navRegisterBtn,
		navPrefBtn,
		navSchedBtn,
	)

	splitLayout := container.NewHSplit(sidebarContainer, ui.rightSpace)
	splitLayout.Offset = 0.22
	return splitLayout
}

// SCREEN 1: Handles Employee Registration Form Setup
func (ui *AppUI) buildRegistrationScreen() fyne.CanvasObject {
	idEntry := widget.NewEntry()
	idEntry.SetPlaceHolder("e.g. EMP001")
	nameEntry := widget.NewEntry()
	nameEntry.SetPlaceHolder("Employee Full Name")

	form := &widget.Form{
		Items: []*widget.FormItem{
			{Text: "Employee ID", Widget: idEntry},
			{Text: "Full Name", Widget: nameEntry},
		},
		OnSubmit: func() {
			id := strings.TrimSpace(idEntry.Text)
			name := strings.TrimSpace(nameEntry.Text)
			if id == "" || name == "" {
				dialog.ShowError(fmt.Errorf("all field inputs are mandatory"), ui.window)
				return
			}

			emp := domain.NewEmployee(id, name)
			if err := ui.storage.RegisterEmployee(emp); err != nil {
				dialog.ShowError(err, ui.window)
				return
			}

			dialog.ShowInformation("Success", "Employee registered successfully!", ui.window)
			idEntry.SetText("")
			nameEntry.SetText("")
		},
	}

	return container.NewVBox(
		widget.NewLabelWithStyle("1. Employee Registration Panel", fyne.TextAlignLeading, fyne.TextStyle{Bold: true}),
		widget.NewSeparator(),
		form,
	)
}

// SCREEN 2: Handles Shift Preference Selection Profile Entries
func (ui *AppUI) buildPreferencesScreen() fyne.CanvasObject {
	// Initialize the dropdown widget
	ui.empDropdown = widget.NewSelect([]string{}, func(value string) {})
	ui.empDropdown.PlaceHolder = "Choose Employee"
	ui.refreshEmployeeDropdown()

	// INTERCEPT: If the user clicks into an empty dropdown, trigger the modal immediately
	ui.empDropdown.OnChanged = func(value string) {
		if len(ui.storage.GetEmployees()) == 0 {
			dialog.ShowError(fmt.Errorf("no employee is inserted. Please register staff members first"), ui.window)
			ui.empDropdown.ClearSelected()
			return
		}
	}

	targetWeek := ui.storage.GetTargetWeek()
	weekLabel := widget.NewLabel(fmt.Sprintf("Target Shift Week Window: %s", targetWeek.StartOfWeek.Format("2006-01-02")))
	weekLabel.TextStyle = fyne.TextStyle{Italic: true}

	daySelect := widget.NewSelect([]string{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}, func(string) {})
	pref1Select := widget.NewSelect([]string{"Morning", "Afternoon", "Evening"}, func(string) {})
	pref2Select := widget.NewSelect([]string{"Morning", "Afternoon", "Evening"}, func(string) {})

	daySelect.PlaceHolder = "Select Target Day"
	pref1Select.PlaceHolder = "1st Shift Preference Tier"
	pref2Select.PlaceHolder = "2nd Shift Preference Tier (Optional)"

	saveBtn := widget.NewButton("Save Preferred Selections", func() {
		// Rule Validation: Block submission immediately if zero employees exist
		if len(ui.storage.GetEmployees()) == 0 {
			dialog.ShowError(fmt.Errorf("no employee is inserted. Please register staff members first before selecting preferences"), ui.window)
			return
		}

		empStr := ui.empDropdown.Selected
		if empStr == "" || daySelect.Selected == "" || pref1Select.Selected == "" {
			dialog.ShowError(fmt.Errorf("please complete form selections"), ui.window)
			return
		}

		empID := strings.Split(empStr, " ")[0]
		dayMap := map[string]domain.Day{"Monday": 0, "Tuesday": 1, "Wednesday": 2, "Thursday": 3, "Friday": 4, "Saturday": 5, "Sunday": 6}
		shiftMap := map[string]domain.Shift{"Morning": 0, "Afternoon": 1, "Evening": 2}

		prefs := []domain.Preference{
			{Day: dayMap[daySelect.Selected], Shift: shiftMap[pref1Select.Selected], Priority: 1},
		}
		if pref2Select.Selected != "" && pref2Select.Selected != pref1Select.Selected {
			prefs = append(prefs, domain.Preference{Day: dayMap[daySelect.Selected], Shift: shiftMap[pref2Select.Selected], Priority: 2})
		}

		ui.storage.SavePreferences(empID, prefs)
		dialog.ShowInformation("Saved", "Preferences successfully logged.", ui.window)
	})

	return container.NewVBox(
		widget.NewLabelWithStyle("2. Shift Preferences Selection Workspace", fyne.TextAlignLeading, fyne.TextStyle{Bold: true}),
		weekLabel,
		widget.NewSeparator(),
		widget.NewLabel("Active Staff Member:"),
		ui.empDropdown,
		widget.NewLabel("Availability Matrix:"),
		daySelect, pref1Select, pref2Select,
		saveBtn,
	)
}

// SCREEN 3: Handles the Weekly Master Schedule View using a 2-Column Calendar Card Layout
func (ui *AppUI) buildScheduleScreen() fyne.CanvasObject {
	noticeLabel := widget.NewLabel("")
	noticeLabel.Hide()

	targetWeek := ui.storage.GetTargetWeek()
	weekLabel := widget.NewLabelWithStyle(fmt.Sprintf("OFFICIAL MATRIX COMMENCING: %s", targetWeek.StartOfWeek.Format("2006-01-02")), fyne.TextAlignLeading, fyne.TextStyle{Italic: true})

	ui.calendarGrid.Objects = []fyne.CanvasObject{
		widget.NewLabel("No active schedule matrix compiled yet. Click the engine update button above to generate shifts."),
	}

	genBtn := widget.NewButton("Compile / Update Weekly Schedule", func() {
		// Rule Validation: Pop-up check if compilation triggers with empty roster pools
		if len(ui.storage.GetEmployees()) == 0 {
			dialog.ShowError(fmt.Errorf("no employee is inserted. Master scheduling operations cannot process an empty staff registry"), ui.window)
			return
		}

		sched, capacityExhausted := ui.scheduler.GenerateSchedule()
		hasUnassignedShifts := false

		if capacityExhausted {
			noticeLabel.SetText("⚠️ NOTICE: All existing employees have worked for 5 days and unassigned shifts could not be covered due to strict labor limits.")
			noticeLabel.Show()
		} else {
			noticeLabel.Hide()
		}

		ui.calendarGrid.Objects = nil

		for d := domain.Monday; d <= domain.Sunday; d++ {
			dayCardContent := container.NewVBox()
			dayHeader := widget.NewLabelWithStyle(fmt.Sprintf("📅 %s", d.String()), fyne.TextAlignLeading, fyne.TextStyle{Bold: true})
			dayCardContent.Add(dayHeader)
			dayCardContent.Add(widget.NewSeparator())

			for _, shift := range []domain.Shift{domain.Morning, domain.Afternoon, domain.Evening} {
				assignments := sched[d][shift]
				var lineText string
				if len(assignments) == 0 {
					lineText = "[UNASSIGNED/EMPTY]"
					hasUnassignedShifts = true // Track state to trigger italics footnote layout
				} else {
					var names []string
					for _, a := range assignments {
						names = append(names, a.EmployeeName)
					}
					lineText = strings.Join(names, ", ")
				}

				shiftRow := widget.NewLabel(fmt.Sprintf("• %s: %s", shift.String(), lineText))
				dayCardContent.Add(shiftRow)
			}

			dayCard := widget.NewCard("", "", dayCardContent)
			ui.calendarGrid.Add(dayCard)
		}

		// Condition: Add a small italics footnote mentioning requirements if any shift is left unassigned
		if hasUnassignedShifts {
			ui.footnoteLabel.SetText("* Number of employees does not satisfy requirements to fill all shifts as all employees are working 5 days.")
			ui.footnoteLabel.Show()
		} else {
			ui.footnoteLabel.Hide()
		}

		ui.calendarGrid.Refresh()
	})

	scrollContainer := container.NewVBox(ui.calendarGrid, ui.footnoteLabel)
	mainScroll := container.NewScroll(scrollContainer)
	mainScroll.SetMinSize(fyne.NewSize(600, 450))

	return container.NewBorder(
		container.NewVBox(
			widget.NewLabelWithStyle("3. Master Weekly Schedule Board", fyne.TextAlignLeading, fyne.TextStyle{Bold: true}),
			weekLabel,
			genBtn,
			noticeLabel,
			widget.NewSeparator(),
		),
		nil, nil, nil,
		mainScroll,
	)
}

func (ui *AppUI) refreshEmployeeDropdown() {
	if ui.empDropdown == nil {
		return
	}
	employees := ui.storage.GetEmployees()
	var options []string
	for _, e := range employees {
		options = append(options, fmt.Sprintf("%s (%s)", e.ID, e.Name))
	}
	ui.empDropdown.Options = options
	ui.empDropdown.Refresh()
}
