import java.sql.*;
import java.util.logging.Logger;

public class ExpenseTracker {
    private static final Logger logger = Logger.getLogger(ExpenseTracker.class.getName());
    private static Connection conn;

    // Initialize Database
    public static void initDB() {
        try {
            logger.info("Loading JDBC driver...");
            Class.forName("org.sqlite.JDBC");
            logger.info("JDBC driver loaded successfully.");

            // Connect to the SQLite database (creates it if it doesn't exist)
            conn = DriverManager.getConnection("jdbc:sqlite:ExpensesDB.db");
            logger.info("Database connected successfully.");

            try (Statement statement = conn.createStatement()) {
                // Create 'accounts' table if it doesn't exist
                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS accounts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE);"
                );

                // Create 'expenses' table if it doesn't exist
                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "account_id INTEGER NOT NULL, " +
                    "date TEXT NOT NULL, " +
                    "description TEXT NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "FOREIGN KEY (account_id) REFERENCES accounts(id) " +
                    "ON DELETE CASCADE ON UPDATE CASCADE);"
                );
            }
        } catch (ClassNotFoundException e) {
            logger.severe("JDBC driver not found. Please ensure the SQLite JDBC driver is in the classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            logger.severe("Database connection or table creation failed. Details:");
            e.printStackTrace();
        }
    }

    // Insert a new expense into the database
    public static void addExpense(int accountId, String date, String description, double amount) {
        String sql = "INSERT INTO expenses (account_id, date, description, amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setString(2, date);
            pstmt.setString(3, description);
            pstmt.setDouble(4, amount);
            pstmt.executeUpdate();
            logger.info("Expense added successfully!");
        } catch (SQLException e) {
            logger.severe("Failed to add expense: " + e.getMessage());
        }
    }

    // Fetch expenses for a given account
    public static void fetchExpensesByAccount(int accountId) {
        String sql = "SELECT * FROM expenses WHERE account_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String date = rs.getString("date");
                    String description = rs.getString("description");
                    double amount = rs.getDouble("amount");
                    System.out.println("Expense ID: " + id + ", Date: " + date + ", Description: " + description + ", Amount: " + amount);
                }
            }
        } catch (SQLException e) {
            logger.severe("Error fetching expenses: " + e.getMessage());
        }
    }

    // Fetch total expenses per account
    public static void generateExpenseReport() {
        String sql = "SELECT account_id, SUM(amount) AS total FROM expenses GROUP BY account_id";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int accountId = rs.getInt("account_id");
                double total = rs.getDouble("total");
                System.out.println("Account ID: " + accountId + " | Total Expense: " + total);
            }
        } catch (SQLException e) {
            logger.severe("Error generating report: " + e.getMessage());
        }
    }

    // Method to perform database transaction
    public static void transferFunds(int fromAccountId, int toAccountId, double amount) throws SQLException {
        String debitSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
        String creditSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";

        try {
            conn.setAutoCommit(false); // Disable auto-commit to manage the transaction manually

            try (PreparedStatement debitStmt = conn.prepareStatement(debitSql);
                 PreparedStatement creditStmt = conn.prepareStatement(creditSql)) {

                // Debit from the first account
                debitStmt.setDouble(1, amount);
                debitStmt.setInt(2, fromAccountId);
                int rowsDebited = debitStmt.executeUpdate();

                // Credit to the second account
                creditStmt.setDouble(1, amount);
                creditStmt.setInt(2, toAccountId);
                int rowsCredited = creditStmt.executeUpdate();

                if (rowsDebited > 0 && rowsCredited > 0) {
                    conn.commit(); // Commit the transaction if both operations succeed
                    logger.info("Funds transferred successfully!");
                } else {
                    conn.rollback(); // Rollback if any of the operations fail
                    logger.warning("Transaction failed. Rolling back changes.");
                }
            } catch (SQLException e) {
                conn.rollback(); // Rollback in case of any exception during the transaction
                throw e;
            }
        } finally {
            conn.setAutoCommit(true); // Restore auto-commit mode
        }
    }

    // Main method to start the tracker
    public static void main(String[] args) {
        logger.info("Starting Expense Tracker...");
        initDB();  // Ensure the database and tables are created

        // Example usage
        addExpense(1, "2024-12-14", "Lunch at Restaurant", 20.50);
        addExpense(1, "2024-12-14", "Groceries", 45.75);
        fetchExpensesByAccount(1);

        // Generate expense report
        generateExpenseReport();

        // Example transaction: transfer funds between two accounts
        try {
            transferFunds(1, 2, 100.00);
        } catch (SQLException e) {
            logger.severe("Transaction failed: " + e.getMessage());
        }
    }
}
