package views.customer;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import models.customer_function.*;

public class CustomerPortal extends JFrame {
    private static final class DS {

        // Backgrounds
        static final Color BG_PAGE       = new Color(244, 244, 249);
        static final Color BG_CARD       = Color.WHITE;
        static final Color BG_SIDEBAR    = Color.WHITE;
        static final Color BG_HEADER     = new Color(20, 20, 100);
        static final Color BG_ACCENT     = new Color(0, 122, 255);
        static final Color BG_NAV_ACTIVE = new Color(237, 240, 255);

        // Foregrounds
        static final Color FG_LABEL      = new Color(28, 28, 30);
        static final Color FG_SECONDARY  = new Color(134, 134, 139);
        static final Color FG_TERTIARY   = new Color(180, 180, 185);
        static final Color FG_ACCENT     = new Color(0, 122, 255);
        static final Color FG_NAV_ACTIVE = new Color(20, 20, 100);
        static final Color FG_RED        = new Color(255, 59, 48);
        static final Color FG_GREEN      = new Color(40, 160, 80);
        static final Color FG_ORANGE     = new Color(180, 120, 0);
        static final Color FG_WHITE      = Color.WHITE;

        // Borders
        static final Color BORDER        = new Color(230, 230, 235);
        static final Color SEPARATOR     = new Color(230, 230, 235);

        // Typography
        static Font f(int weight, int size) { return new Font(face(), weight, size); }
        static Font regular(int size)       { return f(Font.PLAIN, size); }
        static Font bold(int size)          { return f(Font.BOLD,  size); }

        private static String face() {
            String os = System.getProperty("os.name", "").toLowerCase();
            if (os.contains("mac")) {
                Font sf = new Font(".AppleSystemUIFont", Font.PLAIN, 13);
                if (!sf.getFamily().equals("Dialog")) return ".AppleSystemUIFont";
            }
            if (os.contains("win")) {
                Font seg = new Font("Segoe UI", Font.PLAIN, 13);
                if (!seg.getFamily().equals("Dialog")) return "Segoe UI";
            }
            return "SansSerif";
        }

        // Geometry
        static final int RADIUS_CARD  = 10;
        static final int RADIUS_BTN   = 8;
        static final int RADIUS_FIELD = 6;
        static final int SIDEBAR_W    = 210;
    }

    // =========================================================================
    // FIELDS
    // =========================================================================
    private CardLayout cardLayout;
    private JPanel     contentPanel;
    private JButton    activeNavBtn = null;
    private final String customerID;

    private final CustomerDAO    customerDAO    = new CustomerDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final TechnicianDAO  technicianDAO  = new TechnicianDAO();
    private final FeedbackDAO    feedbackDAO    = new FeedbackDAO();
    private final ServiceItemDAO serviceItemDAO = new ServiceItemDAO();

    private Customer    customer;
    private Appointment upcoming;
    private JPanel      homePagePanel;

    private JButton navHomeBtn;
    private JButton navHistoryBtn;
    private JButton navMakeApptBtn;
    private JButton navProfileBtn;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    public CustomerPortal(String customerID) {
        this.customerID = customerID;

        customer = customerDAO.getCustomerByID(customerID);
        upcoming = appointmentDAO.getUpcomingAppointmentByCustomerID(customerID);

        setTitle("Automotive Service Centre — Customer Portal");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 660));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(DS.BG_PAGE);

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildMainArea(),  BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(DS.BG_HEADER);
        h.setPreferredSize(new Dimension(0, 52));
        h.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 22));

        JLabel title = new JLabel("Automotive Service Centre  —  Customer Portal");
        title.setFont(DS.bold(15));
        title.setForeground(DS.FG_WHITE);
        h.add(title, BorderLayout.WEST);
        return h;
    }

    private JPanel buildMainArea() {
        JPanel area = new JPanel(new BorderLayout());
        area.setBackground(DS.BG_PAGE);

        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(DS.BG_PAGE);

        homePagePanel = buildHomePage();
        contentPanel.add(homePagePanel,        "home");
        contentPanel.add(buildOurServicePage(), "ourService");
        contentPanel.add(buildHistoryPage(),    "history");
        contentPanel.add(buildMakeApptPage(),   "makeAppointment");
        contentPanel.add(buildProfilePage(),    "profile");

        area.add(buildSidebar(), BorderLayout.WEST);
        area.add(contentPanel,   BorderLayout.CENTER);
        return area;
    }

    // =========================================================================
    // HOME PAGE
    // =========================================================================
    private JPanel buildHomePage() {
        JPanel page = new JPanel();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(DS.BG_PAGE);
        page.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel greet = label("Welcome, " + (customer != null ? customer.getName() : ""), DS.bold(22), DS.FG_NAV_ACTIVE);
        greet.setAlignmentX(LEFT_ALIGNMENT);
        page.add(greet);
        page.add(Box.createRigidArea(new Dimension(0, 20)));

        upcoming = appointmentDAO.getUpcomingAppointmentByCustomerID(customerID);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(DS.BG_CARD);
        card.setBorder(new LineBorder(DS.BORDER, 1));
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JPanel cardHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        cardHeader.setBackground(DS.BG_HEADER);
        JLabel cardTitle = new JLabel("Next Upcoming Appointment");
        cardTitle.setForeground(DS.FG_WHITE);
        cardTitle.setFont(DS.bold(13));
        cardHeader.add(cardTitle);
        if (upcoming != null) {
            cardHeader.add(statusBadge(upcoming.getStatus()));
        }
        card.add(cardHeader, BorderLayout.NORTH);

        JPanel cardContent = new JPanel(new GridLayout(6, 2, 10, 10));
        cardContent.setBackground(DS.BG_CARD);
        cardContent.setBorder(new EmptyBorder(18, 24, 18, 24));

        if (upcoming != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy");
            String tech    = technicianDAO.getTechnicianName(upcoming.getTechnicianID());
            String vehicle = upcoming.getVehicleBrand() + " " + upcoming.getVehicleModel()
                           + " — " + upcoming.getVehiclePlate();

            addCardRow(cardContent, "Appointment Date:", upcoming.getAppointmentDate().format(fmt));
            addCardRow(cardContent, "Appointment Time:", upcoming.getStartTime());
            addCardRow(cardContent, "Service Type:",     upcoming.getServiceType());
            addCardRow(cardContent, "Technician:",       tech != null ? tech : "N/A");
            addCardRow(cardContent, "Vehicle:",          vehicle);
            addCardRow(cardContent, "Status:",           upcoming.getStatus());
        } else {
            JLabel noAppt = new JLabel("No upcoming appointment found.");
            noAppt.setFont(DS.regular(13));
            noAppt.setForeground(DS.FG_SECONDARY);
            cardContent.add(noAppt);
        }
        card.add(cardContent, BorderLayout.CENTER);

        JPanel reminderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        reminderPanel.setBackground(new Color(255, 255, 220));
        reminderPanel.setBorder(new MatteBorder(1, 0, 0, 0, new Color(220, 210, 100)));
        JLabel reminderLabel = new JLabel("Reminder: Please arrive 10 minutes early and bring your vehicle registration card.");
        reminderLabel.setFont(DS.regular(11));
        reminderPanel.add(reminderLabel);
        card.add(reminderPanel, BorderLayout.SOUTH);

        page.add(card);
        page.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel tiles = new JPanel(new GridLayout(1, 2, 16, 0));
        tiles.setOpaque(false);
        tiles.setAlignmentX(LEFT_ALIGNMENT);
        tiles.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JButton bookTile = createActionButton("Book New Service",   "Schedule a new appointment");
        JButton pastTile = createActionButton("View Past Services", "Browse your service history");
        bookTile.addActionListener(e -> navigate("makeAppointment", navMakeApptBtn));
        pastTile.addActionListener(e -> navigate("history",         navHistoryBtn));
        tiles.add(bookTile);
        tiles.add(pastTile);
        page.add(tiles);

        return page;
    }

    private void addCardRow(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(DS.regular(13));
        lbl.setForeground(DS.FG_SECONDARY);
        JLabel val = new JLabel(value);
        val.setFont(DS.regular(13));
        val.setForeground(DS.FG_LABEL);
        panel.add(lbl);
        panel.add(val);
    }

    private JButton createActionButton(String title, String sub) {
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = getModel().isRollover();
                g2.setColor(hover ? new Color(245, 245, 255) : DS.BG_CARD);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(hover ? DS.FG_NAV_ACTIVE : DS.BORDER);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                int tx = 18;
                g2.setFont(DS.bold(13));
                g2.setColor(DS.FG_LABEL);
                g2.drawString(title, tx, getHeight() / 2 - 2);
                g2.setFont(DS.regular(11));
                g2.setColor(DS.FG_SECONDARY);
                g2.drawString(sub, tx, getHeight() / 2 + 14);
                g2.dispose();
            }
        };
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 70));
        return btn;
    }

    private void refreshHomePage() {
        contentPanel.remove(homePagePanel);
        homePagePanel = buildHomePage();
        contentPanel.add(homePagePanel, "home");
        cardLayout.show(contentPanel, "home");
        setActive(navHomeBtn);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =========================================================================
    // OUR SERVICE PAGE
    // =========================================================================
    private JPanel buildOurServicePage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(DS.BG_PAGE);
        page.setBorder(new EmptyBorder(28, 32, 28, 32));

        JLabel pageTitle = new JLabel("Our Services");
        pageTitle.setFont(DS.bold(20));
        pageTitle.setForeground(DS.FG_NAV_ACTIVE);
        pageTitle.setBorder(new EmptyBorder(0, 0, 18, 0));
        page.add(pageTitle, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(DS.BG_PAGE);

        List<ServiceItem> all = serviceItemDAO.getAllServiceItems();
        java.util.LinkedHashMap<String, List<ServiceItem>> grouped = new java.util.LinkedHashMap<>();
        for (ServiceItem si : all)
            grouped.computeIfAbsent(si.getCategory(), k -> new java.util.ArrayList<>()).add(si);

        final int INIT = 3;
        for (String cat : grouped.keySet()) {
            JPanel catHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            catHeaderPanel.setBackground(DS.BG_PAGE);
            catHeaderPanel.setAlignmentX(LEFT_ALIGNMENT);
            catHeaderPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            catHeaderPanel.setBorder(new EmptyBorder(8, 0, 8, 0));

            JLabel catLbl = label(cat + " Services", DS.bold(14), DS.FG_NAV_ACTIVE);
            catHeaderPanel.add(catLbl);
            body.add(catHeaderPanel);

            List<ServiceItem> items = grouped.get(cat);
            JPanel wrapper = new JPanel();
            wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
            wrapper.setOpaque(false);
            wrapper.setAlignmentX(LEFT_ALIGNMENT);

            JPanel row1 = serviceRow(items, 0, Math.min(INIT, items.size()));
            wrapper.add(row1);

            if (items.size() > INIT) {
                JPanel row2 = serviceRow(items, INIT, items.size());
                row2.setVisible(false);
                wrapper.add(Box.createRigidArea(new Dimension(0, 12)));
                wrapper.add(row2);

                JButton toggle = ghostBtn("Show More");
                toggle.setAlignmentX(LEFT_ALIGNMENT);
                JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
                bp.setOpaque(false);
                bp.setAlignmentX(LEFT_ALIGNMENT);
                bp.add(toggle);
                wrapper.add(bp);
                toggle.addActionListener(e -> {
                    boolean v = !row2.isVisible();
                    row2.setVisible(v);
                    toggle.setText(v ? "Show Less" : "Show More");
                    wrapper.revalidate(); wrapper.repaint();
                });
            }
            body.add(wrapper);
            body.add(Box.createRigidArea(new Dimension(0, 18)));
        }

        body.add(hLine());
        body.add(Box.createRigidArea(new Dimension(0, 18)));

        JLabel locTitle = label("Find Us", DS.bold(14), DS.FG_NAV_ACTIVE);
        locTitle.setAlignmentX(LEFT_ALIGNMENT);
        locTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        locTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        body.add(locTitle);

        JPanel locCard = new JPanel();
        locCard.setLayout(new BoxLayout(locCard, BoxLayout.Y_AXIS));
        locCard.setBackground(DS.BG_CARD);
        locCard.setBorder(new CompoundBorder(
            new LineBorder(DS.BORDER, 1),
            new EmptyBorder(16, 20, 16, 20)));
        locCard.setAlignmentX(LEFT_ALIGNMENT);
        locCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        addLocationRow(locCard, "Address",       "No. 12, Jalan Teknologi 3, Taman Teknologi Malaysia, 57000 Kuala Lumpur");
        locCard.add(Box.createRigidArea(new Dimension(0, 10)));
        addLocationRow(locCard, "Phone",         "+603-8926 1234");
        locCard.add(Box.createRigidArea(new Dimension(0, 10)));
        addLocationRow(locCard, "Opening Hours", "Mon – Fri: 9:00 AM – 5:00 PM  |  Sat, Sun & PH: Closed");
        body.add(locCard);
        body.add(Box.createRigidArea(new Dimension(0, 32)));

        page.add(thinScroll(body), BorderLayout.CENTER);
        return page;
    }

    private JPanel serviceRow(List<ServiceItem> items, int from, int to) {
        JPanel row = new JPanel(new GridLayout(1, to - from, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        for (int i = from; i < to; i++) row.add(serviceCard(items.get(i)));
        return row;
    }

    private JPanel serviceCard(ServiceItem item) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(DS.BG_CARD);
        card.setBorder(new CompoundBorder(
            new LineBorder(DS.BORDER, 1),
            new EmptyBorder(14, 16, 14, 16)));

        JLabel desc = new JLabel("<html><body style='width:150px; font-size:11px'>" + item.getDescription() + "</body></html>");
        desc.setFont(DS.regular(12));
        desc.setForeground(DS.FG_LABEL);
        card.add(desc, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 2));
        bottomRow.setBackground(DS.BG_CARD);
        JLabel dur = label("Duration: " + item.getDuration() + " hr", DS.regular(11), DS.FG_SECONDARY);
        bottomRow.add(dur);
        card.add(bottomRow, BorderLayout.SOUTH);

        return card;
    }

    private void addLocationRow(JPanel panel, String lbl, String val) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(DS.BG_CARD);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        JLabel l = label(lbl, DS.bold(12), DS.FG_NAV_ACTIVE);
        l.setPreferredSize(new Dimension(130, 20));
        row.add(l, BorderLayout.WEST);
        row.add(label(val, DS.regular(12), DS.FG_LABEL), BorderLayout.CENTER);
        panel.add(row);
    }

    private JPanel buildMakeApptPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(DS.BG_PAGE);
        page.setBorder(new EmptyBorder(28, 32, 28, 32));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(DS.BG_PAGE);
        topBar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Book New Appointment");
        title.setFont(DS.bold(20));
        title.setForeground(DS.FG_NAV_ACTIVE);

        JButton backBtn = ghostBtn("Back to Home");
        backBtn.addActionListener(e -> navigate("home", navHomeBtn));

        topBar.add(title,   BorderLayout.WEST);
        topBar.add(backBtn, BorderLayout.EAST);
        page.add(topBar, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(DS.BG_CARD);
        form.setBorder(new CompoundBorder(
            new LineBorder(DS.BORDER, 1),
            new EmptyBorder(24, 32, 24, 32)));

        GridBagConstraints g = new GridBagConstraints();
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(12, 12, 12, 12);
        g.anchor = GridBagConstraints.WEST;

        JTextField brandF  = formField("e.g. Honda");
        JTextField modelF  = formField("e.g. Civic");
        JTextField plateF  = formField("e.g. WAB 1234");

        List<ServiceItem> all = serviceItemDAO.getAllServiceItems();
        Set<String> catSet = new HashSet<>();
        for (ServiceItem si : all) catSet.add(si.getCategory());

        JComboBox<String>      catBox  = styledCombo(catSet.toArray(new String[0]));
        JComboBox<ServiceItem> itemBox = new JComboBox<>();
        styleCombo(itemBox);
        itemBox.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v,
                    int idx, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, v, idx, sel, foc);
                if (v instanceof ServiceItem si) setText(si.getDescription() + " (" + si.getDuration() + " hr)");
                return this;
            }
        });
        catBox.addActionListener(e -> {
            itemBox.removeAllItems();
            String cat = (String) catBox.getSelectedItem();
            if (cat != null) serviceItemDAO.getServiceItemsByCategory(cat).forEach(itemBox::addItem);
        });
        if (catBox.getItemCount() > 0) catBox.setSelectedIndex(0);

        LocalDate today = LocalDate.now();
        JComboBox<Integer> yearBox  = new JComboBox<>();
        JComboBox<Integer> monthBox = new JComboBox<>();
        JComboBox<Integer> dayBox   = new JComboBox<>();
        styleCombo(yearBox); styleCombo(monthBox); styleCombo(dayBox);
        yearBox.addItem(today.getYear()); yearBox.addItem(today.getYear() + 1);
        for (int i = 1; i <= 12; i++) monthBox.addItem(i);
        monthBox.setSelectedItem(today.getMonthValue());

        ActionListener upDays = e -> {
            if (yearBox.getSelectedItem() == null || monthBox.getSelectedItem() == null) return;
            int y = (Integer) yearBox.getSelectedItem(), m = (Integer) monthBox.getSelectedItem();
            int days = java.time.YearMonth.of(y, m).lengthOfMonth();
            Object cur = dayBox.getSelectedItem();
            int cd = cur != null ? (Integer) cur : today.getDayOfMonth();
            dayBox.removeAllItems();
            for (int i = 1; i <= days; i++) dayBox.addItem(i);
            dayBox.setSelectedItem(Math.min(cd, days));
        };
        yearBox.addActionListener(upDays);
        monthBox.addActionListener(upDays);
        upDays.actionPerformed(null);

        JPanel datePicker = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        datePicker.setBackground(DS.BG_CARD);
        addDateCombo(datePicker, yearBox,  "Year");
        addDateCombo(datePicker, monthBox, "Month");
        addDateCombo(datePicker, dayBox,   "Day");

        JComboBox<String> timeBox = styledCombo(new String[]{"09:00","10:00","11:00","13:00","14:00","15:00","16:00"});

        int r = 0;
        addFormRow(form, g, r++, "Vehicle Brand *",  brandF);
        addFormRow(form, g, r++, "Vehicle Model *",  modelF);
        addFormRow(form, g, r++, "Vehicle Plate *",  plateF);
        addFormRow(form, g, r++, "Service Category", catBox);
        addFormRow(form, g, r++, "Service Item *",   itemBox);
        addFormRow(form, g, r++, "Preferred Date *", datePicker);
        addFormRow(form, g, r++, "Preferred Time *", timeBox);

        JButton submit = primaryBtn("Confirm Booking");
        submit.addActionListener(e -> {
            if (brandF.getText().trim().isEmpty() || modelF.getText().trim().isEmpty() || plateF.getText().trim().isEmpty()) {
                dlgWarn("Please fill in all required vehicle fields."); return;
            }
            if (itemBox.getSelectedItem() == null) { dlgWarn("Please select a service item."); return; }
            LocalDate sel = LocalDate.of((Integer) yearBox.getSelectedItem(),
                                         (Integer) monthBox.getSelectedItem(),
                                         (Integer) dayBox.getSelectedItem());
            if (sel.isBefore(today)) { dlgWarn("Please select a future date."); return; }

            ServiceItem si = (ServiceItem) itemBox.getSelectedItem();

            boolean ok = appointmentDAO.submitAppointment(
                customerID, si.getServiceItemID(), sel, (String) timeBox.getSelectedItem(),
                brandF.getText().trim(), modelF.getText().trim(), plateF.getText().trim(), "N/A");

            if (ok) {
                dlgInfo("Appointment booked for " + sel.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + ".");
                brandF.setText(""); modelF.setText(""); plateF.setText("");
                catBox.setSelectedIndex(0);
                yearBox.setSelectedItem(today.getYear());
                monthBox.setSelectedItem(today.getMonthValue());
                dayBox.setSelectedItem(today.getDayOfMonth());
                timeBox.setSelectedIndex(0);
                refreshHomePage();
            } else {
                dlgErr("Booking failed. You may already have an appointment at this date and time.");
            }
        });

        JPanel submitRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        submitRow.setBackground(DS.BG_CARD);
        submitRow.add(submit);
        g.gridx = 0; g.gridy = r; g.gridwidth = 2; g.insets = new Insets(20, 12, 4, 12);
        form.add(submitRow, g);

        page.add(thinScroll(form), BorderLayout.CENTER);
        return page;
    }

    private void addDateCombo(JPanel p, JComboBox<Integer> box, String lbl) {
        p.add(box);
        p.add(label(lbl, DS.regular(12), DS.FG_SECONDARY));
    }

    private JPanel buildHistoryPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(DS.BG_PAGE);
        page.setBorder(new EmptyBorder(28, 32, 28, 32));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(DS.BG_PAGE);
        topBar.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel title = new JLabel("Appointment & Service History");
        title.setFont(DS.bold(20));
        title.setForeground(DS.FG_NAV_ACTIVE);

        JButton back = ghostBtn("Back to Home");
        back.addActionListener(e -> navigate("home", navHomeBtn));

        topBar.add(title, BorderLayout.WEST);
        topBar.add(back,  BorderLayout.EAST);
        page.add(topBar, BorderLayout.NORTH);

        List<Appointment> history = appointmentDAO.getPassAppointments(customerID);
        Object[][] data    = historyData(history);
        String[]   columns = {"Date", "Service", "Technician", "Status"};

        JTable table = new JTable(data, columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setFont(DS.regular(13));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(237, 240, 255));
        table.setSelectionForeground(DS.FG_NAV_ACTIVE);
        table.setBackground(DS.BG_CARD);
        table.setForeground(DS.FG_LABEL);

        table.getTableHeader().setFont(DS.bold(12));
        table.getTableHeader().setBackground(DS.BG_HEADER);
        table.getTableHeader().setForeground(DS.FG_WHITE);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                if (sel) {
                    setBackground(new Color(237, 240, 255));
                    setForeground(DS.FG_NAV_ACTIVE);
                } else {
                    setBackground(row % 2 == 0 ? DS.BG_CARD : new Color(250, 250, 252));
                    setForeground(DS.FG_LABEL);
                    if (col == 3 && v != null) {
                        String s = v.toString();
                        setForeground("Completed".equals(s) ? DS.FG_GREEN
                                    : "Cancelled".equals(s) ? DS.FG_RED : DS.FG_ORANGE);
                    }
                }
                return this;
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(new LineBorder(DS.BORDER));
        tableScroll.getViewport().setBackground(DS.BG_CARD);

        // ── Feedback section (Technician + Staff) ─────────────────────────
        JPanel feedbackSection = new JPanel(new BorderLayout(0, 8));
        feedbackSection.setBackground(DS.BG_PAGE);
        feedbackSection.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel fbTitle = label("Feedback / Comments", DS.bold(14), DS.FG_LABEL);
        JLabel fbHint = label("Click a row above to select an appointment before submitting feedback.",
            DS.regular(11), DS.FG_SECONDARY);

        JPanel feedbackTopPanel = new JPanel(new BorderLayout(0, 4));
        feedbackTopPanel.setBackground(DS.BG_PAGE);
        feedbackTopPanel.add(fbTitle, BorderLayout.NORTH);
        feedbackTopPanel.add(fbHint,  BorderLayout.SOUTH);

        String[] ratings = {"Select rating","1 — Poor","2 — Fair","3 — Good","4 — Very Good","5 — Excellent"};

        // ── Technician feedback panel ──────────────────────────────────
        JLabel techLabel = label("Technician Feedback", DS.bold(12), DS.FG_LABEL);
        JPanel techRatingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        techRatingRow.setBackground(DS.BG_PAGE);
        techRatingRow.add(label("Rating:", DS.regular(12), DS.FG_LABEL));
        JComboBox<String> techRatingBox = styledCombo(ratings);
        techRatingBox.setEnabled(false);
        techRatingRow.add(techRatingBox);

        JTextArea techFbArea = new JTextArea(3, 0);
        techFbArea.setFont(DS.regular(13));
        techFbArea.setForeground(DS.FG_LABEL);
        techFbArea.setLineWrap(true); techFbArea.setWrapStyleWord(true);
        techFbArea.setBorder(new CompoundBorder(new LineBorder(DS.BORDER), new EmptyBorder(8, 10, 8, 10)));
        techFbArea.setEnabled(false); techFbArea.setDisabledTextColor(DS.FG_SECONDARY);
        techFbArea.setText("Select an appointment first...");

        JLabel techStatus = label(" ", DS.regular(11), DS.FG_SECONDARY);

        JPanel techTopRow = new JPanel(new BorderLayout(0, 4));
        techTopRow.setBackground(DS.BG_PAGE);
        techTopRow.add(techLabel, BorderLayout.NORTH);
        JPanel techRatingAndStatus = new JPanel(new BorderLayout(0, 2));
        techRatingAndStatus.setBackground(DS.BG_PAGE);
        techRatingAndStatus.add(techRatingRow, BorderLayout.NORTH);
        techRatingAndStatus.add(techStatus, BorderLayout.SOUTH);
        techTopRow.add(techRatingAndStatus, BorderLayout.SOUTH);

        JPanel techPanel = new JPanel(new BorderLayout(0, 6));
        techPanel.setBackground(DS.BG_PAGE);
        techPanel.add(techTopRow, BorderLayout.NORTH);
        techPanel.add(new JScrollPane(techFbArea), BorderLayout.CENTER);

        // ── Staff feedback panel ───────────────────────────────────────
        JLabel staffLabel = label("Counter Staff Feedback", DS.bold(12), DS.FG_LABEL);
        JPanel staffRatingRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        staffRatingRow.setBackground(DS.BG_PAGE);
        staffRatingRow.add(label("Rating:", DS.regular(12), DS.FG_LABEL));
        JComboBox<String> staffRatingBox = styledCombo(ratings);
        staffRatingBox.setEnabled(false);
        staffRatingRow.add(staffRatingBox);

        JTextArea staffFbArea = new JTextArea(3, 0);
        staffFbArea.setFont(DS.regular(13));
        staffFbArea.setForeground(DS.FG_LABEL);
        staffFbArea.setLineWrap(true); staffFbArea.setWrapStyleWord(true);
        staffFbArea.setBorder(new CompoundBorder(new LineBorder(DS.BORDER), new EmptyBorder(8, 10, 8, 10)));
        staffFbArea.setEnabled(false); staffFbArea.setDisabledTextColor(DS.FG_SECONDARY);
        staffFbArea.setText("Select an appointment first...");

        JLabel staffStatus = label(" ", DS.regular(11), DS.FG_SECONDARY);

        JPanel staffTopRow = new JPanel(new BorderLayout(0, 4));
        staffTopRow.setBackground(DS.BG_PAGE);
        staffTopRow.add(staffLabel, BorderLayout.NORTH);
        JPanel staffRatingAndStatus = new JPanel(new BorderLayout(0, 2));
        staffRatingAndStatus.setBackground(DS.BG_PAGE);
        staffRatingAndStatus.add(staffRatingRow, BorderLayout.NORTH);
        staffRatingAndStatus.add(staffStatus, BorderLayout.SOUTH);
        staffTopRow.add(staffRatingAndStatus, BorderLayout.SOUTH);

        JPanel staffPanel = new JPanel(new BorderLayout(0, 6));
        staffPanel.setBackground(DS.BG_PAGE);
        staffPanel.add(staffTopRow, BorderLayout.NORTH);
        staffPanel.add(new JScrollPane(staffFbArea), BorderLayout.CENTER);

        JPanel dualPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        dualPanel.setBackground(DS.BG_PAGE);
        dualPanel.add(techPanel);
        dualPanel.add(staffPanel);

        JButton submitFb = primaryBtn("Submit Feedback");
        submitFb.setEnabled(false);
        JPanel submitFbRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        submitFbRow.setBackground(DS.BG_PAGE);
        submitFbRow.add(submitFb);

        JPanel inputPanel = new JPanel(new BorderLayout(0, 8));
        inputPanel.setBackground(DS.BG_PAGE);
        inputPanel.add(dualPanel,    BorderLayout.CENTER);
        inputPanel.add(submitFbRow,  BorderLayout.SOUTH);

        feedbackSection.add(feedbackTopPanel, BorderLayout.NORTH);
        feedbackSection.add(inputPanel,       BorderLayout.CENTER);


        final String[] selID = {null};
        final String[] selTechID = {null};
        final String[] selStaffID = {null};
        final boolean[] techAlreadySubmitted = {false};
        final boolean[] staffAlreadySubmitted = {false};

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            if (row < 0 || history == null || row >= history.size()) return;
            Appointment selAppt = history.get(row);
            selID[0] = selAppt.getAppointmentID();
            selTechID[0] = selAppt.getTechnicianID();
            selStaffID[0] = selAppt.getStaffID();
            String selStatus = selAppt.getStatus();

            if (!"Paid".equalsIgnoreCase(selStatus)) {
                techFbArea.setEnabled(false); techFbArea.setText("");
                techRatingBox.setEnabled(false); techRatingBox.setSelectedIndex(0);
                staffFbArea.setEnabled(false); staffFbArea.setText("");
                staffRatingBox.setEnabled(false); staffRatingBox.setSelectedIndex(0);
                submitFb.setEnabled(false);
                techStatus.setText(" "); staffStatus.setText(" ");
                fbHint.setText("Only completed appointments can be rated.");
                fbHint.setForeground(DS.FG_ORANGE);
                techAlreadySubmitted[0] = false;
                staffAlreadySubmitted[0] = false;
            } else {
                fbHint.setText("Rate the technician and counter staff separately, then click Submit.");
                fbHint.setForeground(DS.FG_SECONDARY);

                Feedback exTech = feedbackDAO.getFeedbackByAppointmentAndTarget(selID[0], "TECHNICIAN");
                if (selTechID[0] == null || selTechID[0].isEmpty()
                        || "N/A".equalsIgnoreCase(selTechID[0]) || "null".equalsIgnoreCase(selTechID[0])) {
                    techFbArea.setEnabled(false); techFbArea.setText("");
                    techRatingBox.setEnabled(false); techRatingBox.setSelectedIndex(0);
                    techStatus.setText("No technician assigned.");
                    techStatus.setForeground(DS.FG_ORANGE);
                    techAlreadySubmitted[0] = true;
                } else if (exTech != null) {
                    techFbArea.setEnabled(false); techFbArea.setText(exTech.getComment());
                    techRatingBox.setEnabled(false);
                    try { techRatingBox.setSelectedIndex(Integer.parseInt(exTech.getRating())); }
                    catch (NumberFormatException ignored) { techRatingBox.setSelectedIndex(0); }
                    techStatus.setText("Feedback already submitted.");
                    techStatus.setForeground(DS.FG_GREEN);
                    techAlreadySubmitted[0] = true;
                } else {
                    techFbArea.setEnabled(true); techFbArea.setText(""); techFbArea.setForeground(DS.FG_LABEL);
                    techRatingBox.setEnabled(true); techRatingBox.setSelectedIndex(0);
                    techStatus.setText(" ");
                    techAlreadySubmitted[0] = false;
                }

                // ── Staff feedback state ──
                Feedback exStaff = feedbackDAO.getFeedbackByAppointmentAndTarget(selID[0], "STAFF");
                if (selStaffID[0] == null || selStaffID[0].isEmpty()
                        || "N/A".equalsIgnoreCase(selStaffID[0]) || "null".equalsIgnoreCase(selStaffID[0])) {
                    staffFbArea.setEnabled(false); staffFbArea.setText("");
                    staffRatingBox.setEnabled(false); staffRatingBox.setSelectedIndex(0);
                    staffStatus.setText("No counter staff assigned.");
                    staffStatus.setForeground(DS.FG_ORANGE);
                    staffAlreadySubmitted[0] = true;
                } else if (exStaff != null) {
                    staffFbArea.setEnabled(false); staffFbArea.setText(exStaff.getComment());
                    staffRatingBox.setEnabled(false);
                    try { staffRatingBox.setSelectedIndex(Integer.parseInt(exStaff.getRating())); }
                    catch (NumberFormatException ignored) { staffRatingBox.setSelectedIndex(0); }
                    staffStatus.setText("Feedback already submitted.");
                    staffStatus.setForeground(DS.FG_GREEN);
                    staffAlreadySubmitted[0] = true;
                } else {
                    staffFbArea.setEnabled(true); staffFbArea.setText(""); staffFbArea.setForeground(DS.FG_LABEL);
                    staffRatingBox.setEnabled(true); staffRatingBox.setSelectedIndex(0);
                    staffStatus.setText(" ");
                    staffAlreadySubmitted[0] = false;
                }

                submitFb.setEnabled(!techAlreadySubmitted[0] || !staffAlreadySubmitted[0]);
                if (techAlreadySubmitted[0] && staffAlreadySubmitted[0]) {
                    fbHint.setText("You have already submitted all feedback for this appointment.");
                    fbHint.setForeground(DS.FG_GREEN);
                }
            }
        });

        submitFb.addActionListener(e -> {
            boolean anySubmitted = false;
            boolean anyFailed   = false;

            // ── Submit technician feedback if filled ──
            if (!techAlreadySubmitted[0]) {
                String techComment = techFbArea.getText().trim();
                int techRating = techRatingBox.getSelectedIndex();
                if (!techComment.isEmpty() && techRating > 0) {
                    boolean ok = feedbackDAO.submitFeedback(selID[0], customerID,
                            "TECHNICIAN", selTechID[0],
                            String.valueOf(techRating), techComment);
                    if (ok) {
                        techFbArea.setEnabled(false); techRatingBox.setEnabled(false);
                        techStatus.setText("Feedback already submitted.");
                        techStatus.setForeground(DS.FG_GREEN);
                        techAlreadySubmitted[0] = true;
                        anySubmitted = true;
                    } else { anyFailed = true; }
                }
            }

            if (!staffAlreadySubmitted[0]) {
                String staffComment = staffFbArea.getText().trim();
                int staffRating = staffRatingBox.getSelectedIndex();
                if (!staffComment.isEmpty() && staffRating > 0) {
                    boolean ok = feedbackDAO.submitFeedback(selID[0], customerID,
                            "STAFF", selStaffID[0],
                            String.valueOf(staffRating), staffComment);
                    if (ok) {
                        staffFbArea.setEnabled(false); staffRatingBox.setEnabled(false);
                        staffStatus.setText("Feedback already submitted.");
                        staffStatus.setForeground(DS.FG_GREEN);
                        staffAlreadySubmitted[0] = true;
                        anySubmitted = true;
                    } else { anyFailed = true; }
                }
            }

            if (!anySubmitted && !anyFailed) {
                dlgWarn("Please fill in at least one feedback (rating + comment).");
                return;
            }
            if (anyFailed) { dlgErr("Some feedback failed to submit. Please try again."); }
            if (anySubmitted) { dlgInfo("Thank you for your feedback!"); }

            if (techAlreadySubmitted[0] && staffAlreadySubmitted[0]) {
                submitFb.setEnabled(false);
                fbHint.setText("You have already submitted all feedback for this appointment.");
                fbHint.setForeground(DS.FG_GREEN);
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, feedbackSection);
        split.setDividerLocation(300);
        split.setResizeWeight(0.55);
        split.setBorder(null);
        split.setBackground(DS.BG_PAGE);
        split.setContinuousLayout(true);

        page.add(split, BorderLayout.CENTER);
        return page;
    }

    private Object[][] historyData(List<Appointment> list) {
        if (list == null || list.isEmpty()) return new Object[0][4];
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        Object[][] d = new Object[list.size()][4];
        for (int i = 0; i < list.size(); i++) {
            Appointment a = list.get(i);
            String tech = technicianDAO.getTechnicianName(a.getTechnicianID());
            d[i][0] = a.getAppointmentDate().format(fmt);
            d[i][1] = a.getServiceType();
            d[i][2] = tech != null ? tech : "N/A";
            d[i][3] = a.getStatus();
        }
        return d;
    }

    private JPanel buildProfilePage() {
        Color pageBg       = DS.BG_PAGE;
        Color cardBg       = DS.BG_CARD;
        Color separatorClr = DS.SEPARATOR;
        Color labelGrey    = DS.FG_SECONDARY;
        Color textDark     = DS.FG_LABEL;
        Color accentBlue   = DS.FG_ACCENT;
        Color dangerRed    = DS.FG_RED;
        Color avatarBg     = new Color(90, 200, 250);

        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(pageBg);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(pageBg);
        content.setBorder(new EmptyBorder(30, 32, 40, 32));

        JLabel viewID    = new JLabel();
        JLabel viewName  = new JLabel();
        JLabel viewEmail = new JLabel();
        JLabel viewPhone = new JLabel();
        JLabel viewDate  = new JLabel();
        JLabel hdrName   = new JLabel();
        JLabel hdrSub    = new JLabel();

        JPanel avatarCircle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 88, cx = getWidth() / 2;
                g2.setColor(avatarBg);
                g2.fillOval(cx - size / 2, 0, size, size);
                g2.setColor(DS.FG_WHITE);
                g2.setFont(DS.bold(34));
                FontMetrics fm = g2.getFontMetrics();
                String text = hdrName.getName() != null ? hdrName.getName() : "";
                g2.drawString(text, cx - fm.stringWidth(text) / 2,
                    (size + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatarCircle.setOpaque(false);
        avatarCircle.setPreferredSize(new Dimension(0, 96));
        avatarCircle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        avatarCircle.setAlignmentX(LEFT_ALIGNMENT);

        Runnable load = () -> {
            customer = customerDAO.getCustomerByID(customerID);
            if (customer == null) return;
            viewID.setText(customer.getCustomerID());
            viewName.setText(customer.getName());
            viewEmail.setText(customer.getEmail());
            viewPhone.setText(customer.getPhoneNumber());
            viewDate.setText(customer.getRegisteredDate().toString());
            hdrName.setText(customer.getName());
            hdrSub.setText(customer.getEmail() + "  ·  " + customer.getCustomerID());
            String n = customer.getName() != null ? customer.getName().trim() : "";
            String[] parts = n.split("\\s+");
            String ini = parts[0].isEmpty() ? "" : String.valueOf(parts[0].charAt(0)).toUpperCase();
            if (parts.length > 1 && !parts[parts.length - 1].isEmpty())
                ini += String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase();
            hdrName.setName(ini);
        };
        load.run();

        CardLayout pCards  = new CardLayout();
        JPanel     pSwitch = new JPanel(pCards) {
            @Override public Dimension getPreferredSize() {
                for (Component c : getComponents())
                    if (c.isVisible()) return c.getPreferredSize();
                return super.getPreferredSize();
            }
        };
        pSwitch.setOpaque(false);
        pSwitch.setAlignmentX(LEFT_ALIGNMENT);
        pSwitch.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // VIEW MODE
        JPanel view = new JPanel();
        view.setLayout(new BoxLayout(view, BoxLayout.Y_AXIS));
        view.setOpaque(false);

        hdrName.setFont(DS.bold(26));
        hdrName.setForeground(textDark);
        hdrName.setHorizontalAlignment(SwingConstants.CENTER);
        hdrName.setAlignmentX(LEFT_ALIGNMENT);
        hdrName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        hdrSub.setFont(DS.regular(13));
        hdrSub.setForeground(labelGrey);
        hdrSub.setHorizontalAlignment(SwingConstants.CENTER);
        hdrSub.setAlignmentX(LEFT_ALIGNMENT);
        hdrSub.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

        view.add(avatarCircle);
        view.add(Box.createRigidArea(new Dimension(0, 12)));
        view.add(hdrName);
        view.add(Box.createRigidArea(new Dimension(0, 4)));
        view.add(hdrSub);
        view.add(Box.createRigidArea(new Dimension(0, 28)));
        view.add(sectionHdr("PERSONAL INFORMATION"));

        JPanel infoCard = appleCard(cardBg, separatorClr);
        pInfoRow(infoCard, "Customer ID",  viewID,    labelGrey, textDark);
        pDivider(infoCard, separatorClr);
        pInfoRow(infoCard, "Name",         viewName,  labelGrey, textDark);
        pDivider(infoCard, separatorClr);
        pInfoRow(infoCard, "Email",        viewEmail, labelGrey, textDark);
        pDivider(infoCard, separatorClr);
        pInfoRow(infoCard, "Phone",        viewPhone, labelGrey, textDark);
        pDivider(infoCard, separatorClr);
        pDivider(infoCard, separatorClr);
        pInfoRow(infoCard, "Member Since", viewDate,  labelGrey, textDark);
        view.add(infoCard);
        view.add(Box.createRigidArea(new Dimension(0, 28)));

        view.add(sectionHdr("ACCOUNT"));

        JButton btnEdit   = appleLink("Edit",   accentBlue);
        JButton btnChgPwd = appleLink("Change", accentBlue);
        JButton btnDel    = appleLink("Delete", dangerRed);

        JPanel actCard = appleCard(cardBg, separatorClr);
        pActionRow(actCard, "Edit Profile",    "Update your name, email and phone number", btnEdit,   textDark, labelGrey);
        pDivider(actCard, separatorClr);
        pActionRow(actCard, "Change Password", "Update your account password",                btnChgPwd, textDark, labelGrey);
        pDivider(actCard, separatorClr);
        pActionRow(actCard, "Delete Account",  "Permanently remove your account and data",    btnDel,    dangerRed, labelGrey);
        view.add(actCard);
        pSwitch.add(view, "VIEW");

        // EDIT MODE
        JPanel edit = new JPanel();
        edit.setLayout(new BoxLayout(edit, BoxLayout.Y_AXIS));
        edit.setOpaque(false);

        JTextField eName  = appleField();
        JTextField eEmail = appleField();
        JTextField ePhone = appleField();

        JPanel editTop = new JPanel(new BorderLayout());
        editTop.setOpaque(false);
        editTop.setBorder(new EmptyBorder(0, 0, 18, 0));
        editTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        editTop.setAlignmentX(LEFT_ALIGNMENT);
        JLabel editTitle = new JLabel("Edit Profile");
        editTitle.setFont(DS.bold(22));
        editTitle.setForeground(textDark);
        JButton btnCancel = appleLink("Cancel", accentBlue);
        editTop.add(editTitle,  BorderLayout.WEST);
        editTop.add(btnCancel,  BorderLayout.EAST);
        edit.add(editTop);

        edit.add(sectionHdr("PERSONAL INFORMATION"));
        JPanel editCard = appleCard(cardBg, separatorClr);
        JLabel eIdLbl = label("", DS.regular(13), labelGrey);
        pInfoRow(editCard, "Customer ID", eIdLbl, labelGrey, labelGrey);
        pDivider(editCard, separatorClr);
        pEditRow(editCard, "Name",    eName,  labelGrey);
        pDivider(editCard, separatorClr);
        pEditRow(editCard, "Email",   eEmail, labelGrey);
        pDivider(editCard, separatorClr);
        pEditRow(editCard, "Phone",   ePhone, labelGrey);
        pDivider(editCard, separatorClr);
        edit.add(editCard);
        edit.add(Box.createRigidArea(new Dimension(0, 22)));

        JButton btnSave = new JButton("Save Changes") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DS.RADIUS_BTN * 2, DS.RADIUS_BTN * 2);
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnSave.setFont(DS.bold(14));
        btnSave.setForeground(DS.FG_WHITE);
        btnSave.setBackground(accentBlue);
        btnSave.setFocusPainted(false);
        btnSave.setBorderPainted(false);
        btnSave.setContentAreaFilled(false);
        btnSave.setOpaque(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.setPreferredSize(new Dimension(180, 44));
        btnSave.setMaximumSize(new Dimension(180, 44));
        btnSave.setAlignmentX(LEFT_ALIGNMENT);
        edit.add(btnSave);

        pSwitch.add(edit, "EDIT");
        content.add(pSwitch);

        JScrollPane scroll = thinScroll(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        page.add(scroll, BorderLayout.CENTER);

        // Button actions
        btnEdit.addActionListener(e -> {
            customer = customerDAO.getCustomerByID(customerID);
            if (customer == null) return;
            eIdLbl.setText(customer.getCustomerID());
            eName.setText(customer.getName());
            eEmail.setText(customer.getEmail());
            ePhone.setText(customer.getPhoneNumber());
            pCards.show(pSwitch, "EDIT");
            pSwitch.revalidate(); pSwitch.repaint();
        });

        btnCancel.addActionListener(e -> { pCards.show(pSwitch, "VIEW"); pSwitch.revalidate(); pSwitch.repaint(); });

        btnSave.addActionListener(e -> {
            String nn = eName.getText().trim(), ne = eEmail.getText().trim();
            String np = ePhone.getText().trim();
            if (nn.isEmpty() || ne.isEmpty() || np.isEmpty()) { dlgWarn("All fields are required."); return; }
            if (!ne.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) { dlgWarn("Please enter a valid email address."); return; }
            if (!np.matches("^\\d{10,11}$")) { dlgWarn("Phone number must be 10–11 digits."); return; }
            if (nn.contains("|") || ne.contains("|") || np.contains("|")) { dlgWarn("Fields cannot contain the '|' character."); return; }
            if (dlgConfirm("Save profile changes?") != JOptionPane.YES_OPTION) return;
            customer.setName(nn); customer.setEmail(ne); customer.setPhoneNumber(np);
            if (customerDAO.updateCustomer(customer)) {
                dlgInfo("Profile updated successfully.");
                load.run(); avatarCircle.repaint(); pCards.show(pSwitch, "VIEW"); pSwitch.revalidate(); pSwitch.repaint();
            } else dlgErr("Failed to update profile.");
        });

        btnChgPwd.addActionListener(e -> {
            customer = customerDAO.getCustomerByID(customerID);
            if (customer == null) return;
            JPanel pp = new JPanel(new GridLayout(3, 2, 10, 12));
            pp.setBorder(new EmptyBorder(12, 8, 12, 8));
            JPasswordField pOld = new JPasswordField(16), pNew = new JPasswordField(16), pCon = new JPasswordField(16);
            pp.add(new JLabel("Current Password:")); pp.add(pOld);
            pp.add(new JLabel("New Password:"));     pp.add(pNew);
            pp.add(new JLabel("Confirm Password:")); pp.add(pCon);
            if (JOptionPane.showConfirmDialog(this, pp, "Change Password",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;
            String op = new String(pOld.getPassword()).trim();
            String nw = new String(pNew.getPassword()).trim();
            String cf = new String(pCon.getPassword()).trim();
            if (!op.equals(customer.getPassword()))  { dlgErr("Current password is incorrect."); return; }
            if (nw.length() < 6)                     { dlgWarn("New password must be at least 6 characters."); return; }
            if (!nw.equals(cf))                      { dlgErr("Passwords do not match."); return; }
            if (nw.contains("|"))                    { dlgWarn("Password cannot contain '|'."); return; }
            customer.setPassword(nw);
            if (customerDAO.updateCustomer(customer)) dlgInfo("Password changed successfully.");
            else dlgErr("Failed to change password.");
        });

        btnDel.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this,
                "This action cannot be undone.\n\nAre you sure you want to delete your account?",
                "Delete Account", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
            if (customerDAO.deleteCustomer(customerID)) {
                dlgInfo("Your account has been deleted.");
                SwingUtilities.invokeLater(() -> new views.MainFrame());
                dispose();
            } else dlgErr("Failed to delete account.");
        });

        return page;
    }

    // Profile card primitives
    private JPanel appleCard(Color bg, Color border) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DS.RADIUS_CARD * 2, DS.RADIUS_CARD * 2);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, DS.RADIUS_CARD * 2, DS.RADIUS_CARD * 2);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(4, 0, 4, 0));
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }

    private JLabel sectionHdr(String text) {
        JLabel l = label(text, DS.bold(11), DS.FG_SECONDARY);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(0, 6, 8, 0));
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return l;
    }

    private void pInfoRow(JPanel card, String lbl, JLabel val, Color lc, Color vc) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(13, 20, 13, 20));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        JLabel l = label(lbl, DS.regular(13), lc);
        l.setPreferredSize(new Dimension(120, 20));
        val.setFont(DS.regular(13));
        val.setForeground(vc);
        val.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(l, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        card.add(row);
    }

    private void pActionRow(JPanel card, String title, String sub, JButton btn, Color tc, Color sc) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(14, 20, 14, 20));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        JPanel txt = new JPanel();
        txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
        txt.setOpaque(false);
        txt.add(label(title, DS.regular(13), tc));
        txt.add(Box.createRigidArea(new Dimension(0, 2)));
        txt.add(label(sub, DS.regular(11), sc));
        row.add(txt, BorderLayout.CENTER);
        row.add(btn, BorderLayout.EAST);
        card.add(row);
    }

    private void pEditRow(JPanel card, String lbl, JTextField field, Color lc) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, 20, 8, 20));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        JLabel l = label(lbl, DS.regular(13), lc);
        l.setPreferredSize(new Dimension(120, 20));
        row.add(l, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        card.add(row);
    }

    private void pDivider(JPanel card, Color c) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        wrap.setBorder(new EmptyBorder(0, 20, 0, 0));
        JPanel line = new JPanel();
        line.setBackground(c);
        line.setPreferredSize(new Dimension(0, 1));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        wrap.add(line, BorderLayout.CENTER);
        card.add(wrap);
    }

    private JButton appleLink(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(DS.regular(13));
        btn.setForeground(color);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setText("<html><u>" + text + "</u></html>"); }
            @Override public void mouseExited(MouseEvent e)  { btn.setText(text); }
        });
        return btn;
    }

    private JTextField appleField() {
        JTextField tf = new JTextField(20);
        tf.setFont(DS.regular(13));
        tf.setForeground(DS.FG_LABEL);
        tf.setBorder(new CompoundBorder(
            new LineBorder(new Color(210, 210, 215), 1, true),
            new EmptyBorder(8, 12, 8, 12)));
        tf.setBackground(new Color(250, 250, 252));
        return tf;
    }

    // =========================================================================
    // SIDEBAR
    // =========================================================================
    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(DS.BG_SIDEBAR);
        sb.setPreferredSize(new Dimension(DS.SIDEBAR_W, 0));
        sb.setBorder(new MatteBorder(0, 0, 0, 1, DS.BORDER));

        JPanel logo = new JPanel();
        logo.setLayout(new BoxLayout(logo, BoxLayout.Y_AXIS));
        logo.setBackground(DS.BG_SIDEBAR);
        logo.setMaximumSize(new Dimension(DS.SIDEBAR_W, 90));
        logo.setMinimumSize(new Dimension(DS.SIDEBAR_W, 90));
        logo.setPreferredSize(new Dimension(DS.SIDEBAR_W, 90));
        logo.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, DS.BORDER),
            new EmptyBorder(18, 18, 14, 0)));

        JLabel lMain = label("APU ASC", DS.bold(16), new Color(0, 210, 190));
        lMain.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lSub  = label("Customer Portal", DS.regular(11), DS.FG_TERTIARY);
        lSub.setAlignmentX(LEFT_ALIGNMENT);
        logo.add(lMain);
        logo.add(Box.createRigidArea(new Dimension(0, 4)));
        logo.add(lSub);
        sb.add(logo);
        sb.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel menuLbl = label("  MENU", DS.bold(10), DS.FG_TERTIARY);
        menuLbl.setAlignmentX(LEFT_ALIGNMENT);
        menuLbl.setBorder(new EmptyBorder(0, 14, 6, 0));
        menuLbl.setMaximumSize(new Dimension(DS.SIDEBAR_W, 24));
        sb.add(menuLbl);

        String[][] items = {
            {"Home",                  "home"},
            {"Our Services",          "ourService"},
            {"Appointment & History", "history"},
            {"Make Appointment",      "makeAppointment"},
            {"Profile",               "profile"},
        };
        for (String[] item : items) {
            JButton btn = navBtn(item[0]);
            String key = item[1];
            if ("home".equals(key))            navHomeBtn     = btn;
            if ("history".equals(key))         navHistoryBtn  = btn;
            if ("makeAppointment".equals(key)) navMakeApptBtn = btn;
            if ("profile".equals(key))         navProfileBtn  = btn;
            btn.addActionListener(e -> navigate(key, btn));
            sb.add(btn);
        }

        sb.add(Box.createVerticalGlue());

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(DS.SIDEBAR_W, 1));
        sep.setForeground(DS.BORDER);
        sb.add(sep);
        sb.add(Box.createRigidArea(new Dimension(0, 6)));

        JButton logout = navBtn("Logout");
        logout.setForeground(DS.FG_RED);
        logout.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?",
                    "Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(() -> new views.MainFrame());
                dispose();
            }
        });
        sb.add(logout);
        sb.add(Box.createRigidArea(new Dimension(0, 12)));
        return sb;
    }

    private JButton navBtn(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(DS.SIDEBAR_W, 42));
        btn.setMinimumSize(new Dimension(DS.SIDEBAR_W, 42));
        btn.setPreferredSize(new Dimension(DS.SIDEBAR_W, 42));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(DS.BG_SIDEBAR);
        btn.setForeground(new Color(50, 50, 60));
        btn.setFont(DS.regular(13));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 18, 0, 0));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (btn != activeNavBtn) btn.setBackground(new Color(245, 245, 250));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (btn != activeNavBtn) btn.setBackground(DS.BG_SIDEBAR);
            }
        });
        return btn;
    }

    private void navigate(String card, JButton btn) {
        cardLayout.show(contentPanel, card);
        setActive(btn);
    }

    private void setActive(JButton sel) {
        if (activeNavBtn != null) {
            activeNavBtn.setBackground(DS.BG_SIDEBAR);
            activeNavBtn.setForeground(new Color(50, 50, 60));
            activeNavBtn.setFont(DS.regular(13));
            activeNavBtn.setBorder(new EmptyBorder(0, 18, 0, 0));
            activeNavBtn.repaint();
        }
        if (sel == null) return;
        sel.setBackground(DS.BG_NAV_ACTIVE);
        sel.setForeground(DS.FG_NAV_ACTIVE);
        sel.setFont(DS.bold(13));
        sel.setBorder(new CompoundBorder(
            new MatteBorder(0, 4, 0, 0, DS.FG_NAV_ACTIVE),
            new EmptyBorder(0, 14, 0, 0)));
        activeNavBtn = sel;
        sel.repaint();
    }

    // =========================================================================
    // STATUS BAR
    // =========================================================================
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        bar.setBackground(new Color(248, 248, 250));
        bar.setBorder(new MatteBorder(1, 0, 0, 0, DS.BORDER));
        bar.setPreferredSize(new Dimension(0, 30));
        String name = customer != null ? customer.getName() : "Unknown";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy"));
        JLabel txt = label("   User: " + name + "   |   " + date, DS.regular(11), DS.FG_SECONDARY);
        bar.add(txt);
        return bar;
    }

    // =========================================================================
    // UI FACTORY HELPERS
    // =========================================================================
    private JScrollPane thinScroll(JComponent view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(null);
        sp.getViewport().setBackground(DS.BG_PAGE);
        sp.getVerticalScrollBar().setUnitIncrement(12);
        return sp;
    }

    private JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private JLabel statusBadge(String status) {
        JLabel badge = new JLabel(status) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        badge.setFont(DS.bold(11));
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));
        if ("Completed".equalsIgnoreCase(status))      { badge.setBackground(new Color(0xD4EDDA)); badge.setForeground(new Color(0x155724)); }
        else if ("Cancelled".equalsIgnoreCase(status)) { badge.setBackground(new Color(0xFADEDD)); badge.setForeground(new Color(0xC41230)); }
        else                                           { badge.setBackground(new Color(0xFFF3CD)); badge.setForeground(new Color(0x856404)); }
        return badge;
    }

    private JButton primaryBtn(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? DS.BG_ACCENT : new Color(0xB0C4DE));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), DS.RADIUS_BTN * 2, DS.RADIUS_BTN * 2);
                g2.setColor(DS.FG_WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                    (getWidth()  - fm.stringWidth(getText())) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(DS.bold(13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        return btn;
    }

    private JButton ghostBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(DS.regular(12));
        btn.setBackground(DS.BG_CARD);
        btn.setForeground(DS.FG_NAV_ACTIVE);
        btn.setBorder(new LineBorder(DS.FG_NAV_ACTIVE, 1));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JTextField formField(String tip) {
        JTextField tf = new JTextField(20);
        tf.setFont(DS.regular(13));
        tf.setForeground(DS.FG_LABEL);
        tf.setBackground(DS.BG_CARD);
        tf.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(8, 10, 8, 10)));
        tf.setToolTipText(tip);
        return tf;
    }

    @SuppressWarnings("unchecked")
    private <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        styleCombo(cb);
        return cb;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(DS.regular(13));
        cb.setBackground(DS.BG_CARD);
        cb.setForeground(DS.FG_LABEL);
    }

    private void addFormRow(JPanel p, GridBagConstraints g, int row, String lbl, JComponent comp) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1; g.weightx = 0.2;
        JLabel l = label(lbl, DS.bold(13), new Color(50, 50, 60));
        p.add(l, g);
        g.gridx = 1; g.weightx = 0.8;
        p.add(comp, g);
    }

    private JPanel hLine() {
        JPanel p = new JPanel();
        p.setBackground(DS.SEPARATOR);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        p.setPreferredSize(new Dimension(0, 1));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    private void dlgWarn(String msg)  { JOptionPane.showMessageDialog(this, msg, "Notice",  JOptionPane.WARNING_MESSAGE);     }
    private void dlgInfo(String msg)  { JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE); }
    private void dlgErr(String msg)   { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE);       }
    private int  dlgConfirm(String m) { return JOptionPane.showConfirmDialog(this, m, "Confirm", JOptionPane.YES_NO_OPTION);  }
}