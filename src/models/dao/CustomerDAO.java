package models.dao;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;

public class CustomerDAO {

    private static final String FILE_PATH = "src/txtFiles/customer.txt";

    /**
     * Registers a new customer. Returns an error message string if validation
     * fails, or null on success.
     * Format: UserID|username|password|contactNum|email|Customer|Pending|createdDate
     */
    public String registerCustomer(String username, String password,
                                   String contactNum, String email) {
        Path path = Paths.get(FILE_PATH);

        try {
            // Auto-create file with header if it doesn't exist
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.write(path, "UserID|username|password|contactNum|email|role|status|createdDate\n"
                        .getBytes());
            }

            List<String> lines = Files.readAllLines(path);

            // Check for duplicate email
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("UserID")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 5 && parts[4].trim().equalsIgnoreCase(email.trim())) {
                    return "This email is already taken. Please choose another.";
                }
            }

            // Generate next customer ID
            int maxId = 0;
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("UserID")) continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 1 && parts[0].trim().startsWith("C")) {
                    try {
                        int num = Integer.parseInt(parts[0].trim().substring(1));
                        if (num > maxId) maxId = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
            String newId = String.format("C%03d", maxId + 1);
            String today = LocalDate.now().toString();

            String newLine = String.join("|",
                newId, username, password, contactNum, email,
                "Customer", "Pending", today);

            // Append new customer line
            try (BufferedWriter bw = new BufferedWriter(
                    new FileWriter(path.toFile(), true))) {
                bw.newLine();
                bw.write(newLine);
            }

            return null; // success

        } catch (IOException e) {
            e.printStackTrace();
            return "An error occurred while saving your account. Please try again.";
        }
    }
}