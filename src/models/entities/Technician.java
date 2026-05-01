package models.entities;

public class Technician {

    private String technicianId;
    private String username;
    private String email;
    private String password;
    private String workStatus;
    private String createdDate;

    public Technician(String technicianId, String username, String email,
                      String password, String workStatus, String createdDate) {
        this.technicianId = technicianId;
        this.username     = username;
        this.email        = email;
        this.password     = password;
        this.workStatus   = workStatus;
        this.createdDate  = createdDate;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────
    public String getTechnicianId() { return technicianId; }
    public String getUsername()     { return username; }
    public String getEmail()        { return email; }
    public String getPassword()     { return password; }
    public String getWorkStatus()   { return workStatus; }
    public String getCreatedDate()  { return createdDate; }

    // ─── Setters ──────────────────────────────────────────────────────────────
    public void setTechnicianId(String v) { this.technicianId = v; }
    public void setUsername(String v)     { this.username = v; }
    public void setEmail(String v)        { this.email = v; }
    public void setPassword(String v)     { this.password = v; }
    public void setWorkStatus(String v)   { this.workStatus = v; }
    public void setCreatedDate(String v)  { this.createdDate = v; }
}