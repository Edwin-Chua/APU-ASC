package models.customer_function;

import java.io.*;
import java.util.*;

public class ServiceItemDAO {

    private static final String FILE_PATH = "src/txtFiles/service_items.txt";
    private static final String TYPE_FILE = "src/txtFiles/service_type.txt";

    // ─── service_items.txt (6 columns) ────────────────────────────────────
    // 0  service_item_id
    // 1  service_type_id   (ST001 / ST002)
    // 2  service_name      (Oil Change, etc.)
    // 3  price
    // 4  update_date
    // 5  description

    // ─── service_type.txt (3 columns) ─────────────────────────────────────
    // 0  service_type_id
    // 1  service_type_name  (Normal Service / Major Service)
    // 2  duration           (1 / 3)

    /** Loads service_type.txt → { "ST001" → ["Normal Service","1"], ... } */
    private Map<String, String[]> loadServiceTypes() {
        Map<String, String[]> map = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(TYPE_FILE))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; }
                String[] p = line.split("\\|", -1);
                if (p.length >= 3) {
                    // key = service_type_id, value = [name, duration]
                    map.put(p[0].trim(), new String[]{ p[1].trim(), p[2].trim() });
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading service_type.txt: " + e.getMessage());
        }
        return map;
    }

    public List<ServiceItem> getAllServiceItems() {
        Map<String, String[]> types = loadServiceTypes();
        List<ServiceItem> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; }

                String[] parts = line.split("\\|", -1);
                if (parts.length < 6) continue;

                String typeId = parts[1].trim();

                // Resolve display name & duration from service_type.txt
                String categoryName;
                String duration;
                if (types.containsKey(typeId)) {
                    categoryName = types.get(typeId)[0];   // "Normal Service"
                    duration     = types.get(typeId)[1];   // "1"
                } else {
                    categoryName = typeId;                  // fallback
                    duration     = "1";
                }

                ServiceItem s = new ServiceItem(
                    parts[0].trim(),   // serviceItemID   (SI001)
                    parts[3].trim(),   // servicePriceID  (50.00)
                    categoryName,      // category        ("Normal Service")
                    duration,          // duration        ("1")
                    parts[2].trim()    // description     ("Oil Change")
                );
                list.add(s);
            }
        } catch (IOException e) {
            System.err.println("Error reading service_items.txt: " + e.getMessage());
        }
        return list;
    }

    public ServiceItem getServiceItemByID(String serviceItemID) {
        for (ServiceItem s : getAllServiceItems()) {
            if (s.getServiceItemID().equals(serviceItemID)) {
                return s;
            }
        }
        return null;
    }

    public List<ServiceItem> getServiceItemsByCategory(String category) {
        List<ServiceItem> list = new ArrayList<>();
        for (ServiceItem s : getAllServiceItems()) {
            if (s.getCategory().equals(category)) {
                list.add(s);
            }
        }
        return list;
    }

    public String getServiceTypeByServiceItemID(String serviceItemID) {
        // Read raw service_type_id from file (for writing to appointment.txt)
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; }
                String[] p = line.split("\\|", -1);
                if (p.length >= 2 && p[0].trim().equals(serviceItemID)) {
                    return p[1].trim(); // returns "ST001" or "ST002"
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading service_items.txt: " + e.getMessage());
        }
        return null;
    }
}