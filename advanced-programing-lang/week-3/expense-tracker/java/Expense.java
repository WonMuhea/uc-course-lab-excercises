import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single expense entry.
 * Demonstrates Java OOP: encapsulation, constructors, and toString().
 */
public class Expense {

    // ── Fields ────────────────────────────────────────────────────────────────
    private static int nextId = 1;          // auto-increment ID counter

    private final int    id;
    private LocalDate    date;
    private double       amount;
    private String       category;
    private String       description;

    // ── Constructor ───────────────────────────────────────────────────────────
    public Expense(LocalDate date, double amount, String category, String description) {
        if (amount <= 0)            throw new IllegalArgumentException("Amount must be positive.");
        if (category == null || category.isBlank())
                                    throw new IllegalArgumentException("Category cannot be empty.");

        this.id          = nextId++;
        this.date        = date;
        this.amount      = amount;
        this.category    = category.trim();
        this.description = (description == null) ? "" : description.trim();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int       getId()          { return id; }
    public LocalDate getDate()        { return date; }
    public double    getAmount()      { return amount; }
    public String    getCategory()    { return category; }
    public String    getDescription() { return description; }

    public void setDate(LocalDate date)           { this.date = date; }
    public void setAmount(double amount)          {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive.");
        this.amount = amount;
    }
    public void setCategory(String category)      { this.category = category.trim(); }
    public void setDescription(String description){ this.description = description.trim(); }

    // ── Display ───────────────────────────────────────────────────────────────
    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return String.format("[%3d] %-12s  %-15s  $%8.2f  %s",
                id,
                date.format(fmt),
                category,
                amount,
                description);
    }
}
