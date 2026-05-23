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
import javax.swing.plaf.LayerUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
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
    private JLabel slotsDateLabel;

    private String staffId = "CS001";

    private static final String[] TIME_SLOTS = {
        "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM",
        "12:00 PM", "02:00 PM", "03:00 PM", "04:00 PM"
    };

    public CounterStaffCreateAppointment(MainFrame frame) {
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE));

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

    private JTextField createRoundedField(String placeholder) {
        JTextField field = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 50));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);
                g2.dispose();
            }
        };
        field.putClientProperty("caretWidth", 2);
        field.setFont(AppTheme.FONT_SMALL);
        field.setForeground(AppTheme.TEXT_MUTED);
        field.setBackground(AppTheme.BG_CARD);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        field.setText(placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) { field.setText(""); field.setForeground(AppTheme.TEXT_PRIMARY); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().trim().isEmpty()) { field.setText(placeholder); field.setForeground(AppTheme.TEXT_MUTED); }
            }
        });
        return field;
    }

    // ─── Step 1 ───────────────────────────────────────────────────────────────
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
        vehicleField      = createRoundedField("e.g. WXY-1234");
        vehicleBrandField = createRoundedField("e.g. Toyota");
        vehicleModelField = createRoundedField("e.g. Camry");
        addFormRow(card, "Select Registered Customer *", custRow);
        addFormRow(card, "Vehicle Plate No. *",           vehicleField);
        addFormRow(card, "Vehicle Brand *",               vehicleBrandField);
        addFormRow(card, "Vehicle Model *",               vehicleModelField);
        return wrapCard(card, "Step 1 — Customer Selection");
    }

    // ─── Step 2 ───────────────────────────────────────────────────────────────
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
            String type = item[1].equals("ST001") ? "Normal" : "Major";
            serviceItemCombo.addItem(item[0] + " — " + item[2] + " (" + type + ")");
        }
    }

    // ─── Step 3 ───────────────────────────────────────────────────────────────
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
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? AppTheme.PRIMARY_HOVER : AppTheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 36, 36);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? AppTheme.PRIMARY_HOVER : AppTheme.PRIMARY);
                g2.setStroke(new BasicStroke(2.5f));
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

    // ─── Right panel ──────────────────────────────────────────────────────────
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
            BorderFactory.createEmptyBorder(6, 12, 6, 8)));
        techHeader.add(techTitle,  BorderLayout.WEST);
        techHeader.add(btnRefresh, BorderLayout.EAST);
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
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
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

    // ─── Listeners ────────────────────────────────────────────────────────────
    private void wireListeners() {
        dateField.addDateChangeListener(d -> onScheduleChanged());
        timeSlotCombo.addActionListener(e -> onScheduleChanged());
        serviceItemCombo.addActionListener(e -> refreshTechCombo());
    }

    private void onScheduleChanged() {
        List<Appointment> appts = appointmentDAO.getAllAppointments();
        refreshSlots(appts);
        refreshTechCombo(appts);
    }

    private void populateCustomerCombo() {
        customerCombo.removeAllItems();
        customerCombo.addItem("-- Select Registered Customer --");
        for (String[] c : customerDAO.getApprovedCustomers())
            customerCombo.addItem(c[0].trim() + " — " + c[2].trim());
    }

    // ─── Technician status panel ───────────────────────────────────────────────
    private void refreshTechStatus() {
        techStatusPanel.removeAll();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalTime now = LocalTime.now();
        LocalTime workStart = LocalTime.of(8, 0);
        LocalTime workEnd   = LocalTime.of(17, 0);
        boolean outOfHours  = now.isBefore(workStart) || !now.isBefore(workEnd);

        Set<String> busyNowIds = new HashSet<>();
        if (!outOfHours) {
            for (Appointment a : appointmentDAO.getAllAppointments()) {
                if (!a.getAppointmentDate().equals(today)) continue;
                if (!a.getStatus().equalsIgnoreCase("Approved")) continue;
                if (a.getTechnicianId() == null || a.getTechnicianId().equalsIgnoreCase("N/A")) continue;
                try {
                    LocalTime start = LocalTime.parse(a.getTimeSlot().trim(), DateTimeFormatter.ofPattern("hh:mm a"));
                    int hours = "ST002".equals(a.getServiceTypeId()) ? 3 : 1;
                    if (!now.isBefore(start) && now.isBefore(start.plusHours(hours))) busyNowIds.add(a.getTechnicianId());
                } catch (Exception ignored) {}
            }
        }
        for (Technician t : technicianDAO.getAllTechnicians()) {
            String status = outOfHours ? "Unavailable" : busyNowIds.contains(t.getTechnicianId()) ? "Busy" : "Available";
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
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        JLabel nameLabel = new JLabel(t.getTechnicianId() + ": " + t.getUsername());
        nameLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        nameLabel.setForeground(AppTheme.TEXT_PRIMARY);
        Color statusColor = switch (status) {
            case "Busy"        -> AppTheme.DANGER;
            case "Unavailable" -> AppTheme.TEXT_MUTED;
            default            -> AppTheme.SUCCESS;
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

    // ─── Occupied slots helper ─────────────────────────────────────────────────
    private Set<String> occupiedSlotsOf(Appointment a) {
        List<String> slotList = Arrays.asList(TIME_SLOTS);
        int startIndex = slotList.indexOf(a.getTimeSlot().trim());
        if (startIndex < 0) return Collections.emptySet();
        int count = "ST002".equals(a.getServiceTypeId()) ? 3 : 1;
        Set<String> result = new HashSet<>();
        for (int i = startIndex; i < Math.min(startIndex + count, TIME_SLOTS.length); i++) result.add(TIME_SLOTS[i]);
        return result;
    }

    // ─── Slots panel ──────────────────────────────────────────────────────────
    private void refreshSlots() { refreshSlots(appointmentDAO.getAllAppointments()); }

    private void refreshSlots(List<Appointment> allAppts) {
        slotsPanel.removeAll();
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
        List<Appointment> dateAppts = allAppts.stream()
            .filter(a -> a.getAppointmentDate().equals(targetDate)
                && (a.getStatus().equalsIgnoreCase("Approved") || a.getStatus().equalsIgnoreCase("Pending"))
                && a.getTechnicianId() != null && !a.getTechnicianId().equalsIgnoreCase("N/A"))
            .collect(Collectors.toList());
        for (int si = 0; si < TIME_SLOTS.length; si++) {
            final String slotStr = TIME_SLOTS[si];
            long booked = dateAppts.stream().filter(a -> occupiedSlotsOf(a).contains(slotStr)).count();
            int free = (int) Math.max(0, totalTechs - booked);
            slotsPanel.add(buildSlotRow(TIME_SLOTS[si], free, totalTechs));
            slotsPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        }
        slotsPanel.revalidate();
        slotsPanel.repaint();
    }

    private JPanel buildSlotRow(String slot, int free, int total) {
        Color bg, border, textColor;
        String label;
        if (free == 0)              { bg = new Color(255,235,235); border = new Color(255,180,180); textColor = AppTheme.DANGER;          label = "Full"; }
        else if (free <= total / 2) { bg = new Color(255,248,220); border = new Color(255,210,100); textColor = new Color(180,120,0);      label = free+"/"+total+"  Free"; }
        else                        { bg = new Color(235,255,240); border = new Color(180,230,180); textColor = new Color(30,150,60);       label = free+"/"+total+"  Free"; }
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(true);
        row.setBackground(bg);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < timeSlotCombo.getItemCount(); i++) {
                    if (slot.equals(timeSlotCombo.getItemAt(i))) { timeSlotCombo.setSelectedIndex(i); break; }
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

    // ─── Technician combo ─────────────────────────────────────────────────────
    private void refreshTechCombo() { refreshTechCombo(appointmentDAO.getAllAppointments()); }

    private void refreshTechCombo(List<Appointment> allAppts) {
        techCombo.removeAllItems();
        String dateRaw = dateField.getText().trim();
        String slot    = (String) timeSlotCombo.getSelectedItem();
        String dateISO;
        try {
            LocalDate parsed = LocalDate.parse(dateRaw, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dateISO = parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception ignored) { dateISO = dateRaw; }

        String newServiceSel  = (String) serviceItemCombo.getSelectedItem();
        boolean serviceChosen = newServiceSel != null && !newServiceSel.startsWith("--");
        boolean ready         = !dateISO.isEmpty() && slot != null && serviceChosen;
        techCombo.setEnabled(ready);
        if (btnViewSchedule != null) btnViewSchedule.setEnabled(ready);
        if (!ready) { techCombo.addItem("-- Select date & time slot first --"); return; }

        techCombo.addItem("-- Select Technician --");
        String newServiceItemId = newServiceSel.split(" — ")[0].trim();
        String newServiceTypeId = ServiceItemDAO.getServiceTypeId(newServiceItemId);
        int newSlotsNeeded = "ST002".equals(newServiceTypeId) ? 3 : 1;

        List<String> slotList = Arrays.asList(TIME_SLOTS);
        int newStartIndex = slotList.indexOf(slot.trim());
        if (newStartIndex < 0) { techCombo.addItem("(Invalid time slot selected)"); techCombo.setEnabled(false); return; }

        Set<String> newOccupiedSlots = new HashSet<>();
        for (int i = newStartIndex; i < Math.min(newStartIndex + newSlotsNeeded, TIME_SLOTS.length); i++)
            newOccupiedSlots.add(TIME_SLOTS[i]);

        final String date = dateISO;
        final Set<String> busyIds = allAppts.stream()
            .filter(a -> (a.getStatus().equalsIgnoreCase("Approved") || a.getStatus().equalsIgnoreCase("Pending"))
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

        if (free.isEmpty()) techCombo.addItem("(No technicians free for this slot)");
        else free.forEach(t -> techCombo.addItem(t.getTechnicianId() + " — " + t.getUsername()));
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

        if (custSel == null || custSel.startsWith("--"))                               { showError("Please select a customer."); return; }
        if (plate.isEmpty() || plate.equals("e.g. WXY-1234"))                         { showError("Please enter the vehicle plate number."); return; }
        if (brand.isEmpty() || brand.equals("e.g. Toyota"))                           { showError("Please enter the vehicle brand."); return; }
        if (model.isEmpty() || model.equals("e.g. Camry"))                            { showError("Please enter the vehicle model."); return; }
        if (serviceSel == null || serviceSel.startsWith("--"))                         { showError("Please select a service item."); return; }
        if (date.isEmpty())                                                             { showError("Please enter the appointment date."); return; }
        if (techSel == null || techSel.startsWith("--") || techSel.startsWith("(No")) { showError("Please select an available technician."); return; }

        String customerId    = custSel.split(" — ")[0].trim();
        String serviceItemId = serviceSel.split(" — ")[0].trim();
        String serviceTypeId = ServiceItemDAO.getServiceTypeId(serviceItemId);
        String techId        = techSel.split(" — ")[0].trim();
        String dateISO;
        try {
            LocalDate parsed = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            dateISO = parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception ex) { dateISO = date; }

        Appointment appt = new Appointment("", customerId, staffId, serviceTypeId,
            techId, serviceItemId, slot, dateISO, "Approved", brand, model, plate, "N/A");

        boolean ok = appointmentDAO.addAppointment(appt);
        if (ok) {
            UIComponent.showDialog(this, "Appointment Created",
                UIComponent.DialogType.SUCCESS,
                "Appointment created and approved successfully!",
                null, new UIComponent.DialogAction[]{
                    new UIComponent.DialogAction("OK", UIComponent.DialogActionStyle.PRIMARY, this::resetForm)
                });
        } else {
            showError("Failed to save appointment. Please try again.");
        }
    }

    // ─── Customer search dialog ────────────────────────────────────────────────
    private void showCustomerSearch() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel root = buildDialogRootCard();
        root.setLayout(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD,
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD));

        root.add(buildDialogHeader("Search Customer", dialog), BorderLayout.NORTH);

        // ── search row ────────────────────────────────────────────────────────
        JTextField searchField = UIComponent.createTextField("Search by ID, name or email...");
        JButton btnGo = UIComponent.createSmallButton("Search");
        btnGo.setPreferredSize(new Dimension(82, 36));
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(btnGo,       BorderLayout.EAST);

        // ── table — styled like ManageEmployeesPage ───────────────────────────
        String[] cols = {"ID", "Name", "Email", "Contact"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tbl = buildStyledTable(tableModel);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.getColumnModel().getColumn(0).setPreferredWidth(70);
        tbl.getColumnModel().getColumn(1).setPreferredWidth(150);
        tbl.getColumnModel().getColumn(2).setPreferredWidth(190);
        tbl.getColumnModel().getColumn(3).setPreferredWidth(110);

        // Wrap in rounded card using JLayer (same as ManageEmployeesPage)
        JPanel tablePanel = buildRoundedTablePanel(tbl);
        tablePanel.setPreferredSize(new Dimension(520, 280));

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(searchRow,  BorderLayout.NORTH);
        center.add(tablePanel, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        // ── footer ────────────────────────────────────────────────────────────
        JButton btnCancel = UIComponent.createOutlineButton("Cancel");
        JButton btnSelect = UIComponent.createPrimaryButton("Select");
        btnCancel.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footer.setOpaque(false);
        footer.add(btnCancel);
        footer.add(btnSelect);
        root.add(footer, BorderLayout.SOUTH);

        // ── logic ─────────────────────────────────────────────────────────────
        Runnable populate = () -> {
            tableModel.setRowCount(0);
            String q = searchField.getText().trim().toLowerCase();
            for (String[] c : customerDAO.getAllCustomers()) {
                if (q.isEmpty() || c[0].toLowerCase().contains(q)
                        || c[2].toLowerCase().contains(q) || c[3].toLowerCase().contains(q))
                    tableModel.addRow(new Object[]{c[0], c[2], c[3], c[4]});
            }
        };
        populate.run();
        btnGo.addActionListener(e -> populate.run());
        searchField.addActionListener(e -> populate.run());

        Runnable doSelect = () -> {
            int row = tbl.getSelectedRow();
            if (row < 0) return;
            String id = (String) tableModel.getValueAt(row, 0);
            for (int i = 0; i < customerCombo.getItemCount(); i++) {
                if (customerCombo.getItemAt(i).startsWith(id + " — ")) {
                    customerCombo.setSelectedIndex(i); break;
                }
            }
            dialog.dispose();
        };
        btnSelect.addActionListener(e -> doSelect.run());
        tbl.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) doSelect.run(); }
        });
        tbl.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
           .put(KeyStroke.getKeyStroke("ENTER"), "select");
        tbl.getActionMap().put("select", new javax.swing.AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { doSelect.run(); }
        });

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(560, dialog.getHeight()));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    // ─── Technician schedule dialog ───────────────────────────────────────────
    private void showTechSchedule() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel root = buildDialogRootCard();
        root.setLayout(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD,
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD));

        root.add(buildDialogHeader("Technician Schedule", dialog), BorderLayout.NORTH);

        // ── filter row ────────────────────────────────────────────────────────
        JComboBox<String> techFilter = UIComponent.createComboBox(new String[0]);
        techFilter.setPreferredSize(new Dimension(200, 36));
        techFilter.addItem("All Technicians");
        for (Technician t : technicianDAO.getAllTechnicians())
            techFilter.addItem(t.getTechnicianId() + " — " + t.getUsername());

        UIComponent.DatePickerField datePicker = UIComponent.createDatePicker(LocalDate.now());
        datePicker.setPreferredSize(new Dimension(130, 36));

        JButton btnLoad = UIComponent.createSmallButton("↺  Refresh");
        btnLoad.setPreferredSize(new Dimension(110, 36));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);
        filterRow.add(techFilter);
        filterRow.add(Box.createHorizontalStrut(4));
        filterRow.add(datePicker);
        filterRow.add(btnLoad);

        // ── grid holder ───────────────────────────────────────────────────────
        JPanel gridHolder = new JPanel(new BorderLayout());
        gridHolder.setOpaque(false);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(filterRow,  BorderLayout.NORTH);
        center.add(gridHolder, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        // ── footer: legend + close ────────────────────────────────────────────
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        legend.setOpaque(false);
        for (Object[] item : new Object[][]{
                {new Color(235,255,240), new Color(30,150,60),  "Free"},
                {new Color(255,220,220), AppTheme.DANGER,        "Booked"},
                {new Color(255,235,235), new Color(180,50,50),   "Booked(Cont.)"}}) {
            JLabel dot = new JLabel("●  " + item[2]);
            dot.setFont(AppTheme.FONT_CAPTION);
            dot.setForeground((Color) item[1]);
            dot.setOpaque(true);
            dot.setBackground((Color) item[0]);
            dot.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
            legend.add(dot);
        }
        JButton btnClose = UIComponent.createOutlineButton("Close");
        btnClose.setPreferredSize(new Dimension(100, 36));
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(legend,   BorderLayout.WEST);
        footer.add(btnClose, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);

        // ── load logic ────────────────────────────────────────────────────────
        Runnable loadGrid = () -> {
            String techSel = (String) techFilter.getSelectedItem();
            String filterTechId = (techSel == null || techSel.equals("All Technicians"))
                ? null : techSel.split(" — ")[0].trim();
            LocalDate selDate = datePicker.getDate();
            if (selDate == null) selDate = LocalDate.now();
            String targetDateISO = selDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            List<Technician> allTechs = technicianDAO.getAllTechnicians();
            List<Technician> displayTechs = filterTechId == null ? allTechs
                : allTechs.stream().filter(t -> t.getTechnicianId().equals(filterTechId)).collect(Collectors.toList());

            List<Appointment> appts = appointmentDAO.getAllAppointments().stream()
                .filter(a -> a.getAppointmentDate().trim().equals(targetDateISO)
                    && (a.getStatus().equalsIgnoreCase("Approved") || a.getStatus().equalsIgnoreCase("Pending"))
                    && (filterTechId == null || filterTechId.equals(a.getTechnicianId())))
                .collect(Collectors.toList());

            gridHolder.removeAll();
            gridHolder.add(buildScheduleGrid(appts, displayTechs), BorderLayout.CENTER);
            gridHolder.revalidate();
            gridHolder.repaint();
        };

        btnLoad.addActionListener(e -> loadGrid.run());
        datePicker.addDateChangeListener(d -> loadGrid.run());
        techFilter.addActionListener(e -> loadGrid.run());
        loadGrid.run();

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(720, 560));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    // ─── Schedule grid — rounded via JLayer ───────────────────────────────────
    private JPanel buildScheduleGrid(List<Appointment> appts, List<Technician> displayTechs) {
        int techCount = displayTechs.size();

        String[] colNames = new String[techCount + 1];
        colNames[0] = "Time Slot";
        for (int i = 0; i < techCount; i++) {
            Technician t = displayTechs.get(i);
            colNames[i + 1] = "<html><center><b>" + t.getTechnicianId()
                + "</b><br><font color='#888888' size='2'>" + t.getUsername() + "</font></center></html>";
        }

        Object[][] data = new Object[TIME_SLOTS.length][techCount + 1];
        for (int si = 0; si < TIME_SLOTS.length; si++) {
            data[si][0] = TIME_SLOTS[si];
            String slotStr = TIME_SLOTS[si];
            for (int ti = 0; ti < techCount; ti++) {
                String techId = displayTechs.get(ti).getTechnicianId();
                Optional<Appointment> match = appts.stream()
                    .filter(a -> techId.equals(a.getTechnicianId()) && occupiedSlotsOf(a).contains(slotStr))
                    .findFirst();
                if (match.isPresent()) {
                    Appointment a = match.get();
                    boolean isStart = a.getTimeSlot().trim().equals(slotStr);
                    String type = "ST002".equals(a.getServiceTypeId()) ? "Major" : "Normal";
                    data[si][ti + 1] = isStart ? "BOOKED|" + a.getAppointmentId() + "|" + type
                                                : "CONT|"  + a.getAppointmentId();
                } else {
                    data[si][ti + 1] = "FREE";
                }
            }
        }

        DefaultTableModel gridModel = new DefaultTableModel(data, colNames) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable grid = new JTable(gridModel) {
            // Row dividers — same technique as ManageEmployeesPage
            @Override public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.SEPARATOR);
                for (int i = 0; i < getRowCount(); i++)
                    g2.drawLine(0, (i + 1) * getRowHeight() - 1, getWidth(), (i + 1) * getRowHeight() - 1);
                g2.dispose();
            }
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                JLabel lbl = new JLabel();
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

                if (col == 0) {
                    lbl.setText(TIME_SLOTS[row]);
                    lbl.setFont(AppTheme.FONT_SMALL_BOLD);
                    lbl.setForeground(AppTheme.TEXT_SECONDARY);
                    lbl.setBackground(AppTheme.BG_PAGE);
                } else {
                    String val = (String) getValueAt(row, col);
                    if (val == null || "FREE".equals(val)) {
                        lbl.setText("Free");
                        lbl.setFont(AppTheme.FONT_SMALL);
                        lbl.setBackground(new Color(235, 255, 240));
                        lbl.setForeground(new Color(30, 150, 60));
                    } else if (val.startsWith("CONT|")) {
                        lbl.setText("↓ " + val.split("\\|")[1]);
                        lbl.setFont(AppTheme.FONT_CAPTION);
                        lbl.setBackground(new Color(255, 235, 235));
                        lbl.setForeground(new Color(180, 50, 50));
                    } else {
                        String[] parts = val.split("\\|");
                        String apptId = parts.length > 1 ? parts[1] : "—";
                        String type   = parts.length > 2 ? parts[2] : "";
                        lbl.setText("<html><center><b>" + apptId + "</b>"
                            + "<br><font color='#cc3333' size='2'>" + type + "</font></center></html>");
                        lbl.setFont(AppTheme.FONT_SMALL);
                        lbl.setBackground(new Color(255, 220, 220));
                        lbl.setForeground(AppTheme.DANGER);
                    }
                }
                return lbl;
            }
        };

        grid.setRowHeight(48);
        grid.setShowGrid(false);
        grid.setIntercellSpacing(new Dimension(0, 0));
        grid.setEnabled(false);
        grid.setBackground(AppTheme.BG_CARD);
        grid.setFillsViewportHeight(false);

        // ── Header — same style as ManageEmployeesPage ────────────────────────
        grid.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel lbl = new JLabel(val == null ? "" : val.toString());
                lbl.setOpaque(true);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(AppTheme.BG_PAGE);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)));
                if (col == 0) {
                    lbl.setText("Time Slot");
                    lbl.setFont(AppTheme.FONT_LABEL);
                    lbl.setForeground(AppTheme.TEXT_MUTED);
                } else {
                    lbl.setFont(AppTheme.FONT_SMALL_BOLD);
                    lbl.setForeground(AppTheme.TEXT_PRIMARY);
                }
                return lbl;
            }
        });
        grid.getTableHeader().setPreferredSize(new Dimension(0, 50));
        grid.getTableHeader().setReorderingAllowed(false);
        grid.getTableHeader().setResizingAllowed(false);
        grid.getTableHeader().setBackground(AppTheme.BG_PAGE);

        grid.getColumnModel().getColumn(0).setMinWidth(85);
        grid.getColumnModel().getColumn(0).setMaxWidth(100);
        for (int i = 1; i <= techCount; i++) grid.getColumnModel().getColumn(i).setPreferredWidth(140);

        // ── Pull header + scroll into inner panel, then JLayer for rounded clip ─
        JScrollPane scroll = new JScrollPane(grid);
        // Pin viewport height to exact table height — prevents blank space below last row
        scroll.getViewport().setPreferredSize(
            new Dimension(0, grid.getRowHeight() * TIME_SLOTS.length));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(true);
        scroll.setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        JTableHeader gridHeader = grid.getTableHeader();
        gridHeader.setOpaque(true);
        gridHeader.setBackground(AppTheme.BG_PAGE);
        scroll.setColumnHeader(null);   // pull header out of scroll pane

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(true);
        inner.setBackground(AppTheme.BG_CARD);
        inner.add(gridHeader, BorderLayout.NORTH);
        inner.add(scroll,     BorderLayout.CENTER);

        return wrapWithRoundedLayer(inner);
    }

    // ─── Reset form ───────────────────────────────────────────────────────────
    private void resetForm() {
        customerCombo.setSelectedIndex(0);
        vehicleField.setText("e.g. WXY-1234");    vehicleField.setForeground(AppTheme.TEXT_MUTED);
        vehicleBrandField.setText("e.g. Toyota"); vehicleBrandField.setForeground(AppTheme.TEXT_MUTED);
        vehicleModelField.setText("e.g. Camry");  vehicleModelField.setForeground(AppTheme.TEXT_MUTED);
        serviceItemCombo.setSelectedIndex(0);
        dateField.setDate(LocalDate.now());
        timeSlotCombo.setSelectedIndex(0);
        refreshTechStatus();
        List<Appointment> appts = appointmentDAO.getAllAppointments();
        refreshSlots(appts);
        refreshTechCombo(appts);
    }

    private void showError(String msg) {
        UIComponent.showDialog(this, "Validation Error", UIComponent.DialogType.WARNING, msg, null, null);
    }

    private String resolveTechName(String techId) {
        if (techId == null || techId.equalsIgnoreCase("null") || techId.trim().isEmpty()) return "—";
        return technicianDAO.getAllTechnicians().stream()
            .filter(t -> t.getTechnicianId().equals(techId))
            .map(t -> t.getTechnicianId() + " — " + t.getUsername())
            .findFirst().orElse(techId);
    }

    // ─── Dialog helpers ───────────────────────────────────────────────────────

    private JPanel buildDialogRootCard() {
        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.setColor(AppTheme.BORDER_CARD);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        return root;
    }

    private JPanel buildDialogHeader(String title, JDialog dialog) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(0, 0, 10, 0)));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppTheme.FONT_BODY_BOLD);
        titleLbl.setForeground(AppTheme.TEXT_PRIMARY);
        JButton btnX = new JButton("✕");
        btnX.setFont(AppTheme.FONT_SMALL_BOLD);
        btnX.setForeground(AppTheme.TEXT_MUTED);
        btnX.setContentAreaFilled(false);
        btnX.setBorderPainted(false);
        btnX.setFocusPainted(false);
        btnX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnX.addActionListener(e -> dialog.dispose());
        btnX.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btnX.setForeground(AppTheme.DANGER); }
            @Override public void mouseExited(MouseEvent e)  { btnX.setForeground(AppTheme.TEXT_MUTED); }
        });
        header.add(titleLbl, BorderLayout.WEST);
        header.add(btnX,     BorderLayout.EAST);
        return header;
    }

    /**
     * JTable styled like ManageEmployeesPage:
     * row height 46, subtle row dividers painted in paintComponent,
     * no grid lines, proper header with FONT_LABEL + SEPARATOR bottom border.
     */
    private JTable buildStyledTable(DefaultTableModel model) {
        JTable tbl = new JTable(model) {
            @Override public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(AppTheme.SEPARATOR);
                for (int i = 0; i < getRowCount(); i++)
                    g2.drawLine(0, (i + 1) * getRowHeight() - 1, getWidth(), (i + 1) * getRowHeight() - 1);
                g2.dispose();
            }
        };
        tbl.setRowHeight(46);
        tbl.setFont(AppTheme.FONT_SMALL);
        tbl.setForeground(AppTheme.TEXT_PRIMARY);
        tbl.setBackground(AppTheme.BG_CARD);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setFillsViewportHeight(true);
        tbl.setSelectionBackground(AppTheme.PRIMARY_LIGHT);
        tbl.setSelectionForeground(AppTheme.TEXT_PRIMARY);

        // Header — same as ManageEmployeesPage
        tbl.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = new JLabel(val == null ? "" : val.toString());
                lbl.setFont(AppTheme.FONT_LABEL);
                lbl.setForeground(AppTheme.TEXT_SECONDARY);
                lbl.setBackground(AppTheme.BG_PAGE);
                lbl.setOpaque(true);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 14, 0, 8)));
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                return lbl;
            }
        });
        tbl.getTableHeader().setPreferredSize(new Dimension(0, 38));
        tbl.getTableHeader().setReorderingAllowed(false);
        tbl.getTableHeader().setResizingAllowed(false);

        // Cell renderer — padded, col 0 as muted ID
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 8));
                if (!sel) {
                    setBackground(AppTheme.BG_CARD);
                    setForeground(col == 0 ? AppTheme.TEXT_SECONDARY : AppTheme.TEXT_PRIMARY);
                }
                setFont(col == 0 ? AppTheme.FONT_LABEL : AppTheme.FONT_SMALL);
                return this;
            }
        };
        for (int i = 0; i < model.getColumnCount(); i++)
            tbl.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);

        return tbl;
    }

    /**
     * Wraps a JTable (with its header pulled out) into a rounded-corner card
     * using JLayer — identical approach to ManageEmployeesPage.buildTablePanel().
     */
    private JPanel buildRoundedTablePanel(JTable tbl) {
        JScrollPane scroll = new JScrollPane(tbl);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(true);
        scroll.setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JTableHeader tableHeader = tbl.getTableHeader();
        tableHeader.setOpaque(true);
        tableHeader.setBackground(AppTheme.BG_PAGE);
        scroll.setColumnHeader(null);   // pull header out for clean corner clipping

        JPanel inner = new JPanel(new BorderLayout());
        inner.setOpaque(true);
        inner.setBackground(AppTheme.BG_CARD);
        inner.add(tableHeader, BorderLayout.NORTH);
        inner.add(scroll,      BorderLayout.CENTER);

        return wrapWithRoundedLayer(inner);
    }

    /**
     * Shared JLayer wrapper: clips contents to a rounded rect and draws
     * a 1px SEPARATOR border on top — reusable for both the customer table
     * and the schedule grid.
     */
    private JPanel wrapWithRoundedLayer(JPanel inner) {
        final int r = AppTheme.RADIUS_CARD * 2;
        LayerUI<JPanel> layerUI = new LayerUI<JPanel>() {
            @Override public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), r, r);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, c.getWidth(), c.getHeight(), r, r));
                super.paint(g2, c);
                g2.setClip(null);
                g2.setColor(AppTheme.SEPARATOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, r, r);
                g2.dispose();
            }
        };
        JLayer<JPanel> card = new JLayer<>(inner, layerUI);
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Form row helpers ─────────────────────────────────────────────────────
    private static final int FIELD_H = 38;
    private static final int NOTES_H = 80;
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
        gbcLbl.gridx = 0; gbcLbl.gridy = rowIdx;
        gbcLbl.fill = GridBagConstraints.NONE;
        gbcLbl.anchor = isNotes ? GridBagConstraints.NORTHWEST : GridBagConstraints.WEST;
        gbcLbl.insets = new Insets(5, 0, 5, 12);

        if (!(field instanceof JScrollPane)) {
            int fh = isNotes ? NOTES_H : FIELD_H;
            field.setPreferredSize(new Dimension(field.getPreferredSize().width, fh));
            field.setMinimumSize(new Dimension(0, fh));
            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, fh));
        }

        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.gridx = 1; gbcField.gridy = rowIdx;
        gbcField.fill = GridBagConstraints.HORIZONTAL;
        gbcField.weightx = 1.0; gbcField.weighty = 0.0;
        gbcField.insets = new Insets(5, 0, 5, 0);
        gbcField.anchor = isNotes ? GridBagConstraints.NORTH : GridBagConstraints.CENTER;

        card.add(lbl,   gbcLbl);
        card.add(field, gbcField);

        GridBagConstraints gbcFiller = new GridBagConstraints();
        gbcFiller.gridx = 0; gbcFiller.gridy = rowIdx + 1;
        gbcFiller.gridwidth = 2; gbcFiller.fill = GridBagConstraints.VERTICAL;
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
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        titleLabel.setForeground(AppTheme.TEXT_SECONDARY);
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            BorderFactory.createEmptyBorder(10, AppTheme.PADDING_CARD, 10, AppTheme.PADDING_CARD)));
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
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.setColor(AppTheme.BORDER_CARD);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }
}