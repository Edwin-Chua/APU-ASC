package models.customer_function;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private static final String FILE_PATH = "src/txtFiles/customer.txt";

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");

                if (parts.length == 7) {
                    Customer c = new Customer(
                            parts[0],
                            parts[1],
                            parts[2],
                            parts[3],
                            parts[4],
                            parts[5],
                            parts[6]
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
            if (c.getEmail().equals(email) && c.getPassword().equals(password)) {
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
            } catch (Exception e) {
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

    public String registerCustomer(String username, String password, String contactNum, String email) {
        if (isEmailExists(email)) {
            return "This email is already registered. Please use a different email.";
        }

        String newID = generateCustomerID();
        String registeredDate = java.time.LocalDate.now().toString();

        List<String> rawLines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                rawLines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return "Registration failed due to a system error. Please try again.";
        }

        String newLine = newID        + "|"
                + username            + "|"
                + password            + "|"
                + contactNum          + "|"
                + email               + "|"
                + "Customer"          + "|"
                + "Pending"           + "|"
                + registeredDate;

        rawLines.add(newLine);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (String l : rawLines) {
                bw.write(l);
                bw.newLine();
            }
            return null; // null = 成功
        } catch (IOException e) {
            System.err.println("Cannot write to customer.txt: " + e.getMessage());
            return "Registration failed due to a system error. Please try again.";
        }
    }
}