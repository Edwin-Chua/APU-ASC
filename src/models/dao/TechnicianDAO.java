package models.dao;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class TechnicianDAO {

    private static final String FILE_PATH = "src/txtFiles/technician.txt";

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