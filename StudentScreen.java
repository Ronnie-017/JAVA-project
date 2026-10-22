import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

// =======================================================
// StudentScreen.java
// Allows student to submit requests and view live status.
// =======================================================
public class StudentScreen extends JFrame implements ActionListener {

    // Student Information
    String studentRoll;
    String studentName;
    String studentDept;

    // GUI Form Components
    JComboBox<String> hodBox;
    JTextField requestField;
    JButton attachButton;
    JLabel pathLabel;
    JButton submitButton;
    String selectedImagePath = ""; // Stores path of selected image

    // Table Components to view status
    JTable table;
    DefaultTableModel tableModel;
    JButton refreshButton;
    JButton logoutButton;

    public StudentScreen(String roll, String name, String dept) {
        this.studentRoll = roll;
        this.studentName = name;
        this.studentDept = dept;

        setTitle("Student Dashboard - " + name + " (" + roll + ")");
        setSize(750, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // 1. Top Panel: Welcome Info
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Welcome, " + name + " | Roll No: " + roll + " | Dept: " + dept));
        add(topPanel, BorderLayout.NORTH);

        // 2. Request Form Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        formPanel.add(new JLabel(" Select HOD Department:"));
        String[] depts = {"CS", "CA", "ME", "CC", "ER", "EE", "EC", "CE"};
        hodBox = new JComboBox<>(depts);
        formPanel.add(hodBox);

        formPanel.add(new JLabel(" Permission Request:"));
        requestField = new JTextField();
        formPanel.add(requestField);

        attachButton = new JButton("Attach Image (Optional)");
        attachButton.addActionListener(this);
        formPanel.add(attachButton);

        pathLabel = new JLabel("No file selected");
        formPanel.add(pathLabel);

        submitButton = new JButton("Submit Request");
        submitButton.addActionListener(this);
        formPanel.add(submitButton);

        // Empty label to balance the grid
        formPanel.add(new JLabel(""));

        // 3. Center Panel: Table to display status of requests
        String[] columns = {"Token ID", "HOD Dept", "Request", "Image", "Status", "Faculty Remark", "HOD Remark"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // Group form and table in center container
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(formPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 4. Bottom Panel: Refresh and Logout Buttons
        JPanel bottomPanel = new JPanel();
        refreshButton = new JButton("Refresh Status");
        refreshButton.addActionListener(this);
        bottomPanel.add(refreshButton);

        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(this);
        bottomPanel.add(logoutButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // Load existing requests of this student
        loadStudentRequests();

        setVisible(true);
    }

    // Event Handling
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == attachButton) {
            chooseImage();
        } else if (e.getSource() == submitButton) {
            submitRequest();
        } else if (e.getSource() == refreshButton) {
            loadStudentRequests();
        } else if (e.getSource() == logoutButton) {
            new LoginScreen();
            dispose();
        }
    }

    // Opens file chooser to attach an image
    public void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            selectedImagePath = file.getAbsolutePath();
            pathLabel.setText(file.getName());
        }
    }

    // Saves request into MySQL database
    public void submitRequest() {
        String hodDept = (String) hodBox.getSelectedItem();
        String reqText = requestField.getText().trim();

        if (reqText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your permission request.");
            return;
        }

        try {
            Connection con = DBConnect.getConnection();
            if (con == null) return;

            String sql = "INSERT INTO requests (student_roll, student_name, hod_dept, request_text, image_path, status) " +
                         "VALUES (?, ?, ?, ?, ?, 'Pending Faculty Approval')";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, studentRoll);
            ps.setString(2, studentName);
            ps.setString(3, hodDept);
            ps.setString(4, reqText);
            ps.setString(5, selectedImagePath);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Request Submitted Successfully!");
                requestField.setText("");
                selectedImagePath = "";
                pathLabel.setText("No file selected");
                loadStudentRequests(); // Refresh table
            }

            ps.close();
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error submitting request: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Loads student's own requests from database into table
    public void loadStudentRequests() {
        tableModel.setRowCount(0); // Clear old rows
        try {
            Connection con = DBConnect.getConnection();
            if (con == null) return;

            String sql = "SELECT * FROM requests WHERE student_roll = ? ORDER BY token_id DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, studentRoll);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("token_id");
                String hod = rs.getString("hod_dept");
                String text = rs.getString("request_text");
                String img = (rs.getString("image_path") != null && !rs.getString("image_path").isEmpty()) ? "Yes" : "No";
                String status = rs.getString("status");
                String facRem = rs.getString("faculty_remark");
                String hodRem = rs.getString("hod_remark");

                tableModel.addRow(new Object[]{id, hod, text, img, status, facRem, hodRem});
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception ex) {
            System.out.println("Error loading requests: " + ex.getMessage());
        }
    }
}
