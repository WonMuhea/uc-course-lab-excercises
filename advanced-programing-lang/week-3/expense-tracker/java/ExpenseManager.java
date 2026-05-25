import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the collection of Expense objects.
 * Demonstrates: ArrayList, Java Streams, filtering, sorting, and aggregation.
 */
public class ExpenseManager {

    // ── Storage ───────────────────────────────────────────────────────────────
    private final List<Expense> expenses = new ArrayList<>();

    // ── CRUD Operations ───────────────────────────────────────────────────────

    /** Add a new expense and return it. */
    public Expense addExpense(LocalDate date, double amount,
                              String category, String description) {
        Expense e = new Expense(date, amount, category, description);
        expenses.add(e);
        return e;
    }

    /** Remove an expense by its ID. Returns true if found and removed. */
    public boolean removeExpense(int id) {
        return expenses.removeIf(e -> e.getId() == id);
    }

    /** Find a single expense by ID; returns Optional (may be empty). */
    public Optional<Expense> findById(int id) {
        return expenses.stream()
                       .filter(e -> e.getId() == id)
                       .findFirst();
    }

    /** Return an unmodifiable view of all expenses. */
    public List<Expense> getAllExpenses() {
        return Collections.unmodifiableList(expenses);
    }

    // ── Filtering ─────────────────────────────────────────────────────────────

    /**
     * Filter by category (case-insensitive).
     * Returns a new list – the original is unchanged.
     */
    public List<Expense> filterByCategory(String category) {
        String target = category.trim().toLowerCase();
        return expenses.stream()
                       .filter(e -> e.getCategory().toLowerCase().equals(target))
                       .collect(Collectors.toList());
    }

    /**
     * Filter by date range [start, end] inclusive.
     * Pass null for either bound to make it open-ended.
     */
    public List<Expense> filterByDateRange(LocalDate start, LocalDate end) {
        return expenses.stream()
                       .filter(e -> (start == null || !e.getDate().isBefore(start)))
                       .filter(e -> (end   == null || !e.getDate().isAfter(end)))
                       .sorted(Comparator.comparing(Expense::getDate))
                       .collect(Collectors.toList());
    }

    /**
     * Filter by both category AND date range.
     */
    public List<Expense> filterByCategoryAndDateRange(String category,
                                                       LocalDate start,
                                                       LocalDate end) {
        String target = category.trim().toLowerCase();
        return expenses.stream()
                       .filter(e -> e.getCategory().toLowerCase().equals(target))
                       .filter(e -> (start == null || !e.getDate().isBefore(start)))
                       .filter(e -> (end   == null || !e.getDate().isAfter(end)))
                       .sorted(Comparator.comparing(Expense::getDate))
                       .collect(Collectors.toList());
    }

    /**
     * Search expenses whose description contains the keyword (case-insensitive).
     */
    public List<Expense> searchByDescription(String keyword) {
        String kw = keyword.trim().toLowerCase();
        return expenses.stream()
                       .filter(e -> e.getDescription().toLowerCase().contains(kw))
                       .collect(Collectors.toList());
    }

    // ── Summary / Aggregation ─────────────────────────────────────────────────

    /** Total of ALL expenses. */
    public double getTotalExpenses() {
        return expenses.stream()
                       .mapToDouble(Expense::getAmount)
                       .sum();
    }

    /** Total for a specific category (case-insensitive). */
    public double getTotalByCategory(String category) {
        String target = category.trim().toLowerCase();
        return expenses.stream()
                       .filter(e -> e.getCategory().toLowerCase().equals(target))
                       .mapToDouble(Expense::getAmount)
                       .sum();
    }

    /**
     * Returns a Map of category → total amount, sorted descending by value.
     * Uses Collectors.groupingBy + Collectors.summingDouble.
     */
    public Map<String, Double> getSummaryByCategory() {
        Map<String, Double> raw = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        // Sort by value descending using a LinkedHashMap
        return raw.entrySet().stream()
                  .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                  .collect(Collectors.toMap(
                          Map.Entry::getKey,
                          Map.Entry::getValue,
                          (a, b) -> a,
                          LinkedHashMap::new
                  ));
    }

    /** All distinct categories currently in the tracker. */
    public Set<String> getCategories() {
        return expenses.stream()
                       .map(Expense::getCategory)
                       .collect(Collectors.toCollection(TreeSet::new));
    }

    /** Number of expense records stored. */
    public int getCount() { return expenses.size(); }
}
