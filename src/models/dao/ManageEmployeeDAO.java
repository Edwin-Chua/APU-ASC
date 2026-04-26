package models.dao;

import models.entities.ManageEmployee;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ManageEmployeeDAO {

    private static final String BASE_PATH       = "src/txtFiles/";
    private static final String MANAGER_FILE    = BASE_PATH + "manager.txt";
    private static final String STAFF_FILE      = BASE_PATH + "counterStaff.txt";
    private static final String TECH_FILE       = BASE_PATH + "technician.txt";

    // ─── Read all ─────────────────────────────────────────────────────────────

    public List<ManageEmployee> getAllManagers() {
        List<ManageEmployee> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(MANAGER_FILE))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; if (line.toLowerCase().startsWith("managerid|")) continue; }
                String[] p = line.split("\\|", -1);
                if (p.length < 5) continue;
                list.add(new ManageEmployee(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), "Manager", p[4].trim()));
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    public List<ManageEmployee> getAllCounterStaff() {
        List<ManageEmployee> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(STAFF_FILE))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; if (line.toLowerCase().startsWith("staffid|")) continue; }
                String[] p = line.split("\\|", -1);
                if (p.length < 5) continue;
                list.add(new ManageEmployee(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), "CounterStaff", p[4].trim()));
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    public List<ManageEmployee> getAllTechnicians() {
        List<ManageEmployee> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(TECH_FILE))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; if (line.toLowerCase().startsWith("technicianid|")) continue; }
                String[] p = line.split("\\|", -1);
                if (p.length < 6) continue;
                // col: TechnicianID|username|email|password|work_status|createdDate
                list.add(new ManageEmployee(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), "Technician", p[5].trim()));
            }
        } catch (IOException e) { e.printStackTrace(); }
        return list;
    }

    /** Returns all employees merged: Managers first, then CounterStaff, then Technicians. */
    public List<ManageEmployee> getAllEmployees() {
        List<ManageEmployee> all = new ArrayList<>();
        all.addAll(getAllManagers());
        all.addAll(getAllCounterStaff());
        all.addAll(getAllTechnicians());
        return all;
    }

    // ─── Add ──────────────────────────────────────────────────────────────────

    public boolean addEmployee(String role, String username, String email, String password) {
        String today = LocalDate.now().toString();
        String newId = generateNextId(role);
        if (newId == null) return false;

        String filePath = fileForRole(role);
        if (filePath == null) return false;

        String newLine;
        if ("Technician".equals(role)) {
            // TechnicianID|username|email|password|work_status|createdDate
            newLine = newId + "|" + username + "|" + email + "|" + password + "|Active|" + today;
        } else {
            // ID|username|email|password|createdDate
            newLine = newId + "|" + username + "|" + email + "|" + password + "|" + today;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(newLine);
            bw.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Edit ─────────────────────────────────────────────────────────────────

    /**
     * Updates username, email, and optionally password for an employee.
     * If newPassword is blank, existing password is kept.
     */
    public boolean editEmployee(String role, String id, String newUsername, String newEmail, String newPassword) {
        String filePath = fileForRole(role);
        if (filePath == null) return false;

        Path path = Paths.get(filePath);
        try {
            List<String> lines = Files.readAllLines(path);
            List<String> updated = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || isHeaderLine(trimmed)) {
                    updated.add(line);
                    continue;
                }
                String[] p = line.split("\\|", -1);
                if (p[0].trim().equals(id)) {
                    // col 1=username, 2=email, 3=password (all roles share same index for these)
                    p[1] = newUsername;
                    p[2] = newEmail;
                    if (newPassword != null && !newPassword.isBlank()) {
                        p[3] = newPassword;
                    }
                    updated.add(String.join("|", p));
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

    // ─── Delete ───────────────────────────────────────────────────────────────

    public boolean deleteEmployee(String role, String id) {
        String filePath = fileForRole(role);
        if (filePath == null) return false;

        Path path = Paths.get(filePath);
        try {
            List<String> lines = Files.readAllLines(path);
            List<String> updated = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || isHeaderLine(trimmed)) {
                    updated.add(line);
                    continue;
                }
                String[] p = line.split("\\|", -1);
                if (p[0].trim().equals(id)) {
                    found = true; // skip this line = delete
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

    // ─── Check duplicate email ────────────────────────────────────────────────

    public boolean isEmailTaken(String email) {
        for (ManageEmployee emp : getAllEmployees()) {
            if (emp.getEmail().equalsIgnoreCase(email)) return true;
        }
        return false;
    }

    public boolean isEmailTakenExcluding(String email, String excludeId) {
        for (ManageEmployee emp : getAllEmployees()) {
            if (emp.getEmail().equalsIgnoreCase(email) && !emp.getId().equals(excludeId)) return true;
        }
        return false;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String fileForRole(String role) {
        switch (role) {
            case "Manager":      return MANAGER_FILE;
            case "CounterStaff": return STAFF_FILE;
            case "Technician":   return TECH_FILE;
            default:             return null;
        }
    }

    private boolean isHeaderLine(String line) {
        String low = line.toLowerCase();
        return low.startsWith("managerid|") || low.startsWith("staffid|") || low.startsWith("technicianid|") || low.startsWith("userid|");
    }

    private String generateNextId(String role) {
        List<ManageEmployee> existing;
        String prefix;
        int padLen;

        switch (role) {
            case "Manager":      existing = getAllManagers();     prefix = "M";  padLen = 3; break;
            case "CounterStaff": existing = getAllCounterStaff(); prefix = "CS"; padLen = 3; break;
            case "Technician":   existing = getAllTechnicians();  prefix = "T";  padLen = 3; break;
            default: return null;
        }

        int max = 0;
        for (ManageEmployee emp : existing) {
            String id = emp.getId();
            String numPart = id.replaceAll("[^0-9]", "");
            try { int n = Integer.parseInt(numPart); if (n > max) max = n; } catch (NumberFormatException ignored) {}
        }

        return prefix + String.format("%0" + padLen + "d", max + 1);
    }
}