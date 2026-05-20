package views.staff;

import models.dao.AppointmentDAO;
import models.dao.CustomerDAO;
import models.dao.ServiceItemDAO;
import models.dao.TechnicianDAO;
import models.entities.Appointment;
import models.entities.Technician;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class CounterStaffCreateAppointment extends JPanel {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final CustomerDAO    customerDAO    = new CustomerDAO();
    private final TechnicianDAO  technicianDAO  = new TechnicianDAO();

    // ─── Step 1 ───────────────────────────────────────────────────────────────
    private JComboBox<String> customerCombo;
    private JTextField        vehicleField;
    private JTextField        vehicleBrandField;
    private JTextField        vehicleModelField;

    // ─── Step 2 ───────────────────────────────────────────────────────────────
    private JComboBox<String>            serviceItemCombo;
    private UIComponent.DatePickerField  dateField;
    private JComboBox<String>            timeSlotCombo;

    // ─── Step 3 ───────────────────────────────────────────────────────────────
    private JComboBox<String> techCombo;
    private JButton btnViewSchedule;

    // ─── Right panel ──────────────────────────────────────────────────────────
    private JPanel techStatusPanel;
    private JPanel slotsPanel;
    private JLabel slotsDateLabel; // shows which date the slots reflect

    // staff ID injected from dashboard
    private String staffId = "CS001";

    // time slots offered
    private static final String[] TIME_SLOTS = {
        "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM",
        "12:00 PM", "02:00 PM", "03:00 PM", "04:00 PM"
    };

    public CounterStaffCreateAppointment(MainFrame frame) {
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE
        ));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        body.add(buildFormPanel(),  BorderLayout.CENTER);
        body.add(buildRightPanel(), BorderLayout.EAST);
        add(body, BorderLayout.CENTER);

        wireListeners();

        populateCustomerCombo();
        refreshTechStatus();

        List<Appointment> appts = appointmentDAO.getAllAppointments();
        refreshSlots(appts);
        refreshTechCombo(appts);
    }

    public void setStaffId(String id) { this.staffId = id; }

    // ─── Page header ──────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel title = UIComponent.createPageTitle("Create Appointment");
        JLabel sub   = UIComponent.createCaptionLabel("Walk-In / Telephone Booking Form");
        sub.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(sub);
        p.add(left, BorderLayout.WEST);
        return p;
    }

    // ─── Main form panel ──────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        p.add(buildStep1());
        p.add(Box.createRigidArea(new Dimension(0, 12)));
        p.add(buildStep2());
        p.add(Box.createRigidArea(new Dimension(0, 12)));
        p.add(buildStep3());
        p.add(Box.createRigidArea(new Dimension(0, 12)));
        p.add(buildConfirmButton());

        return p;
    }

    // ─── Step 1 — Customer Selection ──────────────────────────────────────────
    private JPanel buildStep1() {
        JPanel card = createStepCard();

        JPanel custRow = new JPanel(new BorderLayout(8, 0));
        custRow.setOpaque(false);

        customerCombo = UIComponent.createComboBox(new String[0]);
        customerCombo.setPreferredSize(new Dimension(0, 38));

        JButton btnSearch = UIComponent.createSmallButton("Search");
        btnSearch.setPreferredSize(new Dimension(90, 36));
        btnSearch.addActionListener(e -> showCustomerSearch());

        custRow.add(customerCombo, BorderLayout.CENTER);
        custRow.add(btnSearch,     BorderLayout.EAST);

        vehicleField      = UIComponent.createTextField("e.g. WXY-1234");
        vehicleBrandField = UIComponent.createTextField("e.g. Toyota");
        vehicleModelField = UIComponent.createTextField("e.g. Camry");

        addFormRow(card, "Select Registered Customer *", custRow);
        addFormRow(card, "Vehicle Plate No. *",           vehicleField);
        addFormRow(card, "Vehicle Brand *",               vehicleBrandField);
        addFormRow(card, "Vehicle Model *",               vehicleModelField);

        return wrapCard(card, "Step 1 — Customer Selection");
    }

    // ─── Step 2 — Service & Schedule ──────────────────────────────────────────
    private JPanel buildStep2() {
        JPanel card = createStepCard();

        serviceItemCombo = UIComponent.createComboBox(new String[0]);
        serviceItemCombo.setPreferredSize(new Dimension(0, 38));
        populateServiceItems();

        dateField = UIComponent.createDatePicker(LocalDate.now());
        dateField.setPreferredSize(new Dimension(110, 38));

        timeSlotCombo = UIComponent.createComboBox(TIME_SLOTS);
        timeSlotCombo.setPreferredSize(new Dimension(140, 38));

        JPanel dateTimeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        dateTimeRow.setOpaque(false);
        JLabel timeLbl = new JLabel("  Time Slot *  ");
        timeLbl.setFont(AppTheme.FONT_SMALL);
        timeLbl.setForeground(AppTheme.TEXT_SECONDARY);
        dateTimeRow.add(dateField);
        dateTimeRow.add(timeLbl);
        dateTimeRow.add(timeSlotCombo);

        addFormRow(card, "Service Item *",     serviceItemCombo);
        addFormRow(card, "Appointment Date *", dateTimeRow);

        return wrapCard(card, "Step 2 — Service & Schedule");
    }

    private void populateServiceItems() {
        serviceItemCombo.removeAllItems();
        serviceItemCombo.addItem("-- Select Service Item --");
        for (String[] item : ServiceItemDAO.getAllServiceItems()) {
            String type = item[1].equals("ST001") ? "Normal" : "Major"; // col 1 = service_type_id
            serviceItemCombo.addItem(item[0] + " — " + item[2] + " (" + type + ")"); // col 2 = service_name
        }
    }

    // ─── Step 3 — Assign Technician ───────────────────────────────────────────
    private JPanel buildStep3() {
        JPanel card = createStepCard();

        techCombo = UIComponent.createComboBox(new String[0]);
        techCombo.setEnabled(false);
        techCombo.setPreferredSize(new Dimension(0, 38));
        techCombo.setToolTipText("Select a service item, date, and time slot first");

        btnViewSchedule = UIComponent.createSmallButton("View Schedule");
        btnViewSchedule.setEnabled(false);
        btnViewSchedule.setPreferredSize(new Dimension(130, 36));
        btnViewSchedule.setToolTipText("Select a service item, date, and time slot first");
        btnViewSchedule.addActionListener(e -> showTechSchedule());

        JPanel techRow = new JPanel(new BorderLayout(8, 0));
        techRow.setOpaque(false);
        techRow.add(techCombo,       BorderLayout.CENTER);
        techRow.add(btnViewSchedule, BorderLayout.EAST);

        addFormRow(card, "Assign Technician *", techRow);

        // UI-only notes area — not stored
        JTextArea notesArea = UIComponent.createTextArea("Optional instructions or remarks...", 3);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        JScrollPane notesScroll = new JScrollPane(notesArea) {
            @Override public Dimension getPreferredSize() { return new Dimension(super.getPreferredSize().width, 80); }
            @Override public Dimension getMinimumSize()   { return new Dimension(0, 80); }
            @Override public Dimension getMaximumSize()   { return new Dimension(Integer.MAX_VALUE, 80); }
        };
        notesScroll.setBorder(null);
        notesScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        notesScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        notesScroll.getViewport().setBackground(AppTheme.BG_FIELD);

        addFormRow(card, "Additional Notes", notesScroll);

        return wrapCard(card, "Step 3 — Assign Technician");
    }

    // ─── Confirm button ───────────────────────────────────────────────────────
    private JPanel buildConfirmButton() {
        JButton btnConfirm = new JButton("Confirm Appointment") {
        	
            private boolean hovered = false;

            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited (java.awt.event.MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? AppTheme.PRIMARY_HOVER : AppTheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? AppTheme.PRIMARY_HOVER : AppTheme.PRIMARY);
                g2.setStroke(new java.awt.BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 36, 36);
                g2.dispose();
            }
        };

        btnConfirm.setFont(new Font(AppTheme.FONT_SMALL_BOLD.getName(), Font.BOLD, 14));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setContentAreaFilled(false);
        btnConfirm.setBorderPainted(false);
        btnConfirm.setFocusPainted(false);
        btnConfirm.setOpaque(false);
        btnConfirm.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirm.setPreferredSize(new Dimension(0, 38));
        btnConfirm.addActionListener(e -> handleConfirm());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.setPreferredSize(new Dimension(Integer.MAX_VALUE, 38));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        wrapper.setMinimumSize(new Dimension(0, 48));
        wrapper.add(btnConfirm, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Right panel (Technician Status + Today's Slots) ─────────────────────
    private JPanel buildRightPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(200, Short.MAX_VALUE));

        JPanel techCard = createCard();
        techCard.setLayout(new BorderLayout(0, 0));
        techCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel techTitle = new JLabel("Current Status");
        techTitle.setFont(AppTheme.FONT_SMALL_BOLD);
        techTitle.setForeground(AppTheme.TEXT_PRIMARY);

        JButton btnRefresh = new JButton("↺");
        btnRefresh.setFont(AppTheme.FONT_SMALL_BOLD);
        btnRefresh.setForeground(AppTheme.TEXT_SECONDARY);
        btnRefresh.setContentAreaFilled(false);
        btnRefresh.setBorderPainted(false);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.setToolTipText("Refresh technician status");
        btnRefresh.addActionListener(e -> refreshTechStatus());

        JPanel techHeader = new JPanel(new BorderLayout());
        techHeader.setOpaque(false);
        techHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(6, 12, 6, 8)
        ));
        techHeader.add(techTitle,   BorderLayout.WEST);
        techHeader.add(btnRefresh,  BorderLayout.EAST);
        techCard.add(techHeader, BorderLayout.NORTH);

        techStatusPanel = new JPanel();
        techStatusPanel.setLayout(new BoxLayout(techStatusPanel, BoxLayout.Y_AXIS));
        techStatusPanel.setOpaque(false);
        techStatusPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        techCard.add(techStatusPanel, BorderLayout.CENTER);

        JPanel slotsCard = createCard();
        slotsCard.setLayout(new BorderLayout(0, 0));
        slotsCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel slotsTitle = new JLabel("Slots");
        slotsTitle.setFont(AppTheme.FONT_SMALL_BOLD);
        slotsTitle.setForeground(AppTheme.TEXT_PRIMARY);

        slotsDateLabel = new JLabel("—");
        slotsDateLabel.setFont(AppTheme.FONT_CAPTION);
        slotsDateLabel.setForeground(AppTheme.TEXT_SECONDARY);

        JPanel slotsHeader = new JPanel(new BorderLayout(4, 0));
        slotsHeader.setOpaque(false);
        slotsHeader.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        slotsHeader.add(slotsTitle,     BorderLayout.WEST);
        slotsHeader.add(slotsDateLabel, BorderLayout.EAST);
        slotsCard.add(slotsHeader, BorderLayout.NORTH);

        slotsPanel = new JPanel();
        slotsPanel.setLayout(new BoxLayout(slotsPanel, BoxLayout.Y_AXIS));
        slotsPanel.setOpaque(false);
        slotsPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        slotsCard.add(slotsPanel, BorderLayout.CENTER);

        JPanel techWrapper = new JPanel(new BorderLayout());
        techWrapper.setOpaque(false);
        techWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        techWrapper.add(techCard, BorderLayout.CENTER);

        JPanel slotsWrapper = new JPanel(new BorderLayout());
        slotsWrapper.setOpaque(false);
        slotsWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        slotsWrapper.add(slotsCard, BorderLayout.CENTER);

        p.add(techWrapper);
        p.add(Box.createRigidArea(new Dimension(0, 12)));
        p.add(slotsWrapper);

        return p;
    }

    // ─── Wire change listeners ─────────────────────────────────────────────────
    private void wireListeners() {
        dateField.addDateChangeListener(d -> onScheduleChanged());
        timeSlotCombo.addActionListener(e -> onScheduleChanged());
        serviceItemCombo.addActionListener(e -> refreshTechCombo()); // service type affects slot count
        customerCombo.addActionListener(e -> { });
        techCombo.addActionListener(e -> { });
    }

    private void onScheduleChanged() {
        // Fetch appointments once and share across both refresh calls to avoid double DB hit
        List<Appointment> appts = appointmentDAO.getAllAppointments();
        refreshSlots(appts);
        refreshTechCombo(appts);
    }

    // ─── Populate customer combo ───────────────────────────────────────────────
    private void populateCustomerCombo() {
        customerCombo.removeAllItems();
        customerCombo.addItem("-- Select Registered Customer --");
        for (String[] c : customerDAO.getApprovedCustomers()) {
            customerCombo.addItem(c[0].trim() + " — " + c[2].trim()); // col 2 = customer_name
        }
    }

    // ─── Refresh technician status panel (right side) ─────────────────────────
    private void refreshTechStatus() {
        techStatusPanel.removeAll();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalTime now = LocalTime.now();
        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workEnd   = LocalTime.of(17, 0);

        // Outside working hours — all technicians are Unavailable
        boolean outOfHours = now.isBefore(workStart) || !now.isBefore(workEnd);

        // A technician is Busy if they have an Approved appointment RIGHT NOW:
        // their start_time ≤ now < start_time + service duration
        Set<String> busyNowIds = new HashSet<>();
        if (!outOfHours) {
            for (Appointment a : appointmentDAO.getAllAppointments()) {
                if (!a.getAppointmentDate().equals(today)) continue;
                if (!a.getStatus().equalsIgnoreCase("Approved")) continue;
                if (a.getTechnicianId() == null || a.getTechnicianId().equalsIgnoreCase("N/A")) continue;
                try {
                    LocalTime start = LocalTime.parse(a.getTimeSlot().trim(),
                        DateTimeFormatter.ofPattern("hh:mm a"));
                    int hours = "ST002".equals(a.getServiceTypeId()) ? 3 : 1;
                    LocalTime end = start.plusHours(hours);
                    if (!now.isBefore(start) && now.isBefore(end)) {
                        busyNowIds.add(a.getTechnicianId());
                    }
                } catch (Exception ignored) {}
            }
        }

        for (Technician t : technicianDAO.getAllTechnicians()) {
            String status = outOfHours ? "Unavailable"
                          : busyNowIds.contains(t.getTechnicianId()) ? "Busy"
                          : "Available";
            techStatusPanel.add(buildTechStatusRow(t, status));
            techStatusPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        techStatusPanel.revalidate();
        techStatusPanel.repaint();
    }

    private JPanel buildTechStatusRow(Technician t, String status) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_DEFAULT, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JLabel nameLabel = new JLabel(t.getTechnicianId() + ": " + t.getUsername());
        nameLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);

        Color statusColor = switch (status) {
            case "Busy"        -> AppTheme.DANGER;
            case "Unavailable" -> AppTheme.TEXT_MUTED;
            default            -> AppTheme.SUCCESS;   // Available
        };
        JLabel statusLabel = new JLabel("● " + status);
        statusLabel.setFont(AppTheme.FONT_CAPTION);
        statusLabel.setForeground(statusColor);

        JPanel nameStatus = new JPanel();
        nameStatus.setLayout(new BoxLayout(nameStatus, BoxLayout.Y_AXIS));
        nameStatus.setOpaque(false);
        nameStatus.add(nameLabel);
        nameStatus.add(statusLabel);

        row.add(nameStatus, BorderLayout.CENTER);
        return row;
    }

    // ─── Shared helper — slots occupied by an appointment in TIME_SLOTS index space ──
    /**
     * Returns the set of TIME_SLOTS entries that appointment {@code a} occupies.
     * Major service (ST002) occupies 3 consecutive slots; Normal occupies 1.
     */
    private Set<String> occupiedSlotsOf(Appointment a) {
        List<String> slotList  = Arrays.asList(TIME_SLOTS);
        int startIndex = slotList.indexOf(a.getTimeSlot().trim());
        if (startIndex < 0) return Collections.emptySet();
        int count = "ST002".equals(a.getServiceTypeId()) ? 3 : 1;
        Set<String> result = new HashSet<>();
        for (int i = startIndex; i < Math.min(startIndex + count, TIME_SLOTS.length); i++) {
            result.add(TIME_SLOTS[i]);
        }
        return result;
    }

    // ─── Refresh slots panel — synced to date field, shows free/total count ────
    private void refreshSlots() {
        refreshSlots(appointmentDAO.getAllAppointments());
    }

    private void refreshSlots(List<Appointment> allAppts) {
        slotsPanel.removeAll();

        // Resolve selected date — convert dd/MM/yyyy → yyyy-MM-dd
        String dateRaw = dateField.getText().trim();
        String dateISO;
        try {
            LocalDate parsed = LocalDate.parse(dateRaw, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dateISO = parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            slotsDateLabel.setText(dateRaw);
        } catch (Exception e) {
            dateISO = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            slotsDateLabel.setText("Today");
        }
        final String targetDate = dateISO;

        int totalTechs = technicianDAO.getAllTechnicians().size();

        // Count how many techs are booked per slot on the target date
        List<Appointment> dateAppts = allAppts.stream()
            .filter(a -> a.getAppointmentDate().equals(targetDate)
                && (a.getStatus().equalsIgnoreCase("Approved")
                 || a.getStatus().equalsIgnoreCase("Pending"))
                && a.getTechnicianId() != null
                && !a.getTechnicianId().equalsIgnoreCase("N/A"))
            .collect(Collectors.toList());

        for (int si = 0; si < TIME_SLOTS.length; si++) {
            final String slotStr = TIME_SLOTS[si];
            long booked = dateAppts.stream()
                .filter(a -> occupiedSlotsOf(a).contains(slotStr))
                .count();
            int free = (int) Math.max(0, totalTechs - booked);
            slotsPanel.add(buildSlotRow(TIME_SLOTS[si], free, totalTechs));
            slotsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }
        slotsPanel.revalidate();
        slotsPanel.repaint();
    }

    private JPanel buildSlotRow(String slot, int free, int total) {
        // Color: green if plenty free, yellow if limited, red if full
        Color bg, border, textColor;
        String label;
        if (free == 0) {
            bg        = new Color(255, 235, 235);
            border    = new Color(255, 180, 180);
            textColor = AppTheme.DANGER;
            label     = "Full";
        } else if (free <= total / 2) {
            bg        = new Color(255, 248, 220);
            border    = new Color(255, 210, 100);
            textColor = new Color(180, 120, 0);
            label     = free + "/" + total + "  Free";
        } else {
            bg        = new Color(235, 255, 240);
            border    = new Color(180, 230, 180);
            textColor = new Color(30, 150, 60);
            label     = free + "/" + total + "  Free";
        }

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(true);
        row.setBackground(bg);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Click to auto-select this time slot in the combo
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                for (int i = 0; i < timeSlotCombo.getItemCount(); i++) {
                    if (slot.equals(timeSlotCombo.getItemAt(i))) {
                        timeSlotCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        });

        JLabel slotLbl = new JLabel(slot);
        slotLbl.setFont(AppTheme.FONT_SMALL);
        slotLbl.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel countLbl = new JLabel(label);
        countLbl.setFont(AppTheme.FONT_CAPTION);
        countLbl.setForeground(textColor);

        row.add(slotLbl,  BorderLayout.WEST);
        row.add(countLbl, BorderLayout.EAST);
        return row;
    }

    // ─── Refresh technician combo (slot-based availability) ───────────────────
    private void refreshTechCombo() {
        refreshTechCombo(appointmentDAO.getAllAppointments());
    }

    private void refreshTechCombo(List<Appointment> allAppts) {
        techCombo.removeAllItems();

        String dateRaw = dateField.getText().trim();
        String slot    = (String) timeSlotCombo.getSelectedItem();

        // Convert dd/MM/yyyy → yyyy-MM-dd
        String dateISO;
        try {
            LocalDate parsed = LocalDate.parse(dateRaw, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dateISO = parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception ignored) {
            dateISO = dateRaw;
        }

        String newServiceSel  = (String) serviceItemCombo.getSelectedItem();
        boolean serviceChosen = newServiceSel != null && !newServiceSel.startsWith("--");
        boolean ready         = !dateISO.isEmpty() && slot != null && serviceChosen;

        // Enable/disable based on whether service item, date, and slot are all chosen
        techCombo.setEnabled(ready);
        if (btnViewSchedule != null) btnViewSchedule.setEnabled(ready);

        if (!ready) {
            techCombo.addItem("-- Select date & time slot first --");
            return;
        }

        techCombo.addItem("-- Select Technician --");

        // Determine how many slots the NEW appointment would occupy,
        // based on the service type currently selected in the form.
        String newServiceItemId = newServiceSel.split(" — ")[0].trim();
        String newServiceTypeId = ServiceItemDAO.getServiceTypeId(newServiceItemId);
        int newSlotsNeeded = "ST002".equals(newServiceTypeId) ? 3 : 1;

        // Build the set of slots the NEW appointment would occupy.
        // Guard: if the selected slot isn't in TIME_SLOTS, show all techs as free
        // rather than silently treating everyone as available.
        List<String> slotList    = Arrays.asList(TIME_SLOTS);
        int newStartIndex        = slotList.indexOf(slot.trim());
        if (newStartIndex < 0) {
            techCombo.addItem("(Invalid time slot selected)");
            techCombo.setEnabled(false);
            return;
        }
        Set<String> newOccupiedSlots = new HashSet<>();
        for (int i = newStartIndex; i < Math.min(newStartIndex + newSlotsNeeded, TIME_SLOTS.length); i++) {
            newOccupiedSlots.add(TIME_SLOTS[i]);
        }

        final String date = dateISO;

        // A technician is busy if ANY of their existing appointments overlap
        // with ANY of the slots the new appointment would occupy.
        // Uses the shared occupiedSlotsOf() helper — no duplicate index logic.
        final Set<String> busyIds = allAppts.stream()
            .filter(a -> (a.getStatus().equalsIgnoreCase("Approved")
                       || a.getStatus().equalsIgnoreCase("Pending"))
                      && a.getAppointmentDate().trim().equals(date)
                      && a.getTechnicianId() != null
                      && !a.getTechnicianId().equalsIgnoreCase("null")
                      && !a.getTechnicianId().trim().isEmpty()
                      && !Collections.disjoint(occupiedSlotsOf(a), newOccupiedSlots))
            .map(Appointment::getTechnicianId)
            .collect(Collectors.toSet());

        List<Technician> free = technicianDAO.getAllTechnicians().stream()
            .filter(t -> !busyIds.contains(t.getTechnicianId()))
            .collect(Collectors.toList());

        if (free.isEmpty()) {
            techCombo.addItem("(No technicians free for this slot)");
        } else {
            for (Technician t : free) {
                techCombo.addItem(t.getTechnicianId() + " — " + t.getUsername());
            }
        }
    }

    // ─── Confirm handler ──────────────────────────────────────────────────────
    private void handleConfirm() {
        String custSel    = (String) customerCombo.getSelectedItem();
        String serviceSel = (String) serviceItemCombo.getSelectedItem();
        String techSel    = (String) techCombo.getSelectedItem();
        String date       = dateField.getText().trim();
        String slot       = (String) timeSlotCombo.getSelectedItem();
        String plate      = vehicleField.getText().trim();
        String brand      = vehicleBrandField.getText().trim();
        String model      = vehicleModelField.getText().trim();

        if (custSel == null || custSel.startsWith("--")) {
            showError("Please select a customer."); return;
        }
        if (plate.isEmpty() || plate.equals("e.g. WXY-1234")) {
            showError("Please enter the vehicle plate number."); return;
        }
        if (brand.isEmpty() || brand.equals("e.g. Toyota")) {
            showError("Please enter the vehicle brand."); return;
        }
        if (model.isEmpty() || model.equals("e.g. Camry")) {
            showError("Please enter the vehicle model."); return;
        }
        if (serviceSel == null || serviceSel.startsWith("--")) {
            showError("Please select a service item."); return;
        }
        if (date.isEmpty()) {
            showError("Please enter the appointment date."); return;
        }
        if (techSel == null || techSel.startsWith("--") || techSel.startsWith("(No")) {
            showError("Please select an available technician."); return;
        }

        String customerId    = custSel.split(" — ")[0].trim();
        String serviceItemId = serviceSel.split(" — ")[0].trim();
        String serviceTypeId = ServiceItemDAO.getServiceTypeId(serviceItemId);
        String techId        = techSel.split(" — ")[0].trim();

        // Convert date from dd/MM/yyyy (form) to yyyy-MM-dd (storage)
        String dateISO;
        try {
            LocalDate parsed = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dateISO = parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception ex) {
            dateISO = date; // already yyyy-MM-dd or fallback
        }

        String apptId = ""; // auto-generated inside addAppointment

        Appointment appt = new Appointment(
            apptId, customerId, staffId, serviceTypeId,
            techId, serviceItemId,
            slot,     // timeSlot (start_time)
            dateISO,  // appointmentDate (yyyy-MM-dd)
            "Approved",
            brand, model, plate,
            "N/A"     // technicianFeedback
        );

        boolean ok = appointmentDAO.addAppointment(appt);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                "Appointment created and approved successfully!",
                "Appointment Created", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
        } else {
            showError("Failed to save appointment. Please try again.");
        }
    }

    // ─── Customer search dialog ────────────────────────────────────────────────
    private void showCustomerSearch() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Search Customer", true);
        dialog.setSize(520, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JTextField searchField = UIComponent.createTextField("Search by ID, name or email...");
        searchField.setPreferredSize(new Dimension(0, 36));
        JButton btnGo = UIComponent.createSmallButton("Search");
        top.add(searchField, BorderLayout.CENTER);
        top.add(btnGo,       BorderLayout.EAST);

        String[] cols = {"ID", "Name", "Email", "Contact"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(model);
        tbl.setFont(AppTheme.FONT_SMALL);
        tbl.setRowHeight(AppTheme.ROW_HEIGHT_TABLE);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(tbl);

        Runnable populate = () -> {
            model.setRowCount(0);
            String q = searchField.getText().trim().toLowerCase();
            for (String[] c : customerDAO.getAllCustomers()) {
                if (q.isEmpty()
                        || c[0].toLowerCase().contains(q)
                        || c[2].toLowerCase().contains(q)   // col 2 = customer_name
                        || c[3].toLowerCase().contains(q)) { // col 3 = email
                    model.addRow(new Object[]{c[0], c[2], c[3], c[4]}); // id, name, email, phone
                }
            }
        };
        populate.run();
        btnGo.addActionListener(e -> populate.run());
        searchField.addActionListener(e -> populate.run());

        JButton btnSelect = UIComponent.createSmallButton("Select");
        btnSelect.addActionListener(e -> {
            int row = tbl.getSelectedRow();
            if (row < 0) return;
            String id = (String) model.getValueAt(row, 0);
            for (int i = 0; i < customerCombo.getItemCount(); i++) {
                String item = customerCombo.getItemAt(i);
                if (item.startsWith(id + " — ")) {
                    customerCombo.setSelectedIndex(i);
                    break;
                }
            }
            dialog.dispose();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnSelect);

        dialog.add(top,    BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ─── Technician schedule dialog ────────────────────────────────────────────
    private void showTechSchedule() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            "Technician Schedule", true);
        dialog.setSize(680, 460);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        top.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        JComboBox<String> techFilter = UIComponent.createComboBox(new String[0]);
        techFilter.setPreferredSize(new Dimension(200, 38));
        techFilter.addItem("All Technicians");
        for (Technician t : technicianDAO.getAllTechnicians()) {
            techFilter.addItem(t.getTechnicianId() + " — " + t.getUsername());
        }

        JTextField dateFilter = UIComponent.createTextField("dd/MM/yyyy");
        dateFilter.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateFilter.setForeground(AppTheme.TEXT_PRIMARY);
        dateFilter.setPreferredSize(new Dimension(120, 36));

        JButton btnLoad = UIComponent.createSmallButton("Load");

        top.add(new JLabel("Technician:"));
        top.add(techFilter);
        top.add(Box.createHorizontalStrut(8));
        top.add(new JLabel("Date:"));
        top.add(dateFilter);
        top.add(btnLoad);

        String[] cols = {"Time Slot", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = new JTable(model) {
            @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component c = super.prepareRenderer(renderer, row, col);
                String val = (String) getValueAt(row, 1);
                if (val != null && val.startsWith("Booked")) {
                    c.setBackground(new Color(255, 220, 220));
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(new Color(220, 255, 230));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        };
        tbl.setFont(AppTheme.FONT_SMALL);
        tbl.setRowHeight(AppTheme.ROW_HEIGHT_TABLE);
        tbl.setEnabled(false);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(120);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(520);

        Runnable loadTable = () -> {
            model.setRowCount(0);
            String dateVal = dateFilter.getText().trim();
            String techSel = (String) techFilter.getSelectedItem();
            String filterTechId = (techSel == null || techSel.equals("All Technicians"))
                ? null : techSel.split(" — ")[0].trim();

            List<Appointment> appts = appointmentDAO.getAllAppointments().stream()
                .filter(a -> a.getAppointmentDate().trim().equals(dateVal)
                    && (a.getStatus().equalsIgnoreCase("Approved")
                     || a.getStatus().equalsIgnoreCase("Pending"))
                    && (filterTechId == null || filterTechId.equals(a.getTechnicianId())))
                .collect(Collectors.toList());

            for (String slot : TIME_SLOTS) {
                List<String> matches = new ArrayList<>();
                for (Appointment a : appts) {
                    try {
                        LocalTime slotTime  = LocalTime.parse(slot, DateTimeFormatter.ofPattern("hh:mm a"));
                        LocalTime startTime = LocalTime.parse(a.getTimeSlot().trim(), DateTimeFormatter.ofPattern("hh:mm a"));

                        // Major service (ST002) occupies 3 hours, Normal (ST001) occupies 1 hour
                        int hours = "ST002".equals(a.getServiceTypeId()) ? 3 : 1;
                        LocalTime endTime = startTime.plusHours(hours);

                        if (!slotTime.isBefore(startTime) && slotTime.isBefore(endTime)) {
                            String techName = resolveTechName(a.getTechnicianId());
                            matches.add("Booked — " + a.getAppointmentId()
                                + " | " + techName
                                + " | " + ("ST001".equals(a.getServiceTypeId()) ? "Normal" : "Major")
                                + " | " + a.getStatus());
                        }
                    } catch (Exception ignored) {}
                }
                String status = matches.isEmpty() ? "Available" : String.join(";  ", matches);
                model.addRow(new Object[]{slot, status});
            }
        };

        btnLoad.addActionListener(e -> loadTable.run());
        loadTable.run();

        dialog.add(top,                  BorderLayout.NORTH);
        dialog.add(new JScrollPane(tbl), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ─── Reset form after successful submit ───────────────────────────────────
    private void resetForm() {
        customerCombo.setSelectedIndex(0);
        vehicleField.setText("e.g. WXY-1234");
        vehicleField.setForeground(AppTheme.TEXT_MUTED);
        vehicleBrandField.setText("e.g. Toyota");
        vehicleBrandField.setForeground(AppTheme.TEXT_MUTED);
        vehicleModelField.setText("e.g. Camry");
        vehicleModelField.setForeground(AppTheme.TEXT_MUTED);
        serviceItemCombo.setSelectedIndex(0);
        dateField.setDate(LocalDate.now());
        timeSlotCombo.setSelectedIndex(0);
        refreshTechStatus();
        List<Appointment> appts = appointmentDAO.getAllAppointments();
        refreshSlots(appts);
        refreshTechCombo(appts);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.WARNING_MESSAGE);
    }

    private String resolveTechName(String techId) {
        if (techId == null || techId.equalsIgnoreCase("null") || techId.trim().isEmpty()) return "—";
        return technicianDAO.getAllTechnicians().stream()
            .filter(t -> t.getTechnicianId().equals(techId))
            .map(t -> t.getTechnicianId() + " — " + t.getUsername())
            .findFirst().orElse(techId);
    }

    // ─── Form row helper ──────────────────────────────────────────────────────
    private static final int FIELD_H = 38; // uniform height for all input rows
    private static final int NOTES_H = 80; // fixed height for notes scroll pane

    // Track how many rows have been added to each card
    private final Map<JPanel, Integer> cardRowIndex = new HashMap<>();

    private void addFormRow(JPanel card, String label, JComponent field) {
        boolean isNotes = (field instanceof JTextArea) || (field instanceof JScrollPane);

        int rowIdx = cardRowIndex.getOrDefault(card, 0);
        cardRowIndex.put(card, rowIdx + 1);

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(200, isNotes ? NOTES_H : FIELD_H));

        GridBagConstraints gbcLbl = new GridBagConstraints();
        gbcLbl.gridx   = 0;
        gbcLbl.gridy   = rowIdx;
        gbcLbl.fill    = GridBagConstraints.NONE;
        gbcLbl.anchor  = isNotes ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;
        gbcLbl.insets  = new Insets(5, 0, 5, 12);

        // Lock field height — skip JScrollPane since it manages its own size via overrides
        if (!(field instanceof JScrollPane)) {
            int fh = isNotes ? NOTES_H : FIELD_H;
            field.setPreferredSize(new Dimension(field.getPreferredSize().width, fh));
            field.setMinimumSize(new Dimension(0, fh));
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, fh));
        }

        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.gridx   = 1;
        gbcField.gridy   = rowIdx;
        gbcField.fill    = GridBagConstraints.HORIZONTAL;
        gbcField.weightx = 1.0;
        gbcField.weighty = 0.0;
        gbcField.insets  = new Insets(5, 0, 5, 0);
        gbcField.anchor  = isNotes ? GridBagConstraints.NORTH : GridBagConstraints.CENTER;

        card.add(lbl,   gbcLbl);
        card.add(field, gbcField);

        // Add/update a filler row at the bottom that absorbs all extra vertical space
        // so rows above it never stretch
        GridBagConstraints gbcFiller = new GridBagConstraints();
        gbcFiller.gridx   = 0;
        gbcFiller.gridy   = rowIdx + 1;
        gbcFiller.gridwidth = 2;
        gbcFiller.fill    = GridBagConstraints.VERTICAL;
        gbcFiller.weighty = 1.0;
        card.add(Box.createVerticalGlue(), gbcFiller);
    }

    private JPanel createStepCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(4, AppTheme.PADDING_CARD, 8, AppTheme.PADDING_CARD));
        return card;
    }

    private JPanel wrapCard(JPanel inner, String title) {
        JPanel outer = createCard();
        outer.setLayout(new BorderLayout());
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Title label at the top of the card
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        titleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(10, AppTheme.PADDING_CARD, 10, AppTheme.PADDING_CARD)
        ));

        outer.add(titleLabel, BorderLayout.NORTH);
        outer.add(inner,      BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(outer, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                    AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.setColor(AppTheme.BORDER_CARD);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1,
                    AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }
}