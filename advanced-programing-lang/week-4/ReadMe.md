# Automated Employee Shift Scheduler

A cross-platform desktop application designed to manage, prioritize, and compile weekly employee work schedules under strict compliance limits. This repository contains two independent, functionally identical implementations of the architecture:
1. **Java 17+ / JavaFX** (Object-Oriented, Type-Safe implementation)
2. **Go / Fyne UI** (Compiled, Resource-Efficient implementation)

# 🏗️ System Architecture Pattern

Both versions follow a strict **Decoupled Layered Architecture Pattern** to separate the core business constraints from the layout rendering engine.

* **Domain Layer:** Holds pure data types (Employee, Shift, Day, Preference) free from framework dependencies.
* **Repository Layer (`MemoryStorage`):** Acts as an in-memory, thread-safe single source of truth.
* **Service Layer (`SchedulerService`):** Orchestrates sorting, checks scheduling validation, and handles fallback allocations.
* **Presentation Layer:** Implements a sidebar navigation UI, a 2-column calendar grid layout, and front-end empty-state pop-up interceptors.


# 🏗️ Run java
Go to week-4/java/employee-schedule-app and run the following commands:

```bash     
mvn clean compile
mvn javafx:run
```

# 🏗️ Run go
Go to week-4/go/employee-schedule-app and run the following commands

```bash
 go run app/main.go  
 ```