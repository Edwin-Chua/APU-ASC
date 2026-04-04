package models.dao;

import models.entities.User;

import java.io.*;
import java.nio.file.*;

public class UserDAO {

    // Adjust this base path to wherever your txtFiles folder lives relative
    // to the working directory when Eclipse runs the project.
    private static final String BASE_PATH = "src/txtFiles/";

    // ─── Main authenticate method ─────────────────────────────────────────────

    public User authenticate(String username, String password, String role) {
        String fileName = getFileByRole(role);
        if (fileName == null) return null;

        Path path = Paths.get(BASE_PATH + fileName);
        if (!Files.exists(path)) {
            System.err.println("File not found: " + path.toAbsolutePath());
            return null;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Skip header row
                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("userid|")) continue;
                }

                String[] parts = line.split("\\|", -1);

                if ("Customer".equals(role)) {
                    User u = parseCustomer(parts, username, password);
                    if (u != null) return u;
                } else {
                    User u = parseStaff(parts, username, password, role);
                    if (u != null) return u;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─── Staff line parser ────────────────────────────────────────────────────
    // Format: UserID|username|password|role|createdDate

    private User parseStaff(String[] parts, String username, String password, String role) {
        if (parts.length < 5) return null;

        String userId      = parts[0].trim();
        String fileUser    = parts[1].trim();
        String filePass    = parts[2].trim();
        String fileRole    = parts[3].trim();
        String createdDate = parts[4].trim();

        if (fileUser.equals(username) && filePass.equals(password) && fileRole.equals(role)) {
            return new User(userId, fileUser, filePass, null, null, fileRole, "Active", createdDate);
        }
        return null;
    }

    // ─── Customer line parser ─────────────────────────────────────────────────
    // Format: UserID|username|password|contactNum|email|role|status|createdDate

    private User parseCustomer(String[] parts, String username, String password) {
        if (parts.length < 8) return null;

        String userId      = parts[0].trim();
        String fileUser    = parts[1].trim();
        String filePass    = parts[2].trim();
        String contactNum  = parts[3].trim();
        String email       = parts[4].trim();
        String fileRole    = parts[5].trim();
        String status      = parts[6].trim();
        String createdDate = parts[7].trim();

        if (fileUser.equals(username)
                && filePass.equals(password)
                && fileRole.equals("Customer")
                && status.equalsIgnoreCase("Approved")) {
            return new User(userId, fileUser, filePass, contactNum, email, fileRole, status, createdDate);
        }
        return null;
    }

    // ─── Role → file mapping ──────────────────────────────────────────────────

    private String getFileByRole(String role) {
        switch (role) {
            case "Manager":      return "manager.txt";
            case "CounterStaff": return "counterStaff.txt";
            case "Technician":   return "technician.txt";
            case "Customer":     return "customer.txt";
            default:             return null;
        }
    }
}