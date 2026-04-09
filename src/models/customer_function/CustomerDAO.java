package models.customer_function;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private static final String FILE_PATH = "src/txtFiles/customer.txt";

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\|", -1);

                // Expected order:
                // id|password|name|email|phone|address|status|registeredDate
                if (parts.length == 8) {
                    Customer c = new Customer(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            parts[3].trim(),
                            parts[4].trim(),
                            parts[5].trim(),
                            parts[6].trim(),
                            parts[7].trim()
                    );
                    list.add(c);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading customer data: " + e.getMessage());
        }

        return list;
    }

    public Customer getCustomerByID(String customerID) {
        for (Customer c : getAllCustomers()) {
            if (c.getCustomerID().equals(customerID)) {
                return c;
            }
        }
        return null;
    }

    public Customer getCustomerByLogin(String email, String password) {
        for (Customer c : getAllCustomers()) {
            if (c.getEmail().equalsIgnoreCase(email) && c.getPassword().equals(password)) {
                return c;
            }
        }
        return null;
    }

    public String getIdByLogin(String email, String password) {
        Customer c = getCustomerByLogin(email, password);
        return (c != null) ? c.getCustomerID() : null;
    }

    public boolean isEmailExists(String email) {
        for (Customer c : getAllCustomers()) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    public String generateCustomerID() {
        List<Customer> customers = getAllCustomers();
        int max = 0;

        for (Customer c : customers) {
            try {
                String id = c.getCustomerID().substring(1);
                int num = Integer.parseInt(id);
                if (num > max) {
                    max = num;
                }
            } catch (Exception ignored) {
            }
        }

        return String.format("C%03d", max + 1);
    }

    public boolean addCustomer(Customer newCustomer) {
        if (isEmailExists(newCustomer.getEmail())) {
            System.out.println("Email already exists!");
            return false;
        }

        List<Customer> customers = getAllCustomers();
        newCustomer.setCustomerID(generateCustomerID());
        customers.add(newCustomer);
        return saveCustomers(customers);
    }

    private boolean saveCustomers(List<Customer> customers) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Customer c : customers) {
                String line = c.getCustomerID() + "|"
                        + c.getPassword() + "|"
                        + c.getName() + "|"
                        + c.getEmail() + "|"
                        + c.getPhoneNumber() + "|"
                        + c.getAddress() + "|"
                        + c.getStatus() + "|"
                        + c.getRegisteredDate();

                bw.write(line);
                bw.newLine();
            }

            return true;

        } catch (IOException e) {
            System.out.println("Cannot write to customer.txt: " + e.getMessage());
            return false;
        }
    }

    public boolean updateCustomer(Customer updated) {
        List<Customer> customers = getAllCustomers();
        boolean found = false;

        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getCustomerID().equals(updated.getCustomerID())) {
                customers.set(i, updated);
                found = true;
                break;
            }
        }

        if (found) {
            return saveCustomers(customers);
        } else {
            System.out.println("Customer not found: " + updated.getCustomerID());
            return false;
        }
    }

    public boolean deleteCustomer(String customerID) {
        List<Customer> customers = getAllCustomers();
        boolean removed = false;

        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getCustomerID().equals(customerID)) {
                customers.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            return saveCustomers(customers);
        } else {
            System.out.println("Customer not found: " + customerID);
            return false;
        }
    }

    // New full registration method (includes address)
    public String registerCustomer(String name, String password, String contactNum, String email, String address) {
        if (isEmailExists(email)) {
            return "This email is already registered. Please use a different email.";
        }

        String newID = generateCustomerID();
        String registeredDate = LocalDate.now().toString();

        // id|password|name|email|phone|address|status|registeredDate
        String newLine = newID + "|"
                + password + "|"
                + name + "|"
                + email + "|"
                + contactNum + "|"
                + address + "|"
                + "Pending" + "|"
                + registeredDate;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(newLine);
            bw.newLine();
            return null;
        } catch (IOException e) {
            System.err.println("Cannot write to customer.txt: " + e.getMessage());
            return "Registration failed due to a system error. Please try again.";
        }
    }

    public String registerCustomer(String username, String password, String contactNum, String email) {
        return registerCustomer(username, password, contactNum, email, "");
    }
}