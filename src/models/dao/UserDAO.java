package models.dao;

import java.io.*;
import java.nio.file.*;
import models.entities.User;

public class UserDAO {

    private static final String BASE_PATH = "src/txtFiles/";

    // ─── Manager login ────────────────────────────────────────────────────────
    // Format: ManagerID|username|email|password|createdDate
    public User authenticateManagerByEmail(String email, String password) {
        Path path = Paths.get(BASE_PATH + "manager.txt");
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
                    if (line.toLowerCase().startsWith("managerid|")
                            || line.toLowerCase().startsWith("userid|")) continue;
                }

                String[] parts = line.split("\\|", -1);
                User u = parseManager(parts, email, password);
                if (u != null) return u;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─── Manager parser ───────────────────────────────────────────────────────
    // Format: ManagerID|username|email|password|createdDate
    // col 0=ManagerID, 1=username, 2=email, 3=password, 4=createdDate

    private User parseManager(String[] parts, String email, String password) {
        if (parts.length < 5) return null;

        String managerId   = parts[0].trim();
        String username    = parts[1].trim();
        String fileEmail   = parts[2].trim();
        String filePass    = parts[3].trim();
        String createdDate = parts[4].trim();

        if (fileEmail.equals(email) && filePass.equals(password)) {
            return new User(managerId, username, filePass, null, fileEmail, "Manager", "Active", createdDate);
        }
        return null;
    }

    // ─── CounterStaff login ──────────────────────────────────────────────────
    // Format: StaffID|username|email|password|createdDate
    public User authenticateCounterStaffByEmail(String email, String password) {
        Path path = Paths.get(BASE_PATH + "counterStaff.txt");
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

                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("staffid|")
                            || line.toLowerCase().startsWith("userid|")) continue;
                }

                String[] parts = line.split("\\|", -1);
                User u = parseCounterStaff(parts, email, password);
                if (u != null) return u;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User parseCounterStaff(String[] parts, String email, String password) {
        if (parts.length < 5) return null;

        String staffId     = parts[0].trim();
        String username    = parts[1].trim();
        String fileEmail   = parts[2].trim();
        String filePass    = parts[3].trim();
        String createdDate = parts[4].trim();

        if (fileEmail.equals(email) && filePass.equals(password)) {
            return new User(staffId, username, filePass, null, fileEmail, "CounterStaff", "Active", createdDate);
        }
        return null;
    }

    // ─── Technician login ────────────────────────────────────────────────────
    // Format: TechnicianID|username|email|password|work_status|createdDate
    public User authenticateTechnicianByEmail(String email, String password) {
        Path path = Paths.get(BASE_PATH + "technician.txt");
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

                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("technicianid|")
                            || line.toLowerCase().startsWith("userid|")) continue;
                }

                String[] parts = line.split("\\|", -1);
                User u = parseTechnician(parts, email, password);
                if (u != null) return u;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User parseTechnician(String[] parts, String email, String password) {
        if (parts.length < 6) return null;

        String technicianId = parts[0].trim();
        String username     = parts[1].trim();
        String fileEmail    = parts[2].trim();
        String filePass     = parts[3].trim();
        String workStatus   = parts[4].trim();
        String createdDate  = parts[5].trim();

        if (fileEmail.equals(email) && filePass.equals(password)) {
            return new User(technicianId, username, filePass, null, fileEmail, "Technician", workStatus, createdDate);
        }
        return null;
    }

    // ─── Manager profile update methods ───────────────────────────────────────
    public boolean updateManagerUsername(String managerId, String newUsername) {
        return updateManagerField(managerId, 1, newUsername);
    }

    public boolean updateManagerPassword(String managerId, String newPassword) {
        return updateManagerField(managerId, 3, newPassword);
    }

    private boolean updateManagerField(String managerId, int colIndex, String newValue) {
        Path path = Paths.get(BASE_PATH + "manager.txt");
        try {
            java.util.List<String> lines = Files.readAllLines(path);
            java.util.List<String> updated = new java.util.ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()
                        || trimmed.toLowerCase().startsWith("userid")
                        || trimmed.toLowerCase().startsWith("managerid")) {
                    updated.add(line);
                    continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length > colIndex && parts[0].trim().equals(managerId)) {
                    parts[colIndex] = newValue;
                    updated.add(String.join("|", parts));
                    found = true;
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
}
