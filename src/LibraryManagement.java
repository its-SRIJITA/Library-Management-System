import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class LibraryManagement extends JFrame {

    int userId;
    String role;

    JTable table;
    DefaultTableModel model;

    JTextField txtTitle, txtAuthor, txtCopies, txtCopyId;

    JButton btnAdd, btnIssue, btnReturn, btnLogout;

    public LibraryManagement(int userId, String role) {
        this.userId = userId;
        this.role = role;

        setTitle("Library Management - " + role);
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        model = new DefaultTableModel(
            new String[]{"Book ID", "Title", "Author", "Copy ID", "Status"}, 0
        );

        table = new JTable(model);
        loadBooks();

        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        txtTitle = new JTextField();
        txtAuthor = new JTextField();
        txtCopies = new JTextField();
        txtCopyId = new JTextField();

        inputPanel.add(new JLabel("Title"));
        inputPanel.add(new JLabel("Author"));
        inputPanel.add(new JLabel("Copies"));
        inputPanel.add(new JLabel("Copy ID"));

        inputPanel.add(txtTitle);
        inputPanel.add(txtAuthor);
        inputPanel.add(txtCopies);
        inputPanel.add(txtCopyId);

        btnAdd = new JButton("Add Book");
        btnIssue = new JButton("Issue");
        btnReturn = new JButton("Return");
        btnLogout = new JButton("Logout");

        JPanel btnPanel = new JPanel();
        btnPanel.add(btnAdd);
        btnPanel.add(btnIssue);
        btnPanel.add(btnReturn);
        btnPanel.add(btnLogout);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> addBook());
        btnIssue.addActionListener(e -> issueBook());
        btnReturn.addActionListener(e -> returnBook());
        btnLogout.addActionListener(e -> logout());

        if (!role.equalsIgnoreCase("admin")) {
            btnAdd.setEnabled(false);
        }
    }

    void loadBooks() {
        model.setRowCount(0);

        String sql =
            """
            SELECT b.book_id, b.title, a.author_name,
                   bc.copy_id, bc.status
            FROM books b
            JOIN authors a ON b.author_id = a.author_id
            JOIN book_copies bc ON b.book_id = bc.book_id
            """;

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("book_id"),
                    rs.getString("title"),
                    rs.getString("author_name"),
                    rs.getInt("copy_id"),
                    rs.getString("status")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addBook() {
        String title = txtTitle.getText();
        String author = txtAuthor.getText();
        int copies = Integer.parseInt(txtCopies.getText());

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            int authorId;
            PreparedStatement ps = con.prepareStatement(
                "SELECT author_id FROM authors WHERE author_name=?"
            );
            ps.setString(1, author);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                authorId = rs.getInt(1);
            } else {
                ps = con.prepareStatement(
                    "INSERT INTO authors(author_name) VALUES(?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, author);
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                rs.next();
                authorId = rs.getInt(1);
            }

            ps = con.prepareStatement(
                "INSERT INTO books(title, author_id) VALUES(?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, title);
            ps.setInt(2, authorId);
            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            rs.next();
            int bookId = rs.getInt(1);

            ps = con.prepareStatement(
                "INSERT INTO book_copies(book_id) VALUES(?)"
            );

            for (int i = 0; i < copies; i++) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }

            con.commit();
            loadBooks();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void issueBook() {
        int copyId = Integer.parseInt(txtCopyId.getText());

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO transactions(copy_id, user_id, issue_date) VALUES(?,?,CURDATE())"
            );
            ps.setInt(1, copyId);
            ps.setInt(2, userId);
            ps.executeUpdate();

            ps = con.prepareStatement(
                "UPDATE book_copies SET status='ISSUED' WHERE copy_id=?"
            );
            ps.setInt(1, copyId);
            ps.executeUpdate();

            con.commit();
            loadBooks();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void returnBook() {
        int copyId = Integer.parseInt(txtCopyId.getText());

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            PreparedStatement ps = con.prepareStatement(
                "UPDATE transactions SET return_date=CURDATE() WHERE copy_id=? AND return_date IS NULL"
            );
            ps.setInt(1, copyId);
            ps.executeUpdate();

            ps = con.prepareStatement(
                "UPDATE book_copies SET status='AVAILABLE' WHERE copy_id=?"
            );
            ps.setInt(1, copyId);
            ps.executeUpdate();

            con.commit();
            loadBooks();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }
}
