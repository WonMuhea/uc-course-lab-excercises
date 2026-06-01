package main

import (
	"fmt"
	"strings"

	"fyne.io/fyne/v2"
	"fyne.io/fyne/v2/app"
	"fyne.io/fyne/v2/container"
	"fyne.io/fyne/v2/dialog"
	"fyne.io/fyne/v2/theme"
	"fyne.io/fyne/v2/widget"

	"employee-schedule-app/internal/domain"
	"employee-schedule-app/internal/repository"
	"employee-schedule-app/internal/service"
)

type AppUI struct {
	storage       *repository.MemoryStorage
	scheduler     *service.SchedulerService
	window        fyne.Window
	rightSpace    *fyne.Container
	calendarGrid  *fyne.Container
	footnoteLabel *widget.Label
	noticeLabel   *widget.Label

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
		noticeLabel:   widget.NewLabel(""),
	}

	ui.footnoteLabel.TextStyle = fyne.TextStyle{Italic: true}
	ui.footnoteLabel.Hide()
	ui.noticeLabel.Hide()

	w.SetContent(ui.buildSidebarNavigationLayout())
	w.ShowAndRun()
}

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
		ui.refreshScheduleBoardView()
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

func (ui *AppUI) buildRegistrationScreen() fyne.CanvasObject {
	idEntry := widget.NewEntry()
	idEntry.SetPlaceHolder("e.g. EMP001")
	nameEntry := widget.NewEntry()
	nameEntry.SetPlaceHolder("Employee Full Name")

	var loadDefaultsBtn *widget.Button

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

			ui.refreshEmployeeDropdown()
			dialog.ShowInformation("Success", "Employee registered successfully!", ui.window)
			idEntry.SetText("")
			nameEntry.SetText("")
		},
	}

	// FEATURE 1: Load defaults now self-deactivates smoothly on completion
	loadDefaultsBtn = widget.NewButtonWithIcon("Load Default Roster (6 Employees)", theme.ContentAddIcon(), func() {
		defaultStaff := []struct {
			id   string
			name string
		}{
			{"EMP001", "Alice Smith"},
			{"EMP002", "Bob Jones"},
			{"EMP003", "Charlie Brown"},
			{"EMP004", "Diana Prince"},
			{"EMP005", "Evan Wright"},
			{"EMP006", "Fiona Gallagher"},
		}

		insertedCount := 0
		for _, staff := range defaultStaff {
			emp := domain.NewEmployee(staff.id, staff.name)
			if err := ui.storage.RegisterEmployee(emp); err == nil {
				insertedCount++
			}
		}

		ui.refreshEmployeeDropdown()

		if insertedCount > 0 {
			dialog.ShowInformation("Roster Loaded", fmt.Sprintf("Successfully seeded %d default employees into the system!", insertedCount), ui.window)
			loadDefaultsBtn.Disable() // Deactivate button widget states immediately
		} else {
			dialog.ShowInformation("Notice", "Default employees are already registered.", ui.window)
			loadDefaultsBtn.Disable()
		}
	})

	return container.NewVBox(
		widget.NewLabelWithStyle("1. Employee Registration Panel", fyne.TextAlignLeading, fyne.TextStyle{Bold: true}),
		widget.NewSeparator(),
		form,
		widget.NewSeparator(),
		widget.NewLabelWithStyle("Development Shortcuts:", fyne.TextAlignLeading, fyne.TextStyle{Italic: true}),
		loadDefaultsBtn,
	)
}

func (ui *AppUI) buildPreferencesScreen() fyne.CanvasObject {
	ui.empDropdown = widget.NewSelect([]string{}, func(value string) {})
	ui.empDropdown.PlaceHolder = "Choose Employee"
	ui.refreshEmployeeDropdown()

	ui.empDropdown.OnChanged = func(value string) {
		if len(ui.scheduler.GetEmployees()) == 0 {
			dialog.ShowError(fmt.Errorf("no employee is inserted. Please register staff members first"), ui.window)
			ui.empDropdown.ClearSelected()
			return
		}
	}

	targetWeek := ui.scheduler.GetTargetWeek()
	weekLabel := widget.NewLabel(fmt.Sprintf("Target Shift Week Window: %s", targetWeek.StartOfWeek.Format("2006-01-02")))
	weekLabel.TextStyle = fyne.TextStyle{Italic: true}

	daySelect := widget.NewSelect([]string{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}, func(string) {})
	pref1Select := widget.NewSelect([]string{"Morning", "Afternoon", "Evening"}, func(string) {})
	pref2Select := widget.NewSelect([]string{"Morning", "Afternoon", "Evening"}, func(string) {})

	daySelect.PlaceHolder = "Select Target Day"
	pref1Select.PlaceHolder = "1st Shift Preference Tier"
	pref2Select.PlaceHolder = "2nd Shift Preference Tier (Optional)"

	saveBtn := widget.NewButton("Save Preferred Selections", func() {
		if len(ui.scheduler.GetEmployees()) == 0 {
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

		targetDay := dayMap[daySelect.Selected]
		targetShift := shiftMap[pref1Select.Selected]

		currentSchedule := ui.scheduler.GetWeeklySchedule()

		assignedDay := targetDay
		assignedShift := targetShift
		wasRedirected := false

		// FEATURE 2: Advanced Cascading Rules with Next-Day Work Guards
		if len(currentSchedule[targetDay][targetShift]) >= 2 {
			wasRedirected = true
			foundAlternative := false

			// Rule A: Try alternative open shifts on the SAME DAY
			for _, altShift := range []domain.Shift{domain.Morning, domain.Afternoon, domain.Evening} {
				if altShift != targetShift && len(currentSchedule[targetDay][altShift]) < 2 {
					assignedDay = targetDay
					assignedShift = altShift
					foundAlternative = true
					break
				}
			}

			// Rule B: Cascade to the next available shift on the NEXT DAY (with strict conditional guards)
			if !foundAlternative {
				nextDay := (targetDay + 1) % 7

				// Look ahead: Check if they have an assignment locked in for the next day already
				var existingNextDayShift domain.Shift
				hasExistingNextDayAssignment := false

				for _, sft := range []domain.Shift{domain.Morning, domain.Afternoon, domain.Evening} {
					for _, assign := range currentSchedule[nextDay][sft] {
						if assign.EmployeeID == empID {
							existingNextDayShift = sft
							hasExistingNextDayAssignment = true
							break
						}
					}
					if hasExistingNextDayAssignment {
						break
					}
				}

				// Look for open slots on the next day
				for _, altShift := range []domain.Shift{domain.Morning, domain.Afternoon, domain.Evening} {
					if len(currentSchedule[nextDay][altShift]) < 2 {
						// Guard Check: If they are working later that day, can they take this earlier alternative slot?
						if hasExistingNextDayAssignment {
							if altShift < existingNextDayShift {
								assignedDay = nextDay
								assignedShift = altShift
								foundAlternative = true
								break
							}
							// If the available shift falls at or after their existing assignment, skip it.
							continue
						}

						// No pre-existing next day shift found, take the open slot safely
						assignedDay = nextDay
						assignedShift = altShift
						foundAlternative = true
						break
					}
				}
			}

			// Blocked Path: If no alternative shifts could satisfy the constraints, do nothing and alert the user.
			if !foundAlternative {
				msg := fmt.Sprintf("Conflict detected: The selected shift is full, and fallback options are blocked because this employee is already scheduled for assignments on the following day.")
				dialog.ShowInformation("Routing Blocked", msg, ui.window)
				return
			}
		}

		prefs := []domain.Preference{
			{Day: assignedDay, Shift: assignedShift, Priority: 1},
		}
		if pref2Select.Selected != "" && pref2Select.Selected != pref1Select.Selected {
			prefs = append(prefs, domain.Preference{Day: assignedDay, Shift: shiftMap[pref2Select.Selected], Priority: 2})
		}

		ui.scheduler.SavePreferences(empID, prefs)

		// Regenerate live schedule allocations in memory
		_, _ = ui.scheduler.GenerateSchedule()

		if wasRedirected {
			msg := fmt.Sprintf("Notice: Your requested shift was full (max 2 workers reached).\n\nYou have been automatically routed to the next available slot:\n📅 %s - 🕒 %s Shift", assignedDay.String(), assignedShift.String())
			dialog.ShowInformation("Shift Redirected", msg, ui.window)
		} else {
			dialog.ShowInformation("Saved", "Preferences successfully logged and live schedule compiled!", ui.window)
		}
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

func (ui *AppUI) buildScheduleScreen() fyne.CanvasObject {
	targetWeek := ui.scheduler.GetTargetWeek()
	weekLabel := widget.NewLabelWithStyle(fmt.Sprintf("OFFICIAL MATRIX COMMENCING: %s", targetWeek.StartOfWeek.Format("2006-01-02")), fyne.TextAlignLeading, fyne.TextStyle{Italic: true})

	ui.calendarGrid.Objects = []fyne.CanvasObject{
		widget.NewLabel("Loading schedule board views..."),
	}

	refreshBtn := widget.NewButtonWithIcon("Refresh Roster Matrix", theme.ViewRefreshIcon(), func() {
		ui.refreshScheduleBoardView()
	})

	scrollContainer := container.NewVBox(ui.calendarGrid, ui.footnoteLabel)
	mainScroll := container.NewScroll(scrollContainer)
	mainScroll.SetMinSize(fyne.NewSize(600, 450))

	return container.NewBorder(
		container.NewVBox(
			widget.NewLabelWithStyle("3. Master Weekly Schedule Board", fyne.TextAlignLeading, fyne.TextStyle{Bold: true}),
			weekLabel,
			refreshBtn,
			ui.noticeLabel,
			widget.NewSeparator(),
		),
		nil, nil, nil,
		mainScroll,
	)
}

func (ui *AppUI) refreshScheduleBoardView() {
	sched := ui.scheduler.GetWeeklySchedule()
	employees := ui.scheduler.GetEmployees()

	if len(employees) == 0 {
		ui.calendarGrid.Objects = []fyne.CanvasObject{widget.NewLabel("No active schedule matrix compiled yet. Please register staff registry pools first.")}
		ui.calendarGrid.Refresh()
		return
	}

	workDaysCount := make(map[string]int)
	for d := domain.Monday; d <= domain.Sunday; d++ {
		for _, shift := range []domain.Shift{domain.Morning, domain.Afternoon, domain.Evening} {
			for _, assign := range sched[d][shift] {
				workDaysCount[assign.EmployeeID]++
			}
		}
	}

	allCapReached := true
	for _, emp := range employees {
		if workDaysCount[emp.ID] < 5 {
			allCapReached = false
			break
		}
	}

	hasUnassignedShifts := false
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
				hasUnassignedShifts = true
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

	if allCapReached && hasUnassignedShifts {
		ui.noticeLabel.SetText("⚠️ NOTICE: All existing employees have worked for 5 days and unassigned shifts could not be covered due to strict labor limits.")
		ui.noticeLabel.Show()
	} else {
		ui.noticeLabel.Hide()
	}

	if hasUnassignedShifts {
		ui.footnoteLabel.SetText("* Number of employees does not satisfy requirements to fill all shifts as all employees are working 5 days.")
		ui.footnoteLabel.Show()
	} else {
		ui.footnoteLabel.Hide()
	}

	ui.calendarGrid.Refresh()
}

func (ui *AppUI) refreshEmployeeDropdown() {
	if ui.empDropdown == nil {
		return
	}
	employees := ui.scheduler.GetEmployees()
	var options []string
	for _, e := range employees {
		options = append(options, fmt.Sprintf("%s (%s)", e.ID, e.Name))
	}
	ui.empDropdown.Options = options
	ui.empDropdown.Refresh()
}
