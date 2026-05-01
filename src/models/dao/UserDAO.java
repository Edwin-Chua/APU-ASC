package models.dao;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import models.entities.User;

public class UserDAO {

    private static final String BASE_PATH = "src/txtFiles/";

    // ─────────────────────────────────────────────────────────────────────────
    // customer.txt: customer_id|password|customer_name|customer_email|customer_phone|status|register_date
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
                if (firstLine) { firstLine = false; continue; }

                String[] p = line.split("\\|", -1);
                if (p.length < 7) continue;

                String fileEmail = p[3].trim();
                String filePass  = p[1].trim();
                String status    = p[5].trim();

                if (fileEmail.equalsIgnoreCase(email.trim()) && filePass.equals(password.trim())) {
                    return new User(p[0].trim(), p[2].trim(), filePass,
                        p[4].trim(), fileEmail, "Customer", status, p[6].trim());
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // manager.txt: ManagerID|username|email|password|createdDate
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
                if (firstLine) { firstLine = false; continue; }

                String[] p = line.split("\\|", -1);
                if (p.length < 4) continue;

                String managerId   = p[0].trim();
                String managerName = p[1].trim();
                String fileEmail   = p[2].trim();
                String filePass    = p[3].trim();

                if (fileEmail.equalsIgnoreCase(email.trim()) && filePass.equals(password.trim())) {
                    return new User(managerId, managerName, filePass,
                        null, fileEmail, "Manager", "Active", "");
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
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
                if (line.isEmpty() || line.toLowerCase().startsWith("managerid")) continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 4) continue;
                if (p[0].trim().equals(managerId)) {
                    lines.set(i, p[0].trim() + "|" + newUsername + "|" + p[2].trim() + "|" + p[3].trim()
                        + (p.length > 4 ? "|" + p[4].trim() : ""));
                    updated = true; break;
                }
            }
            if (updated) Files.write(path, lines);
            return updated;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    public boolean updateManagerPassword(String managerId, String newPassword) {
        Path path = Paths.get(BASE_PATH + "manager.txt");
        if (!Files.exists(path)) return false;
        try {
            List<String> lines = Files.readAllLines(path);
            boolean updated = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("managerid")) continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 4) continue;
                if (p[0].trim().equals(managerId)) {
                    lines.set(i, p[0].trim() + "|" + p[1].trim() + "|" + p[2].trim() + "|" + newPassword
                        + (p.length > 4 ? "|" + p[4].trim() : ""));
                    updated = true; break;
                }
            }
            if (updated) Files.write(path, lines);
            return updated;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // counterStaff.txt: StaffID|username|email|password|createdDate
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
                if (firstLine) { firstLine = false; continue; }

                String[] p = line.split("\\|", -1);
                if (p.length < 4) continue;

                String staffId   = p[0].trim();
                String staffName = p[1].trim();
                String fileEmail = p[2].trim();
                String filePass  = p[3].trim();

                if (fileEmail.equalsIgnoreCase(email.trim()) && filePass.equals(password.trim())) {
                    return new User(staffId, staffName, filePass,
                        null, fileEmail, "CounterStaff", "Active", "");
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
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
                if (line.isEmpty() || line.toLowerCase().startsWith("staffid")) continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 4) continue;
                if (p[0].trim().equals(staffId)) {
                    lines.set(i, p[0].trim() + "|" + newUsername + "|" + p[2].trim() + "|" + p[3].trim()
                        + (p.length > 4 ? "|" + p[4].trim() : ""));
                    updated = true; break;
                }
            }
            if (updated) Files.write(path, lines);
            return updated;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    public boolean updateCounterStaffPassword(String staffId, String newPassword) {
        Path path = Paths.get(BASE_PATH + "counterStaff.txt");
        if (!Files.exists(path)) return false;
        try {
            List<String> lines = Files.readAllLines(path);
            boolean updated = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("staffid")) continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 4) continue;
                if (p[0].trim().equals(staffId)) {
                    lines.set(i, p[0].trim() + "|" + p[1].trim() + "|" + p[2].trim() + "|" + newPassword
                        + (p.length > 4 ? "|" + p[4].trim() : ""));
                    updated = true; break;
                }
            }
            if (updated) Files.write(path, lines);
            return updated;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // technician.txt: technician_id|technician_name|technician_email|technician_password|status|specialization
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
                if (firstLine) { firstLine = false; continue; }

                String[] p = line.split("\\|", -1);
                if (p.length < 6) continue;

                String techId    = p[0].trim();
                String techName  = p[1].trim();
                String fileEmail = p[2].trim();
                String filePass  = p[3].trim();
                String status    = p[4].trim();

                if (fileEmail.equalsIgnoreCase(email.trim()) && filePass.equals(password.trim())) {
                    return new User(techId, techName, filePass,
                        null, fileEmail, "Technician", status, "");
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
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
                String[] p = line.split("\\|", -1);
                if (p.length < 6) continue;
                if (p[0].trim().equals(techId)) {
                    lines.set(i, p[0].trim() + "|" + newUsername + "|" +
                        p[2].trim() + "|" + p[3].trim() + "|" + p[4].trim() + "|" + p[5].trim());
                    updated = true; break;
                }
            }
            if (updated) Files.write(path, lines);
            return updated;
        } catch (IOException e) { e.printStackTrace(); return false; }
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
                String[] p = line.split("\\|", -1);
                if (p.length < 6) continue;
                if (p[0].trim().equals(techId)) {
                    lines.set(i, p[0].trim() + "|" + p[1].trim() + "|" +
                        p[2].trim() + "|" + newPassword + "|" + p[4].trim() + "|" + p[5].trim());
                    updated = true; break;
                }
            }
            if (updated) Files.write(path, lines);
            return updated;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Legacy wrapper
    // ─────────────────────────────────────────────────────────────────────────
    public User authenticateStaff(String email, String password, String role) {
        switch (role) {
            case "Manager":      return authenticateManagerByEmail(email, password);
            case "CounterStaff": return authenticateCounterStaffByEmail(email, password);
            case "Technician":   return authenticateTechnicianByEmail(email, password);
            default:             return null;
        }
    }
}