package models.customer_function;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private static final String FILE_PATH = "src/txtFiles/customer.txt";

    // customer.txt format (row 1 = header, skip it):
    // customer_id|password|customer_name|customer_email|customer_phone|status|register_date

    private Customer parseLine(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length < 7) return null;
        return new Customer(
            p[0].trim(), p[1].trim(), p[2].trim(),
            p[3].trim(), p[4].trim(), p[5].trim(), p[6].trim()
        );
    }

    private String toLine(Customer c) {
        return String.join("|",
            c.getCustomerID(), c.getPassword(), c.getName(),
            c.getEmail(), c.getPhoneNumber(), c.getStatus(),
            c.getRegisteredDate().toString()
        );
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; }
                Customer c = parseLine(line);
                if (c != null) list.add(c);
            }
        } catch (IOException e) {
            System.err.println("Error reading customer.txt: " + e.getMessage());
        }
        return list;
    }

    public Customer getCustomerByID(String customerID) {
        for (Customer c : getAllCustomers())
            if (c.getCustomerID().equals(customerID)) return c;
        return null;
    }

    public Customer getCustomerByLogin(String email, String password) {
        for (Customer c : getAllCustomers())
            if (c.getEmail().equalsIgnoreCase(email.trim()) &&
                c.getPassword().equals(password.trim())) return c;
        return null;
    }

    public String getIdByLogin(String email, String password) {
        Customer c = getCustomerByLogin(email, password);
        return c != null ? c.getCustomerID() : null;
    }

    public boolean isEmailExists(String email) {
        for (Customer c : getAllCustomers())
            if (c.getEmail().equalsIgnoreCase(email.trim())) return true;
        return false;
    }

    public String generateCustomerID() {
        int max = 0;
        for (Customer c : getAllCustomers()) {
            String id = c.getCustomerID();
            if (id.matches("C\\d+")) {
                int num = Integer.parseInt(id.substring(1));
                if (num > max) max = num;
            }
        }
        return String.format("C%03d", max + 1);
    }

    private boolean saveCustomers(List<Customer> customers) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            bw.write("customer_id|password|customer_name|customer_email|customer_phone|status|register_date");
            bw.newLine();
            for (Customer c : customers) {
                bw.write(toLine(c));
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Cannot write to customer.txt: " + e.getMessage());
            return false;
        }
    }

    public boolean addCustomer(Customer newCustomer) {
        if (isEmailExists(newCustomer.getEmail())) return false;
        newCustomer.setCustomerID(generateCustomerID());
        List<Customer> customers = getAllCustomers();
        customers.add(newCustomer);
        return saveCustomers(customers);
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
        return found && saveCustomers(customers);
    }

    public boolean deleteCustomer(String customerID) {
        List<Customer> customers = getAllCustomers();
        boolean removed = customers.removeIf(c -> c.getCustomerID().equals(customerID));
        return removed && saveCustomers(customers);
    }

    public String registerCustomer(String name, String password,
                                   String email, String phoneNumber) {
        if (isEmailExists(email))
            return "This email is already registered. Please use a different email.";

        String id   = generateCustomerID();
        String date = LocalDate.now().toString();
        Customer c  = new Customer(id, password, name, email, phoneNumber, "Pending", date);

        List<Customer> customers = getAllCustomers();
        customers.add(c);
        return saveCustomers(customers) ? null
            : "Registration failed due to a system error. Please try again.";
    }
}