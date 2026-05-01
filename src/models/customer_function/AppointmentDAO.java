package models.customer_function;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    private static final String FILE_PATH = "src/txtFiles/appointments.txt";

    // appointments.txt format (row 1 = header, skip it):
    // appointment_id|customer_id|staff_id|service_type_id|technician_id|service_item_id|
    // appointment_date|time_slot|end_time|status|vehicle_brand|vehicle_model|vehicle_plate|
    // customer_comment|service_type

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; } // skip header
                String[] p = line.split("\\|", -1);
                if (p.length < 15) continue;
                try {
                    list.add(new Appointment(
                        p[0], p[1], p[2], p[3], p[4], p[5],
                        LocalDate.parse(p[6]),
                        p[7], p[8], p[9], p[10], p[11], p[12], p[13], p[14]
                    ));
                } catch (Exception e) {
                    System.err.println("Skipping bad line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading appointments.txt: " + e.getMessage());
        }
        return list;
    }

    public Appointment getUpcomingAppointmentByCustomerID(String customerID) {
        LocalDate today = LocalDate.now();
        Appointment upcoming = null;
        for (Appointment a : getAllAppointments()) {
            if (a.getCustomerID().equals(customerID)
                    && !a.getAppointmentDate().isBefore(today)
                    && ("Confirmed".equals(a.getStatus()) || "Pending".equals(a.getStatus()))) {
                if (upcoming == null || a.getAppointmentDate().isBefore(upcoming.getAppointmentDate()))
                    upcoming = a;
            }
        }
        return upcoming;
    }

    public List<Appointment> getPassAppointments(String customerID) {
        LocalDate today = LocalDate.now();
        List<Appointment> list = new ArrayList<>();
        for (Appointment a : getAllAppointments()) {
            if (a.getCustomerID().equals(customerID)
                    && (a.getAppointmentDate().isBefore(today)
                        || "Completed".equals(a.getStatus())))
                list.add(a);
        }
        return list;
    }

    public boolean submitAppointment(String customerID, String serviceItemID,
                                     LocalDate date, String time,
                                     String vehicleBrand, String vehicleModel,
                                     String vehiclePlate, String comment) {
        for (Appointment a : getAllAppointments()) {
            if (a.getCustomerID().equals(customerID)
                    && a.getAppointmentDate().equals(date)
                    && a.getStartTime().equals(time)) {
                System.out.println("Duplicate appointment detected.");
                return false;
            }
        }

        String newID       = generateNewAppointmentId();
        String serviceType = new ServiceItemDAO().getServiceTypeByServiceItemID(serviceItemID);

        String line = String.join("|",
            newID, customerID, "N/A", "N/A", "N/A", serviceItemID,
            date.toString(), time, "N/A", "Pending",
            vehicleBrand, vehicleModel, vehiclePlate,
            comment.isEmpty() ? "N/A" : comment,
            serviceType != null ? serviceType : "N/A"
        );

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.newLine();
            bw.write(line);
            return true;
        } catch (IOException e) {
            System.err.println("Cannot write to appointments.txt: " + e.getMessage());
            return false;
        }
    }

    private String generateNewAppointmentId() {
        int max = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (firstLine) { firstLine = false; continue; }
                String[] p = line.split("\\|", -1);
                if (p[0].startsWith("A")) {
                    try {
                        int num = Integer.parseInt(p[0].substring(1));
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (IOException e) {
            System.err.println("Error generating appointment ID: " + e.getMessage());
        }
        return String.format("A%03d", max + 1);
    }
}