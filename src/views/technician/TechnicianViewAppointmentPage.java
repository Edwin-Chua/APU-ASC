package views.technician;

import models.dao.AppointmentDAO;
import models.entities.Appointment;
import models.entities.User;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class TechnicianViewAppointmentPage extends JPanel {

    private final MainFrame frame;
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private User currentUser;

    // Filter state
    private String activeFilter = "All";

    // UI references
    private JLabel lblTotal, lblApproved, lblCompleted;
    private JPanel appointmentListPanel;
    private JTextField searchField;

    // Filter buttons for toggling active style
    private JButton btnAll, btnApproved, btnCompleted;

    public TechnicianViewAppointmentPage(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_CONTENT);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildScrollBody(), BorderLayout.CENTER);

        // Auto-refresh whenever this page is shown (e.g. switching back from Service Task)
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0
                    && isShowing()) {
                refreshList();
            }
        });
    }

    // ─────────────────────────────────────────────
    //  Header
    // ─────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BG_HEADER);
        header.setPreferredSize(new Dimension(0, AppTheme.HEADER_HEIGHT));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_HEADER),
            BorderFactory.createEmptyBorder(0, AppTheme.PADDING_PAGE, 0, AppTheme.PADDING_PAGE)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(UIComponent.createPageTitle("My Appointments"));

        JButton btnRefresh = UIComponent.createLinkButton("↻  Refresh");
        btnRefresh.addActionListener(e -> refreshList());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnRefresh);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ─────────────────────────────────────────────
    //  Scrollable body
    // ─────────────────────────────────────────────

    private JScrollPane buildScrollBody() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BG_CONTENT);
        content.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, 80, AppTheme.PADDING_PAGE, 80
        ));

        // Subtitle
        JLabel subtitle = new JLabel("Appointments assigned to you by counter staff");
        subtitle.setFont(AppTheme.FONT_CAPTION);
        subtitle.setForeground(AppTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(subtitle);
        content.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        // Stats row
        content.add(buildStatsRow());
        content.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        // Filter + search bar
        content.add(buildFilterBar());
        content.add(UIComponent.verticalGap(AppTheme.GAP_ITEM));

        // Appointment list (refreshable)
        appointmentListPanel = new JPanel();
        appointmentListPanel.setLayout(new BoxLayout(appointmentListPanel, BoxLayout.Y_AXIS));
        appointmentListPanel.setOpaque(false);
        appointmentListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(appointmentListPanel);

        return UIComponent.createAppleScrollPane(content);
    }

    // ─────────────────────────────────────────────
    //  Stats row: 4 metric cards
    // ─────────────────────────────────────────────

    private JPanel buildStatsRow() {
    	JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
    	row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        lblTotal    = new JLabel("0");
        lblApproved = new JLabel("0");
        lblCompleted = new JLabel("0");

        row.add(buildStatCard("Total Assigned",  lblTotal,    AppTheme.TEXT_PRIMARY));
        row.add(buildStatCard("Approved",        lblApproved, new Color(0x1D9E75)));
        row.add(buildStatCard("Completed",       lblCompleted, new Color(0x185FA5)));

        return row;
    }

    private JPanel buildStatCard(String labelText, JLabel valueLabel, Color valueColor) {
        JPanel card = UIComponent.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(AppTheme.FONT_CAPTION);
        lbl.setForeground(AppTheme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(AppTheme.FONT_HEADING_2);
        valueLabel.setForeground(valueColor);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(lbl);
        card.add(UIComponent.verticalGap(4));
        card.add(valueLabel);

        return card;
    }

    // ─────────────────────────────────────────────
    //  Filter bar
    // ─────────────────────────────────────────────

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        bar.setOpaque(false);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        btnAll       = buildFilterButton("All");
        btnApproved  = buildFilterButton("Approved");
        btnCompleted = buildFilterButton("Completed");

        setFilterActive(btnAll);

        btnAll.addActionListener(e       -> applyFilter("All",      btnAll));
        btnApproved.addActionListener(e  -> applyFilter("Approved",  btnApproved));
        btnCompleted.addActionListener(e -> applyFilter("Completed",  btnCompleted));

        bar.add(btnAll);
        bar.add(btnApproved);
        bar.add(btnCompleted);

        // Search field pushed to right
        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchWrap.setOpaque(false);
        searchField = UIComponent.createTextField("Search by ID or customer...");
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.addActionListener(e -> refreshList());
        searchWrap.add(searchField);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        outer.add(bar, BorderLayout.WEST);
        outer.add(searchWrap, BorderLayout.EAST);

        return outer;
    }

    private JButton buildFilterButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.FONT_SMALL);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_CARD, 1, true),
            new EmptyBorder(4, 12, 4, 12)
        ));
        btn.setBackground(AppTheme.BG_CONTENT);
        btn.setForeground(AppTheme.TEXT_MUTED);
        btn.setOpaque(true);
        return btn;
    }

    private void setFilterActive(JButton btn) {
        for (JButton b : new JButton[]{btnAll, btnApproved, btnCompleted}) {
            if (b == null) continue;
            b.setBackground(AppTheme.BG_CONTENT);
            b.setForeground(AppTheme.TEXT_MUTED);
            b.setFont(AppTheme.FONT_SMALL);
        }
        btn.setBackground(AppTheme.BG_CARD);
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setFont(AppTheme.FONT_SMALL.deriveFont(Font.BOLD));
    }

    private void applyFilter(String filter, JButton btn) {
        activeFilter = filter;
        setFilterActive(btn);
        refreshList();
    }

    // ─────────────────────────────────────────────
    //  Appointment list
    // ─────────────────────────────────────────────

    private void refreshList() {
        appointmentListPanel.removeAll();

        if (currentUser == null) {
            appointmentListPanel.revalidate();
            appointmentListPanel.repaint();
            return;
        }

        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        List<Appointment> appointments = appointmentDAO.getAppointmentsByTechnicianId(currentUser.getUserId());

        int total = 0, approved = 0, completed = 0;

        for (Appointment appt : appointments) {
            String status = appt.getStatus();

            if (!"Approved".equalsIgnoreCase(status)
                    && !"Completed".equalsIgnoreCase(status)) {
                continue;
            }

            total++;

            if ("Approved".equalsIgnoreCase(status)) approved++;
            if ("Completed".equalsIgnoreCase(status)) completed++;
        }

        lblTotal.setText(String.valueOf(total));
        lblApproved.setText(String.valueOf(approved));
        lblCompleted.setText(String.valueOf(completed));

        for (Appointment appt : appointments) {
            String status = appt.getStatus();

            if (!"Approved".equalsIgnoreCase(status)
                    && !"Completed".equalsIgnoreCase(status)) {
                continue;
            }

            boolean matchFilter = activeFilter.equals("All")
                    || activeFilter.equalsIgnoreCase(status);

            boolean matchSearch = query.isEmpty()
                    || appt.getAppointmentId().toLowerCase().contains(query)
                    || (appt.getCustomerId() != null && appt.getCustomerId().toLowerCase().contains(query));

            if (!matchFilter || !matchSearch) continue;

            appointmentListPanel.add(buildAppointmentCard(appt));
            appointmentListPanel.add(UIComponent.verticalGap(10));
        }

        if (appointmentListPanel.getComponentCount() == 0) {
            JLabel empty = new JLabel("No appointments found.");
            empty.setFont(AppTheme.FONT_CAPTION);
            empty.setForeground(AppTheme.TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);

            appointmentListPanel.add(UIComponent.verticalGap(24));
            appointmentListPanel.add(empty);
        }

        appointmentListPanel.revalidate();
        appointmentListPanel.repaint();
    }

    // ─────────────────────────────────────────────
    //  Single appointment card
    // ─────────────────────────────────────────────

    private JPanel buildAppointmentCard(Appointment appt) {
        JPanel card = UIComponent.createCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        // ── Top row: ID + date on left, status badge on right ──
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JPanel idDateCol = new JPanel();
        idDateCol.setLayout(new BoxLayout(idDateCol, BoxLayout.Y_AXIS));
        idDateCol.setOpaque(false);

        JLabel idLabel = new JLabel(appt.getAppointmentId());
        idLabel.setFont(AppTheme.FONT_SMALL.deriveFont(Font.BOLD));
        idLabel.setForeground(AppTheme.TEXT_PRIMARY);

        String dateTime = appt.getAppointmentDate() + "  ·  " + appt.getTimeSlot();
        JLabel dateLabel = new JLabel(dateTime);
        dateLabel.setFont(AppTheme.FONT_CAPTION);
        dateLabel.setForeground(AppTheme.TEXT_MUTED);

        idDateCol.add(idLabel);
        idDateCol.add(UIComponent.verticalGap(2));
        idDateCol.add(dateLabel);

        JLabel statusBadge = buildStatusBadge(appt.getStatus());

        topRow.add(idDateCol, BorderLayout.WEST);
        topRow.add(statusBadge, BorderLayout.EAST);
        card.add(topRow, BorderLayout.NORTH);

        // ── Info grid: vehicle / plate / service type / service item / time slot ──
        JPanel infoGrid = new JPanel(new GridLayout(2, 3, 12, 6));
        infoGrid.setOpaque(false);

        infoGrid.add(buildInfoCell("Vehicle",      appt.getVehicleBrand() + " " + appt.getVehicleModel()));
        infoGrid.add(buildInfoCell("Plate no.",    appt.getVehiclePlate()));
        infoGrid.add(buildInfoCell("Service type", appt.getServiceTypeId()));
        infoGrid.add(buildInfoCell("Service item", appt.getServiceItemId()));
        infoGrid.add(buildInfoCell("Time slot",    appt.getTimeSlot()));
        infoGrid.add(new JLabel()); // spacer

        card.add(infoGrid, BorderLayout.CENTER);

        // ── Footer: customer ID left, View Details button right ──
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.BORDER_CARD),
            new EmptyBorder(6, 0, 0, 0)
        ));

        JLabel customerLabel = new JLabel("Customer: " + appt.getCustomerId());
        customerLabel.setFont(AppTheme.FONT_CAPTION);
        customerLabel.setForeground(AppTheme.TEXT_MUTED);

        JButton btnView = UIComponent.createLinkButton("View Details");
        btnView.addActionListener(e -> showAppointmentDetail(appt));

        footer.add(customerLabel, BorderLayout.WEST);
        footer.add(btnView, BorderLayout.EAST);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JPanel buildInfoCell(String label, String value) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_CAPTION);
        lbl.setForeground(AppTheme.TEXT_MUTED);

        JLabel val = new JLabel(value != null && !value.isBlank() ? value : "—");
        val.setFont(AppTheme.FONT_SMALL.deriveFont(Font.BOLD));
        val.setForeground(AppTheme.TEXT_PRIMARY);

        cell.add(lbl);
        cell.add(UIComponent.verticalGap(2));
        cell.add(val);
        return cell;
    }

    private JLabel buildStatusBadge(String status) {
        JLabel badge = new JLabel(status != null ? status : "—");
        badge.setFont(AppTheme.FONT_CAPTION.deriveFont(Font.BOLD));
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));

        if (status == null) status = "";
        switch (status.toLowerCase()) {
            case "approved" -> { badge.setBackground(new Color(0xE1F5EE)); badge.setForeground(new Color(0x0F6E56)); }
            case "completed" -> {
                badge.setBackground(new Color(0xE6F1FB));
                badge.setForeground(new Color(0x0C447C));
            }            default         -> { badge.setBackground(AppTheme.BG_CARD);   badge.setForeground(AppTheme.TEXT_MUTED); }
        }

        // Rounded corners via custom component
        badge.putClientProperty("html.disable", Boolean.FALSE);
        return badge;
    }

    // ─────────────────────────────────────────────
    //  Detail dialog
    // ─────────────────────────────────────────────

    private void showAppointmentDetail(Appointment appt) {
        JDialog dialog = new JDialog(
            SwingUtilities.getWindowAncestor(this) instanceof Frame f ? f : null,
            "Appointment Details — " + appt.getAppointmentId(),
            true
        );
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(AppTheme.BG_CONTENT);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppTheme.BG_CONTENT);
        body.setBorder(new EmptyBorder(24, 28, 24, 28));

        body.add(UIComponent.createSectionLabel("APPOINTMENT INFORMATION"));

        JPanel infoCard = UIComponent.createCard();
        infoCard.add(UIComponent.createInfoRow("Appointment ID",  labelFor(appt.getAppointmentId())));
        infoCard.add(UIComponent.createRowDivider());
        infoCard.add(UIComponent.createInfoRow("Date",            labelFor(appt.getAppointmentDate())));
        infoCard.add(UIComponent.createRowDivider());
        infoCard.add(UIComponent.createInfoRow("Time Slot",       labelFor(appt.getTimeSlot())));
        infoCard.add(UIComponent.createRowDivider());
        infoCard.add(UIComponent.createInfoRow("Status",          buildStatusBadge(appt.getStatus())));
        body.add(infoCard);
        body.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        body.add(UIComponent.createSectionLabel("VEHICLE & SERVICE"));

        JPanel vehicleCard = UIComponent.createCard();
        vehicleCard.add(UIComponent.createInfoRow("Brand",        labelFor(appt.getVehicleBrand())));
        vehicleCard.add(UIComponent.createRowDivider());
        vehicleCard.add(UIComponent.createInfoRow("Model",        labelFor(appt.getVehicleModel())));
        vehicleCard.add(UIComponent.createRowDivider());
        vehicleCard.add(UIComponent.createInfoRow("Plate",        labelFor(appt.getVehiclePlate())));
        vehicleCard.add(UIComponent.createRowDivider());
        vehicleCard.add(UIComponent.createInfoRow("Service Type", labelFor(appt.getServiceTypeId())));
        vehicleCard.add(UIComponent.createRowDivider());
        vehicleCard.add(UIComponent.createInfoRow("Service Item", labelFor(appt.getServiceItemId())));
        body.add(vehicleCard);
        body.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        if (appt.getTechnicianFeedback() != null && !appt.getTechnicianFeedback().isBlank()) {
            body.add(UIComponent.createSectionLabel("TECHNICIAN FEEDBACK"));
            JPanel fbCard = UIComponent.createCard();
            JLabel fbLabel = new JLabel("<html>" + appt.getTechnicianFeedback() + "</html>");
            fbLabel.setFont(AppTheme.FONT_SMALL);
            fbLabel.setForeground(AppTheme.TEXT_PRIMARY);
            fbCard.add(fbLabel);
            body.add(fbCard);
            body.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));
        }

        JButton btnClose = UIComponent.createPrimaryButton("Close");
        btnClose.setPreferredSize(new Dimension(120, 40));
        btnClose.setMaximumSize(new Dimension(120, 40));
        btnClose.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnClose.addActionListener(e -> dialog.dispose());
        body.add(btnClose);

        dialog.add(UIComponent.createAppleScrollPane(body));
        dialog.setVisible(true);
    }

    private JLabel labelFor(String text) {
        JLabel lbl = new JLabel(text != null && !text.isBlank() ? text : "—");
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        return lbl;
    }

    // ─────────────────────────────────────────────
    //  Load user (called by dashboard on refresh)
    // ─────────────────────────────────────────────

    public void loadUser(User user) {
        this.currentUser = user;
        refreshList();
    }
}
