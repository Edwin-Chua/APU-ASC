package models.dao;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ServiceItemDAO {

    private static final String FILE_PATH = "src/txtFiles/serviceItem.txt";

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
}