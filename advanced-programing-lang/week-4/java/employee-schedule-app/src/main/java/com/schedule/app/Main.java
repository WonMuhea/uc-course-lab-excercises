package com.schedule.app;

import com.schedule.app.domain.*;
import com.schedule.app.repository.MemoryStorage;
import com.schedule.app.service.SchedulerService;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {
    private MemoryStorage storage;
    private SchedulerService schedulerService;
    
    private StackPane workspaceArea;
    private ComboBox<Employee> employeeComboBox;
    private GridPane calendarGrid;
    private Label noticeLabel;
    private Label footnoteLabel;

    @Override
    public void start(Stage primaryStage) {
        this.storage = new MemoryStorage();
        this.schedulerService = new SchedulerService(storage);

        primaryStage.setTitle("Employee Management Dashboard");

        BorderPane rootLayout = new BorderPane();
        workspaceArea = new StackPane();
        workspaceArea.getChildren().add(buildRegistrationScreen());

        VBox sidebar = new VBox(12);
        sidebar.setPadding(new Insets(15));
        sidebar.setStyle("-fx-background-color: #2F3542;");
        sidebar.setPrefWidth(240);

        Label sidebarTitle = new Label(" NAVIGATION MENU");
        sidebarTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        sidebarTitle.setStyle("-fx-text-fill: #FFFFFF;");

        Button navRegisterBtn = createSidebarButton("Employee Registration");
        Button navPrefBtn = createSidebarButton("Shift Selection");
        Button navSchedBtn = createSidebarButton("Weekly Schedule Board");

        navRegisterBtn.setOnAction(e -> switchWorkspace(buildRegistrationScreen()));
        navPrefBtn.setOnAction(e -> switchWorkspace(buildPreferencesScreen()));
        navSchedBtn.setOnAction(e -> switchWorkspace(buildScheduleScreen()));

        sidebar.getChildren().addAll(sidebarTitle, new Separator(), navRegisterBtn, navPrefBtn, navSchedBtn);

        rootLayout.setLeft(sidebar);
        rootLayout.setCenter(workspaceArea);

        Scene scene = new Scene(rootLayout, 1150, 720);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createSidebarButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.BASELINE_LEFT);
        btn.setStyle("-fx-background-color: #57606F; -fx-text-fill: white; -fx-cursor: hand;");
        return btn;
    }

    private void switchWorkspace(javafx.scene.Node panel) {
        workspaceArea.getChildren().clear();
        workspaceArea.getChildren().add(panel);
    }

    private boolean checkEmptyStorage() {
        if (storage.getEmployees().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "No employee is inserted. Please register staff members first.");
            return true;
        }
        return false;
    }

    // SCREEN 1: Employee Registration
    private VBox buildRegistrationScreen() {
        VBox view = new VBox(15);
        view.setPadding(new Insets(20));

        Label header = new Label("1. Employee Registration Panel");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        TextField idInput = new TextField();
        idInput.setPromptText("e.g. EMP001");
        TextField nameInput = new TextField();
        nameInput.setPromptText("Employee Full Name");

        formGrid.add(new Label("Employee ID:"), 0, 0);
        formGrid.add(idInput, 1, 0);
        formGrid.add(new Label("Full Name:"), 0, 1);
        formGrid.add(nameInput, 1, 1);

        Button submitBtn = new Button("Register New Staff");
        submitBtn.setOnAction(e -> {
            String id = idInput.getText().trim();
            String name = nameInput.getText().trim();

            if (id.isEmpty() || name.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "All field inputs are mandatory.");
                return;
            }

            try {
                storage.registerEmployee(new Employee(id, name));
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee registered successfully!");
                idInput.clear();
                nameInput.clear();
            } catch (IllegalArgumentException ex) {
                showAlert(Alert.AlertType.ERROR, "Registration Error", ex.getMessage());
            }
        });

        view.getChildren().addAll(header, new Separator(), formGrid, submitBtn);
        return view;
    }

    // SCREEN 2: Shift Preference Setup Selection Screen
    private VBox buildPreferencesScreen() {
        VBox view = new VBox(15);
        view.setPadding(new Insets(20));

        Label header = new Label("2. Shift Preferences Selection Workspace");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label weekLabel = new Label("Target Shift Week Window: " + storage.getTargetWeekStart());
        weekLabel.setStyle("-fx-font-style: italic;");

        employeeComboBox = new ComboBox<>();
        employeeComboBox.setPromptText("Choose Employee");
        employeeComboBox.getItems().addAll(storage.getEmployees());

        // INTERCEPT: Block dropdown usage if empty
        employeeComboBox.setOnMouseClicked(e -> checkEmptyStorage());

        ComboBox<Day> dayComboBox = new ComboBox<>();
        dayComboBox.setPromptText("Select Target Day");
        dayComboBox.getItems().addAll(Day.values());

        ComboBox<Shift> pref1Box = new ComboBox<>();
        pref1Box.setPromptText("1st Shift Preference Tier");
        pref1Box.getItems().addAll(Shift.values());

        ComboBox<Shift> pref2Box = new ComboBox<>();
        pref2Box.setPromptText("2nd Shift Preference Tier (Optional)");
        pref2Box.getItems().addAll(Shift.values());

        Button saveBtn = new Button("Save Preferred Selections");
        saveBtn.setOnAction(e -> {
            if (checkEmptyStorage()) return;

            Employee selectedEmp = employeeComboBox.getValue();
            Day selectedDay = dayComboBox.getValue();
            Shift p1 = pref1Box.getValue();
            Shift p2 = pref2Box.getValue();

            if (selectedEmp == null || selectedDay == null || p1 == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please complete required form selections.");
                return;
            }

            List<Preference> prefs = new ArrayList<>();
            prefs.add(new Preference(selectedDay, p1, 1));
            if (p2 != null && p2 != p1) {
                prefs.add(new Preference(selectedDay, p2, 2));
            }

            storage.savePreferences(selectedEmp.getId(), prefs);
            showAlert(Alert.AlertType.INFORMATION, "Saved", "Preferences successfully logged.");
        });

        view.getChildren().addAll(header, weekLabel, new Separator(), 
                new Label("Active Staff Member:"), employeeComboBox, 
                new Label("Availability Matrix:"), dayComboBox, pref1Box, pref2Box, saveBtn);
        return view;
    }

    // SCREEN 3: Master Schedule Grid Board (2 Days Per Row Layout)
    private VBox buildScheduleScreen() {
        VBox view = new VBox(15);
        view.setPadding(new Insets(20));

        Label header = new Label("3. Master Weekly Schedule Board");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label weekLabel = new Label("OFFICIAL MATRIX COMMENCING: " + storage.getTargetWeekStart());
        weekLabel.setStyle("-fx-font-style: italic;");

        noticeLabel = new Label("");
        noticeLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        noticeLabel.setManaged(false);
        noticeLabel.setVisible(false);

        footnoteLabel = new Label("* Number of employees does not satisfy requirements to fill all shifts as all employees are working 5 days.");
        footnoteLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #555555;");
        footnoteLabel.setManaged(false);
        footnoteLabel.setVisible(false);

        calendarGrid = new GridPane();
        calendarGrid.setHgap(15);
        calendarGrid.setVgap(15);
        calendarGrid.getChildren().add(new Label("No active schedule matrix compiled yet."));

        Button genBtn = new Button("Compile / Update Weekly Schedule");
        genBtn.setOnAction(e -> {
            if (checkEmptyStorage()) return;

            SchedulerService.SchedulingResult result = schedulerService.generateSchedule();
            WeeklySchedule sched = result.schedule();
            boolean hasUnassignedShifts = false;

            if (result.capacityExhausted()) {
                noticeLabel.setText("⚠️ NOTICE: All existing employees have worked for 5 days and unassigned shifts could not be covered due to strict labor limits.");
                noticeLabel.setManaged(true);
                noticeLabel.setVisible(true);
            } else {
                noticeLabel.setManaged(false);
                noticeLabel.setVisible(false);
            }

            calendarGrid.getChildren().clear();

            int currentColumn = 0;
            int currentRow = 0;

            for (Day day : Day.values()) {
                VBox dayCardContent = new VBox(5);
                dayCardContent.setPadding(new Insets(10));
                dayCardContent.setStyle("-fx-border-color: #BDC3C7; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-color: #FAFAFA;");
                dayCardContent.setPrefWidth(400);

                Label dayHeader = new Label("📅 " + day.toString());
                dayHeader.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                dayCardContent.getChildren().addAll(dayHeader, new Separator());

                for (Shift shift : Shift.values()) {
                    List<WeeklySchedule.ShiftAssignment> line = sched.getAssignments(day, shift);
                    String lineText;
                    if (line.isEmpty()) {
                        lineText = "[UNASSIGNED/EMPTY]";
                        hasUnassignedShifts = true;
                    } else {
                        lineText = line.stream().map(WeeklySchedule.ShiftAssignment::employeeName).collect(Collectors.joining(", "));
                    }
                    dayCardContent.getChildren().add(new Label("• " + shift + ": " + lineText));
                }

                // Enforce 2 calendar cards per row layout constraint
                calendarGrid.add(dayCardContent, currentColumn, currentRow);
                currentColumn++;
                if (currentColumn > 1) {
					currentColumn = 0;
					currentRow++;
                }
            }

            if (hasUnassignedShifts) {
                footnoteLabel.setManaged(true);
                footnoteLabel.setVisible(true);
            } else {
                footnoteLabel.setManaged(false);
                footnoteLabel.setVisible(false);
            }
        });

        ScrollPane scrollPane = new ScrollPane(new VBox(15, calendarGrid, footnoteLabel));
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(450);

        view.getChildren().addAll(header, weekLabel, genBtn, noticeLabel, new Separator(), scrollPane);
        return view;
    }

    private void showAlert(Alert.AlertType type, String title, String contents) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(contents);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}