import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class LibraryManagement extends JFrame {

    DefaultTableModel model;
    JTable table;
    JTextField txtTitle, txtAuthor, txtBookId;
    String role;

    JButton btnAdd, btnIssue, btnReturn, btnLogout;

    public LibraryManagement(String role) {

        this.role = role;

        setTitle("Library Management System");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===== Header =====
        JLabel heading = new JLabel(
                "Library Management System (" + role + ")", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setOpaque(true);
        heading.setBackground(Color.DARK_GRAY);
        heading.setForeground(Color.WHITE);
        heading.setPreferredSize(new Dimension(100, 50));
        add(heading, BorderLayout.NORTH);

        // ===== Table =====
        model = new DefaultTableModel(
                new String[]{"Book ID", "Title", "Author", "Status"}, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== Bottom Panel =====
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));

        // Add Book Panel
        JPanel addPanel = new JPanel();
        txtTitle = new JTextField(10);
        txtAuthor = new JTextField(10);
        btnAdd = new JButton("Add Book");

        addPanel.add(new JLabel("Title:"));
        addPanel.add(txtTitle);
        addPanel.add(new JLabel("Author:"));
        addPanel.add(txtAuthor);
        addPanel.add(btnAdd);

        // Issue / Return Panel
        JPanel issuePanel = new JPanel();
        txtBookId = new JTextField(5);
        btnIssue = new JButton("Issue");
        btnReturn = new JButton("Return");
        btnLogout = new JButton("Logout");

        issuePanel.add(new JLabel("Book ID:"));
        issuePanel.add(txtBookId);
        issuePanel.add(btnIssue);
        issuePanel.add(btnReturn);
        issuePanel.add(btnLogout);

        bottomPanel.add(addPanel);
        bottomPanel.add(issuePanel);
        add(bottomPanel, BorderLayout.SOUTH);

        // ===== Role Control =====
        if (role.equals("User")) {
            btnAdd.setEnabled(false);
        }

        // ===== Actions =====
        btnAdd.addActionListener(e -> addBook());
        btnIssue.addActionListener(e -> issueBook());
        btnReturn.addActionListener(e -> returnBook());
        btnLogout.addActionListener(e -> logout());

        loadBooks();
    }

    // ===== Load Books =====
    void loadBooks() {
        try {
            model.setRowCount(0);
            Connection con = DBConnection.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM books");

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("book_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Add Book =====
    void addBook() {
        String title = txtTitle.getText();
        String author = txtAuthor.getText();

        if (title.isEmpty() || author.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill all fields");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO books (title, author, status) VALUES (?, ?, 'Available')"
            );
            ps.setString(1, title);
            ps.setString(2, author);
            ps.executeUpdate();

            loadBooks();
            txtTitle.setText("");
            txtAuthor.setText("");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===== Issue Book =====
    void issueBook() {
        try {
            int id = Integer.parseInt(txtBookId.getText());
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE books SET status='Issued' WHERE book_id=?"
            );
            ps.setInt(1, id);

            if (ps.executeUpdate() == 0)
                JOptionPane.showMessageDialog(this, "Book ID not found");

            loadBooks();
            txtBookId.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid Book ID");
        }
    }

    // ===== Return Book =====
    void returnBook() {
        try {
            int id = Integer.parseInt(txtBookId.getText());
            Connection con = DBConnection.getConnection();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE books SET status='Available' WHERE book_id=?"
            );
            ps.setInt(1, id);

            if (ps.executeUpdate() == 0)
                JOptionPane.showMessageDialog(this, "Book ID not found");

            loadBooks();
            txtBookId.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid Book ID");
        }
    }

    // ===== Logout =====
    void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Do you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
