import java.awt.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class ExpenseTrackerGUI {
    private JFrame frame;
    private JTextField nameField, dateField, descField, amountField;
    private JComboBox<String> accountBox, categoryBox;
    private JTable expenseTable;
    private JLabel totalLabel;
    private Connection conn;

    public ExpenseTrackerGUI() {
        initDB(); // Initialize the database

        // Create main frame
        frame = new JFrame("Expense Tracker");
        frame.setSize(1000, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(245, 245, 245)); // Light gray background
        frame.setResizable(true);

        Font headerFont = new Font("Arial", Font.BOLD, 16);
        Font labelFont = new Font("Arial", Font.PLAIN, 14);

        // Section 1: Left Panel for Add Expense and Add Account
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setPreferredSize(new Dimension(400, 700));
        leftPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Add Expense", TitledBorder.LEFT, TitledBorder.TOP, headerFont, Color.BLUE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Account Field
        JLabel accountLabel = new JLabel("Account:");
        accountLabel.setFont(labelFont);
        gbc.gridy = 0;
        gbc.gridx = 0;
        leftPanel.add(accountLabel, gbc);

        accountBox = new JComboBox<>();
        updateAccountBox();
        gbc.gridx = 1;
        leftPanel.add(accountBox, gbc);

        JButton addAccountButton = new JButton("Add Account");
        addAccountButton.setBackground(new Color(30, 144, 255));
        addAccountButton.setForeground(Color.WHITE);
        addAccountButton.setPreferredSize(new Dimension(180, 40));
        gbc.gridy = 1;
        gbc.gridx = 1;
        leftPanel.add(addAccountButton, gbc);

        // Delete Account Button
        JButton deleteAccountButton = new JButton("Delete Account");
        deleteAccountButton.setBackground(new Color(255, 69, 0));
        deleteAccountButton.setForeground(Color.WHITE);
        deleteAccountButton.setPreferredSize(new Dimension(180, 40));
        gbc.gridy = 2;
        gbc.gridx = 1;
        leftPanel.add(deleteAccountButton, gbc);

        // Date Field
        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        dateLabel.setFont(labelFont);
        gbc.gridy = 3;
        gbc.gridx = 0;
        leftPanel.add(dateLabel, gbc);

        dateField = new JTextField(LocalDate.now().toString(), 15);
        gbc.gridx = 1;
        leftPanel.add(dateField, gbc);

        // Description Field
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(labelFont);
        gbc.gridy = 4;
        gbc.gridx = 0;
        leftPanel.add(descLabel, gbc);

        descField = new JTextField(20);
        gbc.gridx = 1;
        leftPanel.add(descField, gbc);

        // Amount Field
        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(labelFont);
        gbc.gridy = 5;
        gbc.gridx = 0;
        leftPanel.add(amountLabel, gbc);

        amountField = new JTextField(15);
        gbc.gridx = 1;
        leftPanel.add(amountField, gbc);

        // Category Field
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(labelFont);
        gbc.gridy = 6;
        gbc.gridx = 0;
        leftPanel.add(categoryLabel, gbc);

        categoryBox = new JComboBox<>(new String[]{"Food", "Transport", "Bills", "Entertainment"});
        gbc.gridx = 1;
        leftPanel.add(categoryBox, gbc);

        // Add Expense Button
        JButton addExpenseButton = new JButton("Add Expense");
        addExpenseButton.setPreferredSize(new Dimension(180, 40));
        addExpenseButton.setBackground(new Color(60, 179, 113));
        addExpenseButton.setForeground(Color.WHITE);
        gbc.gridy = 7;
        gbc.gridx = 1;
        leftPanel.add(addExpenseButton, gbc);

        // Reset Form Button
        JButton resetFormButton = new JButton("Reset Form");
        resetFormButton.setPreferredSize(new Dimension(180, 40));
        resetFormButton.setBackground(new Color(255, 69, 0));
        resetFormButton.setForeground(Color.WHITE);
        gbc.gridy = 8;
        gbc.gridx = 1;
        leftPanel.add(resetFormButton, gbc);

        // Section 2: Right Panel for Table and Summary
        JPanel tablePanel = new JPanel(new BorderLayout(10, 10));

        // Table for displaying expenses
        expenseTable = new JTable(new DefaultTableModel(new Object[][]{}, new String[]{"Date", "Description", "Amount", "Category"}));
        JScrollPane scrollPane = new JScrollPane(expenseTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Summary panel
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel totalTextLabel = new JLabel("Total Expense:");
        totalTextLabel.setFont(labelFont);
        summaryPanel.add(totalTextLabel);

        totalLabel = new JLabel("0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryPanel.add(totalLabel);

        JButton exportButton = new JButton("Export to CSV");
        exportButton.setBackground(new Color(70, 130, 180));
        exportButton.setForeground(Color.WHITE);
        summaryPanel.add(exportButton);

        JButton deleteExpenseButton = new JButton("Delete Expense");
        deleteExpenseButton.setBackground(new Color(255, 69, 0));
        deleteExpenseButton.setForeground(Color.WHITE);
        summaryPanel.add(deleteExpenseButton);

        tablePanel.add(summaryPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, tablePanel);
        splitPane.setDividerLocation(400);
        frame.add(splitPane, BorderLayout.CENTER);

        frame.setVisible(true);

        // Add listeners
        addExpenseButton.addActionListener(e -> addExpense());
        resetFormButton.addActionListener(e -> resetForm());
        exportButton.addActionListener(e -> exportToCSV());
        deleteExpenseButton.addActionListener(e -> deleteExpense());
        addAccountButton.addActionListener(e -> addAccount());
        deleteAccountButton.addActionListener(e -> deleteAccount());
        accountBox.addActionListener(e -> updateTable());
    }

    // Database initialization (same as your existing code)
    private void initDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:ExpensesDB.db");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS accounts (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT);");
                stmt.execute("CREATE TABLE IF NOT EXISTS expenses (id INTEGER PRIMARY KEY AUTOINCREMENT, account_id INTEGER, date TEXT, description TEXT, amount REAL, category TEXT, FOREIGN KEY (account_id) REFERENCES accounts(id));");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error initializing database: " + e.getMessage());
        }
    }

    // Add account logic
    private void addAccount() {
        String accountName = JOptionPane.showInputDialog(frame, "Enter new account name:");
        if (accountName != null && !accountName.trim().isEmpty()) {
            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO accounts (name) VALUES (?)")) {
                stmt.setString(1, accountName.trim());
                stmt.executeUpdate();
                updateAccountBox();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(frame, "Error adding account: " + e.getMessage());
            }
        }
    }

    // Update account combo box
    private void updateAccountBox() {
        accountBox.removeAllItems();
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT name FROM accounts")) {
            while (rs.next()) {
                accountBox.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error loading accounts: " + e.getMessage());
        }
    }

    // Add expense logic
    private void addExpense() {
        String date = dateField.getText().trim();
        String desc = descField.getText().trim();
        String category = categoryBox.getSelectedItem().toString();
        String accountName = (String) accountBox.getSelectedItem();
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid amount.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO expenses (account_id, date, description, amount, category) VALUES ((SELECT id FROM accounts WHERE name = ?), ?, ?, ?, ?)")) {
            stmt.setString(1, accountName);
            stmt.setString(2, date);
            stmt.setString(3, desc);
            stmt.setDouble(4, amount);
            stmt.setString(5, category);
            stmt.executeUpdate();
            updateTable();
            resetForm();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error adding expense: " + e.getMessage());
        }
    }

    // Update the expense table after adding a new expense
    private void updateTable() {
        String selectedAccount = (String) accountBox.getSelectedItem();
        DefaultTableModel model = (DefaultTableModel) expenseTable.getModel();
        model.setRowCount(0); // Clear previous data

        try (PreparedStatement stmt = conn.prepareStatement("SELECT date, description, amount, category FROM expenses WHERE account_id = (SELECT id FROM accounts WHERE name = ?)")) {
            stmt.setString(1, selectedAccount);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("date"),
                        rs.getString("description"),
                        rs.getDouble("amount"),
                        rs.getString("category")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error fetching expenses: " + e.getMessage());
        }

        updateTotal();
    }

    // Update the total expense
    private void updateTotal() {
        String selectedAccount = (String) accountBox.getSelectedItem();
        double total = 0.0;

        try (PreparedStatement stmt = conn.prepareStatement("SELECT SUM(amount) FROM expenses WHERE account_id = (SELECT id FROM accounts WHERE name = ?)")) {
            stmt.setString(1, selectedAccount);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error calculating total: " + e.getMessage());
        }

        totalLabel.setText(String.format("%.2f", total));
    }

    // Reset form fields
    private void resetForm() {
        dateField.setText(LocalDate.now().toString());
        descField.setText("");
        amountField.setText("");
    }

    // Delete an expense
    private void deleteExpense() {
        int selectedRow = expenseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select an expense to delete.");
            return;
        }

        String date = (String) expenseTable.getValueAt(selectedRow, 0);
        String desc = (String) expenseTable.getValueAt(selectedRow, 1);

        try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM expenses WHERE date = ? AND description = ?")) {
            stmt.setString(1, date);
            stmt.setString(2, desc);
            stmt.executeUpdate();
            updateTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error deleting expense: " + e.getMessage());
        }
    }

    // Delete an account and its expenses
    private void deleteAccount() {
        String accountName = (String) accountBox.getSelectedItem();
        if (accountName == null) {
            JOptionPane.showMessageDialog(frame, "Please select an account to delete.");
            return;
        }

        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete the account '" + accountName + "'? All associated expenses will be deleted.", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Delete associated expenses first
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM expenses WHERE account_id = (SELECT id FROM accounts WHERE name = ?)")) {
                stmt.setString(1, accountName);
                stmt.executeUpdate();
            }

            // Now delete the account
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM accounts WHERE name = ?")) {
                stmt.setString(1, accountName);
                stmt.executeUpdate();
            }

            // Update the account list in the combo box
            updateAccountBox();
            JOptionPane.showMessageDialog(frame, "Account deleted successfully.");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(frame, "Error deleting account: " + e.getMessage());
        }
    }

    // Export expenses to a CSV file
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                DefaultTableModel model = (DefaultTableModel) expenseTable.getModel();
                for (int i = 0; i < model.getColumnCount(); i++) {
                    writer.write(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) writer.write(",");
                }
                writer.newLine();
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        writer.write(model.getValueAt(i, j).toString());
                        if (j < model.getColumnCount() - 1) writer.write(",");
                    }
                    writer.newLine();
                }
                JOptionPane.showMessageDialog(frame, "Export successful.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error exporting data: " + e.getMessage());
            }
        }
    }

    // Main method to launch the application
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ExpenseTrackerGUI::new);
    }
}
