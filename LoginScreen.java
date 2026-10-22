import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

// =======================================================
// LoginScreen.java
// Main entry point for Student, Faculty, and HOD Login.
// =======================================================
public class LoginScreen extends JFrame implements ActionListener {

    // Swing GUI Components
    JComboBox<String> roleBox;
    JTextField userField;
    JPasswordField passField;
    JButton loginButton;
    JButton registerButton;

    // Constructor to build UI
    public LoginScreen() {
        setTitle("College Permission System - Login");
        setSize(400, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));

        panel.add(new JLabel(" Select Role:"));
        String[] roles = {"Student", "Faculty", "HOD"};
        roleBox = new JComboBox<>(roles);
        panel.add(roleBox);

        panel.add(new JLabel(" Username / Roll No:"));
        userField = new JTextField();
        panel.add(userField);

        panel.add(new JLabel(" Password:"));
        passField = new JPasswordField();
        panel.add(passField);

        loginButton = new JButton("Login");
        loginButton.addActionListener(this);
        panel.add(loginButton);

        registerButton = new JButton("New Student Register");
        registerButton.addActionListener(this);
        panel.add(registerButton);

        add(panel);
        setVisible(true);
    }

    // Action listener to handle button clicks
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            performLogin();
        } else if (e.getSource() == registerButton) {
            new RegisterScreen();
            dispose();
        }
    }

    // Method to verify credentials from MySQL database
    public void performLogin() {
        String role = (String) roleBox.getSelectedItem();
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Username and Password.");
            return;
        }

        try {
            Connection con = DBConnect.getConnection();
            if (con == null) return;

            if (role.equals("Student")) {
                // Check in students table
                String sql = "SELECT * FROM students WHERE roll_no = ? AND password = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, user);
                ps.setString(2, pass);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String studentName = rs.getString("name");
                    String studentDept = rs.getString("dept");
                    JOptionPane.showMessageDialog(this, "Welcome " + studentName + "!");
                    new StudentScreen(user, studentName, studentDept);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Student Roll Number or Password.");
                }
                rs.close();
                ps.close();

            } else if (role.equals("Faculty")) {
                // Check in faculty table (Only fac1, fac2, fac3)
                String sql = "SELECT * FROM faculty WHERE username = ? AND password = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, user);
                ps.setString(2, pass);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Welcome Faculty: " + user);
                    new FacultyScreen(user);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Faculty Credentials!\nOnly fac1, fac2, fac3 accounts exist.");
                }
                rs.close();
                ps.close();

            } else if (role.equals("HOD")) {
                // HOD usernames are the department codes (CS, CA, ME, CC, ER, EE, EC, CE)
                // Normalize username: allow "CS" or "cs" or "hod_cs"
                String deptCode = user.toUpperCase();
                if (deptCode.startsWith("HOD_")) {
                    deptCode = deptCode.substring(4);
                } else if (deptCode.startsWith("HOD")) {
                    deptCode = deptCode.substring(3);
                }

                String sql = "SELECT * FROM hod WHERE dept = ? AND password = ?";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, deptCode);
                ps.setString(2, pass);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Welcome HOD of Department: " + deptCode);
                    new HodScreen(deptCode);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid HOD Credentials!\nAllowed Departments: CS, CA, ME, CC, ER, EE, EC, CE");
                }
                rs.close();
                ps.close();
            }

            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Main method to run the project
    public static void main(String[] args) {
        // Automatically set up database tables if they do not exist
        DBConnect.setupDatabase();

        // Launch the Login Screen
        new LoginScreen();
    }
}
