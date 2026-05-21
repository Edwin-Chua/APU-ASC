package models.customer_function;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {

    private static final String FILE_PATH = "src/txtFiles/feedback.txt";

    public Feedback getFeedbackByAppointmentAndTarget(String appointmentID, String targetType) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; }

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 8
                        && parts[1].equals(appointmentID)
                        && parts[3].equalsIgnoreCase(targetType)) {
                    return new Feedback(parts[0], parts[1], parts[2],
                            parts[3], parts[4], parts[5], parts[6], parts[7]);
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot read feedback.txt: " + e.getMessage());
        }
        return null;
    }

    public Feedback getFeedbackByAppointmentID(String appointmentID) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; }

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 8 && parts[1].equals(appointmentID)) {
                    return new Feedback(parts[0], parts[1], parts[2],
                            parts[3], parts[4], parts[5], parts[6], parts[7]);
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot read feedback.txt: " + e.getMessage());
        }
        return null;
    }

    public List<Feedback> getFeedbackByCustomerID(String customerID) {
        List<Feedback> feedbacks = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; }

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 8 && parts[2].equals(customerID)) {
                    feedbacks.add(new Feedback(parts[0], parts[1], parts[2],
                            parts[3], parts[4], parts[5], parts[6], parts[7]));
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot read feedback.txt: " + e.getMessage());
        }
        return feedbacks;
    }

    public boolean submitFeedback(String appointmentID, String customerID,
                                  String targetType, String targetID,
                                  String rating, String comment) {
        // prevent duplicate per appointment + target
        if (getFeedbackByAppointmentAndTarget(appointmentID, targetType) != null) {
            return false;
        }

        String newID = generateNewID();
        String date  = LocalDate.now().toString();
        String record = newID + "|" + appointmentID + "|" + customerID + "|"
                + targetType + "|" + targetID + "|" + comment + "|" + rating + "|" + date;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.newLine();
            bw.write(record);
            return true;
        } catch (IOException e) {
            System.out.println("Cannot write to feedback.txt: " + e.getMessage());
            return false;
        }
    }

    public boolean submitFeedback(String appointmentID, String customerID,
                                  String ratings, String comments) {
        return submitFeedback(appointmentID, customerID, "TECHNICIAN", "N/A", ratings, comments);
    }

    public List<Feedback> getFeedbackByTargetID(String targetID) {
        List<Feedback> feedbacks = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; }

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 8 && parts[4].equals(targetID)) {
                    feedbacks.add(new Feedback(parts[0], parts[1], parts[2],
                            parts[3], parts[4], parts[5], parts[6], parts[7]));
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot read feedback.txt: " + e.getMessage());
        }
        return feedbacks;
    }

    private String generateNewID() {
        int max = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts[0].matches("FB\\d+")) {
                    try {
                        int num = Integer.parseInt(parts[0].substring(2));
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception ignored) {}
        return String.format("FB%03d", max + 1);
    }
}