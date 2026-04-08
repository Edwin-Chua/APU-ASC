package models.entities;

import models.customer_function.CustomerDAO;

public class CustomerRegister {

    private final String username;
    private final String contactNum;
    private final String email;
    private final String password;
    private final String confirmPassword;

    public CustomerRegister(String username, String contactNum, String email,
                            String password, String confirmPassword) {
        this.username        = username        != null ? username.trim()        : "";
        this.contactNum      = contactNum      != null ? contactNum.trim()      : "";
        this.email           = email           != null ? email.trim()           : "";
        this.password        = password        != null ? password.trim()        : "";
        this.confirmPassword = confirmPassword != null ? confirmPassword.trim() : "";
    }

    public String validate() {
        if (username.isEmpty() || contactNum.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            return "Please fill in all fields before submitting.";
        }
        if (username.contains("|")) {
            return "Username cannot contain the '|' character.";
        }
        if (!contactNum.matches("\\d{10,11}")) {
            return "Contact number must be 10 or 11 digits (numbers only).";
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return "Please enter a valid email address.";
        }
        if (email.contains("|")) {
            return "Email cannot contain the '|' character.";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match. Please try again.";
        }
        int pwLen = password.length();
        if (pwLen < 8 || pwLen > 16) {
            return "Password must be between 8 and 16 characters long.";
        }
        if (password.contains("|")) {
            return "Password cannot contain the '|' character.";
        }
        return null;
    }

    public String register(CustomerDAO dao) {
        String err = validate();
        if (err != null) return err;
        return dao.registerCustomer(username, password, contactNum, email);
    }
}