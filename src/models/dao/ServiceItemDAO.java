package models.dao;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ServiceItemDAO {

    private static final String FILE_PATH = "src/txtFiles/serviceItem.txt";

    // Returns String[] { serviceItemId, itemName, serviceTypeId, description }
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
                if (p.length < 4) continue;
                list.add(new String[]{
                    p[0].trim(), // SI001
                    p[1].trim(), // Engine Oil Change
                    p[2].trim(), // ST001
                    p[3].trim()  // description
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String getServiceTypeId(String serviceItemId) {
        for (String[] item : getAllServiceItems()) {
            if (item[0].equals(serviceItemId)) return item[2];
        }
        return "ST001"; // fallback
    }

    // Returns the human-readable service item name (col 1) for a given serviceItemId.
    // e.g. "SI001" → "Engine Oil Change"
    // Returns the raw id as fallback if not found.
    public static String getServiceName(String serviceItemId) {
        if (serviceItemId == null || serviceItemId.isBlank() || serviceItemId.equals("null")) {
            return "-";
        }
        for (String[] item : getAllServiceItems()) {
            if (item[0].equals(serviceItemId)) return item[1];
        }
        return serviceItemId; // fallback: show the id rather than blank
    }
}