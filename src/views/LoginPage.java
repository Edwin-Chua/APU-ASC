package views;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import models.dao.UserDAO;
import models.entities.User;
import views.shared.AppTheme;
import views.shared.UIComponent;

public class LoginPage extends JPanel {

    private final MainFrame frame;
    private final UserDAO userDAO = new UserDAO();

    private JTextField txtEmail;
    private JPasswordField txtPassword;

    private JToggleButton tglManager;
    private JToggleButton tglStaff;
    private JToggleButton tglTechnician;
    private JToggleButton tglCustomer;

    public LoginPage(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PAGE);
        add(buildBackground(), BorderLayout.CENTER);
    }

    private JPanel buildBackground() {
        Image bgImage = loadImage("/images/signin_bg.jpg");

        JPanel bg = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 90));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bg.setOpaque(false);
        bg.setBackground(AppTheme.BG_PAGE);
        bg.add(buildCard());
        return bg;
    }

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.SHADOW);
                g2.fillRoundRect(4, 6, getWidth() - 4, getHeight() - 4,
                        AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4,
                        AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.setColor(AppTheme.BORDER_CARD);
                g2.drawRoundRect(0, 0, getWidth() - 5, getHeight() - 5,
                        AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(520, 560));
        card.setBorder(BorderFactory.createEmptyBorder(36, 40, 32, 40));

        // ── Logo ──────────────────────────────────────────────────────────────
        Image logoRaw = loadImage("/images/oodj_logo.png");
        JLabel logoLabel;
        if (logoRaw != null) {
            Image logoScaled = logoRaw.getScaledInstance(160, 90, Image.SCALE_SMOOTH);
            logoLabel = new JLabel(new ImageIcon(logoScaled), SwingConstants.CENTER);
        } else {
            logoLabel = new JLabel("APU-ASC", SwingConstants.CENTER);
            logoLabel.setFont(AppTheme.FONT_HEADING_1);
            logoLabel.setForeground(AppTheme.PRIMARY);
        }
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logoLabel);

        card.add(UIComponent.verticalGap(10));

        // ── Title ─────────────────────────────────────────────────────────────
        JLabel title = new JLabel("APU Automotive Service Centre", SwingConstants.CENTER);
        title.setFont(AppTheme.FONT_HEADING_3);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(title);

        JLabel subtitle = new JLabel("Sign in to continue", SwingConstants.CENTER);
        subtitle.setFont(AppTheme.FONT_CAPTION);
        subtitle.setForeground(AppTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitle);

        card.add(UIComponent.verticalGap(24));

        // ── Email ─────────────────────────────────────────────────────────────
        JLabel lblEmail = new JLabel("Email");
        lblEmail.setFont(AppTheme.FONT_SMALL_BOLD);
        lblEmail.setForeground(AppTheme.TEXT_PRIMARY);
        lblEmail.setHorizontalAlignment(SwingConstants.LEFT);
        lblEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        lblEmail.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblEmail);

        card.add(UIComponent.verticalGap(6));

        txtEmail = UIComponent.createTextField("Enter email address");
        txtEmail.setPreferredSize(new Dimension(Integer.MAX_VALUE, 44));
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtEmail.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(txtEmail);

        card.add(UIComponent.verticalGap(16));

        // ── Password ──────────────────────────────────────────────────────────
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(AppTheme.FONT_SMALL_BOLD);
        lblPass.setForeground(AppTheme.TEXT_PRIMARY);
        lblPass.setHorizontalAlignment(SwingConstants.LEFT);
        lblPass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        lblPass.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblPass);

        card.add(UIComponent.verticalGap(5));

        txtPassword = UIComponent.createPasswordField();
        txtPassword.setPreferredSize(new Dimension(Integer.MAX_VALUE, 44));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtPassword.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(txtPassword);

        card.add(UIComponent.verticalGap(20));

        // ── Role divider ──────────────────────────────────────────────────────
        card.add(buildRoleDivider());

        card.add(UIComponent.verticalGap(10));

        // ── Role toggle row ───────────────────────────────────────────────────
        card.add(buildRoleRow());

        card.add(UIComponent.verticalGap(20));

        // ── Sign In button ────────────────────────────────────────────────────
        JButton btnSignIn = buildSignInButton();
        btnSignIn.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(btnSignIn);

        card.add(UIComponent.verticalGap(14));

        // ── Register link ─────────────────────────────────────────────────────
        JButton registerLink = UIComponent.createLinkButton("Register an account");
        registerLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerLink.addActionListener(e -> handleRegister());
        card.add(registerLink);

        return card;
    }

    private JPanel buildRoleDivider() {
        JPanel divider = new JPanel();
        divider.setLayout(new GridBagLayout());
        divider.setOpaque(false);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        divider.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 8);

        JSeparator left = new JSeparator();
        left.setForeground(AppTheme.BORDER_DEFAULT);
        gbc.gridx = 0; gbc.weightx = 1.0;
        divider.add(left, gbc);

        JLabel lbl = new JLabel("Select Role");
        lbl.setFont(AppTheme.FONT_CAPTION);
        lbl.setForeground(AppTheme.TEXT_MUTED);
        gbc.gridx = 1; gbc.weightx = 0; gbc.insets = new Insets(0, 8, 0, 8);
        divider.add(lbl, gbc);

        JSeparator right = new JSeparator();
        right.setForeground(AppTheme.BORDER_DEFAULT);
        gbc.gridx = 2; gbc.weightx = 1.0; gbc.insets = new Insets(0, 8, 0, 0);
        divider.add(right, gbc);

        return divider;
    }

    private JPanel buildRoleRow() {
        tglManager    = makeRoleToggle("Manager");
        tglStaff      = makeRoleToggle("Staff");
        tglTechnician = makeRoleToggle("Technician");
        tglCustomer   = makeRoleToggle("Customer");

        ButtonGroup group = new ButtonGroup();
        group.add(tglManager);
        group.add(tglStaff);
        group.add(tglTechnician);
        group.add(tglCustomer);

        tglManager.setSelected(true);
        applyAllRoleStyles();

        javax.swing.event.ChangeListener onChange = e -> applyAllRoleStyles();
        tglManager.addChangeListener(onChange);
        tglStaff.addChangeListener(onChange);
        tglTechnician.addChangeListener(onChange);
        tglCustomer.addChangeListener(onChange);

        JPanel row = new JPanel(new GridLayout(1, 4, 8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.add(tglManager);
        row.add(tglStaff);
        row.add(tglTechnician);
        row.add(tglCustomer);

        return row;
    }

    private JToggleButton makeRoleToggle(String text) {
        JToggleButton btn = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
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
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 40));
        return btn;
    }

    private void applyAllRoleStyles() {
        applyRoleStyle(tglManager);
        applyRoleStyle(tglStaff);
        applyRoleStyle(tglTechnician);
        applyRoleStyle(tglCustomer);
    }

    private void applyRoleStyle(JToggleButton btn) {
        if (btn.isSelected()) {
            btn.setBackground(AppTheme.PRIMARY);
            btn.setForeground(AppTheme.TEXT_ON_PRIMARY);
        } else {
            btn.setBackground(AppTheme.BG_FIELD);
            btn.setForeground(AppTheme.TEXT_SECONDARY);
        }
        btn.repaint();
    }

    private JButton buildSignInButton() {
        JButton btn = new JButton("Sign In") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        AppTheme.RADIUS_BUTTON * 2, AppTheme.RADIUS_BUTTON * 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.FONT_BODY_BOLD);
        btn.setForeground(AppTheme.TEXT_ON_PRIMARY);
        btn.setBackground(AppTheme.PRIMARY);
        btn.setPreferredSize(new Dimension(360, 48));
        btn.setMaximumSize(new Dimension(360, 48));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(AppTheme.PRIMARY_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(AppTheme.PRIMARY); }
        });
        btn.addActionListener(e -> handleSignIn());
        return btn;
    }

    private Image loadImage(String resourcePath) {
        java.net.URL url = getClass().getResource(resourcePath);
        if (url == null) {
            System.err.println("Image not found: " + resourcePath);
            return null;
        }
        return new ImageIcon(url).getImage();
    }

    // ─── Logic ────────────────────────────────────────────────────────────────

    private void handleSignIn() {
        String email    = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String role     = getSelectedRole();

        // ── Validation ────────────────────────────────────────────────────────
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                "Please enter both email and password.",
                "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (role == null) {
            JOptionPane.showMessageDialog(frame,
                "Please select a role.",
                "No Role Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── Customer ──────────────────────────────────────────────────────────
        if (role.equals("Customer")) {
            models.customer_function.CustomerDAO customerDAO =
                new models.customer_function.CustomerDAO();

            models.customer_function.Customer customer =
                customerDAO.getCustomerByLogin(email, password);

            if (customer == null) {
                JOptionPane.showMessageDialog(frame,
                    "Incorrect email or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!"Approved".equalsIgnoreCase(customer.getStatus())) {
                JOptionPane.showMessageDialog(frame,
                    "Your account is pending approval.\nPlease wait for manager approval before logging in.",
                    "Access Denied", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User user = new User(
                customer.getCustomerID(),
                customer.getName(),
                customer.getPassword(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                "Customer",
                "Approved",
                customer.getRegisteredDate().toString()
            );
            frame.setLoggedInUser(user);
            frame.showCustomerPage(customer.getCustomerID());

        // ── Manager ───────────────────────────────────────────────────────────
        } else if (role.equals("Manager")) {
            User user = userDAO.authenticateStaff(email, password, role);

            if (user == null) {
                JOptionPane.showMessageDialog(frame,
                    "Incorrect email or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            frame.setLoggedInUser(user);
            frame.showPage(MainFrame.PAGE_MANAGER);

        // ── CounterStaff ──────────────────────────────────────────────────────
        } else if (role.equals("CounterStaff")) {
            User user = userDAO.authenticateCounterStaffByEmail(email, password);

            if (user == null) {
                JOptionPane.showMessageDialog(frame,
                    "Incorrect email or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            frame.setLoggedInUser(user);
            frame.showPage(MainFrame.PAGE_COUNTERSTAFF);

        // ── Technician ────────────────────────────────────────────────────────
        } else if (role.equals("Technician")) {
            User user = userDAO.authenticateTechnicianByEmail(email, password);

            if (user == null) {
                JOptionPane.showMessageDialog(frame,
                    "Incorrect email or password.",
                    "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            frame.setLoggedInUser(user);
            frame.showPage(MainFrame.PAGE_TECHNICIAN);
        }

        // ── Clear fields ──────────────────────────────────────────────────────
        txtEmail.setText("");
        txtPassword.setText("");
    }

    private String getSelectedRole() {
        if (tglManager.isSelected())    return "Manager";
        if (tglStaff.isSelected())      return "CounterStaff";
        if (tglTechnician.isSelected()) return "Technician";
        if (tglCustomer.isSelected())   return "Customer";
        return null;
    }

    private void handleRegister() {
        frame.showPage(MainFrame.PAGE_REGISTER);
    }

}