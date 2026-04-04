package models.entities;

import models.dao.CustomerDAO;

/**
 * Holds customer self-registration data and encapsulates validation + persistence.
 * The view collects input and calls {@link #register(CustomerDAO)}.
 */
public class CustomerRegister {

    private final String username;
    private final String contactNum;
    private final String email;
    private final String password;
    private final String confirmPassword;

    public CustomerRegister(String username, String contactNum, String email,
                            String password, String confirmPassword) {
        this.username = username != null ? username.trim() : "";
        this.contactNum = contactNum != null ? contactNum.trim() : "";
        this.email = email != null ? email.trim() : "";
        this.password = password != null ? password.trim() : "";
        this.confirmPassword = confirmPassword != null ? confirmPassword.trim() : "";
    }

    /**
     * Validates all registration rules. Returns {@code null} if valid, otherwise a user-facing error message.
     */
    public String validate() {
        if (username.isEmpty() || contactNum.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            return "Please fill in all fields before submitting.";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match. Please try again.";
        }
        if (!contactNum.matches("\\d{11}")) {
            return "Contact number must be exactly 11 digits (numbers only, no spaces or dashes).";
        }
        if (!email.toLowerCase().endsWith("@gmail.com")) {
            return "Email address must be a Gmail address (must end with @gmail.com).";
        }
        int pwLen = password.length();
        if (pwLen < 8 || pwLen > 16) {
            return "Password must be between 8 and 16 characters long.";
        }
        return null;
    }

    /**
     * Runs {@link #validate()} then, if valid, appends the customer via the DAO.
     * Returns {@code null} on success, or an error message (validation or duplicate email / IO).
     */
    public String register(CustomerDAO dao) {
        String err = validate();
        if (err != null) {
            return err;
        }
        return dao.registerCustomer(username, password, contactNum, email);
    }
}