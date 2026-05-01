package models.dao;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private static final String FILE_PATH = "src/txtFiles/customer.txt";
    // customer.txt format:
    // UserID|username|password|contactNum|email|role|status|createdDate

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
                    if (line.toLowerCase().startsWith("userid")) continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 8) list.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── Register customer ────────────────────────────────────────────────────

    public String registerCustomer(String username, String password,
                                   String contactNum, String email, String status) {
        // Check for duplicate email
        if (emailExists(email)) return "An account with this email already exists.";

        String id = generateNextCustomerId();
        String createdDate = LocalDate.now().toString();

        // UserID|username|password|contactNum|email|role|status|createdDate
        String record = String.join("|",
            id, username, password, contactNum, email, "Customer", status, createdDate
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
                if (trimmed.isEmpty() || trimmed.toLowerCase().startsWith("userid")) {
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

    // ─── Email existence check ────────────────────────────────────────────────

    public boolean emailExists(String email) {
        for (String[] c : getAllCustomers()) {
            if (c.length >= 5 && c[4].trim().equalsIgnoreCase(email)) return true;
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