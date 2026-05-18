package models.dao;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ServiceTypeDAO {

    private static final String FILE_PATH = "src/txtFiles/service_type.txt";

    // ─── Column index reference (pipe-delimited) ───────────────────────────────
    // 0  service_type_id
    // 1  service_type_name
    // 2  duration

    /**
     * Returns all service types as String[3]:
     *   [0] service_type_id, [1] service_type_name, [2] duration
     */
    public static List<String[]> getAllServiceTypes() {
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
                if (p.length < 3) continue;
                list.add(new String[]{ p[0].trim(), p[1].trim(), p[2].trim() });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Returns the service type name for a given typeId (e.g. "ST001" → "Normal Service").
     * Falls back to the typeId itself if not found.
     */
    public static String getServiceTypeName(String typeId) {
        for (String[] type : getAllServiceTypes()) {
            if (type[0].equals(typeId)) return type[1];
        }
        return typeId;
    }
}