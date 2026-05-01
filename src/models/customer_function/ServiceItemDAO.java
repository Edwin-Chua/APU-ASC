package models.customer_function;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceItemDAO {

    private static final String FILE_PATH = "src/txtFiles/service_items.txt";

    // service_items.txt format (row 1 = header, skip it):
    // service_item_id|service_price_id|category|duration|description

    public List<ServiceItem> getAllServiceItems() {
        List<ServiceItem> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; }
                String[] p = line.split("\\|", -1);
                if (p.length >= 5)
                    list.add(new ServiceItem(
                        p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), p[4].trim()));
            }
        } catch (IOException e) {
            System.err.println("Error reading service_items.txt: " + e.getMessage());
        }
        return list;
    }

    public ServiceItem getServiceItemByID(String serviceItemID) {
        for (ServiceItem s : getAllServiceItems())
            if (s.getServiceItemID().equals(serviceItemID)) return s;
        return null;
    }

    public List<ServiceItem> getServiceItemsByCategory(String category) {
        List<ServiceItem> list = new ArrayList<>();
        for (ServiceItem s : getAllServiceItems())
            if (s.getCategory().equals(category)) list.add(s);
        return list;
    }

    public String getServiceTypeByServiceItemID(String serviceItemID) {
        ServiceItem s = getServiceItemByID(serviceItemID);
        return s != null ? s.getCategory() : null;
    }
}