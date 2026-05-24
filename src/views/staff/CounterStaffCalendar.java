package views.staff;

import models.dao.AppointmentDAO;
import models.dao.CustomerDAO;
import models.dao.ServiceItemDAO;
import models.dao.TechnicianDAO;
import models.entities.Appointment;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CounterStaffCalendar extends JPanel {

    // ─── DAOs ─────────────────────────────────────────────────────────────────
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final CustomerDAO    customerDAO    = new CustomerDAO();
    private final TechnicianDAO  technicianDAO  = new TechnicianDAO();

    // ─── State ────────────────────────────────────────────────────────────────
    private YearMonth  viewMonth  = YearMonth.now();
    private LocalDate  selectedDate = LocalDate.now();
    private List<Appointment> allAppointments = new ArrayList<>();

    // ─── UI references ────────────────────────────────────────────────────────
    private JLabel    monthLabel;
    private JPanel    calendarGrid;
    private JPanel    detailPanel;
    private JLabel    detailDateLabel;
    private JPanel    detailList;
    private JLabel    detailCountLabel;

    // ─── Status dot colors ────────────────────────────────────────────────────
    private static final Color DOT_PENDING   = new Color(245, 158, 11);   // amber
    private static final Color DOT_APPROVED  = new Color(59,  130, 246);  // blue
    private static final Color DOT_COMPLETED = new Color(34,  197, 94);   // green
    private static final Color DOT_CANCELLED = new Color(156, 163, 175);  // grey
    private static final Color DOT_REJECTED  = new Color(239, 68,  68);   // red

    private static final DateTimeFormatter MONTH_FMT =
        DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter DETAIL_FMT =
        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy");

    // ─── Constructor ──────────────────────────────────────────────────────────
    public CounterStaffCalendar(MainFrame frame) {
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG_PAGE);
        setBorder(new EmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildBody(),    BorderLayout.CENTER);

        refresh();
    }

    // ─── Public API ───────────────────────────────────────────────────────────
    public void refresh() {
        new Thread(() -> {
            List<Appointment> appts = appointmentDAO.getAllAppointments();
            SwingUtilities.invokeLater(() -> {
                allAppointments = appts;
                rebuildGrid();
                showDayDetail(selectedDate);
            });
        }, "calendar-refresh").start();
    }

    // ─── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, AppTheme.GAP_SECTION, 0));

        // Title + subtitle
        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        JLabel title = UIComponent.createPageTitle("Appointment Calendar");
        JLabel sub   = UIComponent.createCaptionLabel("Click a date to view appointments");
        sub.setBorder(new EmptyBorder(4, 0, 0, 0));
        titleBlock.add(title);
        titleBlock.add(sub);

        // Today button
        JButton btnToday = UIComponent.createSmallButton("Today");
        btnToday.setPreferredSize(new Dimension(80, 34));
        btnToday.addActionListener(e -> {
            viewMonth    = YearMonth.now();
            selectedDate = LocalDate.now();
            rebuildGrid();
            showDayDetail(selectedDate);
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(btnToday);

        header.add(titleBlock, BorderLayout.WEST);
        header.add(right,      BorderLayout.EAST);
        return header;
    }

    // ─── Body: calendar + detail panel side by side ───────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        body.add(buildCalendarCard(), BorderLayout.CENTER);
        body.add(buildDetailCard(),   BorderLayout.EAST);
        return body;
    }

    // ─── Calendar card ────────────────────────────────────────────────────────
    private JPanel buildCalendarCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 0));

        // Month nav bar
        card.add(buildMonthNav(), BorderLayout.NORTH);

        // Day headers + grid — clipped inside a panel that respects card radius
        JPanel gridWrapper = new JPanel(new BorderLayout(0, 4));
        gridWrapper.setOpaque(false);
        gridWrapper.setBorder(new EmptyBorder(0, AppTheme.PADDING_CARD, 0, AppTheme.PADDING_CARD));
        gridWrapper.add(buildDayHeaders(), BorderLayout.NORTH);

        calendarGrid = new JPanel(new GridLayout(0, 7, 1, 1));
        calendarGrid.setOpaque(false);
        calendarGrid.setBorder(new EmptyBorder(0, 0, AppTheme.PADDING_CARD, 0));
        gridWrapper.add(calendarGrid, BorderLayout.CENTER);

        // Legend at bottom — sits inside card so it inherits rounded corners
        JPanel centerAndLegend = new JPanel(new BorderLayout(0, 0));
        centerAndLegend.setOpaque(false);
        centerAndLegend.add(gridWrapper,    BorderLayout.CENTER);
        centerAndLegend.add(buildLegend(),  BorderLayout.SOUTH);

        card.add(centerAndLegend, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildMonthNav() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setOpaque(false);
        nav.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            new EmptyBorder(14, AppTheme.PADDING_CARD, 14, AppTheme.PADDING_CARD)));

        // Prev / Next buttons
        JButton btnPrev = navArrow("prev");
        JButton btnNext = navArrow("next");
        btnPrev.addActionListener(e -> { viewMonth = viewMonth.minusMonths(1); rebuildGrid(); });
        btnNext.addActionListener(e -> { viewMonth = viewMonth.plusMonths(1);  rebuildGrid(); });

        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(AppTheme.FONT_BODY_BOLD);
        monthLabel.setForeground(AppTheme.TEXT_PRIMARY);

        nav.add(btnPrev,    BorderLayout.WEST);
        nav.add(monthLabel, BorderLayout.CENTER);
        nav.add(btnNext,    BorderLayout.EAST);
        return nav;
    }

    private JButton navArrow(String direction) {
        boolean isPrev = direction.equals("prev");
        JButton btn = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(AppTheme.BG_PAGE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }

                // Draw arrow manually as a triangle
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int size = 6;
                int[] xp, yp;
                if (isPrev) {
                    xp = new int[]{cx + size/2, cx - size/2, cx + size/2};
                    yp = new int[]{cy - size,   cy,          cy + size};
                } else {
                    xp = new int[]{cx - size/2, cx + size/2, cx - size/2};
                    yp = new int[]{cy - size,   cy,          cy + size};
                }
                g2.setColor(getForeground());
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawPolyline(xp, yp, 3);
                g2.dispose();
            }
        };
        btn.setForeground(AppTheme.TEXT_SECONDARY);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(32, 32));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(AppTheme.PRIMARY); btn.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { btn.setForeground(AppTheme.TEXT_SECONDARY); btn.repaint(); }
        });
        return btn;
    }

    private JPanel buildDayHeaders() {
        JPanel row = new JPanel(new GridLayout(1, 7, 1, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(8, AppTheme.PADDING_CARD, 8, AppTheme.PADDING_CARD));
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(AppTheme.FONT_LABEL);
            lbl.setForeground(AppTheme.TEXT_SECONDARY);
            row.add(lbl);
        }
        return row;
    }

    private JPanel buildLegend() {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        legend.setOpaque(false);
        legend.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, AppTheme.SEPARATOR),
            new EmptyBorder(12, AppTheme.PADDING_CARD, 12, AppTheme.PADDING_CARD)));

        Object[][] items = {
            {DOT_PENDING,   "Pending"},
            {DOT_APPROVED,  "Approved"},
            {DOT_COMPLETED, "Completed"},
            {DOT_CANCELLED, "Cancelled"},
            {DOT_REJECTED,  "Rejected"},
        };
        for (Object[] item : items) {
            legend.add(buildLegendDot((Color) item[0], (String) item[1]));
        }
        return legend;
    }

    private JPanel buildLegendDot(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("●") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                int s = 8;
                g2.fillOval((getWidth() - s) / 2, (getHeight() - s) / 2, s, s);
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(12, 12));
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_CAPTION);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        p.add(dot);
        p.add(lbl);
        return p;
    }

    // ─── Rebuild grid ─────────────────────────────────────────────────────────
    private void rebuildGrid() {
        monthLabel.setText(viewMonth.format(MONTH_FMT));
        calendarGrid.removeAll();

        // Group appointments by date
        Map<LocalDate, List<Appointment>> byDate = allAppointments.stream()
            .filter(a -> {
                try { LocalDate.parse(a.getAppointmentDate()); return true; }
                catch (Exception ex) { return false; }
            })
            .collect(Collectors.groupingBy(a -> LocalDate.parse(a.getAppointmentDate())));

        LocalDate today    = LocalDate.now();
        LocalDate firstDay = viewMonth.atDay(1);
        int startDow = firstDay.getDayOfWeek().getValue() % 7; // Sun=0
        int daysIn   = viewMonth.lengthOfMonth();

        // Leading empty cells
        for (int i = 0; i < startDow; i++)
            calendarGrid.add(buildEmptyCell());

        // Day cells
        for (int day = 1; day <= daysIn; day++) {
            LocalDate date = viewMonth.atDay(day);
            List<Appointment> appts = byDate.getOrDefault(date, Collections.emptyList());
            calendarGrid.add(buildDayCell(date, appts, today));
        }

        // Trailing empty cells to complete the grid
        int total = startDow + daysIn;
        int remainder = total % 7;
        if (remainder != 0)
            for (int i = 0; i < 7 - remainder; i++)
                calendarGrid.add(buildEmptyCell());

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private JPanel buildEmptyCell() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, 62));
        return p;
    }
    private JPanel buildDayCell(LocalDate date, List<Appointment> appts, LocalDate today) {
        boolean isToday      = date.equals(today);
        boolean isSelected   = date.equals(selectedDate);
        boolean isOtherMonth = !date.getMonth().equals(viewMonth.getMonth());

        List<Color> dotColors = appts.stream()
            .map(a -> dotColor(a.getStatus()))
            .collect(Collectors.toList());
        int totalAppts = dotColors.size();

        JPanel cell = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int pad = (int) Math.max(4, w * 0.07);

                // ── Cell highlight (selected / hover) ─────────────────────────
                if (isSelected) {
                    g2.setColor(new Color(0, 122, 255, 18));
                    g2.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);
                    g2.setColor(AppTheme.PRIMARY);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(2, 2, w - 5, h - 5, 12, 12);
                } else if (Boolean.TRUE.equals(getClientProperty("hovered"))) {
                    g2.setColor(AppTheme.BG_PAGE);
                    g2.fillRoundRect(2, 2, w - 4, h - 4, 12, 12);
                }

                // ── Day number: scale with cell height, 11pt baseline ─────────
                int numPx = (int) Math.max(9, Math.min(20, h * 0.14));
                Font numFont = new Font("SansSerif",
                    (isToday || isSelected) ? Font.BOLD : Font.PLAIN, numPx);
                g2.setFont(numFont);

                String dayStr = String.valueOf(date.getDayOfMonth());
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(dayStr);
                int textH = fm.getAscent();

                int circleSize = numPx + 10;
                int cx = pad;
                int cy = pad;

                if (isToday) {
                    g2.setColor(AppTheme.PRIMARY);
                    g2.fillOval(cx, cy, circleSize, circleSize);
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(isOtherMonth ? AppTheme.TEXT_MUTED
                        : isSelected ? AppTheme.PRIMARY : AppTheme.TEXT_PRIMARY);
                }

                int tx = cx + (circleSize - textW) / 2;
                int ty = cy + (circleSize + textH) / 2 - fm.getDescent();
                g2.drawString(dayStr, tx, ty);

                // ── Dots below number ─────────────────────────────────────────
                if (totalAppts > 0) {
                    // Dot: 6px baseline. Scales up with cell height.
                    int dotSize = (int) Math.max(5, Math.min(9, h * 0.08));
                    int dotGap  = (int) Math.max(2, dotSize * 0.4);
                    int dotsPerRow = 4;
                    int maxDots = 8;

                    int dotY = cy + circleSize + (int) Math.max(3, h * 0.05);

                    int shown = Math.min(totalAppts, maxDots);
                    int rows  = (int) Math.ceil((double) shown / dotsPerRow);

                    for (int r = 0; r < rows; r++) {
                        int from = r * dotsPerRow;
                        int to   = Math.min(from + dotsPerRow, shown);
                        int dotX = pad;

                        for (int i = from; i < to; i++) {
                            g2.setColor(dotColors.get(i));
                            g2.fillOval(dotX, dotY, dotSize, dotSize);
                            dotX += dotSize + dotGap;
                        }

                        // "+N" on last row if overflow
                        if (r == rows - 1 && totalAppts > maxDots) {
                            int remaining = totalAppts - maxDots;
                            Font moreFont = new Font("SansSerif", Font.BOLD,
                                Math.max(8, dotSize - 1));
                            g2.setFont(moreFont);
                            g2.setColor(AppTheme.TEXT_MUTED);
                            g2.drawString("+" + remaining, dotX + 2,
                                dotY + dotSize - 1);
                        }

                        dotY += dotSize + dotGap + 1;
                    }
                }

                g2.dispose();
            }
        };

        cell.setOpaque(false);
        cell.setPreferredSize(new Dimension(0, 62));
        cell.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ── Hover + click ─────────────────────────────────────────────────────
        cell.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                cell.putClientProperty("hovered", true); cell.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                cell.putClientProperty("hovered", false); cell.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                selectedDate = date;
                rebuildGrid();
                showDayDetail(date);
            }
        });

        return cell;
    }


    // ─── Detail panel ─────────────────────────────────────────────────────────
    private JPanel buildDetailCard() {
        JPanel card = createCard();
        card.setLayout(new BorderLayout(0, 0));
        card.setPreferredSize(new Dimension(300, 0));

        // Detail header
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
            new EmptyBorder(14, AppTheme.PADDING_CARD, 14, AppTheme.PADDING_CARD)));

        detailDateLabel = new JLabel("Select a date");
        detailDateLabel.setFont(AppTheme.FONT_SMALL_BOLD);
        detailDateLabel.setForeground(AppTheme.TEXT_PRIMARY);

        detailCountLabel = new JLabel("");
        detailCountLabel.setFont(AppTheme.FONT_CAPTION);
        detailCountLabel.setForeground(AppTheme.TEXT_SECONDARY);

        header.add(detailDateLabel,  BorderLayout.NORTH);
        header.add(detailCountLabel, BorderLayout.SOUTH);
        card.add(header, BorderLayout.NORTH);

        // Scrollable list — non-opaque so the card's rounded clip shows through
        detailList = new JPanel();
        detailList.setLayout(new BoxLayout(detailList, BoxLayout.Y_AXIS));
        detailList.setBackground(AppTheme.BG_CARD);
        detailList.setOpaque(true);
        detailList.setBorder(new EmptyBorder(8, 0, 8, 0));

        JScrollPane scroll = new JScrollPane(detailList);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.setBackground(AppTheme.BG_CARD);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private void showDayDetail(LocalDate date) {
        detailDateLabel.setText(date.format(DETAIL_FMT));

        List<Appointment> appts = allAppointments.stream()
            .filter(a -> {
                try { return LocalDate.parse(a.getAppointmentDate()).equals(date); }
                catch (Exception ex) { return false; }
            })
            .sorted(Comparator.comparing(Appointment::getTimeSlot))
            .collect(Collectors.toList());

        detailCountLabel.setText(appts.isEmpty()
            ? "No appointments"
            : appts.size() + " appointment" + (appts.size() > 1 ? "s" : ""));

        detailList.removeAll();

        if (appts.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setOpaque(false);
            JLabel msg = new JLabel("No appointments on this day");
            msg.setFont(AppTheme.FONT_SMALL);
            msg.setForeground(AppTheme.TEXT_MUTED);
            empty.add(msg);
            empty.setPreferredSize(new Dimension(0, 160));
            detailList.add(empty);
        } else {
            for (Appointment a : appts) {
                detailList.add(buildDetailRow(a));
                // Thin divider
                JPanel div = new JPanel();
                div.setBackground(AppTheme.SEPARATOR);
                div.setPreferredSize(new Dimension(0, 1));
                div.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
                div.setOpaque(true);
                detailList.add(div);
            }
        }

        detailList.revalidate();
        detailList.repaint();
    }

    private JPanel buildDetailRow(Appointment a) {
        Color[] colors = statusColors(a.getStatus());

        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        row.setBorder(new EmptyBorder(10, AppTheme.PADDING_CARD, 10, AppTheme.PADDING_CARD));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Left accent bar colored by status
        JPanel accent = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colors[1]);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getWidth(), getWidth());
                g2.dispose();
            }
        };
        accent.setPreferredSize(new Dimension(3, 0));
        accent.setOpaque(false);
        row.add(accent, BorderLayout.WEST);

        // ── Badge — fixed top-right, always same position ─────────────────────
        JLabel badge = new JLabel(a.getStatus()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colors[0]);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(AppTheme.FONT_LABEL);
        badge.setForeground(colors[1]);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(2, 8, 2, 8));

        // Wrap badge in a NORTH-anchored panel so it sticks to top-right
        JPanel badgeAnchor = new JPanel(new BorderLayout());
        badgeAnchor.setOpaque(false);
        JPanel badgeTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeTop.setOpaque(false);
        badgeTop.add(badge);
        badgeAnchor.add(badgeTop, BorderLayout.NORTH);
        row.add(badgeAnchor, BorderLayout.EAST);

        // ── Main info ─────────────────────────────────────────────────────────
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel timeLbl = new JLabel(a.getTimeSlot());
        timeLbl.setFont(AppTheme.FONT_SMALL_BOLD);
        timeLbl.setForeground(AppTheme.TEXT_PRIMARY);
        timeLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(timeLbl);
        info.add(Box.createRigidArea(new Dimension(0, 3)));

        String custName    = resolveCustomerName(a.getCustomerId());
        String serviceName = ServiceItemDAO.getServiceName(a.getServiceItemId());
        JLabel custLbl = new JLabel(custName + "  ·  " + serviceName);
        custLbl.setFont(AppTheme.FONT_CAPTION);
        custLbl.setForeground(AppTheme.TEXT_SECONDARY);
        custLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        info.add(custLbl);
        info.add(Box.createRigidArea(new Dimension(0, 2)));

        JLabel vehLbl = new JLabel(a.getVehiclePlate() + "  ·  " + a.getVehicleBrand() + " " + a.getVehicleModel());
        vehLbl.setFont(AppTheme.FONT_CAPTION);
        vehLbl.setForeground(AppTheme.TEXT_MUTED);
        vehLbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 16));
        info.add(vehLbl);

        row.add(info, BorderLayout.CENTER);

        // Hover highlight
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { row.setBackground(AppTheme.BG_PAGE); row.setOpaque(true); }
            @Override public void mouseExited(MouseEvent e)  { row.setOpaque(false); row.repaint(); }
            @Override public void mouseClicked(MouseEvent e) { showAppointmentPopup(a); }
        });

        return row;
    }

    // ─── Appointment detail popup ─────────────────────────────────────────────
    private void showAppointmentPopup(Appointment a) {
        String custName    = resolveCustomerName(a.getCustomerId());
        String serviceName = ServiceItemDAO.getServiceName(a.getServiceItemId());
        String techName    = resolveTechName(a.getTechnicianId());

        UIComponent.showDialog(this, "Appointment Details",
            UIComponent.DialogType.INFO,
            a.getAppointmentId() + "  ·  " + a.getTimeSlot() + "  ·  " + a.getStatus(),
            new String[][]{
                {"Customer",    custName},
                {"Service",     serviceName},
                {"Vehicle",     a.getVehiclePlate() + " — " + a.getVehicleBrand() + " " + a.getVehicleModel()},
                {"Technician",  techName},
                {"Date",        a.getAppointmentDate()},
                {"Status",      a.getStatus()},
            },
            new UIComponent.DialogAction[]{
                new UIComponent.DialogAction("Close", UIComponent.DialogActionStyle.OUTLINE, null)
            });
    }

    // ─── Card factory ─────────────────────────────────────────────────────────
    /**
     * Returns a wrapper JPanel that:
     *  - paints a white rounded background + border
     *  - clips ALL children (including opaque scroll panes) via paintComponent + paintChildren
     * The inner content panel uses setOpaque(false) so the card background shows through.
     */
    private JPanel createCard() {
        int arc = AppTheme.RADIUS_CARD * 2;
        JPanel card = new JPanel(new BorderLayout()) {
            @Override public boolean isOptimizedDrawingEnabled() { return false; }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                g2.dispose();
            }

            @Override protected void paintChildren(Graphics g) {
                // Clip children to the rounded shape before delegating
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), arc, arc));
                super.paintChildren(g2);
                g2.dispose();

                // Draw border AFTER children so it's always on top
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g3.setColor(AppTheme.BORDER_CARD);
                g3.setStroke(new BasicStroke(1f));
                g3.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g3.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }
    
    // ─── Helpers ──────────────────────────────────────────────────────────────
    private Color[] statusColors(String status) {
        if (status == null) status = "";
        switch (status.toLowerCase()) {
            case "pending":   return new Color[]{new Color(255, 243, 205), DOT_PENDING};
            case "approved":  return new Color[]{new Color(209, 231, 255), DOT_APPROVED};
            case "completed": return new Color[]{new Color(209, 244, 223), DOT_COMPLETED};
            case "paid":      return new Color[]{new Color(209, 244, 223), DOT_COMPLETED};
            case "cancelled": return new Color[]{new Color(240, 240, 245), DOT_CANCELLED};
            case "rejected":  return new Color[]{new Color(255, 218, 215), DOT_REJECTED};
            default:          return new Color[]{AppTheme.BG_PAGE,         AppTheme.TEXT_MUTED};
        }
    }

    private Color dotColor(String status) {
        if (status == null) status = "";
        switch (status.toLowerCase()) {
            case "pending":   return DOT_PENDING;
            case "approved":  return DOT_APPROVED;
            case "completed": return DOT_COMPLETED;
            case "paid":      return DOT_COMPLETED;
            case "cancelled": return DOT_CANCELLED;
            case "rejected":  return DOT_REJECTED;
            default:          return AppTheme.TEXT_MUTED;
        }
    }

    private String resolveCustomerName(String customerId) {
        try {
            for (String[] c : customerDAO.getAllCustomers())
                if (c[0].trim().equals(customerId)) return c[2].trim();
        } catch (Exception ignored) {}
        return customerId;
    }

    private String resolveTechName(String techId) {
        if (techId == null || techId.equalsIgnoreCase("null") || techId.trim().isEmpty()
                || techId.equalsIgnoreCase("N/A")) return "—";
        try {
            return technicianDAO.getAllTechnicians().stream()
                .filter(t -> t.getTechnicianId().equals(techId))
                .map(t -> t.getTechnicianId() + " — " + t.getUsername())
                .findFirst().orElse(techId);
        } catch (Exception ignored) { return techId; }
    }
}