import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * ExpenseTrackerApp – entry point and interactive console menu.
 *
 * Java concepts demonstrated:
 *  - OOP (classes, encapsulation, constructors)
 *  - Collections (ArrayList, Map, Set)
 *  - Streams & Lambdas (filtering, groupingBy, sorting)
 *  - java.time API (LocalDate, DateTimeFormatter)
 *  - Exception handling (try/catch, input validation)
 *  - Optional<T>
 */
public class ExpenseTrackerApp {

    // ── Shared state ──────────────────────────────────────────────────────────
    private static final ExpenseManager manager = new ExpenseManager();
    private static final Scanner        scanner  = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       JAVA EXPENSE TRACKER v1.0          ║");
        System.out.println("╚══════════════════════════════════════════╝");

        loadSampleData();   // pre-populate with demo entries

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");
            System.out.println();

            switch (choice) {
                case 1  -> addExpense();
                case 2  -> viewAllExpenses();
                case 3  -> filterByCategory();
                case 4  -> filterByDateRange();
                case 5  -> searchByDescription();
                case 6  -> showSummary();
                case 7  -> removeExpense();
                case 8  -> { System.out.println("Goodbye!"); running = false; }
                default -> System.out.println("  ✗ Invalid option. Try again.\n");
            }
        }
        scanner.close();
    }

    // ── Menu ──────────────────────────────────────────────────────────────────
    private static void printMenu() {
        System.out.println("┌─────────────────────────────────────┐");
        System.out.println("│              MAIN MENU              │");
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│  1. Add Expense                     │");
        System.out.println("│  2. View All Expenses               │");
        System.out.println("│  3. Filter by Category              │");
        System.out.println("│  4. Filter by Date Range            │");
        System.out.println("│  5. Search by Description           │");
        System.out.println("│  6. Show Summary Report             │");
        System.out.println("│  7. Remove Expense                  │");
        System.out.println("│  8. Exit                            │");
        System.out.println("└─────────────────────────────────────┘");
    }

    // ── Feature 1: Add Expense ────────────────────────────────────────────────
    private static void addExpense() {
        System.out.println("── Add New Expense ──────────────────────");

        LocalDate date = readDate("Date (MM/DD/YYYY) [blank = today]: ", true);
        double    amount   = readPositiveDouble("Amount: $");
        String    category = readNonBlank("Category (e.g. Food, Transport): ");
        System.out.print("Description (optional): ");
        String    desc     = scanner.nextLine().trim();

        Expense e = manager.addExpense(date, amount, category, desc);
        System.out.printf("  ✓ Expense #%d added successfully.%n%n", e.getId());
    }

    // ── Feature 2: View All ───────────────────────────────────────────────────
    private static void viewAllExpenses() {
        System.out.println("── All Expenses ─────────────────────────");
        List<Expense> all = manager.getAllExpenses();
        if (all.isEmpty()) {
            System.out.println("  (no expenses recorded yet)\n");
            return;
        }
        printHeader();
        all.forEach(System.out::println);
        System.out.printf("%nTotal (%d records): $%.2f%n%n",
                all.size(), manager.getTotalExpenses());
    }

    // ── Feature 3: Filter by Category ────────────────────────────────────────
    private static void filterByCategory() {
        System.out.println("── Filter by Category ───────────────────");
        System.out.println("  Available categories: " + manager.getCategories());
        String cat = readNonBlank("Enter category: ");

        List<Expense> results = manager.filterByCategory(cat);
        if (results.isEmpty()) {
            System.out.printf("  No expenses found for category '%s'.%n%n", cat);
            return;
        }
        printHeader();
        results.forEach(System.out::println);
        System.out.printf("%nSubtotal for '%s': $%.2f  (%d records)%n%n",
                cat, manager.getTotalByCategory(cat), results.size());
    }

    // ── Feature 4: Filter by Date Range ──────────────────────────────────────
    private static void filterByDateRange() {
        System.out.println("── Filter by Date Range ─────────────────");
        LocalDate start = readDate("Start date (MM/DD/YYYY) [blank = no limit]: ", true);
        LocalDate end   = readDate("End   date (MM/DD/YYYY) [blank = no limit]: ", true);

        List<Expense> results = manager.filterByDateRange(start, end);
        if (results.isEmpty()) {
            System.out.println("  No expenses found in that date range.\n");
            return;
        }
        printHeader();
        results.forEach(System.out::println);
        double subtotal = results.stream().mapToDouble(Expense::getAmount).sum();
        System.out.printf("%nSubtotal: $%.2f  (%d records)%n%n",
                subtotal, results.size());
    }

    // ── Feature 5: Search ─────────────────────────────────────────────────────
    private static void searchByDescription() {
        System.out.println("── Search by Description ────────────────");
        String kw = readNonBlank("Enter keyword: ");

        List<Expense> results = manager.searchByDescription(kw);
        if (results.isEmpty()) {
            System.out.printf("  No expenses matching '%s'.%n%n", kw);
            return;
        }
        printHeader();
        results.forEach(System.out::println);
        System.out.printf("%n%d result(s) found.%n%n", results.size());
    }

    // ── Feature 6: Summary Report ─────────────────────────────────────────────
    private static void showSummary() {
        System.out.println("══════════════════════════════════════════");
        System.out.println("           EXPENSE SUMMARY REPORT         ");
        System.out.println("══════════════════════════════════════════");

        if (manager.getCount() == 0) {
            System.out.println("  No data yet.\n");
            return;
        }

        Map<String, Double> summary = manager.getSummaryByCategory();
        double total = manager.getTotalExpenses();

        System.out.printf("%-20s  %10s  %6s%n", "CATEGORY", "AMOUNT", "SHARE");
        System.out.println("─".repeat(42));

        summary.forEach((cat, amt) -> {
            double pct = (total > 0) ? (amt / total * 100) : 0;
            System.out.printf("%-20s  $%9.2f  %5.1f%%%n", cat, amt, pct);
        });

        System.out.println("─".repeat(42));
        System.out.printf("%-20s  $%9.2f  100.0%%%n", "TOTAL", total);
        System.out.printf("%nRecords: %d%n%n", manager.getCount());
    }

    // ── Feature 7: Remove ─────────────────────────────────────────────────────
    private static void removeExpense() {
        System.out.println("── Remove Expense ───────────────────────");
        int id = readInt("Enter expense ID to remove: ");
        if (manager.removeExpense(id)) {
            System.out.printf("  ✓ Expense #%d removed.%n%n", id);
        } else {
            System.out.printf("  ✗ No expense with ID %d found.%n%n", id);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void printHeader() {
        System.out.printf("%-6s %-12s  %-15s  %-10s  %s%n",
                "ID", "DATE", "CATEGORY", "AMOUNT", "DESCRIPTION");
        System.out.println("─".repeat(65));
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  ✗ Please enter a whole number.");
            }
        }
    }

    private static double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(scanner.nextLine().trim());
                if (v > 0) return v;
                System.out.println("  ✗ Amount must be greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("  ✗ Please enter a valid number (e.g. 12.50).");
            }
        }
    }

    private static String readNonBlank(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            if (!s.isEmpty()) return s;
            System.out.println("  ✗ This field cannot be blank.");
        }
    }

    /**
     * Reads a date from the user.
     * @param allowBlank if true, an empty input returns null (open-ended bound)
     *                   or today's date when used for the "add expense" flow.
     */
    private static LocalDate readDate(String prompt, boolean allowBlank) {
        while (true) {
            System.out.print(prompt);
            String s = scanner.nextLine().trim();
            if (s.isEmpty() && allowBlank) return null;   // caller decides meaning
            if (s.isEmpty()) return LocalDate.now();
            try {
                return LocalDate.parse(s, DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("  ✗ Invalid date. Use MM/DD/YYYY (e.g. 05/15/2025).");
            }
        }
    }

    // ── Sample Data ───────────────────────────────────────────────────────────
    private static void loadSampleData() {
        manager.addExpense(LocalDate.of(2025, 5,  1),  12.50, "Food",      "Lunch at cafe");
        manager.addExpense(LocalDate.of(2025, 5,  3),  45.00, "Transport", "Monthly bus pass");
        manager.addExpense(LocalDate.of(2025, 5,  5), 120.00, "Utilities", "Electric bill");
        manager.addExpense(LocalDate.of(2025, 5,  8),   8.99, "Food",      "Grocery snacks");
        manager.addExpense(LocalDate.of(2025, 5, 10),  60.00, "Health",    "Gym membership");
        manager.addExpense(LocalDate.of(2025, 5, 12),  15.49, "Food",      "Dinner takeout");
        manager.addExpense(LocalDate.of(2025, 5, 15),  25.00, "Transport", "Uber rides");
        manager.addExpense(LocalDate.of(2025, 5, 18),  89.99, "Shopping",  "New headphones");
        manager.addExpense(LocalDate.of(2025, 5, 20),  10.00, "Health",    "Vitamins");
        manager.addExpense(LocalDate.of(2025, 5, 22),  55.00, "Utilities", "Internet bill");
        System.out.println("  ✓ 10 sample expenses loaded.\n");
    }
}
