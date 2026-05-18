package models.dao;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

public class ServiceItemDAO {

    private static final String FILE_PATH = "src/txtFiles/service_items.txt";

    // ─── Column index reference (pipe-delimited) ───────────────────────────────
    // 0  service_item_id
    // 1  service_type_id
    // 2  service_name
    // 3  price
    // 4  update_date
    // 5  description

    // Returns String[] { serviceItemId, serviceTypeId, serviceName, price, updateDate, description }
    public static List<String[]> getAllServiceItems() {
        List<String[]> list = new ArrayList<>();
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
                list.add(new String[]{
                    p[0].trim(), // service_item_id
                    p[1].trim(), // service_type_id
                    p[2].trim(), // service_name
                    p[3].trim(), // price
                    p[4].trim(), // update_date
                    p[5].trim()  // description
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String getServiceTypeId(String serviceItemId) {
        for (String[] item : getAllServiceItems()) {
            if (item[0].equals(serviceItemId)) return item[1]; // col 1 = service_type_id
        }
        return "ST001"; // fallback
    }

    // Returns the human-readable service item name (col 2) for a given serviceItemId.
    // e.g. "SI001" → "Oil Change"
    // Returns the raw id as fallback if not found.
    public static String getServiceName(String serviceItemId) {
        if (serviceItemId == null || serviceItemId.isBlank() || serviceItemId.equals("null")) {
            return "-";
        }
        for (String[] item : getAllServiceItems()) {
            if (item[0].equals(serviceItemId)) return item[2]; // col 2 = service_name
        }
        return serviceItemId; // fallback: show the id rather than blank
    }
    
    // ─── Create ────────────────────────────────────────────────────────────────
    /**
     * Appends a new service item to service_items.txt.
     * @param typeId      e.g. "ST001" or "ST002"
     * @param name        e.g. "Oil Change"
     * @param price       e.g. "50.00"
     * @param description e.g. "Standard engine oil replacement"
     */
    public static boolean addServiceItem(String typeId, String name, String price, String description) {
        String newId = generateNextId();
        String date  = LocalDate.now().toString();
        String line  = newId + "|" + typeId + "|" + name + "|" + price + "|" + date + "|" + description;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(line);
            bw.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    // ─── Update ────────────────────────────────────────────────────────────────
    /**
     * Updates the name, price, and description of an existing service item.
     * Also sets update_date to today.
     * @return true if the item was found and updated
     */
    public static boolean updateServiceItem(String itemId, String name, String price, String description) {
        try {
            Path path = Paths.get(FILE_PATH);
            List<String> lines   = Files.readAllLines(path);
            List<String> updated = new ArrayList<>();
            boolean found = false;
            for (String line : lines) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 6 && p[0].trim().equals(itemId)) {
                    updated.add(itemId + "|" + p[1].trim() + "|" + name + "|"
                        + price + "|" + LocalDate.now() + "|" + description);
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
    // ─── Delete ────────────────────────────────────────────────────────────────
    /**
     * Removes the service item with the given ID from service_items.txt.
     * @return true if the item was found and removed
     */
    public static boolean deleteServiceItem(String itemId) {
        try {
            Path path = Paths.get(FILE_PATH);
            List<String> lines   = Files.readAllLines(path);
            List<String> updated = new ArrayList<>();
            boolean found = false;
            for (String line : lines) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 1 && p[0].trim().equals(itemId)) {
                    found = true; // skip this line = delete it
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
    // ─── ID generator ──────────────────────────────────────────────────────────
    /** Generates the next sequential service item ID (e.g. "SI011"). */
    public static String generateNextId() {
        List<String[]> all = getAllServiceItems();
        int max = 0;
        for (String[] item : all) {
            String num = item[0].replaceAll("[^0-9]", "");
            try { int n = Integer.parseInt(num); if (n > max) max = n; }
            catch (NumberFormatException ignored) {}
        }
        return "SI" + String.format("%03d", max + 1);
    }
}