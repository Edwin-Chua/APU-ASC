package models.entities;

/**
 * Represents a logged-in account loaded from the role-specific .txt files.
 * Staff roles use null for contact/email; customers have contact and email filled.
 */
public class User {

    private final String userId;
    private final String username;
    private final String password;
    private final String contactNum;
    private final String email;
    private final String role;
    private final String status;
    private final String createdDate;

    public User(String userId, String username, String password,
                String contactNum, String email,
                String role, String status, String createdDate) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.contactNum = contactNum;
        this.email = email;
        this.role = role;
        this.status = status;
        this.createdDate = createdDate;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /** May be null for staff roles. */
    public String getContactNum() {
        return contactNum;
    }

    /** May be null for staff roles. */
    public String getEmail() {
        return email;
    }

    /**
     * One of: Manager, CounterStaff, Technician, Customer
     * (must match values in .txt files and LoginPage navigation).
     */
    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}