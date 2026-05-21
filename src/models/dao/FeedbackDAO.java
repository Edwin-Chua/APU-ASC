package models.dao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for feedback.txt — used by manager-side views.
 *
 * feedback.txt column layout (8 columns, header on line 1):
 *   0  feedback_id
 *   1  appointment_id
 *   2  customer_id
 *   3  target_type      (TECHNICIAN | STAFF)
 *   4  target_id        (technician_id or staff_id)
 *   5  comment
 *   6  rating           (1–5 integer as String)
 *   7  feedback_date
 */
public class FeedbackDAO {

    private static final String FEEDBACK_FILE    = "src/txtFiles/feedback.txt";
    private static final String APPOINTMENT_FILE = "src/txtFiles/appointment.txt";

    // =========================================================================
    // Inner record — one row of feedback.txt
    // =========================================================================
    public static class FeedbackRecord {
        public final String feedbackId;
        public final String appointmentId;
        public final String customerId;
        public final String targetType;   // "TECHNICIAN" or "STAFF"
        public final String targetId;
        public final String comment;
        public final String rating;       // "1"–"5"
        public final String date;

        public FeedbackRecord(String feedbackId, String appointmentId,
                              String customerId,  String targetType,
                              String targetId,    String comment,
                              String rating,      String date) {
            this.feedbackId    = feedbackId;
            this.appointmentId = appointmentId;
            this.customerId    = customerId;
            this.targetType    = targetType;
            this.targetId      = targetId;
            this.comment       = comment;
            this.rating        = rating;
            this.date          = date;
        }
    }

    // =========================================================================
    // Inner record — one merged row for the manager view table
    // Holds appointment data + both feedback records (either may be null)
    // =========================================================================
    public static class FeedbackSummary {
        public final String appointmentId;
        public final String customerId;
        public final String serviceTypeId;
        public final String technicianId;
        public final String staffId;
        public final String appointmentDate;
        public final String timeSlot;
        public final String vehicleBrand;
        public final String vehicleModel;
        public final String vehiclePlate;

        // May be null if customer didn't submit for that role
        public final FeedbackRecord techFeedback;
        public final FeedbackRecord staffFeedback;

        public FeedbackSummary(String appointmentId, String customerId,
                               String serviceTypeId,  String technicianId,
                               String staffId,        String appointmentDate,
                               String timeSlot,       String vehicleBrand,
                               String vehicleModel,   String vehiclePlate,
                               FeedbackRecord techFeedback,
                               FeedbackRecord staffFeedback) {
            this.appointmentId   = appointmentId;
            this.customerId      = customerId;
            this.serviceTypeId   = serviceTypeId;
            this.technicianId    = technicianId;
            this.staffId         = staffId;
            this.appointmentDate = appointmentDate;
            this.timeSlot        = timeSlot;
            this.vehicleBrand    = vehicleBrand;
            this.vehicleModel    = vehicleModel;
            this.vehiclePlate    = vehiclePlate;
            this.techFeedback    = techFeedback;
            this.staffFeedback   = staffFeedback;
        }

        /** True if at least one feedback record exists. */
        public boolean hasFeedback() {
            return techFeedback != null || staffFeedback != null;
        }
    }

    // =========================================================================
    // Core read — all feedback rows
    // =========================================================================
    public List<FeedbackRecord> getAllFeedbacks() {
        List<FeedbackRecord> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FEEDBACK_FILE))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; } // skip header

                String[] p = line.split("\\|", -1);
                if (p.length < 8) continue;

                list.add(new FeedbackRecord(
                    p[0].trim(), p[1].trim(), p[2].trim(),
                    p[3].trim(), p[4].trim(), p[5].trim(),
                    p[6].trim(), p[7].trim()
                ));
            }
        } catch (IOException e) {
            System.err.println("FeedbackDAO: cannot read feedback.txt — " + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // Stats
    // =========================================================================

    /** Total number of feedback rows. */
    public int getTotalFeedbackCount() {
        return getAllFeedbacks().size();
    }

    /**
     * Average rating for feedbacks of the given targetType ("TECHNICIAN" or "STAFF").
     * Returns -1.0 if no ratings exist (caller should show "N/A").
     */
    public double getAverageRating(String targetType) {
        double sum   = 0;
        int    count = 0;
        for (FeedbackRecord r : getAllFeedbacks()) {
            if (!r.targetType.equalsIgnoreCase(targetType)) continue;
            try {
                sum += Double.parseDouble(r.rating);
                count++;
            } catch (NumberFormatException ignored) {}
        }
        return count == 0 ? -1.0 : sum / count;
    }

    // =========================================================================
    // Manager view — merged summaries
    //
    // Rules:
    //   • Only Completed appointments
    //   • Only if at least one feedback record exists for that appointment
    //   • One FeedbackSummary per appointment (techFeedback / staffFeedback
    //     may each be null if the customer didn't submit for that role)
    // =========================================================================
    public List<FeedbackSummary> getFeedbackSummaries() {
        // Step 1: load all feedbacks, group by appointmentId
        List<FeedbackRecord> allFb = getAllFeedbacks();

        java.util.Map<String, FeedbackRecord> techMap  = new java.util.LinkedHashMap<>();
        java.util.Map<String, FeedbackRecord> staffMap = new java.util.LinkedHashMap<>();
        java.util.Set<String> apptIdsWithFeedback      = new java.util.LinkedHashSet<>();

        for (FeedbackRecord fb : allFb) {
            apptIdsWithFeedback.add(fb.appointmentId);
            if ("TECHNICIAN".equalsIgnoreCase(fb.targetType)) {
                techMap.put(fb.appointmentId, fb);
            } else if ("STAFF".equalsIgnoreCase(fb.targetType)) {
                staffMap.put(fb.appointmentId, fb);
            }
        }

        if (apptIdsWithFeedback.isEmpty()) return new ArrayList<>();

        // Step 2: load appointments, keep only Completed ones that have feedback
        List<FeedbackSummary> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(APPOINTMENT_FILE))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (first) { first = false; continue; }

                String[] p = line.split("\\|", -1);
                if (p.length < 13) continue;

                String apptId = p[0].trim();
                String status = p[8].trim();

                // Must be Paid AND have at least one feedback
                if (!"Paid".equalsIgnoreCase(status)) continue;
                if (!apptIdsWithFeedback.contains(apptId))  continue;

                // Swap legacy date/time if needed (col6=time, col7=date normally)
                String col6 = p[6].trim();
                String col7 = p[7].trim();
                if (looksLikeDate(col6) && !looksLikeDate(col7)) {
                    String tmp = col6; col6 = col7; col7 = tmp;
                }

                result.add(new FeedbackSummary(
                    apptId,          // appointmentId
                    p[1].trim(),     // customerId
                    p[3].trim(),     // serviceTypeId
                    p[4].trim(),     // technicianId
                    p[2].trim(),     // staffId
                    col7,            // appointmentDate (yyyy-MM-dd)
                    col6,            // timeSlot
                    p[9].trim(),     // vehicleBrand
                    p[10].trim(),    // vehicleModel
                    p[11].trim(),    // vehiclePlate
                    techMap.get(apptId),   // may be null
                    staffMap.get(apptId)   // may be null
                ));
            }
        } catch (IOException e) {
            System.err.println("FeedbackDAO: cannot read appointment.txt — " + e.getMessage());
        }

        // Sort newest date first
        result.sort((a, b) -> b.appointmentDate.compareTo(a.appointmentDate));
        return result;
    }

    // =========================================================================
    // Helpers
    // =========================================================================
    private boolean looksLikeDate(String s) {
        return s != null && s.matches("\\d{4}-\\d{2}-\\d{2}");
    }
}