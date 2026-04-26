package views.manager;

import models.customer_function.ServiceItem;
import models.customer_function.ServiceItemDAO;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServicePricesPage extends JPanel {

    private static final String FILE_PATH = "src/txtFiles/service_items.txt";

    private final MainFrame frame;
    private final ServiceItemDAO dao = new ServiceItemDAO();

    // Stat card labels
    private JLabel lblTotal;
    private JLabel lblNormal;
    private JLabel lblSpecial;

    // Tables
    private DefaultTableModel normalModel;
    private DefaultTableModel specialModel;
    private JTable normalTable;
    private JTable specialTable;

    // Body panel (scrollable)
    private JPanel body;

    public ServicePricesPage(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_CONTENT);
        add(buildHeader(), BorderLayout.NORTH);

        JScrollPane scroll = buildScrollableBody();
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    // ─── Header ───────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BG_HEADER);
        header.setPreferredSize(new Dimension(0, AppTheme.HEADER_HEIGHT));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_HEADER),
            BorderFactory.createEmptyBorder(0, AppTheme.PADDING_PAGE, 0, AppTheme.PADDING_PAGE)
        ));
        header.add(UIComponent.createPageTitle("Service Prices"), BorderLayout.WEST);
        return header;
    }

    // ─── Scrollable body ──────────────────────────────────────────────────────

    private JScrollPane buildScrollableBody() {
        body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppTheme.BG_CONTENT);
        body.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE
        ));

        // Subtitle
        JLabel subtitle = new JLabel("Manage service items for Normal and Special service packages.");
        subtitle.setFont(AppTheme.FONT_SMALL);
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(subtitle);
        body.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        // Stat cards
        body.add(buildStatCards());
        body.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        // Normal section
        body.add(buildSectionHeader("P001", "Normal Service",
            new Color(0xDCEAF8), new Color(0x185FA5), false));
        body.add(UIComponent.verticalGap(AppTheme.GAP_ITEM));
        body.add(buildTableCard("normal"));
        body.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        // Special section
        body.add(buildSectionHeader("P002", "Special Service",
            new Color(0xFAEEDA), new Color(0x854F0B), true));
        body.add(UIComponent.verticalGap(AppTheme.GAP_ITEM));
        body.add(buildTableCard("special"));
        body.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppTheme.BG_CONTENT);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    // ─── Stat cards ───────────────────────────────────────────────────────────

    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, AppTheme.GAP_ITEM, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTotal   = new JLabel("0");
        lblNormal  = new JLabel("0");
        lblSpecial = new JLabel("0");

        row.add(buildStatCard("Total services",   lblTotal,   AppTheme.TEXT_PRIMARY));
        row.add(buildStatCard("Normal services",  lblNormal,  new Color(0x185FA5)));
        row.add(buildStatCard("Special services", lblSpecial, new Color(0x854F0B)));

        return row;
    }

    private JPanel buildStatCard(String title, JLabel countLabel, Color accent) {
        JPanel card = UIComponent.createCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD,
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppTheme.FONT_CAPTION);
        lblTitle.setForeground(AppTheme.TEXT_SECONDARY);

        countLabel.setFont(AppTheme.FONT_HEADING_1);
        countLabel.setForeground(accent);

        card.add(lblTitle,    BorderLayout.NORTH);
        card.add(countLabel,  BorderLayout.CENTER);
        return card;
    }

    // ─── Section header ───────────────────────────────────────────────────────

    private JPanel buildSectionHeader(String badgeText, String sectionTitle,
                                      Color badgeBg, Color badgeFg, boolean isSpecial) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left: pill badge + label
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel badge = new JLabel(badgeText) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(badgeBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setFont(AppTheme.FONT_SMALL_BOLD);
        badge.setForeground(badgeFg);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

        JLabel lbl = new JLabel(sectionTitle);
        lbl.setFont(AppTheme.FONT_HEADING_3);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);

        left.add(badge);
        left.add(lbl);

        // Right: Add Service button
        JButton btnAdd = makeAddButton("+ Add Service");
        btnAdd.addActionListener(e -> showAddDialog(isSpecial));

        row.add(left,   BorderLayout.WEST);
        row.add(btnAdd, BorderLayout.EAST);
        return row;
    }

    // ─── Table card ───────────────────────────────────────────────────────────

    private JPanel buildTableCard(String type) {
        String[] columns = {"ID", "Description", "Hours", "Actions"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 3; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(52);
        table.setFont(AppTheme.FONT_SMALL);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.setBackground(AppTheme.BG_CARD);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setFocusable(false);

        // Row divider painter
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBackground(AppTheme.BG_CARD);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 14, 0, 8)
                ));
                return this;
            }
        });

        // Header styling
        table.getTableHeader().setFont(AppTheme.FONT_LABEL);
        table.getTableHeader().setBackground(AppTheme.BG_PAGE);
        table.getTableHeader().setForeground(AppTheme.TEXT_SECONDARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        // Column renderers
        // ID column
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setFont(AppTheme.FONT_LABEL);
                setForeground(AppTheme.TEXT_SECONDARY);
                setBackground(AppTheme.BG_CARD);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 14, 0, 8)
                ));
                return this;
            }
        });

        // Description column
        table.getColumnModel().getColumn(1).setPreferredWidth(400);
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setFont(AppTheme.FONT_SMALL);
                setForeground(AppTheme.TEXT_PRIMARY);
                setBackground(AppTheme.BG_CARD);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 14, 0, 8)
                ));
                return this;
            }
        });

        // Hours column
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val == null ? "" : val + " hrs", sel, focus, row, col);
                setFont(AppTheme.FONT_SMALL);
                setForeground(AppTheme.TEXT_PRIMARY);
                setBackground(AppTheme.BG_CARD);
                setHorizontalAlignment(SwingConstants.RIGHT);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
                    BorderFactory.createEmptyBorder(0, 8, 0, 14)
                ));
                return this;
            }
        });

        // Actions column
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setCellRenderer(new ServiceActionRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new ServiceActionEditor(table));

        if ("normal".equals(type)) {
            normalModel = model;
            normalTable = table;
        } else {
            specialModel = model;
            specialTable = table;
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        final int RADIUS = 16;
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 35));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, RADIUS, RADIUS);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
                g2.setColor(AppTheme.SEPARATOR);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS, RADIUS);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(scroll, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Data loading ─────────────────────────────────────────────────────────

    public void loadData() {
        List<ServiceItem> all = dao.getAllServiceItems();

        List<ServiceItem> normalItems  = new ArrayList<>();
        List<ServiceItem> specialItems = new ArrayList<>();
        for (ServiceItem s : all) {
            if ("P001".equals(s.getServicePriceID())) normalItems.add(s);
            else if ("P002".equals(s.getServicePriceID())) specialItems.add(s);
        }

        // Stat cards
        lblTotal.setText(String.valueOf(all.size()));
        lblNormal.setText(String.valueOf(normalItems.size()));
        lblSpecial.setText(String.valueOf(specialItems.size()));

        // Populate tables
        populateTable(normalModel,  normalItems);
        populateTable(specialModel, specialItems);
    }

    private void populateTable(DefaultTableModel model, List<ServiceItem> items) {
        model.setRowCount(0);
        for (ServiceItem s : items) {
            model.addRow(new Object[]{s.getServiceItemID(), s.getDescription(), s.getDuration(), s});
        }
    }

    // ─── Add dialog ───────────────────────────────────────────────────────────

    private void showAddDialog(boolean isSpecial) {
        String packageId   = isSpecial ? "P002" : "P001";
        String packageType = isSpecial ? "Special" : "Normal";
        String packageLabel = (isSpecial ? "Special Service (P002)" : "Normal Service (P001)");

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Add Service", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = buildDialogForm();

        // Auto-generated ID
        String newId = generateNextId();
        addDialogRow(form, "SERVICE ID", buildReadOnlyField(newId));
        addDialogGap(form);

        // Package (read-only)
        addDialogRow(form, "PACKAGE", buildReadOnlyField(packageLabel));
        addDialogGap(form);

        // Description
        addDialogRow(form, "DESCRIPTION", null);
        JTextField txtDesc = UIComponent.createTextField("Enter service description");
        txtDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtDesc);
        addDialogGap(form);

        // Hours
        addDialogRow(form, "HOURS", null);
        JTextField txtHours = UIComponent.createTextField("e.g. 1.0");
        txtHours.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtHours.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtHours);

        // Buttons
        form.add(UIComponent.verticalGap(20));
        JPanel btnRow = buildButtonRow();
        JButton btnCancel = UIComponent.createOutlineButton("Cancel");
        JButton btnAdd    = UIComponent.createPrimaryButton("Add");
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnAdd.setPreferredSize(new Dimension(100, 40));

        btnCancel.addActionListener(e -> dialog.dispose());
        btnAdd.addActionListener(e -> {
            String desc  = txtDesc.getText().trim();
            String hours = txtHours.getText().trim();

            if (desc.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Description cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (desc.contains("|")) {
                JOptionPane.showMessageDialog(dialog, "Description cannot contain '|'.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double h;
            try { h = Double.parseDouble(hours); } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Hours must be a valid decimal number.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (h <= 0) {
                JOptionPane.showMessageDialog(dialog, "Hours must be greater than 0.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String line = newId + "|" + packageId + "|" + packageType + "|" + hours + "|" + desc;
            if (appendLine(line)) {
                JOptionPane.showMessageDialog(dialog, "Service added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to write to file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(btnCancel);
        btnRow.add(btnAdd);
        form.add(btnRow);

        dialog.add(new JScrollPane(form) {{
            setBorder(null);
            getViewport().setBackground(AppTheme.BG_CARD);
        }}, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ─── Edit dialog ──────────────────────────────────────────────────────────

    private void showEditDialog(ServiceItem item) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Edit Service", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = buildDialogForm();

        // ID (read-only)
        addDialogRow(form, "SERVICE ID", buildReadOnlyField(item.getServiceItemID()));
        addDialogGap(form);

        // Package (read-only)
        String pkgLabel = "P001".equals(item.getServicePriceID())
            ? "Normal Service (P001)" : "Special Service (P002)";
        addDialogRow(form, "PACKAGE", buildReadOnlyField(pkgLabel));
        addDialogGap(form);

        // Description
        addDialogRow(form, "DESCRIPTION", null);
        JTextField txtDesc = UIComponent.createTextField("Description");
        txtDesc.setText(item.getDescription());
        txtDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtDesc);
        addDialogGap(form);

        // Hours
        addDialogRow(form, "HOURS", null);
        JTextField txtHours = UIComponent.createTextField("e.g. 1.0");
        txtHours.setText(item.getDuration());
        txtHours.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtHours.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtHours);

        // Buttons
        form.add(UIComponent.verticalGap(20));
        JPanel btnRow = buildButtonRow();
        JButton btnCancel = UIComponent.createOutlineButton("Cancel");
        JButton btnSave   = UIComponent.createPrimaryButton("Save");
        btnCancel.setPreferredSize(new Dimension(100, 40));
        btnSave.setPreferredSize(new Dimension(100, 40));

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String desc  = txtDesc.getText().trim();
            String hours = txtHours.getText().trim();

            if (desc.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Description cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (desc.contains("|")) {
                JOptionPane.showMessageDialog(dialog, "Description cannot contain '|'.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double h;
            try { h = Double.parseDouble(hours); } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Hours must be a valid decimal number.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (h <= 0) {
                JOptionPane.showMessageDialog(dialog, "Hours must be greater than 0.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (updateLine(item.getServiceItemID(), desc, hours)) {
                JOptionPane.showMessageDialog(dialog, "Service updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        form.add(btnRow);

        dialog.add(new JScrollPane(form) {{
            setBorder(null);
            getViewport().setBackground(AppTheme.BG_CARD);
        }}, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    private void handleDelete(ServiceItem item) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete " + item.getServiceItemID() + "? This action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (deleteLine(item.getServiceItemID())) {
            JOptionPane.showMessageDialog(this, "Service deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── File I/O ─────────────────────────────────────────────────────────────

    private boolean appendLine(String line) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(line);
            bw.newLine();
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    private boolean updateLine(String id, String newDesc, String newHours) {
        try {
            Path path = Paths.get(FILE_PATH);
            List<String> lines = Files.readAllLines(path);
            List<String> updated = new ArrayList<>();
            for (String line : lines) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 5 && p[0].trim().equals(id)) {
                    updated.add(p[0] + "|" + p[1] + "|" + p[2] + "|" + newHours + "|" + newDesc);
                } else {
                    updated.add(line);
                }
            }
            Files.write(path, updated);
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    private boolean deleteLine(String id) {
        try {
            Path path = Paths.get(FILE_PATH);
            List<String> lines = Files.readAllLines(path);
            List<String> updated = new ArrayList<>();
            boolean found = false;
            for (String line : lines) {
                String[] p = line.split("\\|", -1);
                if (p.length >= 1 && p[0].trim().equals(id)) { found = true; }
                else updated.add(line);
            }
            if (!found) return false;
            Files.write(path, updated);
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    private String generateNextId() {
        List<ServiceItem> all = dao.getAllServiceItems();
        int max = 0;
        for (ServiceItem s : all) {
            String num = s.getServiceItemID().replaceAll("[^0-9]", "");
            try { int n = Integer.parseInt(num); if (n > max) max = n; } catch (NumberFormatException ignored) {}
        }
        return "SI" + String.format("%03d", max + 1);
    }

    // ─── Action cell renderer / editor ───────────────────────────────────────

    private class ServiceActionRenderer implements javax.swing.table.TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean focus, int row, int col) {
            return buildActionsPanel(val instanceof ServiceItem ? (ServiceItem) val : null, false, null);
        }
    }

    private class ServiceActionEditor extends DefaultCellEditor {
        private ServiceItem current;
        private final JTable table;

        ServiceActionEditor(JTable table) {
            super(new JCheckBox());
            this.table = table;
            setClickCountToStart(1);
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object val,
                boolean sel, int row, int col) {
            current = val instanceof ServiceItem ? (ServiceItem) val : null;
            return buildActionsPanel(current, true, table);
        }

        @Override public Object getCellEditorValue() { return current; }
    }

    private JPanel buildActionsPanel(ServiceItem item, boolean interactive, JTable table) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(AppTheme.BG_CARD);

        if (item == null) return panel;

        JButton btnEdit   = makeActionButton("Edit",   false);
        JButton btnDelete = makeActionButton("Delete", true);

        if (interactive) {
            btnEdit.addActionListener(e -> {
                if (table != null && table.isEditing()) table.getCellEditor().stopCellEditing();
                showEditDialog(item);
            });
            btnDelete.addActionListener(e -> {
                if (table != null && table.isEditing()) table.getCellEditor().stopCellEditing();
                handleDelete(item);
            });
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.gridy = 0;
        gbc.gridx = 0; panel.add(btnEdit,   gbc);
        gbc.gridx = 1; panel.add(btnDelete, gbc);
        return panel;
    }

    private JButton makeActionButton(String text, boolean danger) {
        final Color activeBorder = danger ? new Color(0xA32D2D) : AppTheme.SEPARATOR;
        final Color activeFg     = danger ? new Color(0xA32D2D) : AppTheme.TEXT_PRIMARY;
        final Color hoverBg      = danger ? new Color(0xFCEBEB) : AppTheme.BG_PAGE;
        final int ARC = 10;

        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isEnabled() && getModel().isRollover()) {
                    g2.setColor(hoverBg);
                    g2.fillRoundRect(0, 1, getWidth(), getHeight() - 2, ARC, ARC);
                }
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? activeBorder
                    : new Color(AppTheme.SEPARATOR.getRed(), AppTheme.SEPARATOR.getGreen(),
                                AppTheme.SEPARATOR.getBlue(), 100));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 1, getWidth() - 1, getHeight() - 2, ARC, ARC);
                g2.dispose();
            }
        };
        btn.setFont(AppTheme.FONT_LABEL);
        btn.setForeground(activeFg);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        btn.setPreferredSize(new Dimension(74, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─── Dialog helpers ───────────────────────────────────────────────────────

    private JPanel buildDialogForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(AppTheme.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        return form;
    }

    private void addDialogRow(JPanel form, String labelText, JComponent field) {
        JLabel lbl = UIComponent.createFieldLabel(labelText);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lbl);
        form.add(UIComponent.verticalGap(4));
        if (field != null) {
            field.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(field);
        }
    }

    private void addDialogGap(JPanel form) {
        form.add(UIComponent.verticalGap(12));
    }

    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private JTextField buildReadOnlyField(String text) {
        JTextField tf = UIComponent.createTextField("");
        tf.setText(text);
        tf.setEditable(false);
        tf.setForeground(AppTheme.TEXT_SECONDARY);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    // ─── Add button ───────────────────────────────────────────────────────────

    private JButton makeAddButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_BUTTON * 2, AppTheme.RADIUS_BUTTON * 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.FONT_SMALL_BOLD);
        btn.setForeground(AppTheme.TEXT_ON_PRIMARY);
        btn.setBackground(AppTheme.PRIMARY);
        btn.setPreferredSize(new Dimension(140, 36));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(AppTheme.PRIMARY_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(AppTheme.PRIMARY); }
        });
        return btn;
    }
}