import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

// =======================================================
// FacultyScreen.java
// Allows faculty to view pending student requests, inspect
// uploaded images, and approve (to HOD) or disapprove.
// Implements ApprovalInterface (demonstrating Module 3 OOP).
// =======================================================
public class FacultyScreen extends JFrame implements ActionListener, ApprovalInterface {

    String facultyUsername;

    // Table Components
    JTable table;
    DefaultTableModel tableModel;

    // Action Components
    JTextField remarkField;
    JButton viewImageButton;
    JButton approveButton;
    JButton disapproveButton;
    JButton refreshButton;
    JButton logoutButton;

    public FacultyScreen(String username) {
        this.facultyUsername = username;

        setTitle("Faculty Dashboard - Logged in as: " + username);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // 1. Top Panel: Welcome Label
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Faculty User: " + username + " | Pending Student Requests"));
        add(topPanel, BorderLayout.NORTH);

        // 2. Center Panel: Table showing pending requests
        String[] columns = {"Token ID", "Roll No", "Student Name", "HOD Dept", "Request Details", "Image Path"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. Bottom Panel: Actions (Remark field and Buttons)
        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        // Row 1: Remark Input
        JPanel remarkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        remarkPanel.add(new JLabel("Faculty Remark:"));
        remarkField = new JTextField(40);
        remarkPanel.add(remarkField);
        bottomPanel.add(remarkPanel);

        // Row 2: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        viewImageButton = new JButton("View Attached Image");
        viewImageButton.addActionListener(this);
        buttonPanel.add(viewImageButton);

        approveButton = new JButton("Approve (Forward to HOD)");
        approveButton.addActionListener(this);
        buttonPanel.add(approveButton);

        disapproveButton = new JButton("Disapprove (Reject)");
        disapproveButton.addActionListener(this);
        buttonPanel.add(disapproveButton);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(this);
        buttonPanel.add(refreshButton);

        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(this);
        buttonPanel.add(logoutButton);

        bottomPanel.add(buttonPanel);
        add(bottomPanel, BorderLayout.SOUTH);

        // Load data into table
        loadPendingRequests();

        setVisible(true);
    }

    // Event Handling
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == viewImageButton) {
            viewSelectedImage();
        } else if (e.getSource() == approveButton) {
            processAction(true);
        } else if (e.getSource() == disapproveButton) {
            processAction(false);
        } else if (e.getSource() == refreshButton) {
            loadPendingRequests();
        } else if (e.getSource() == logoutButton) {
            new LoginScreen();
            dispose();
        }
    }

    // Opens image for selected row
    public void viewSelectedImage() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request from the table first.");
            return;
        }

        String imagePath = (String) tableModel.getValueAt(row, 5);
        if (imagePath == null || imagePath.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No image was uploaded by student for this request.");
            return;
        }

        new ImageViewer(imagePath);
    }

    // Helper to get selected Token ID and trigger interface methods
    public void processAction(boolean isApproval) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request from the table first.");
            return;
        }

        int tokenId = (int) tableModel.getValueAt(row, 0);
        String remark = remarkField.getText().trim();

        if (!isApproval && remark.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please write a remark explaining why you are disapproving.");
            return;
        }

        if (isApproval) {
            approveRequest(tokenId, remark);
        } else {
            disapproveRequest(tokenId, remark);
        }

        remarkField.setText("");
        loadPendingRequests();
    }

    // Implementation of ApprovalInterface: Forward to HOD
    public void approveRequest(int tokenId, String remark) {
        try {
            Connection con = DBConnect.getConnection();
            if (con == null) return;

            String sql = "UPDATE requests SET status = 'Pending HOD Approval', faculty_remark = ? WHERE token_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, remark.isEmpty() ? "Approved by Faculty" : remark);
            ps.setInt(2, tokenId);

            ps.executeUpdate();
            ps.close();
            con.close();

            JOptionPane.showMessageDialog(this, "Request #" + tokenId + " Approved and Forwarded to HOD!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // Implementation of ApprovalInterface: Reject
    public void disapproveRequest(int tokenId, String remark) {
        try {
            Connection con = DBConnect.getConnection();
            if (con == null) return;

            String sql = "UPDATE requests SET status = 'Rejected by Faculty', faculty_remark = ? WHERE token_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, remark);
            ps.setInt(2, tokenId);

            ps.executeUpdate();
            ps.close();
            con.close();

            JOptionPane.showMessageDialog(this, "Request #" + tokenId + " Disapproved.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // Load requests that are waiting for faculty
    public void loadPendingRequests() {
        tableModel.setRowCount(0);
        try {
            Connection con = DBConnect.getConnection();
            if (con == null) return;

            String sql = "SELECT * FROM requests WHERE status = 'Pending Faculty Approval' ORDER BY token_id ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("token_id");
                String roll = rs.getString("student_roll");
                String name = rs.getString("student_name");
                String dept = rs.getString("hod_dept");
                String text = rs.getString("request_text");
                String img = rs.getString("image_path");

                tableModel.addRow(new Object[]{id, roll, name, dept, text, img});
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception ex) {
            System.out.println("Error loading requests: " + ex.getMessage());
        }
    }
}
