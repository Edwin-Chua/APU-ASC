package models.dao;

import models.entities.Appointment;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AppointmentDAO {

    private static final String FILE_PATH = "src/txtFiles/appointment.txt";

    // ─── Column index reference (pipe-delimited) ───────────────────────────────
    // 0  appointment_id
    // 1  customer_id
    // 2  staff_id
    // 3  service_type_id
    // 4  technician_id
    // 5  service_item_id
    // 6  time_slot
    // 7  appointment_date
    // 8  status
    // 9  comment_id
    // 10 vehicle_brand
    // 11 vehicle_model
    // 12 vehicle_plate
    // 13 technician_feedback

    // ─── Read all ──────────────────────────────────────────────────────────────
    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
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
                if (p.length < 14) continue;
                list.add(new Appointment(
                    p[0].trim(),  // appointmentId
                    p[1].trim(),  // customerId
                    p[2].trim(),  // staffId
                    p[3].trim(),  // serviceTypeId
                    p[4].trim(),  // technicianId
                    p[5].trim(),  // serviceItemId
                    p[6].trim(),  // timeSlot
                    p[7].trim(),  // appointmentDate
                    p[8].trim(),  // status
                    p[9].trim(),  // commentId
                    p[10].trim(), // vehicleBrand
                    p[11].trim(), // vehicleModel
                    p[12].trim(), // vehiclePlate
                    p[13].trim()  // technicianFeedback
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── Get by status ─────────────────────────────────────────────────────────
    public List<Appointment> getAppointmentsByStatus(String status) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : getAllAppointments()) {
            if (a.getStatus().equalsIgnoreCase(status)) result.add(a);
        }
        return result;
    }

    // ─── Get by ID ─────────────────────────────────────────────────────────────
    public Appointment getAppointmentById(String id) {
        for (Appointment a : getAllAppointments()) {
            if (a.getAppointmentId().equals(id)) return a;
        }
        return null;
    }

    // ─── Approve: status → Approved, assign technicianId + staffId + notes ─────
    public boolean approveAppointment(String apptId, String techId, String staffId, String notes) {
        return updateFields(apptId, map -> {
            map.put(2, staffId);     // col 2 = staff_id
            map.put(4, techId);      // col 4 = technician_id
            map.put(8, "Approved");  // col 8 = status
            if (notes != null && !notes.isEmpty()) {
                map.put(9, notes);   // col 9 = comment_id
            }
        });
    }

    // ─── Reject: status → Rejected ────────────────────────────────────────────
    public boolean rejectAppointment(String apptId) {
        return updateFields(apptId, map -> map.put(8, "Rejected"));
    }

    // ─── Cancel: status → Cancelled, clear staff + technician ────────────────
    public boolean cancelAppointment(String apptId) {
        return updateFields(apptId, map -> {
            map.put(2, "null");      // col 2 = staff_id → clear
            map.put(4, "null");      // col 4 = technician_id → clear
            map.put(8, "Cancelled"); // col 8 = status
        });
    }

    // ─── Complete: status → Completed ─────────────────────────────────────────
    public boolean completeAppointment(String apptId) {
        return updateFields(apptId, map -> map.put(8, "Completed"));
    }

    // ─── Add new appointment ───────────────────────────────────────────────────
    public boolean addAppointment(Appointment a) {
        try {
            List<String> lines = new ArrayList<>();
            Path path = Paths.get(FILE_PATH);
            if (Files.exists(path)) {
                lines = new ArrayList<>(Files.readAllLines(path));
            } else {
                lines.add("appointment_id|customer_id|staff_id|service_type_id|technician_id"
                    + "|service_item_id|time_slot|appointment_date|status"
                    + "|comment_id|vehicle_brand|vehicle_model|vehicle_plate|technician_feedback");
            }

            String newLine = String.join("|",
                a.getAppointmentId(), a.getCustomerId(),
                nvl(a.getStaffId()),       a.getServiceTypeId(),
                nvl(a.getTechnicianId()),  nvl(a.getServiceItemId()),
                a.getTimeSlot(),           a.getAppointmentDate(),
                a.getStatus(),             nvl(a.getCommentId()),
                a.getVehicleBrand(),       a.getVehicleModel(),
                a.getVehiclePlate(),       nvl(a.getTechnicianFeedback())
            );
            lines.add(newLine);
            Files.write(path, lines);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String generateAppointmentId() {
        int max = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|", -1);
                if (parts[0].startsWith("A")) {
                    try {
                        int num = Integer.parseInt(parts[0].substring(1));
                        if (num > max) {
                            max = num;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading appointments for ID generation: " + e.getMessage());
        }
        return String.format("A%03d", max + 1);
    }

    // ─── Internal: generic field updater ──────────────────────────────────────
    private boolean updateFields(String apptId, java.util.function.Consumer<Map<Integer, String>> editor) {
        Path path = Paths.get(FILE_PATH);
        try {
            List<String> lines  = Files.readAllLines(path);
            List<String> result = new ArrayList<>();
            boolean found = false;

            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("appointment_id")) {
                    result.add(line);
                    continue;
                }
                String[] parts = trimmed.split("\\|", -1);
                if (parts[0].trim().equals(apptId)) {
                    Map<Integer, String> edits = new LinkedHashMap<>();
                    editor.accept(edits);
                    for (Map.Entry<Integer, String> e : edits.entrySet()) {
                        if (e.getKey() < parts.length) parts[e.getKey()] = e.getValue();
                    }
                    result.add(String.join("|", parts));
                    found = true;
                } else {
                    result.add(line);
                }
            }

            if (!found) return false;
            Files.write(path, result);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Resolve service name for display ─────────────────────────────────────
    public String getServiceName(Appointment a) {
        return ServiceItemDAO.getServiceName(a.getServiceItemId());
    }

    // ─── Null-safe helper ─────────────────────────────────────────────────────
    private String nvl(String s) {
        return (s == null || s.isEmpty()) ? "null" : s;
    }
}