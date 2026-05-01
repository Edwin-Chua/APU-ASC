package models.customer_function;

import java.time.LocalDate;

public class Customer {
    private String customerID;
    private String password;
    private String name;
    private String email;
    private String address;
    private String status;
    private LocalDate registeredDate;

    public Customer(String customerID, String password, String name,
                    String email, String address, String status, String registeredDate) {
        this.customerID     = customerID;
        this.password       = password;
        this.name           = name;
        this.email          = email;
        this.address        = address;
        this.status         = status;
        this.registeredDate = LocalDate.parse(registeredDate);
    }

    public String getCustomerID()        { return customerID; }
    public String getPassword()          { return password; }
    public String getName()              { return name; }
    public String getEmail()             { return email; }
    public String getAddress()           { return address; }
    public String getStatus()            { return status; }
    public LocalDate getRegisteredDate() { return registeredDate; }

    public void setCustomerID(String customerID) { this.customerID = customerID; }
    public void setPassword(String password)     { this.password   = password; }
    public void setName(String name)             { this.name       = name; }
    public void setEmail(String email)           { this.email      = email; }
    public void setAddress(String address)       { this.address    = address; }
    public void setStatus(String status)         { this.status     = status; }
}