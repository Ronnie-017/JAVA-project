import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import javax.swing.JOptionPane;

// =======================================================
// DBConnect.java
// Simple class to connect Java to MySQL database using JDBC.
// =======================================================
public class DBConnect {

    // Database details
    public static String url = "jdbc:mysql://localhost:3306/collegedb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true";
    public static String user = "root";
    
    // CHANGE THIS TO YOUR MYSQL PASSWORD (e.g., "root", "1234", or "")
    public static String password = "123456";

    // Method to get database connection
    public static Connection getConnection() {
        Connection con = null;
        try {
            // Step 1: Load the MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Step 2: Establish the connection
            con = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            System.out.println("Connection Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Database Connection Failed!\n" +
                "Error: " + e.getMessage() + "\n\n" +
                "Please check:\n" +
                "1. Is MySQL Server running?\n" +
                "2. Is your password correct in DBConnect.java?\n" +
                "(Currently set to: '" + password + "')",
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        return con;
    }

    // Simple method to create tables automatically if they do not exist
    public static void setupDatabase() {
        Connection con = getConnection();
        if (con == null) {
            return;
        }

        try {
            Statement st = con.createStatement();

            // Create students table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS students (" +
                "roll_no VARCHAR(20) PRIMARY KEY, " +
                "name VARCHAR(50), " +
                "dept VARCHAR(10), " +
                "password VARCHAR(50))");

            // Create faculty table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS faculty (" +
                "username VARCHAR(20) PRIMARY KEY, " +
                "password VARCHAR(50))");

            // Create hod table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS hod (" +
                "dept VARCHAR(10) PRIMARY KEY, " +
                "password VARCHAR(50))");

            // Create requests table
            st.executeUpdate("CREATE TABLE IF NOT EXISTS requests (" +
                "token_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "student_roll VARCHAR(20), " +
                "student_name VARCHAR(50), " +
                "hod_dept VARCHAR(10), " +
                "request_text VARCHAR(255), " +
                "image_path VARCHAR(255), " +
                "status VARCHAR(50) DEFAULT 'Pending Faculty Approval', " +
                "faculty_remark VARCHAR(255) DEFAULT '', " +
                "hod_remark VARCHAR(255) DEFAULT '')");

            // Insert default 3 faculty accounts
            st.executeUpdate("INSERT IGNORE INTO faculty (username, password) VALUES " +
                "('fac1', '123'), ('fac2', '456'), ('fac3', '789')");

            // Insert default 8 HOD accounts
            st.executeUpdate("INSERT IGNORE INTO hod (dept, password) VALUES " +
                "('CS', '123'), ('CA', '123'), ('ME', '123'), ('CC', '123'), " +
                "('ER', '123'), ('EE', '123'), ('EC', '123'), ('CE', '123')");

            st.close();
            con.close();
        } catch (Exception e) {
            System.out.println("Setup Error: " + e.getMessage());
        }
    }
}
