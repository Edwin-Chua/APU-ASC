package views.manager;

import models.dao.ServiceItemDAO;
import models.dao.ServiceTypeDAO;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class ServicePricesPage extends JPanel {

    private final MainFrame frame;

    // type_id → table model, populated during buildScrollableBody
    private final Map<String, DefaultTableModel> tableModels = new LinkedHashMap<>();

    private JLabel lblTotal;
    private JLabel lblNormalCount;
    private JLabel lblMajorCount;

    public ServicePricesPage(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_CONTENT);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildScrollableBody(), BorderLayout.CENTER);
    }

    // ─── Header ───────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BG_HEADER);
        header.setPreferredSize(new Dimension(0, AppTheme.HEADER_HEIGHT));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_HEADER),
            new EmptyBorder(0, AppTheme.PADDING_PAGE, 0, AppTheme.PADDING_PAGE)
        ));
        header.add(UIComponent.createPageTitle("Service Prices"), BorderLayout.WEST);
        return header;
    }

    // ─── Scrollable body ──────────────────────────────────────────────────────

    private JScrollPane buildScrollableBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(AppTheme.BG_CONTENT);
        body.setBorder(new EmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE
        ));

        JLabel subtitle = new JLabel("Manage service items for each service package.");
        subtitle.setFont(AppTheme.FONT_SMALL);
        subtitle.setForeground(AppTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(subtitle);
        body.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        body.add(buildStatCards());
        body.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        // ── One section per service type ─────────────────────────────────────
        List<String[]> types = ServiceTypeDAO.getAllServiceTypes();
        for (String[] type : types) {
            body.add(buildSectionHeader(type[0], type[1]));
            body.add(UIComponent.verticalGap(AppTheme.GAP_ITEM));
            body.add(buildTableCard(type[0]));
            body.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));
        }

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(AppTheme.BG_CONTENT);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        loadData();
        return scroll;
    }

    // ─── Stat cards ───────────────────────────────────────────────────────────

    private JPanel buildStatCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, AppTheme.GAP_ITEM, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTotal       = new JLabel("0");
        lblNormalCount = new JLabel("0");
        lblMajorCount  = new JLabel("0");

        row.add(buildStatCard("Total Services", lblTotal,       AppTheme.PRIMARY));
        row.add(buildStatCard("Normal Service", lblNormalCount, AppTheme.WARNING));
        row.add(buildStatCard("Major Service",  lblMajorCount,  AppTheme.SUCCESS));

        return row;
    }

    private JPanel buildStatCard(String title, JLabel countLabel, Color accentColor) {
        JPanel card = UIComponent.createCard();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD,
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(AppTheme.FONT_HEADING_3);
        lblTitle.setForeground(AppTheme.TEXT_SECONDARY);

        countLabel.setFont(AppTheme.FONT_HEADING_1);
        countLabel.setForeground(accentColor);

        card.add(lblTitle,    BorderLayout.NORTH);
        card.add(countLabel,  BorderLayout.CENTER);
        return card;
    }

    // ─── Section header (badge + title + Add button) ──────────────────────────

    private JPanel buildSectionHeader(String typeId, String typeName) {
        Color badgeBg = getTypeBadgeBg(typeId);
        Color badgeFg = getTypeAccent(typeId);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel badge = new JLabel(typeId) {
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
        badge.setBorder(new EmptyBorder(3, 10, 3, 10));

        JLabel lbl = new JLabel(typeName);
        lbl.setFont(AppTheme.FONT_HEADING_3);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);

        left.add(badge);
        left.add(lbl);

        JButton btnAdd = makeAddButton("+ Add Service");
        btnAdd.addActionListener(e -> showAddDialog(typeId, typeName));

        row.add(left,   BorderLayout.WEST);
        row.add(btnAdd, BorderLayout.EAST);
        return row;
    }

    // ─── Table card ───────────────────────────────────────────────────────────

    private JPanel buildTableCard(String typeId) {
        String[] columns = {"Service ID", "Service Name", "Description", "Price (RM)", "Last Updated", "Actions"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 5; }
        };
        tableModels.put(typeId, model);

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

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBackground(AppTheme.BG_CARD);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
                    new EmptyBorder(0, 14, 0, 8)
                ));
                return this;
            }
        });

        table.getTableHeader().setFont(AppTheme.FONT_LABEL);
        table.getTableHeader().setBackground(AppTheme.BG_PAGE);
        table.getTableHeader().setForeground(AppTheme.TEXT_SECONDARY);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(90);   // Service ID
        table.getColumnModel().getColumn(1).setPreferredWidth(160);  // Service Name
        table.getColumnModel().getColumn(2).setPreferredWidth(310);  // Description
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // Price
        table.getColumnModel().getColumn(4).setPreferredWidth(120);  // Last Updated
        table.getColumnModel().getColumn(5).setPreferredWidth(160);  // Actions

        // Muted style for ID column
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setFont(AppTheme.FONT_LABEL);
                setForeground(AppTheme.TEXT_SECONDARY);
                setBackground(AppTheme.BG_CARD);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.SEPARATOR),
                    new EmptyBorder(0, 14, 0, 8)
                ));
                return this;
            }
        });

        table.getColumnModel().getColumn(5).setCellRenderer(new ActionRenderer());
        table.getColumnModel().getColumn(5).setCellEditor(new ActionEditor(table));

        final int RADIUS = 16;
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.setBackground(AppTheme.BG_CARD);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

    // ─── Load data ────────────────────────────────────────────────────────────

    public void loadData() {
        List<String[]> all = ServiceItemDAO.getAllServiceItems();

        for (DefaultTableModel m : tableModels.values()) m.setRowCount(0);

        // Group items by service_type_id
        Map<String, List<String[]>> grouped = new LinkedHashMap<>();
        for (String[] item : all) {
            grouped.computeIfAbsent(item[1], k -> new ArrayList<>()).add(item);
        }

        int total = 0;
        for (Map.Entry<String, DefaultTableModel> entry : tableModels.entrySet()) {
            String typeId = entry.getKey();
            DefaultTableModel model = entry.getValue();
            List<String[]> items = grouped.getOrDefault(typeId, Collections.emptyList());

            for (String[] item : items) {
                model.addRow(new Object[]{
                    item[0],          // Service ID
                    item[2],          // Service Name
                    item[5],          // Description
                    "RM " + item[3],  // Price
                    item[4],          // Last Updated
                    item              // String[] stored in actions column for editor
                });
            }

            if ("ST001".equals(typeId) && lblNormalCount != null) lblNormalCount.setText(String.valueOf(items.size()));
            else if ("ST002".equals(typeId) && lblMajorCount != null) lblMajorCount.setText(String.valueOf(items.size()));
            total += items.size();
        }

        if (lblTotal != null) lblTotal.setText(String.valueOf(total));
    }

    // ─── Add dialog ───────────────────────────────────────────────────────────

    private void showAddDialog(String typeId, String typeName) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Add Service — " + typeName, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = buildDialogForm();

        addDialogRow(form, "SERVICE ID",   buildReadOnlyField(ServiceItemDAO.generateNextId()));
        addDialogGap(form);
        addDialogRow(form, "SERVICE TYPE", buildReadOnlyField(typeId + " — " + typeName));
        addDialogGap(form);

        addDialogRow(form, "SERVICE NAME", null);
        JTextField txtName = UIComponent.createTextField("e.g. Oil Change");
        txtName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtName);
        addDialogGap(form);

        addDialogRow(form, "DESCRIPTION", null);
        JTextField txtDesc = UIComponent.createTextField("Brief description of the service");
        txtDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtDesc);
        addDialogGap(form);

        addDialogRow(form, "PRICE (RM)", null);
        JTextField txtPrice = UIComponent.createTextField("e.g. 50.00");
        txtPrice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPrice.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtPrice);

        form.add(UIComponent.verticalGap(20));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnCancel = UIComponent.createOutlineButton("Cancel");
        JButton btnAdd    = UIComponent.createPrimaryButton("Add");
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnAdd.setPreferredSize(new Dimension(100, 38));

        btnCancel.addActionListener(e -> dialog.dispose());
        btnAdd.addActionListener(e -> {
            String name  = txtName.getText().trim();
            String desc  = txtDesc.getText().trim();
            String price = txtPrice.getText().trim();

            if (name.isEmpty() || desc.isEmpty() || price.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (name.contains("|") || desc.contains("|")) {
                JOptionPane.showMessageDialog(dialog, "Fields cannot contain '|'.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                double p = Double.parseDouble(price);
                if (p <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Price must be a positive number (e.g. 50.00).", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (ServiceItemDAO.addServiceItem(typeId, name, price, desc)) {
                JOptionPane.showMessageDialog(dialog, "Service added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to save. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(btnCancel);
        btnRow.add(btnAdd);
        form.add(btnRow);

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        sp.getViewport().setBackground(AppTheme.BG_CARD);
        dialog.add(sp, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ─── Edit dialog ──────────────────────────────────────────────────────────

    private void showEditDialog(String[] item) {
        // item: [0]=id, [1]=typeId, [2]=name, [3]=price, [4]=date, [5]=description
        String typeName = ServiceTypeDAO.getServiceTypeName(item[1]);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Edit Service", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = buildDialogForm();

        addDialogRow(form, "SERVICE ID",   buildReadOnlyField(item[0]));
        addDialogGap(form);
        addDialogRow(form, "SERVICE TYPE", buildReadOnlyField(item[1] + " — " + typeName));
        addDialogGap(form);

        addDialogRow(form, "SERVICE NAME", null);
        JTextField txtName = UIComponent.createTextField("Service name");
        txtName.setText(item[2]);
        txtName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtName);
        addDialogGap(form);

        addDialogRow(form, "DESCRIPTION", null);
        JTextField txtDesc = UIComponent.createTextField("Description");
        txtDesc.setText(item[5]);
        txtDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtDesc);
        addDialogGap(form);

        addDialogRow(form, "PRICE (RM)", null);
        JTextField txtPrice = UIComponent.createTextField("e.g. 50.00");
        txtPrice.setText(item[3]);
        txtPrice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPrice.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtPrice);

        form.add(UIComponent.verticalGap(20));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnCancel = UIComponent.createOutlineButton("Cancel");
        JButton btnSave   = UIComponent.createPrimaryButton("Save");
        btnCancel.setPreferredSize(new Dimension(100, 38));
        btnSave.setPreferredSize(new Dimension(100, 38));

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSave.addActionListener(e -> {
            String name  = txtName.getText().trim();
            String desc  = txtDesc.getText().trim();
            String price = txtPrice.getText().trim();

            if (name.isEmpty() || desc.isEmpty() || price.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (name.contains("|") || desc.contains("|")) {
                JOptionPane.showMessageDialog(dialog, "Fields cannot contain '|'.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                double p = Double.parseDouble(price);
                if (p <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Price must be a positive number (e.g. 50.00).", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (ServiceItemDAO.updateServiceItem(item[0], name, price, desc)) {
                JOptionPane.showMessageDialog(dialog, "Service updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to update. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRow.add(btnCancel);
        btnRow.add(btnSave);
        form.add(btnRow);

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        sp.getViewport().setBackground(AppTheme.BG_CARD);
        dialog.add(sp, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    private void handleDelete(String[] item) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete \"" + item[2] + "\" (" + item[0] + ")?\nThis action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (ServiceItemDAO.deleteServiceItem(item[0])) {
            JOptionPane.showMessageDialog(this, "Service deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─── Action column renderer / editor ──────────────────────────────────────

    private class ActionRenderer implements javax.swing.table.TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean focus, int row, int col) {
            return buildActionsPanel(val instanceof String[] ? (String[]) val : null, false, null);
        }
    }

    private class ActionEditor extends DefaultCellEditor {
        private String[] current;
        private final JTable table;

        ActionEditor(JTable table) {
            super(new JCheckBox());
            this.table = table;
            setClickCountToStart(1);
        }

        @Override public Component getTableCellEditorComponent(JTable t, Object val,
                boolean sel, int row, int col) {
            current = val instanceof String[] ? (String[]) val : null;
            return buildActionsPanel(current, true, table);
        }

        @Override public Object getCellEditorValue() { return current; }
    }

    private JPanel buildActionsPanel(String[] item, boolean interactive, JTable table) {
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
        gbc.gridy  = 0;
        gbc.gridx  = 0; panel.add(btnEdit,   gbc);
        gbc.gridx  = 1; panel.add(btnDelete, gbc);
        return panel;
    }

    // ─── Dialog helpers ───────────────────────────────────────────────────────

    private JPanel buildDialogForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(AppTheme.BG_CARD);
        form.setBorder(new EmptyBorder(24, 28, 24, 28));
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

    private JTextField buildReadOnlyField(String text) {
        JTextField tf = UIComponent.createTextField("");
        tf.setText(text);
        tf.setEditable(false);
        tf.setForeground(AppTheme.TEXT_SECONDARY);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    // ─── Buttons ──────────────────────────────────────────────────────────────

    private JButton makeAddButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                    AppTheme.RADIUS_BUTTON * 2, AppTheme.RADIUS_BUTTON * 2);
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

    private JButton makeActionButton(String text, boolean danger) {
        final Color borderColor = danger ? new Color(0xA32D2D) : AppTheme.SEPARATOR;
        final Color fgColor     = danger ? new Color(0xA32D2D) : AppTheme.TEXT_PRIMARY;
        final Color hoverBg     = danger ? new Color(0xFCEBEB) : AppTheme.BG_PAGE;
        final int   ARC = 10;

        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(hoverBg);
                    g2.fillRoundRect(0, 1, getWidth(), getHeight() - 2, ARC, ARC);
                }
                g2.dispose();
                super.paintComponent(g);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 1, getWidth() - 1, getHeight() - 2, ARC, ARC);
                g2.dispose();
            }
        };
        btn.setFont(AppTheme.FONT_LABEL);
        btn.setForeground(fgColor);
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        btn.setPreferredSize(new Dimension(74, 32));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ─── Color helpers ────────────────────────────────────────────────────────

    private Color getTypeAccent(String typeId) {
        switch (typeId) {
            case "ST001": return new Color(0x185FA5); // blue  (Normal)
            case "ST002": return new Color(0x854F0B); // amber (Major)
            default:      return AppTheme.TEXT_PRIMARY;
        }
    }

    private Color getTypeBadgeBg(String typeId) {
        switch (typeId) {
            case "ST001": return new Color(0xDCEAF8); // light blue
            case "ST002": return new Color(0xFAEEDA); // light amber
            default:      return AppTheme.BG_PAGE;
        }
    }
}