package models.dao;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private static final String FILE_PATH = "src/txtFiles/customer.txt";
    // customer.txt format:
    // customer_id|password|customer_name|customer_email|customer_phone|status|register_date
    // index:      0        1             2               3               4              5       6

    // ─── Get all customers ────────────────────────────────────────────────────

    public List<String[]> getAllCustomers() {
        List<String[]> list = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("customer_id")) continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 7) list.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // ─── Get approved customers ────────────────────────────────────────────────────

    public List<String[]> getApprovedCustomers() {
        List<String[]> list = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("customer_id")) continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 7 && parts[5].trim().equalsIgnoreCase("Approved")) list.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── Register customer ────────────────────────────────────────────────────

    public String registerCustomer(String customerName, String password,
                                   String customerPhone, String customerEmail, String status) {
        // Check for duplicate email
        if (emailExists(customerEmail)) return "An account with this email already exists.";

        String id = generateNextCustomerId();
        String registerDate = LocalDate.now().toString();

        // customer_id|password|customer_name|customer_email|customer_phone|status|register_date
        String record = String.join("|",
            id, password, customerName, customerEmail, customerPhone, status, registerDate
        );

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.newLine();
            bw.write(record);
            return "Customer registered successfully! ID: " + id;
        } catch (IOException e) {
            e.printStackTrace();
            return "Error: Failed to save customer record.";
        }
    }

    // ─── Delete customer by ID ────────────────────────────────────────────────

    public boolean deleteCustomerById(String customerId) {
        Path path = Paths.get(FILE_PATH);
        try {
            List<String> lines = Files.readAllLines(path);
            List<String> updated = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.toLowerCase().startsWith("customer_id")) {
                    updated.add(line);
                    continue;
                }
                String[] parts = trimmed.split("\\|", -1);
                if (parts[0].trim().equals(customerId)) {
                    found = true; // skip (delete) this line
                } else {
                    updated.add(line);
                }
            }

            if (!found) return false;
            Files.write(path, updated);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Get password by customer ID ─────────────────────────────────────────

    public String getPasswordById(String customerId) {
        for (String[] c : getAllCustomers()) {
            if (c[0].trim().equalsIgnoreCase(customerId)) {
                return c[1].trim(); // index 1 = password
            }
        }
        return null;
    }

    // ─── Update customer (all editable fields) ────────────────────────────────
    // Rewrites the matching line with new values; register_date is preserved.
    // Signature: (id, name, password, phone, email, status)

    public boolean updateCustomer(String customerId, String customerName, String password,
                                  String customerPhone, String customerEmail, String status) {
        Path path = Paths.get(FILE_PATH);
        try {
            List<String> lines = Files.readAllLines(path);
            List<String> updated = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.toLowerCase().startsWith("customer_id")) {
                    updated.add(line);
                    continue;
                }
                String[] parts = trimmed.split("\\|", -1);
                if (parts[0].trim().equals(customerId)) {
                    found = true;
                    // Preserve original register_date (index 6)
                    String registerDate = parts.length >= 7 ? parts[6].trim() : LocalDate.now().toString();
                    // customer_id|password|customer_name|customer_email|customer_phone|status|register_date
                    String newLine = String.join("|",
                        customerId, password, customerName, customerEmail, customerPhone, status, registerDate
                    );
                    updated.add(newLine);
                } else {
                    updated.add(line);
                }
            }

            if (!found) return false;
            Files.write(path, updated);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Approve customer (set status → "Approved") ───────────────────────────

    public boolean approveCustomer(String customerId) {
        Path path = Paths.get(FILE_PATH);
        try {
            List<String> lines = Files.readAllLines(path);
            List<String> updated = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.toLowerCase().startsWith("customer_id")) {
                    updated.add(line);
                    continue;
                }
                String[] parts = trimmed.split("\\|", -1);
                if (parts[0].trim().equals(customerId)) {
                    found = true;
                    parts[5] = "Approved"; // index 5 = status
                    updated.add(String.join("|", parts));
                } else {
                    updated.add(line);
                }
            }

            if (!found) return false;
            Files.write(path, updated);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Email existence check ────────────────────────────────────────────────

    public boolean emailExists(String email) {
        for (String[] c : getAllCustomers()) {
            if (c.length >= 4 && c[3].trim().equalsIgnoreCase(email)) return true;
        }
        return false;
    }

    // ─── Email existence check (excluding a specific customer ID) ─────────────
    // Used during edit to allow a customer to keep their own email.

    public boolean emailExistsExcluding(String email, String excludeId) {
        for (String[] c : getAllCustomers()) {
            if (c[0].trim().equalsIgnoreCase(excludeId)) continue;
            if (c.length >= 4 && c[3].trim().equalsIgnoreCase(email)) return true;
        }
        return false;
    }

    // ─── Generate next customer ID ────────────────────────────────────────────
    // Finds the highest existing CXX number and increments by 1

    private String generateNextCustomerId() {
        int max = 0;
        for (String[] c : getAllCustomers()) {
            String id = c[0].trim();
            if (id.matches("C\\d+")) {
                int num = Integer.parseInt(id.substring(1));
                if (num > max) max = num;
            }
        }
        return String.format("C%03d", max + 1);
    }
}