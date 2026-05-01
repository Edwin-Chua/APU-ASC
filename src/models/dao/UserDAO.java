package models.dao;

import java.io.*;
import java.nio.file.*;
import models.entities.User;

public class UserDAO {

    private static final String BASE_PATH = "src/txtFiles/";

    // Customer login (email + password + approved only)
    public User authenticateCustomer(String email, String password) {
        Path path = Paths.get(BASE_PATH + "customer.txt");
        if (!Files.exists(path)) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length < 8) continue;

                String userId = parts[0].trim();
                String username = "", filePass = "", fileEmail = "", contact = "", role = "Customer", createdDate = "";
                String status = parts[7].trim();

                if (parts[3].contains("@")) {
                    filePass = parts[1].trim();
                    username = parts[2].trim();
                    fileEmail = parts[3].trim();
                    contact = parts[4].trim();
                    createdDate = parts[6].trim();
                } else if (parts[4].contains("@")) {
                    username = parts[1].trim();
                    filePass = parts[2].trim();
                    contact = parts[3].trim();
                    fileEmail = parts[4].trim();
                    role = parts[5].trim();
                    createdDate = parts[6].trim();
                } else continue;

                if (fileEmail.equalsIgnoreCase(email.trim()) &&
                    filePass.equals(password.trim())) {

                    if (!status.equalsIgnoreCase("Approved")) return null;

                    return new User(userId, username, filePass, contact, fileEmail, role, status, createdDate);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Staff login (username)
    public User authenticateStaff(String input, String password, String role) {
        String fileName = getFileByRole(role);
        if (fileName == null) return null;

        Path path = Paths.get(BASE_PATH + fileName);
        if (!Files.exists(path)) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Skip header row
                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("managerid|")
                            || line.toLowerCase().startsWith("userid|")) continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length < 5) continue;

                String userId = parts[0].trim();
                String username = parts[1].trim();
                String filePass = parts[2].trim();
                String fileRole = parts[3].trim();
                String createdDate = parts[4].trim();

                if (username.equalsIgnoreCase(input.trim()) &&
                    filePass.equals(password.trim()) &&
                    fileRole.equalsIgnoreCase(role.trim())) {

                    return new User(userId, username, filePass, null, null, fileRole, "Active", createdDate);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Role mapping
    private String getFileByRole(String role) {
        switch (role) {
            case "Manager": return "manager.txt";
            case "CounterStaff": return "counterStaff.txt";
            case "Technician": return "technician.txt";
            default: return null;
        }
    }
}
