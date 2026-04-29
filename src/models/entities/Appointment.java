package models.entities;

public class Appointment {

    private String appointmentId;
    private String customerId;
    private String staffId;
    private String serviceTypeId;
    private String technicianId;
    private String serviceItemId;
    private String timeSlot;
    private String endTime;
    private String appointmentDate;
    private String status;
    private String customerComment;
    private String vehicleBrand;
    private String vehicleModel;
    private String vehiclePlate;

    public Appointment(String appointmentId, String customerId, String staffId,
                       String serviceTypeId, String technicianId, String serviceItemId,
                       String timeSlot, String endTime, String appointmentDate,
                       String status, String customerComment,
                       String vehicleBrand, String vehicleModel, String vehiclePlate) {
        this.appointmentId   = appointmentId;
        this.customerId      = customerId;
        this.staffId         = staffId;
        this.serviceTypeId   = serviceTypeId;
        this.technicianId    = technicianId;
        this.serviceItemId   = serviceItemId;
        this.timeSlot        = timeSlot;
        this.endTime         = endTime;
        this.appointmentDate = appointmentDate;
        this.status          = status;
        this.customerComment = customerComment;
        this.vehicleBrand    = vehicleBrand;
        this.vehicleModel    = vehicleModel;
        this.vehiclePlate    = vehiclePlate;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────
    public String getAppointmentId()   { return appointmentId; }
    public String getCustomerId()      { return customerId; }
    public String getStaffId()         { return staffId; }
    public String getServiceTypeId()   { return serviceTypeId; }
    public String getTechnicianId()    { return technicianId; }
    public String getServiceItemId()   { return serviceItemId; }
    public String getTimeSlot()        { return timeSlot; }
    public String getEndTime()         { return endTime; }
    public String getAppointmentDate() { return appointmentDate; }
    public String getStatus()          { return status; }
    public String getCustomerComment() { return customerComment; }
    public String getVehicleBrand()    { return vehicleBrand; }
    public String getVehicleModel()    { return vehicleModel; }
    public String getVehiclePlate()    { return vehiclePlate; }

    // ─── Setters ──────────────────────────────────────────────────────────────
    public void setAppointmentId(String v)   { this.appointmentId = v; }
    public void setCustomerId(String v)      { this.customerId = v; }
    public void setStaffId(String v)         { this.staffId = v; }
    public void setServiceTypeId(String v)   { this.serviceTypeId = v; }
    public void setTechnicianId(String v)    { this.technicianId = v; }
    public void setServiceItemId(String v)   { this.serviceItemId = v; }
    public void setTimeSlot(String v)        { this.timeSlot = v; }
    public void setEndTime(String v)         { this.endTime = v; }
    public void setAppointmentDate(String v) { this.appointmentDate = v; }
    public void setStatus(String v)          { this.status = v; }
    public void setCustomerComment(String v) { this.customerComment = v; }
    public void setVehicleBrand(String v)    { this.vehicleBrand = v; }
    public void setVehicleModel(String v)    { this.vehicleModel = v; }
    public void setVehiclePlate(String v)    { this.vehiclePlate = v; }
}