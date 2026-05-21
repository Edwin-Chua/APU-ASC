package models.customer_function;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    private static final String FILE_PATH = "src/txtFiles/appointment.txt";

    // ─── appointment.txt 13-column layout ──────────────────────────────────
    // 0  appointment_id
    // 1  customer_id
    // 2  staff_id
    // 3  service_type_id
    // 4  technician_id
    // 5  service_item_id
    // 6  start_time        (e.g. "10:00", "09:00 AM")
    // 7  appointment_date   (e.g. "2026-04-26")
    // 8  status
    // 9  vehicle_brand
    // 10 vehicle_model
    // 11 vehicle_plate
    // 12 technician_feedback

    public List<Appointment> getAllAppointments() {
        List<Appointment> appointments = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; }

                String[] p = line.split("\\|", -1);
                if (p.length < 13) continue;

                String col6 = p[6].trim();
                String col7 = p[7].trim();

                // Auto-fix swapped date/time in legacy rows
                if (looksLikeDate(col6) && !looksLikeDate(col7)) {
                    String tmp = col6;
                    col6 = col7;
                    col7 = tmp;
                }

                LocalDate date;
                try {
                    date = LocalDate.parse(col7);
                } catch (Exception e) {
                    continue;
                }

                String serviceTypeId   = p[3].trim();
                String serviceTypeName = resolveServiceTypeName(serviceTypeId, p[5].trim());

                Appointment a = new Appointment(
                    p[0].trim(),       // appointmentID
                    p[1].trim(),       // customerID
                    p[2].trim(),       // staffID
                    serviceTypeId,     // serviceTypeID
                    p[4].trim(),       // technicianID
                    p[5].trim(),       // serviceItemID
                    date,              // appointmentDate
                    col6,              // startTime
                    "N/A",             // endTime
                    p[8].trim(),       // status
                    p[9].trim(),       // vehicleBrand
                    p[10].trim(),      // vehicleModel
                    p[11].trim(),      // vehiclePlate
                    p[12].trim(),      // command
                    serviceTypeName    // serviceType
                );
                appointments.add(a);
            }

        } catch (IOException e) {
            System.err.println("Error reading appointment data: " + e.getMessage());
        }
        return appointments;
    }

    private boolean looksLikeDate(String s) {
        return s != null && s.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    /**
     * 解析服务类型名称：
     *  1. 先从 service_type.txt 按 serviceTypeId 查询
     *  2. 若 serviceTypeId 无效，则从 service_items.txt 按 serviceItemId 反查 service_type_id，再查名称
     *  3. 再找不到 → 返回 "Unknown Service"
     */
    private String resolveServiceTypeName(String serviceTypeId, String serviceItemId) {
        // Step 1: 尝试从 service_type.txt 查 serviceTypeId
        String name = lookupServiceTypeName(serviceTypeId);
        if (name != null) return name;

        // Step 2: serviceTypeId 无效，改用 serviceItemId 反查
        if (serviceItemId != null && !serviceItemId.isEmpty()
                && !"null".equalsIgnoreCase(serviceItemId)) {
            String resolvedTypeId = lookupServiceTypeIdByItemId(serviceItemId);
            name = lookupServiceTypeName(resolvedTypeId);
            if (name != null) return name;
        }

        return "Unknown Service";
    }

    /** 从 service_type.txt 查名称，ID 无效返回 null */
    private String lookupServiceTypeName(String id) {
        if (id == null || id.isEmpty()
                || "null".equalsIgnoreCase(id)
                || "N/A".equalsIgnoreCase(id)) {
            return null;
        }
        try (BufferedReader br = new BufferedReader(new FileReader("src/txtFiles/service_type.txt"))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || first) { first = false; continue; }
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 2 && parts[0].trim().equalsIgnoreCase(id)) {
                    return parts[1].trim();
                }
            }
        } catch (IOException ignored) {}
        return null;
    }

    /** 从 service_items.txt 通过 serviceItemId 找到其 service_type_id */
    private String lookupServiceTypeIdByItemId(String itemId) {
        try (BufferedReader br = new BufferedReader(new FileReader("src/txtFiles/service_items.txt"))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || first) { first = false; continue; }
                String[] parts = line.split("\\|", -1);
                // service_items.txt 格式: service_item_id|service_type_id|...
                if (parts.length >= 2 && parts[0].trim().equalsIgnoreCase(itemId)) {
                    return parts[1].trim();
                }
            }
        } catch (IOException ignored) {}
        return null;
    }

    public Appointment getUpcomingAppointmentByCustomerID(String customerID) {
        LocalDate today = LocalDate.now();
        Appointment upcoming = null;

        for (Appointment a : getAllAppointments()) {
            if (!a.getCustomerID().equals(customerID)) continue;
            if (a.getAppointmentDate().isBefore(today)) continue;
            String status = a.getStatus();
            if (!"Approved".equalsIgnoreCase(status) && !"Pending".equalsIgnoreCase(status)) continue;

            if (upcoming == null || a.getAppointmentDate().isBefore(upcoming.getAppointmentDate())) {
                upcoming = a;
            }
        }
        return upcoming;
    }

    public List<Appointment> getPassAppointments(String customerID) {
        List<Appointment> list = new ArrayList<>();
        for (Appointment a : getAllAppointments()) {
            if (a.getCustomerID().equals(customerID)) {
                list.add(a);
            }
        }
        // 按预约日期降序排列（最新的在最上面）
        list.sort((a, b) -> b.getAppointmentDate().compareTo(a.getAppointmentDate()));
        return list;
    }

    public boolean submitAppointment(String customerID, String serviceItemID, LocalDate date,
            String time, String vehicleBrand, String vehicleModel, String vehiclePlate, String command) {

        for (Appointment a : getAllAppointments()) {
            if (a.getCustomerID().equals(customerID)
                    && a.getAppointmentDate().equals(date)
                    && a.getStartTime().equals(time)) {
                System.out.println("Duplicate appointment detected. Submission rejected.");
                return false;
            }
        }

        ServiceItemDAO serviceItemDAO = new ServiceItemDAO();
        String serviceTypeId = serviceItemDAO.getServiceTypeByServiceItemID(serviceItemID);
        if (serviceTypeId == null) serviceTypeId = "null";

        String newID = generateNewAppointmentId();

        try {
            Path path = Paths.get(FILE_PATH);
            List<String> lines = new ArrayList<>(Files.readAllLines(path));
            String newLine = String.join("|",
                newID, customerID, "null", serviceTypeId, "null", serviceItemID,
                time, date.toString(), "Pending",
                vehicleBrand, vehicleModel, vehiclePlate,
                (command == null || command.isEmpty()) ? "N/A" : command
            );
            lines.add(newLine);
            Files.write(path, lines);
            return true;
        } catch (IOException e) {
            System.err.println("Cannot write to appointment.txt: " + e.getMessage());
            return false;
        }
    }

    private String generateNewAppointmentId() {
        int max = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\|", -1);
                if (parts[0].matches("A\\d+")) {
                    try {
                        int num = Integer.parseInt(parts[0].substring(1));
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading appointments for ID generation: " + e.getMessage());
        }
        return String.format("A%03d", max + 1);
    }
}