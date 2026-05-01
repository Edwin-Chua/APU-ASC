package models.entities;

public class ManageEmployee {

    private String id;
    private String username;
    private String email;
    private String password;
    private String role;
    private String createdDate;

    public ManageEmployee(String id, String username, String email,
                          String password, String role, String createdDate) {
        this.id          = id;
        this.username    = username;
        this.email       = email;
        this.password    = password;
        this.role        = role;
        this.createdDate = createdDate;
    }

    public String getId()          { return id; }
    public String getUsername()    { return username; }
    public String getEmail()       { return email; }
    public String getPassword()    { return password; }
    public String getRole()        { return role; }
    public String getCreatedDate() { return createdDate; }

    public void setId(String id)                   { this.id = id; }
    public void setUsername(String username)       { this.username = username; }
    public void setEmail(String email)             { this.email = email; }
    public void setPassword(String password)       { this.password = password; }
    public void setRole(String role)               { this.role = role; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }
}