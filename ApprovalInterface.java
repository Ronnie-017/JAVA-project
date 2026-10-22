// =======================================================
// ApprovalInterface.java
// Demonstrates OOP Concept: Interface (from Module 3)
// Implemented by both FacultyScreen and HodScreen
// =======================================================
public interface ApprovalInterface {
    // Method to approve a request
    void approveRequest(int tokenId, String remark);

    // Method to disapprove a request with remark
    void disapproveRequest(int tokenId, String remark);
}
