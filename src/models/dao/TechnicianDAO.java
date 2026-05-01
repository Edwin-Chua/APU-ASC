package models.dao;

import models.entities.Technician;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TechnicianDAO {

    private static final String FILE_PATH = "src/txtFiles/technician.txt";

    // ─── Column index reference (pipe-delimited) ───────────────────────────────
    // 0  technician_id
    // 1  username
    // 2  email
    // 3  password
    // 4  workStatus
    // 5  createdDate

    // ─── Read all ──────────────────────────────────────────────────────────────
    public List<Technician> getAllTechnicians() {
        List<Technician> list = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; } // skip header
                String[] p = line.split("\\|", -1);
                if (p.length < 6) continue;
                list.add(new Technician(
                    p[0].trim(), // technicianId
                    p[1].trim(), // username
                    p[2].trim(), // email
                    p[3].trim(), // password
                    p[4].trim(), // workStatus
                    p[5].trim()  // createdDate
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── Available only ────────────────────────────────────────────────────────
    public List<Technician> getAvailableTechnicians() {
        List<Technician> result = new ArrayList<>();
        for (Technician t : getAllTechnicians()) {
            if (t.getWorkStatus().equalsIgnoreCase("Available")) result.add(t);
        }
        return result;
    }

    // ─── Get by ID ─────────────────────────────────────────────────────────────
    public Technician getTechnicianById(String id) {
        for (Technician t : getAllTechnicians()) {
            if (t.getTechnicianId().equals(id)) return t;
        }
        return null;
    }

    // ─── Update work status ────────────────────────────────────────────────────
    public boolean updateWorkStatus(String techId, String newStatus) {
        Path path = Paths.get(FILE_PATH);
        try {
            List<String> lines  = Files.readAllLines(path);
            List<String> result = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.toLowerCase().startsWith("technician_id")) {
                    result.add(line);
                    continue;
                }
                String[] parts = trimmed.split("\\|", -1);
                if (parts.length >= 6 && parts[0].trim().equals(techId)) {
                    parts[4] = newStatus; // col 4 = workStatus
                    result.add(String.join("|", parts));
                    found = true;
                } else {
                    result.add(line);
                }
            }

            if (!found) return false;
            Files.write(path, result);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    

    public boolean updateUsername(String userId, String newUsername) {
        return updateField(userId, 1, newUsername);
    }

    public boolean updatePassword(String userId, String newPassword) {
        return updateField(userId, 2, newPassword);
    }

    private boolean updateField(String userId, int colIndex, String newValue) {
        Path path = Paths.get(FILE_PATH);

        try {
            List<String> lines = Files.readAllLines(path);
            List<String> updatedLines = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    updatedLines.add(line);
                    continue;
                }

                if (line.toLowerCase().startsWith("userid")) {
                    updatedLines.add(line);
                    continue;
                }

                String[] parts = line.split("\\|", -1);

                if (parts.length > colIndex && parts[0].trim().equals(userId)) {
                    parts[colIndex] = newValue;
                    updatedLines.add(String.join("|", parts));
                    found = true;
                } else {
                    updatedLines.add(line);
                }
            }

            if (!found) {
                return false;
            }

            Files.write(path, updatedLines);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}