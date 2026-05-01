package models.customer_function;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {

    private static final String FILE_PATH = "src/txtFiles/feedback.txt";

    // feedback.txt format (row 1 = header, skip it):
    // feedback_id|appointment_id|customer_id|comment|rating|date

    private List<Feedback> getAllFeedback() {
        List<Feedback> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; }
                String[] p = line.split("\\|", -1);
                if (p.length >= 6)
                    list.add(new Feedback(p[0], p[1], p[2], p[3], p[4], p[5]));
            }
        } catch (IOException e) {
            System.err.println("Cannot read feedback.txt: " + e.getMessage());
        }
        return list;
    }

    public Feedback getFeedbackByAppointmentID(String appointmentID) {
        for (Feedback f : getAllFeedback())
            if (f.getAppointmentID().trim().equals(appointmentID)) return f;
        return null;
    }

    public List<Feedback> getFeedbackByCustomerID(String customerID) {
        List<Feedback> list = new ArrayList<>();
        for (Feedback f : getAllFeedback())
            if (f.getCustomerID().trim().equals(customerID)) list.add(f);
        return list;
    }

    public boolean submitFeedback(String appointmentID, String customerID,
                                  String rating, String comment) {
        if (getFeedbackByAppointmentID(appointmentID) != null) return false;

        String id   = generateNewID();
        String date = LocalDate.now().toString();
        String line = String.join("|", id, appointmentID, customerID, comment, rating, date);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.newLine();
            bw.write(line);
            return true;
        } catch (IOException e) {
            System.err.println("Cannot write to feedback.txt: " + e.getMessage());
            return false;
        }
    }

    private String generateNewID() {
        int max = 0;
        for (Feedback f : getAllFeedback()) {
            if (f.getFeedbackID().startsWith("FB")) {
                try {
                    int num = Integer.parseInt(f.getFeedbackID().substring(2));
                    if (num > max) max = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("FB%03d", max + 1);
    }
}