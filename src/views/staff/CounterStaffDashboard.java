package views.staff;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import models.dao.AppointmentDAO;
import models.dao.CustomerDAO;
import models.entities.User;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.BaseDashboard;
import views.shared.UIComponent;

public class CounterStaffDashboard extends BaseDashboard {

    private static final String PAGE_REGISTER_CUSTOMER  = "registerCustomer";
    private static final String PAGE_VIEW_APPOINTMENT   = "viewAppointment";
    private static final String PAGE_CREATE_APPOINTMENT = "createAppointment";
    private static final String PAGE_BILLING            = "billing";
    private static final String PAGE_CALENDAR = "calendar";
    private static final String PAGE_PROFILE            = "profile";

    private JPanel contentPanel;

    private JButton btnRegisterCustomer;
    private JButton btnViewAppointment;
    private JButton btnCreateAppointment;
    private JButton btnBilling;
    private JButton btnCalendar;
    private JButton btnProfile;

    // Badge counts
    private int pendingCustomers    = 0;
    private int pendingAppointments = 0;
    private int pendingBilling      = 0;

    // Sub-pages
    private final CounterStaffRegisterCustomer  registerCustomerPage;
    private final CounterStaffViewAppointment   viewAppointmentPage;
    private final CounterStaffCreateAppointment createAppointmentPage;
    private final CounterStaffBilling           billingPage;
    private final CounterStaffCalendar calendarPage;
    private final CounterStaffProfile           profilePage;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final CustomerDAO    customerDAO    = new CustomerDAO();

    public CounterStaffDashboard(MainFrame frame) {
        super(frame);
        registerCustomerPage  = new CounterStaffRegisterCustomer(frame);
        viewAppointmentPage   = new CounterStaffViewAppointment(frame);
        createAppointmentPage = new CounterStaffCreateAppointment(frame);
        billingPage           = new CounterStaffBilling(frame);
        calendarPage = new CounterStaffCalendar(frame);
        profilePage           = new CounterStaffProfile(frame);

        contentPanel.add(registerCustomerPage,  PAGE_REGISTER_CUSTOMER);
        contentPanel.add(viewAppointmentPage,   PAGE_VIEW_APPOINTMENT);
        contentPanel.add(createAppointmentPage, PAGE_CREATE_APPOINTMENT);
        contentPanel.add(billingPage,           PAGE_BILLING);
        contentPanel.add(calendarPage, PAGE_CALENDAR);
        contentPanel.add(profilePage,           PAGE_PROFILE);

        refreshBadges();
        registerCustomerPage.setOnDataChanged(this::refreshBadges);
        viewAppointmentPage.setOnDataChanged(this::refreshBadges);
    }

    // ─── Badge data ───────────────────────────────────────────────────────────

    /** Re-queries pending counts and repaints affected nav buttons. */
    public void refreshBadges() {
        // Count pending customers
        pendingCustomers = 0;
        for (String[] c : customerDAO.getAllCustomers())
            if (c.length > 5 && "Pending".equalsIgnoreCase(c[5].trim()))
                pendingCustomers++;

        // Count pending appointments
        pendingAppointments = (int) appointmentDAO.getAllAppointments().stream()
            .filter(a -> "Pending".equalsIgnoreCase(a.getStatus()))
            .count();

        SwingUtilities.invokeLater(() -> {
            if (btnRegisterCustomer != null) btnRegisterCustomer.repaint();
            if (btnViewAppointment  != null) btnViewAppointment.repaint();
        });
        
        pendingBilling = (int) appointmentDAO.getAllAppointments().stream()
                .filter(a -> "Completed".equalsIgnoreCase(a.getStatus()))
                .count();

            SwingUtilities.invokeLater(() -> {
                if (btnRegisterCustomer != null) btnRegisterCustomer.repaint();
                if (btnViewAppointment  != null) btnViewAppointment.repaint();
                if (btnBilling          != null) btnBilling.repaint();  
            });
    }

    // ─── Badge nav button factory ─────────────────────────────────────────────

    /**
     * Creates a nav button that paints a red circle badge on the right
     * when count > 0. The count supplier is called on every repaint.
     */
    private JButton createBadgeNavButton(String label, java.util.function.IntSupplier countSupplier) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                // Paint the standard nav button background (active highlight)
                if (Boolean.TRUE.equals(getClientProperty("active"))) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(AppTheme.PRIMARY_LIGHT);
                    g2.fillRoundRect(8, 3, getWidth() - 16, getHeight() - 6, 14, 14);
                    g2.dispose();
                }
                super.paintComponent(g);

                int count = countSupplier.getAsInt();
                if (count <= 0) return;

                // Badge dimensions
                String text    = count > 99 ? "99+" : String.valueOf(count);
                Font   badgeFont = getFont().deriveFont(Font.BOLD, 10f);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setFont(badgeFont);
                FontMetrics fm   = g2.getFontMetrics();
                int textW        = fm.stringWidth(text);
                int badgeW       = Math.max(18, textW + 8);
                int badgeH       = 18;
                int badgeX       = getWidth() - badgeW - 12;
                int badgeY       = (getHeight() - badgeH) / 2;

                // Red pill background
                g2.setColor(new Color(220, 38, 38));
                g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, badgeH, badgeH);

                // White count text
                g2.setColor(Color.WHITE);
                int textX = badgeX + (badgeW - textW) / 2;
                int textY = badgeY + (badgeH + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, textX, textY);
                g2.dispose();
            }
        };

        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new java.awt.Dimension(AppTheme.SIDEBAR_WIDTH, 44));
        btn.setPreferredSize(new java.awt.Dimension(AppTheme.SIDEBAR_WIDTH, 44));
        btn.setFont(AppTheme.FONT_BODY);
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btn.setBorder(new javax.swing.border.EmptyBorder(0, 18, 0, 0));
        btn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        return btn;
    }

    // ─── Sidebar ──────────────────────────────────────────────────────────────

    @Override
    protected JPanel createSidebar() {
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);

        // Badge buttons for pages that have pending items
        btnRegisterCustomer  = createBadgeNavButton("Customer Management",
                                   () -> pendingCustomers);
        btnViewAppointment   = createBadgeNavButton("Appointments",
                                   () -> pendingAppointments);

        // Normal buttons for pages without badges
        btnCreateAppointment = UIComponent.createNavButton("Create Appointment");
        btnCalendar = UIComponent.createNavButton("Calendar");
        btnBilling			 = createBadgeNavButton("Billing", () -> pendingBilling);
        btnProfile           = UIComponent.createNavButton("Profile");

        btnRegisterCustomer.addActionListener(e  -> {
            navigate(btnRegisterCustomer, PAGE_REGISTER_CUSTOMER);
            refreshBadges();
        });
        btnViewAppointment.addActionListener(e   -> {
            navigate(btnViewAppointment, PAGE_VIEW_APPOINTMENT);
            refreshBadges();
        });
        btnCreateAppointment.addActionListener(e -> {
            navigate(btnCreateAppointment, PAGE_CREATE_APPOINTMENT);
            createAppointmentPage.refresh();
            refreshBadges();
        });       
        btnBilling.addActionListener(e -> {
            navigate(btnBilling, PAGE_BILLING);
            refreshBadges();
        });     
        btnCalendar.addActionListener(e -> {
            navigate(btnCalendar, PAGE_CALENDAR);
            calendarPage.refresh();
        });
        btnProfile.addActionListener(e           -> navigate(btnProfile,           PAGE_PROFILE));

        navPanel.add(btnRegisterCustomer);
        navPanel.add(btnViewAppointment);
        navPanel.add(btnCreateAppointment);
        navPanel.add(btnBilling);
        navPanel.add(btnCalendar); //
        navPanel.add(btnProfile);

        return buildSidebarShell("Counter Staff", navPanel);
    }

    @Override
    protected JPanel createContentPanel() {
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(AppTheme.BG_CONTENT);
        return contentPanel;
    }

    @Override
    public void refreshUser() {
        User user = frame.getLoggedInUser();
        if (profilePage != null && user != null) {
            profilePage.loadUser(user);
        }
        refreshBadges();
        navigate(btnRegisterCustomer, PAGE_REGISTER_CUSTOMER);
    }

    private void navigate(JButton btn, String page) {
        setActiveNav(btn);
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, page);
    }
}