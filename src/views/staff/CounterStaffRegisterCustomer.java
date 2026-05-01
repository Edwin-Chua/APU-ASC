package views.staff;

import models.dao.CustomerDAO;
import models.entities.CustomerRegister;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class CounterStaffRegisterCustomer extends JPanel {

    private final MainFrame frame;
    private final CustomerDAO customerDAO = new CustomerDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    // File 1 columns (original content)
    private static final String[] COLS = {
        "", "Customer ID", "Username", "Email", "Contact No.", "Status", "Registered Date"
    };

    public CounterStaffRegisterCustomer(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(0, 0));
        setBackground(AppTheme.BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE
        ));

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadTableData("");
    }

    // ─── Top bar: title + search + action buttons (File 2 layout) ─────────────

    private JPanel buildTopBar() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);

        // Page title
        JLabel title = UIComponent.createPageTitle("Register Customer");
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrapper.add(title);

        // Action row
        JPanel actions = new JPanel(new BorderLayout(8, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // ── Left: search + search button ──────────────────────────────────────
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(260, 38));
        searchField.setFont(AppTheme.FONT_SMALL);
        searchField.setForeground(AppTheme.TEXT_PRIMARY);
        searchField.setBackground(AppTheme.BG_FIELD);
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER_DEFAULT, 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        searchField.setToolTipText("Search by name, email or ID…");
        searchField.addActionListener(e -> loadTableData(searchField.getText().trim()));

        JButton btnSearch = UIComponent.createSmallButton("Search");
        btnSearch.setPreferredSize(new Dimension(80, 38));
        btnSearch.addActionListener(e -> loadTableData(searchField.getText().trim()));

        leftPanel.add(searchField);
        leftPanel.add(Box.createRigidArea(new Dimension(8, 0)));
        leftPanel.add(btnSearch);

        // ── Right: Refresh | Delete Selected | + Add Customer ────────────────
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        JButton btnRefresh = UIComponent.createSmallButton("↺ Refresh");
        btnRefresh.setPreferredSize(new Dimension(110, 38));

        JButton btnDelete = UIComponent.createDangerButton("Delete");
        btnDelete.setPreferredSize(new Dimension(100, 38));
        btnDelete.setMaximumSize(new Dimension(150, 38));

        JButton btnAdd = UIComponent.createPrimaryButton("＋ Add Customer");
        btnAdd.setPreferredSize(new Dimension(160, 38));
        btnAdd.setMaximumSize(new Dimension(160, 38));

        btnRefresh.addActionListener(e -> {
            searchField.setText("");
            loadTableData("");
        });
        btnDelete.addActionListener(e -> handleDeleteSelected());
        btnAdd.addActionListener(e -> showAddCustomerDialog());

        rightPanel.add(btnRefresh);
        rightPanel.add(btnDelete);
        rightPanel.add(btnAdd);

        actions.add(leftPanel,  BorderLayout.WEST);
        actions.add(rightPanel, BorderLayout.EAST);

        wrapper.add(actions);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        return wrapper;
    }

    // ─── Table card (File 2 card style + File 1 columns & renderers) ──────────

    private JPanel buildTableCard() {
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public Class<?> getColumnClass(int col) {
                return col == 0 ? Boolean.class : String.class;
            }
            @Override public boolean isCellEditable(int row, int col) {
                return col == 0;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(AppTheme.ROW_HEIGHT_TABLE);
        table.setFont(AppTheme.FONT_SMALL);
        table.setForeground(AppTheme.TEXT_PRIMARY);
        table.setBackground(AppTheme.BG_CARD);
        table.setGridColor(AppTheme.SEPARATOR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setSelectionBackground(AppTheme.PRIMARY_LIGHT);
        table.setSelectionForeground(AppTheme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ── Header styling ─────────────────────────────────────────────────────
        JTableHeader header = table.getTableHeader();
        header.setFont(AppTheme.FONT_LABEL);
        header.setBackground(AppTheme.BG_PAGE);
        header.setForeground(AppTheme.TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(0, 40));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_DEFAULT));
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setFont(AppTheme.FONT_LABEL);
                setForeground(AppTheme.TEXT_SECONDARY);
                setBackground(AppTheme.BG_PAGE);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_DEFAULT),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)
                ));
                return this;
            }
        };
        for (int i = 0; i < COLS.length; i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }

        // ── Column widths (File 1 proportions) ────────────────────────────────
        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setMinWidth(38);
        cm.getColumn(0).setMaxWidth(38);
        cm.getColumn(1).setPreferredWidth(100);
        cm.getColumn(2).setPreferredWidth(140);
        cm.getColumn(3).setPreferredWidth(200);
        cm.getColumn(4).setPreferredWidth(120);
        cm.getColumn(5).setPreferredWidth(90);
        cm.getColumn(6).setPreferredWidth(130);
        
		// Separator color — visible on both white and grey rows
		Color ROW_SEPARATOR = new Color(220, 220, 225);
		
		// ── Alternating row renderer ───────────────────────────────────────────
		DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
		    @Override
		    public Component getTableCellRendererComponent(
		            JTable t, Object val, boolean sel, boolean focus, int row, int col) {
		        super.getTableCellRendererComponent(t, val, sel, focus, row, col);
		        setBorder(BorderFactory.createCompoundBorder(
		            BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
		            BorderFactory.createEmptyBorder(0, 12, 0, 12)
		        ));
		        setFont(AppTheme.FONT_SMALL);
		        if (sel) {
		            setBackground(AppTheme.PRIMARY_LIGHT);
		            setForeground(AppTheme.TEXT_PRIMARY);
		        } else {
		            setBackground(row % 2 == 0 ? AppTheme.BG_CARD : new Color(248, 248, 250));
		            setForeground(AppTheme.TEXT_PRIMARY);
		        }
		        return this;
		    }
		};
		
		DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
		    @Override
		    public Component getTableCellRendererComponent(
		            JTable t, Object val, boolean sel, boolean focus, int row, int col) {
		        JLabel lbl = new JLabel(val != null ? val.toString() : "");
		        lbl.setOpaque(true);
		        lbl.setFont(AppTheme.FONT_SMALL);
		        lbl.setBorder(BorderFactory.createCompoundBorder(
		            BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_SEPARATOR),
		            BorderFactory.createEmptyBorder(0, 12, 0, 12)
		        ));
		        if (sel) {
		            lbl.setBackground(AppTheme.PRIMARY_LIGHT);
		        } else {
		            lbl.setBackground(row % 2 == 0 ? AppTheme.BG_CARD : new Color(248, 248, 250));
		        }
		        String status = val != null ? val.toString().toLowerCase() : "";
		        switch (status) {
		            case "approved": lbl.setForeground(AppTheme.STATUS_COMPLETED_FG); break;
		            case "pending":  lbl.setForeground(AppTheme.STATUS_PENDING_FG);   break;
		            default:         lbl.setForeground(AppTheme.TEXT_SECONDARY);
		        }
		        return lbl;
		    }
		};
		
		// Apply renderers — col 5 = Status gets badge renderer
		for (int i = 1; i < COLS.length; i++) {
		    table.getColumnModel().getColumn(i).setCellRenderer(
		        i == 5 ? statusRenderer : rowRenderer
		    );
		}

        // ── Rounded card wrapper (File 2 style) ────────────────────────────────
        JPanel card = new JPanel(new BorderLayout()) {
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

        JScrollPane scroll = UIComponent.createAppleScrollPane(table);
        scroll.getViewport().setBackground(AppTheme.BG_CARD);
        card.add(scroll, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Load / filter table data (File 1 format) ─────────────────────────────
    // customer.txt: UserID|username|password|contactNum|email|role|status|createdDate

    private void loadTableData(String query) {
        tableModel.setRowCount(0);
        List<String[]> customers = customerDAO.getAllCustomers();
        String q = query.toLowerCase().trim();

        for (String[] c : customers) {
            if (c.length < 8) continue;
            String id          = c[0];
            String username    = c[1];
            String contactNum  = c[3];
            String email       = c[4];
            String status      = c[6];
            String createdDate = c[7];

            boolean matches = q.isEmpty()
                || id.toLowerCase().contains(q)
                || username.toLowerCase().contains(q)
                || email.toLowerCase().contains(q);

            if (matches) {
                tableModel.addRow(new Object[]{
                    Boolean.FALSE, id, username, email, contactNum, status, createdDate
                });
            }
        }
    }

    // ─── Delete selected (File 1 logic, File 2 style) ─────────────────────────

    private void handleDeleteSelected() {
        List<String> toDelete = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(tableModel.getValueAt(i, 0))) {
                toDelete.add((String) tableModel.getValueAt(i, 1));
            }
        }

        if (toDelete.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please select at least one customer to delete.",
                "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete " + toDelete.size() + " selected customer(s)?\nThis action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        int failed = 0;
        for (String id : toDelete) {
            if (!customerDAO.deleteCustomerById(id)) failed++;
        }

        if (failed > 0) {
            JOptionPane.showMessageDialog(this,
                failed + " customer(s) could not be deleted.",
                "Partial Failure", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                toDelete.size() + " customer(s) deleted successfully.",
                "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }

        searchField.setText("");
        loadTableData("");
    }

    // ─── Add Customer dialog (File 2 form style) ──────────────────────────────

    private void showAddCustomerDialog() {
        Window ancestor = SwingUtilities.getWindowAncestor(this);
        Frame owner = (ancestor instanceof Frame) ? (Frame) ancestor : null;
        JDialog dialog = new JDialog(owner, "Add New Customer", true);
        dialog.setSize(480, 590);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(AppTheme.BG_PAGE);
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));

        // Title + subtitle
        JLabel dlgTitle = UIComponent.createPageTitle("Add New Customer");
        dlgTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        root.add(dlgTitle);

        JLabel dlgSubtitle = UIComponent.createCaptionLabel("Fill in the details below to register a new customer.");
        dlgSubtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        root.add(dlgSubtitle);

        // Form fields
        JLabel lblName    = UIComponent.createFieldLabel("USERNAME");
        JTextField tfName = UIComponent.createTextField("Full name");

        JLabel lblEmail    = UIComponent.createFieldLabel("EMAIL");
        JTextField tfEmail = UIComponent.createTextField("Email address");

        JLabel lblPhone    = UIComponent.createFieldLabel("CONTACT NO.");
        JTextField tfPhone = UIComponent.createTextField("e.g. 0123456789");

        JLabel lblPass     = UIComponent.createFieldLabel("PASSWORD");
        JPasswordField tfPass = UIComponent.createPasswordField();

        JLabel lblConfirm  = UIComponent.createFieldLabel("CONFIRM PASSWORD");
        JPasswordField tfConfirm = UIComponent.createPasswordField();

        // Status dropdown (kept from File 1)
        JLabel lblStatus = UIComponent.createFieldLabel("STATUS");
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Pending", "Approved"});
        cbStatus.setFont(AppTheme.FONT_BODY);
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(AppTheme.FONT_SMALL);
        errLabel.setForeground(AppTheme.DANGER);
        errLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel[]     labels = { lblName, lblEmail, lblPhone, lblPass, lblConfirm, lblStatus };
        JComponent[] fields = { tfName,  tfEmail,  tfPhone,  tfPass,  tfConfirm,  cbStatus  };

        for (int i = 0; i < labels.length; i++) {
            root.add(labels[i]);
            root.add(Box.createRigidArea(new Dimension(0, 5)));
            root.add(fields[i]);
            root.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        root.add(errLabel);
        root.add(Box.createRigidArea(new Dimension(0, 16)));

        // Buttons
        JButton btnSave   = UIComponent.createPrimaryButton("Register Customer");
        JButton btnCancel = UIComponent.createOutlineButton("Cancel");

        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnCancel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCancel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        btnCancel.addActionListener(e -> dialog.dispose());

        btnSave.addActionListener(e -> {
            String username = tfName.getText().trim();
            String email    = tfEmail.getText().trim();
            String contact  = tfPhone.getText().trim();
            String pass     = new String(tfPass.getPassword()).trim();
            String conf     = new String(tfConfirm.getPassword()).trim();
            String status   = (String) cbStatus.getSelectedItem();

            CustomerRegister reg = new CustomerRegister(username, contact, email, pass, conf);
            String validationErr = reg.validate();
            if (validationErr != null) {
                errLabel.setText(validationErr);
                return;
            }

            String result = customerDAO.registerCustomer(username, pass, contact, email, status);
            if (result != null && result.toLowerCase().contains("success")) {
                JOptionPane.showMessageDialog(dialog,
                    "Customer \"" + username + "\" registered successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                searchField.setText("");
                loadTableData("");
            } else {
                errLabel.setText(result != null ? result : "Registration failed.");
            }
        });

        root.add(btnSave);
        root.add(Box.createRigidArea(new Dimension(0, 8)));
        root.add(btnCancel);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }
}