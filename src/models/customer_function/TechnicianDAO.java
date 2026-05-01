package models.customer_function;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TechnicianDAO {

    private static final String FILE_PATH = "src/txtFiles/technician.txt";

    // technician.txt format (row 1 = header, skip it):
    // technician_id|technician_name|technician_email|technician_password|status|specialization

    public List<Technician> getAllTechnicianList() {
        List<Technician> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; }
                String[] p = line.split("\\|", -1);
                if (p.length >= 6)
                    list.add(new Technician(
                        p[0].trim(), p[1].trim(), p[2].trim(),
                        p[3].trim(), p[4].trim(), p[5].trim()));
            }
        } catch (IOException e) {
            System.err.println("Error reading technician.txt: " + e.getMessage());
        }
        return list;
    }

    public String getTechnicianName(String technicianID) {
        for (Technician t : getAllTechnicianList())
            if (t.getTechnicianID().equals(technicianID)) return t.getTechnicianName();
        return null;
    }
}