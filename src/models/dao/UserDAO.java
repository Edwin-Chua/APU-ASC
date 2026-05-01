package models.dao;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import models.entities.User;

public class UserDAO {

    private static final String BASE_PATH = "src/txtFiles/";

    // ─────────────────────────────────────────────────────────────────────────
    // CUSTOMER_T: customer_id | customer_name | customer_email |
    //             customer_password | customer_address | customer_register_date
    // ─────────────────────────────────────────────────────────────────────────
    public User authenticateCustomer(String email, String password) {
        Path path = Paths.get(BASE_PATH + "customer.txt");
        if (!Files.exists(path)) return null;

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
                if (parts.length < 6) continue;

                String customerId   = parts[0].trim();
                String customerName = parts[1].trim();
                String fileEmail    = parts[2].trim();
                String filePassword = parts[3].trim();
                String address      = parts[4].trim();
                String regDate      = parts[5].trim();

                if (fileEmail.equalsIgnoreCase(email.trim()) &&
                    filePassword.equals(password.trim())) {
                    return new User(
                        customerId, customerName, filePassword,
                        null, fileEmail, "Customer", "Approved", regDate
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MANAGER_T: manager_id | manager_name | manager_password | manager_email
    // ─────────────────────────────────────────────────────────────────────────
    public User authenticateManagerByEmail(String email, String password) {
        Path path = Paths.get(BASE_PATH + "manager.txt");
        if (!Files.exists(path)) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("manager_id")) continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length < 4) continue;

                String managerId   = parts[0].trim();
                String managerName = parts[1].trim();
                String filePass    = parts[2].trim();
                String fileEmail   = parts[3].trim();

                if (fileEmail.equalsIgnoreCase(email.trim()) &&
                    filePass.equals(password.trim())) {
                    return new User(
                        managerId, managerName, filePass,
                        null, fileEmail, "Manager", "Active", ""
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateManagerUsername(String managerId, String newUsername) {
        Path path = Paths.get(BASE_PATH + "manager.txt");
        if (!Files.exists(path)) return false;

        try {
            List<String> lines = Files.readAllLines(path);
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("manager_id")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length < 4) continue;

                if (parts[0].trim().equals(managerId)) {
                    // manager_id | newUsername | password | email
                    lines.set(i, parts[0].trim() + "|" + newUsername + "|" +
                                  parts[2].trim() + "|" + parts[3].trim());
                    updated = true;
                    break;
                }
            }

            if (updated) Files.write(path, lines);
            return updated;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateManagerPassword(String managerId, String newPassword) {
        Path path = Paths.get(BASE_PATH + "manager.txt");
        if (!Files.exists(path)) return false;

        try {
            List<String> lines = Files.readAllLines(path);
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("manager_id")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length < 4) continue;

                if (parts[0].trim().equals(managerId)) {
                    // manager_id | name | newPassword | email
                    lines.set(i, parts[0].trim() + "|" + parts[1].trim() + "|" +
                                  newPassword + "|" + parts[3].trim());
                    updated = true;
                    break;
                }
            }

            if (updated) Files.write(path, lines);
            return updated;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // COUNTER_STAFF_T: staff_id | staff_name | staff_password | staff_email
    // ─────────────────────────────────────────────────────────────────────────
    public User authenticateCounterStaffByEmail(String email, String password) {
        Path path = Paths.get(BASE_PATH + "counterStaff.txt");
        if (!Files.exists(path)) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("staff_id")) continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length < 4) continue;

                String staffId   = parts[0].trim();
                String staffName = parts[1].trim();
                String filePass  = parts[2].trim();
                String fileEmail = parts[3].trim();

                if (fileEmail.equalsIgnoreCase(email.trim()) &&
                    filePass.equals(password.trim())) {
                    return new User(
                        staffId, staffName, filePass,
                        null, fileEmail, "CounterStaff", "Active", ""
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateCounterStaffUsername(String staffId, String newUsername) {
        Path path = Paths.get(BASE_PATH + "counterStaff.txt");
        if (!Files.exists(path)) return false;

        try {
            List<String> lines = Files.readAllLines(path);
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("staff_id")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length < 4) continue;

                if (parts[0].trim().equals(staffId)) {
                    // staff_id | newUsername | password | email
                    lines.set(i, parts[0].trim() + "|" + newUsername + "|" +
                                  parts[2].trim() + "|" + parts[3].trim());
                    updated = true;
                    break;
                }
            }

            if (updated) Files.write(path, lines);
            return updated;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCounterStaffPassword(String staffId, String newPassword) {
        Path path = Paths.get(BASE_PATH + "counterStaff.txt");
        if (!Files.exists(path)) return false;

        try {
            List<String> lines = Files.readAllLines(path);
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("staff_id")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length < 4) continue;

                if (parts[0].trim().equals(staffId)) {
                    // staff_id | name | newPassword | email
                    lines.set(i, parts[0].trim() + "|" + parts[1].trim() + "|" +
                                  newPassword + "|" + parts[3].trim());
                    updated = true;
                    break;
                }
            }

            if (updated) Files.write(path, lines);
            return updated;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TECHNICIAN_T: technician_id | technician_name | technician_email |
    //               technician_password | status | specialization
    // ─────────────────────────────────────────────────────────────────────────
    public User authenticateTechnicianByEmail(String email, String password) {
        Path path = Paths.get(BASE_PATH + "technician.txt");
        if (!Files.exists(path)) return null;

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (firstLine) {
                    firstLine = false;
                    if (line.toLowerCase().startsWith("technician_id")) continue;
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length < 6) continue;

                String techId    = parts[0].trim();
                String techName  = parts[1].trim();
                String fileEmail = parts[2].trim();
                String filePass  = parts[3].trim();
                String status    = parts[4].trim();
                String special   = parts[5].trim();

                if (fileEmail.equalsIgnoreCase(email.trim()) &&
                    filePass.equals(password.trim())) {
                    return new User(
                        techId, techName, filePass,
                        null, fileEmail, "Technician", status, ""
                    );
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateTechnicianUsername(String techId, String newUsername) {
        Path path = Paths.get(BASE_PATH + "technician.txt");
        if (!Files.exists(path)) return false;

        try {
            List<String> lines = Files.readAllLines(path);
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("technician_id")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length < 6) continue;

                if (parts[0].trim().equals(techId)) {
                    // technician_id | newUsername | email | password | status | specialization
                    lines.set(i, parts[0].trim() + "|" + newUsername + "|" +
                                  parts[2].trim() + "|" + parts[3].trim() + "|" +
                                  parts[4].trim() + "|" + parts[5].trim());
                    updated = true;
                    break;
                }
            }

            if (updated) Files.write(path, lines);
            return updated;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTechnicianPassword(String techId, String newPassword) {
        Path path = Paths.get(BASE_PATH + "technician.txt");
        if (!Files.exists(path)) return false;

        try {
            List<String> lines = Files.readAllLines(path);
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("technician_id")) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length < 6) continue;

                if (parts[0].trim().equals(techId)) {
                    // technician_id | name | email | newPassword | status | specialization
                    lines.set(i, parts[0].trim() + "|" + parts[1].trim() + "|" +
                                  parts[2].trim() + "|" + newPassword + "|" +
                                  parts[4].trim() + "|" + parts[5].trim());
                    updated = true;
                    break;
                }
            }

            if (updated) Files.write(path, lines);
            return updated;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Legacy: authenticate staff by username (kept for backward compatibility)
    // ─────────────────────────────────────────────────────────────────────────
    public User authenticateStaff(String username, String password, String role) {
        switch (role) {
            case "Manager":      return authenticateManagerByEmail(username, password);
            case "CounterStaff": return authenticateCounterStaffByEmail(username, password);
            case "Technician":   return authenticateTechnicianByEmail(username, password);
            default:             return null;
        }
    }
}