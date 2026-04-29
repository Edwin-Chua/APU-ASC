package models.dao;

import models.entities.Appointment;
import models.dao.ServiceItemDAO;

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
    // 7  end_time
    // 8  appointment_date
    // 9  status
    // 10 customer_comment
    // 11 vehicle_brand
    // 12 vehicle_model
    // 13 vehicle_plate

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
                    p[7].trim(),  // endTime
                    p[8].trim(),  // appointmentDate
                    p[9].trim(),  // status
                    p[10].trim(), // customerComment
                    p[11].trim(), // vehicleBrand
                    p[12].trim(), // vehicleModel
                    p[13].trim()  // vehiclePlate
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
    // NOTE: was "Confirmed" before — changed to "Approved" to match UI and schema.
    public boolean approveAppointment(String apptId, String techId, String staffId, String notes) {
        return updateFields(apptId, map -> {
            map.put(2, staffId);     // col 2 = staff_id
            map.put(4, techId);      // col 4 = technician_id
            map.put(9, "Approved");  // col 9 = status  ← fixed from "Confirmed"
            if (notes != null && !notes.isEmpty()) {
                map.put(10, notes);  // col 10 = customer_comment
            }
        });
    }

    // ─── Reject: status → Rejected ────────────────────────────────────────────
    public boolean rejectAppointment(String apptId) {
        return updateFields(apptId, map -> map.put(9, "Rejected"));
    }

    // ─── Cancel: status → Cancelled, clear staff + technician ────────────────
    // Used by counter staff to cancel an Approved appointment.
    // Clears staff_id and technician_id so the technician can be freed.
    public boolean cancelAppointment(String apptId) {
        return updateFields(apptId, map -> {
            map.put(2, "null");      // col 2 = staff_id → clear
            map.put(4, "null");      // col 4 = technician_id → clear
            map.put(9, "Cancelled"); // col 9 = status
        });
    }

    // ─── Complete: status → Completed ─────────────────────────────────────────
    public boolean completeAppointment(String apptId) {
        return updateFields(apptId, map -> map.put(9, "Completed"));
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
                    + "|service_item_id|time_slot|end_time|appointment_date|status"
                    + "|customer_comment|vehicle_brand|vehicle_model|vehicle_plate");
            }

            String newLine = String.join("|",
                a.getAppointmentId(), a.getCustomerId(),
                nvl(a.getStaffId()),      a.getServiceTypeId(),
                nvl(a.getTechnicianId()), nvl(a.getServiceItemId()),
                a.getTimeSlot(),          nvl(a.getEndTime()),
                a.getAppointmentDate(),   a.getStatus(),
                nvl(a.getCustomerComment()),
                a.getVehicleBrand(),      a.getVehicleModel(),
                a.getVehiclePlate()
            );
            lines.add(newLine);
            Files.write(path, lines);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Generate next appointment ID ─────────────────────────────────────────
    public String generateAppointmentId() {
        int max = 0;
        for (Appointment a : getAllAppointments()) {
            try {
                int num = Integer.parseInt(a.getAppointmentId().replace("AP", ""));
                if (num > max) max = num;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("AP%03d", max + 1);
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
    // Converts the serviceItemId stored on an appointment into the item's display
    // name, e.g. "SI001" → "Engine Oil Change".
    // Use this instead of showing serviceTypeId or raw IDs in the UI.
    public String getServiceName(Appointment a) {
        return ServiceItemDAO.getServiceName(a.getServiceItemId());
    }

    // ─── Null-safe helper ─────────────────────────────────────────────────────
    private String nvl(String s) {
        return (s == null || s.isEmpty()) ? "null" : s;
    }
}