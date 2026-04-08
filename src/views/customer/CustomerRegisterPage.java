package views.customer;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import models.customer_function.CustomerDAO;
import models.entities.CustomerRegister;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

public class CustomerRegisterPage extends JPanel {

    private final MainFrame frame;
    private final CustomerDAO customerDAO = new CustomerDAO();

    private JTextField txtName;
    private JTextField txtContact;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;

    public CustomerRegisterPage(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_PAGE);
        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildContent(),  BorderLayout.CENTER);
    }

    // ─── Top bar with Back button ─────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 12));
        bar.setBackground(AppTheme.BG_PAGE);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_HEADER));

        JButton backBtn = new JButton("← Back") {
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
        backBtn.setFont(AppTheme.FONT_SMALL_BOLD);
        backBtn.setForeground(AppTheme.PRIMARY);
        backBtn.setBackground(AppTheme.PRIMARY_LIGHT);
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setOpaque(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(110, 34));
        backBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { backBtn.setBackground(new Color(0, 122, 255, 35)); }
            @Override public void mouseExited(MouseEvent e)  { backBtn.setBackground(AppTheme.PRIMARY_LIGHT); }
        });
        backBtn.addActionListener(e -> frame.showPage(MainFrame.PAGE_LOGIN));

        bar.add(backBtn);
        return bar;
    }

    // ─── Main content: image left + form right ────────────────────────────────

    private JPanel buildContent() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(AppTheme.BG_PAGE);

        // Outer card
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
        card.setLayout(new GridLayout(1, 2, 0, 0));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(860, 560));

        card.add(buildImagePanel());
        card.add(buildFormPanel());

        content.add(card);
        return content;
    }

    // ─── Left: illustration ───────────────────────────────────────────────────

    private JPanel buildImagePanel() {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Rounded only on the left two corners
                g2.setColor(AppTheme.PRIMARY_LIGHT);
                g2.fillRoundRect(0, 0, getWidth() + AppTheme.RADIUS_CARD,
                        getHeight(), AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.dispose();
            }
        };
        panel.setOpaque(false);

        java.net.URL imgUrl = getClass().getResource("/images/register.png");
        if (imgUrl != null) {
            Image img = new ImageIcon(imgUrl).getImage()
                    .getScaledInstance(320, 320, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(img));
            panel.add(imgLabel);
        } else {
            JLabel fallback = new JLabel("APU-ASC", SwingConstants.CENTER);
            fallback.setFont(AppTheme.FONT_HEADING_1);
            fallback.setForeground(AppTheme.PRIMARY);
            panel.add(fallback);
        }

        return panel;
    }

    // ─── Right: form ──────────────────────────────────────────────────────────

    private JPanel buildFormPanel() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(40, 36, 40, 40));

        // Title
        JLabel title = new JLabel("Register An Account");
        title.setFont(AppTheme.FONT_HEADING_2);
        title.setForeground(AppTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);

        JLabel subtitle = new JLabel("Fill in your details to create a customer account.");
        subtitle.setFont(AppTheme.FONT_CAPTION);
        subtitle.setForeground(AppTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(subtitle);

        form.add(UIComponent.verticalGap(24));

        // Name
        form.add(fieldLabel("Name"));
        form.add(UIComponent.verticalGap(5));
        txtName = stretchedField("Enter your full name");
        form.add(txtName);

        form.add(UIComponent.verticalGap(14));

        // Contact
        form.add(fieldLabel("Contact Number"));
        form.add(UIComponent.verticalGap(5));
        txtContact = stretchedField("e.g. 0123456789");
        form.add(txtContact);

        form.add(UIComponent.verticalGap(14));

        // Email
        form.add(fieldLabel("Email Address"));
        form.add(UIComponent.verticalGap(5));
        txtEmail = stretchedField("e.g. you@email.com");
        form.add(txtEmail);

        form.add(UIComponent.verticalGap(14));

        // Password
        form.add(fieldLabel("Password"));
        form.add(UIComponent.verticalGap(5));
        txtPassword = stretchedPasswordField();
        form.add(txtPassword);

        form.add(UIComponent.verticalGap(14));

        // Confirm Password
        form.add(fieldLabel("Confirm Password"));
        form.add(UIComponent.verticalGap(5));
        txtConfirmPassword = stretchedPasswordField();
        form.add(txtConfirmPassword);

        form.add(UIComponent.verticalGap(28));

        // Submit button
        JButton submitBtn = buildSubmitButton();
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(submitBtn);

        return form;
    }

    // ─── Field helpers ────────────────────────────────────────────────────────

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_SMALL_BOLD);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        lbl.setHorizontalAlignment(SwingConstants.LEFT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField stretchedField(String placeholder) {
        JTextField field = UIComponent.createTextField(placeholder);
        field.setPreferredSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JPasswordField stretchedPasswordField() {
        JPasswordField field = UIComponent.createPasswordField();
        field.setPreferredSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    // ─── Submit button ────────────────────────────────────────────────────────

    private JButton buildSubmitButton() {
        JButton btn = new JButton("Submit") {
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
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(AppTheme.PRIMARY_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(AppTheme.PRIMARY); }
        });
        btn.addActionListener(e -> handleSubmit());
        return btn;
    }

    // ─── Logic ────────────────────────────────────────────────────────────────

    private void handleSubmit() {
        String name     = txtName.getText().trim();
        String contact  = txtContact.getText().trim();
        String email    = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String confirm  = new String(txtConfirmPassword.getPassword()).trim();

        CustomerRegister registration = new CustomerRegister(name, contact, email, password, confirm);
        String error = registration.register(customerDAO);
        if (error != null) {
            JOptionPane.showMessageDialog(frame, error,
                "Registration Failed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(frame,
            "Account created successfully!\n" +
            "Status: Pending — a manager must approve your account before you can sign in.",
            "Registration Successful", JOptionPane.INFORMATION_MESSAGE);

        clearFields();
        frame.showPage(MainFrame.PAGE_LOGIN);
    }

    private void clearFields() {
        txtName.setText("");
        txtContact.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        txtConfirmPassword.setText("");
    }
}