import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

// =======================================================
// RegisterScreen.java
// Simple registration form for new students.
// =======================================================
public class RegisterScreen extends JFrame implements ActionListener {

    // Swing GUI Components
    JTextField rollField;
    JTextField nameField;
    JComboBox<String> deptBox;
    JPasswordField passField;
    JButton registerButton;
    JButton backButton;

    // Constructor to build UI
    public RegisterScreen() {
        setTitle("Student Registration");
        setSize(380, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Simple grid layout: 5 rows, 2 columns
        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));

        panel.add(new JLabel(" Roll Number:"));
        rollField = new JTextField();
        panel.add(rollField);

        panel.add(new JLabel(" Full Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel(" Department:"));
        String[] depts = {"CS", "CA", "ME", "CC", "ER", "EE", "EC", "CE"};
        deptBox = new JComboBox<>(depts);
        panel.add(deptBox);

        panel.add(new JLabel(" Password:"));
        passField = new JPasswordField();
        panel.add(passField);

        registerButton = new JButton("Register");
        registerButton.addActionListener(this);
        panel.add(registerButton);

        backButton = new JButton("Back to Login");
        backButton.addActionListener(this);
        panel.add(backButton);

        add(panel);
        setVisible(true);
    }

    // Action listener to handle button clicks
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerButton) {
            registerStudent();
        } else if (e.getSource() == backButton) {
            new LoginScreen();
            dispose();
        }
    }

    // Method to insert student record into MySQL database
    public void registerStudent() {
        String roll = rollField.getText().trim();
        String name = nameField.getText().trim();
        String dept = (String) deptBox.getSelectedItem();
        String pass = new String(passField.getPassword()).trim();

        // Simple validation
        if (roll.isEmpty() || name.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        try {
            Connection con = DBConnect.getConnection();
            if (con == null) return;

            // JDBC PreparedStatement for inserting data
            String query = "INSERT INTO students (roll_no, name, dept, password) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, roll);
            ps.setString(2, name);
            ps.setString(3, dept);
            ps.setString(4, pass);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Registration Successful! Please login now.");
                new LoginScreen();
                dispose(); // Close registration window
            }

            ps.close();
            con.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: Roll number might already exist or DB error.");
            System.out.println("Registration Error: " + ex.getMessage());
        }
    }
}
