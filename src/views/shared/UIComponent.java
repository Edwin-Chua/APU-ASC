package views.shared;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class UIComponent {

    private UIComponent() {}

    // ─── Buttons ─────────────────────────────────────────────────────────────

    /** Filled blue button — primary action (e.g. Login, Save, Confirm). */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
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
        btn.setMaximumSize(AppTheme.BUTTON_SIZE);
        btn.setPreferredSize(AppTheme.BUTTON_SIZE);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
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

    /** White background, blue border button — secondary action (e.g. Cancel, Back). */
    public static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_BUTTON * 2, AppTheme.RADIUS_BUTTON * 2);
                g2.setColor(AppTheme.PRIMARY);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_BUTTON * 2, AppTheme.RADIUS_BUTTON * 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.FONT_SMALL_BOLD);
        btn.setForeground(AppTheme.PRIMARY);
        btn.setBackground(AppTheme.BG_CARD);
        btn.setMaximumSize(AppTheme.BUTTON_SIZE);
        btn.setPreferredSize(AppTheme.BUTTON_SIZE);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(AppTheme.PRIMARY_LIGHT); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(AppTheme.BG_CARD); }
        });
        return btn;
    }

    /** Small filled red button — destructive action (e.g. Delete). */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
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
        btn.setBackground(AppTheme.DANGER);
        btn.setMaximumSize(AppTheme.BUTTON_SM);
        btn.setPreferredSize(AppTheme.BUTTON_SM);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(AppTheme.DANGER_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(AppTheme.DANGER); }
        });
        return btn;
    }

    /** Small outline button — used in table rows / cards (e.g. Edit, View). */
    public static JButton createSmallButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_BUTTON * 2, AppTheme.RADIUS_BUTTON * 2);
                g2.setColor(AppTheme.BORDER_DEFAULT);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_BUTTON * 2, AppTheme.RADIUS_BUTTON * 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(AppTheme.FONT_CAPTION);
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setBackground(AppTheme.BG_CARD);
        btn.setMaximumSize(AppTheme.BUTTON_SM);
        btn.setPreferredSize(AppTheme.BUTTON_SM);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(AppTheme.BG_PAGE); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(AppTheme.BG_CARD); }
        });
        return btn;
    }

    /** Inline text link — no background, underlines on hover. */
    public static JButton createLinkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(AppTheme.FONT_SMALL);
        btn.setForeground(AppTheme.TEXT_LINK);
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

    // ─── Sidebar nav buttons ──────────────────────────────────────────────────

    /** Full-width sidebar nav item. Use setNavActive() to toggle active state. */
    public static JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 42));
        btn.setMinimumSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 42));
        btn.setPreferredSize(new Dimension(AppTheme.SIDEBAR_WIDTH, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(AppTheme.SIDEBAR_BG);
        btn.setForeground(AppTheme.TEXT_PRIMARY);
        btn.setFont(AppTheme.FONT_SIDEBAR_ITEM);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!AppTheme.SIDEBAR_ACTIVE_BG.equals(btn.getBackground())) {
                    btn.setBackground(AppTheme.SIDEBAR_HOVER_BG);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!AppTheme.SIDEBAR_ACTIVE_BG.equals(btn.getBackground())) {
                    btn.setBackground(AppTheme.SIDEBAR_BG);
                }
            }
        });
        return btn;
    }

    /**
     * Switches active state between nav buttons.
     * Call this whenever the user navigates: setNavActive(newBtn, previousBtn)
     */
    public static void setNavActive(JButton selected, JButton previous) {
        if (previous != null) {
            previous.setBackground(AppTheme.SIDEBAR_BG);
            previous.setForeground(AppTheme.TEXT_PRIMARY);
            previous.setFont(AppTheme.FONT_SIDEBAR_ITEM);
            previous.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));
        }
        selected.setBackground(AppTheme.SIDEBAR_ACTIVE_BG);
        selected.setForeground(AppTheme.PRIMARY);
        selected.setFont(AppTheme.FONT_SIDEBAR_BOLD);
        selected.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, AppTheme.NAV_INDICATOR_WIDTH, 0, 0, AppTheme.SIDEBAR_ACTIVE_INDICATOR),
            BorderFactory.createEmptyBorder(0, 14, 0, 0)
        ));
    }

    // ─── Cards ────────────────────────────────────────────────────────────────

    /** Rounded white card with subtle border. Use as a container panel. */
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.setColor(AppTheme.BORDER_CARD);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD,
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD
        ));
        return card;
    }

    /** Card with a tinted background — useful for info/summary panels. */
    public static JPanel createTintedCard(Color bg, Color border) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD,
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD
        ));
        return card;
    }

    // ─── Form fields ──────────────────────────────────────────────────────────

    /** Rounded text field with placeholder-style appearance. */
    public static JTextField createTextField(String placeholder) {
        JTextField field = new JTextField() {
        	@Override
        	protected void paintComponent(Graphics g) {
        	    Graphics2D g2 = (Graphics2D) g.create();
        	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        	    g2.setColor(getBackground());
        	    g2.fillRoundRect(0, 0, getWidth(), getHeight(),
        	            AppTheme.RADIUS_FIELD * 2, AppTheme.RADIUS_FIELD * 2);
        	    g2.dispose();
        	    super.paintComponent(g);
        	}
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? AppTheme.BORDER_FOCUS : AppTheme.BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3,
                        AppTheme.RADIUS_FIELD * 2, AppTheme.RADIUS_FIELD * 2);
                g2.dispose();
            }
        };
        field.setFont(AppTheme.FONT_SMALL);
        field.setForeground(AppTheme.TEXT_PRIMARY);
        field.setBackground(AppTheme.BG_FIELD);
        field.setPreferredSize(AppTheme.FIELD_SIZE);
        field.setMaximumSize(AppTheme.FIELD_SIZE);
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        field.setOpaque(false);
        field.setToolTipText(placeholder);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { field.repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent e)   { field.repaint(); }
        });
        return field;
    }

    /** Rounded password field — same style as createTextField. */
    public static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField() {
        	@Override
        	protected void paintComponent(Graphics g) {
        	    Graphics2D g2 = (Graphics2D) g.create();
        	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        	    g2.setColor(getBackground());
        	    g2.fillRoundRect(0, 0, getWidth(), getHeight(),
        	            AppTheme.RADIUS_FIELD * 2, AppTheme.RADIUS_FIELD * 2);
        	    g2.dispose();
        	    super.paintComponent(g);
        	}
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isFocusOwner() ? AppTheme.BORDER_FOCUS : AppTheme.BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3,
                        AppTheme.RADIUS_FIELD * 2, AppTheme.RADIUS_FIELD * 2);
                g2.dispose();
            }
        };
        field.setFont(AppTheme.FONT_SMALL);
        field.setForeground(AppTheme.TEXT_PRIMARY);
        field.setBackground(AppTheme.BG_FIELD);
        field.setPreferredSize(AppTheme.FIELD_SIZE);
        field.setMaximumSize(AppTheme.FIELD_SIZE);
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        field.setOpaque(false);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { field.repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent e)   { field.repaint(); }
        });
        return field;
    }

    /** Bold uppercase label above a form field. */
    public static JLabel createFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_LABEL);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // ─── Labels ───────────────────────────────────────────────────────────────

    /** Page title — largest heading used at the top of a content panel. */
    public static JLabel createPageTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_HEADING_2);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /** Section heading — used above a group of cards or a table. */
    public static JLabel createSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_BODY_BOLD);
        lbl.setForeground(AppTheme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.GAP_ITEM, 0));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return lbl;
    }

    /** Muted caption — used for subtitles, hints, timestamps. */
    public static JLabel createCaptionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AppTheme.FONT_CAPTION);
        lbl.setForeground(AppTheme.TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // ─── Status badge ─────────────────────────────────────────────────────────

    /**
     * Pill-shaped colored badge for status values.
     * Recognized values: "Completed", "Pending", "Cancelled", "In Progress".
     * Any other value gets a neutral grey badge.
     */
    public static JLabel createStatusBadge(String status) {
        JLabel badge = new JLabel(status, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_BADGE, AppTheme.RADIUS_BADGE);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(AppTheme.FONT_LABEL);
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));

        switch (status.toLowerCase()) {
            case "completed":
                badge.setBackground(AppTheme.STATUS_COMPLETED_BG);
                badge.setForeground(AppTheme.STATUS_COMPLETED_FG);
                break;
            case "pending":
                badge.setBackground(AppTheme.STATUS_PENDING_BG);
                badge.setForeground(AppTheme.STATUS_PENDING_FG);
                break;
            case "cancelled":
                badge.setBackground(AppTheme.STATUS_CANCELLED_BG);
                badge.setForeground(AppTheme.STATUS_CANCELLED_FG);
                break;
            case "in progress":
                badge.setBackground(AppTheme.STATUS_INPROGRESS_BG);
                badge.setForeground(AppTheme.STATUS_INPROGRESS_FG);
                break;
            default:
                badge.setBackground(new Color(230, 230, 235));
                badge.setForeground(AppTheme.TEXT_SECONDARY);
        }
        return badge;
    }

    // ─── Card rows (Apple-style info list) ────────────────────────────────────

    /**
     * A single label + value row inside a card.
     * Label is fixed width (INFO_LABEL_WIDTH), value is right-aligned.
     */
    public static JPanel createInfoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_ROW, AppTheme.PADDING_CARD,
            AppTheme.PADDING_ROW, AppTheme.PADDING_CARD
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(AppTheme.INFO_LABEL_WIDTH, 20));

        JLabel val = new JLabel(value);
        val.setFont(AppTheme.FONT_SMALL);
        val.setForeground(AppTheme.TEXT_PRIMARY);
        val.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    /**
     * A single label + custom component row inside a card.
     * Use when the value is an editable field, badge, or button instead of plain text.
     */
    public static JPanel createInfoRow(String label, JComponent valueComponent) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_ROW, AppTheme.PADDING_CARD,
            AppTheme.PADDING_ROW, AppTheme.PADDING_CARD
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.FONT_SMALL);
        lbl.setForeground(AppTheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(AppTheme.INFO_LABEL_WIDTH, 20));

        row.add(lbl, BorderLayout.WEST);
        row.add(valueComponent, BorderLayout.CENTER);
        return row;
    }

    /** Horizontal inset divider line — separates rows inside a card. */
    public static JPanel createRowDivider() {
        JPanel line = new JPanel();
        line.setBackground(AppTheme.SEPARATOR);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(0, 1));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, AppTheme.PADDING_CARD, 0, 0));
        wrapper.add(line, BorderLayout.CENTER);
        return wrapper;
    }

    // ─── Spacing helpers ──────────────────────────────────────────────────────

    /** Vertical rigid spacer. Equivalent to Box.createRigidArea but named clearly. */
    public static Component verticalGap(int height) {
        return Box.createRigidArea(new Dimension(0, height));
    }

    /** Full-width horizontal separator line (between sections, not inside cards). */
    public static JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(AppTheme.SEPARATOR);
        return sep;
    }

    // ─── Profile components ───────────────────────────────────────────────────

    /**
     * Circular avatar panel that paints initials from the given label's text.
     * Pass a JLabel whose text you update separately; call repaint() on the
     * returned panel after updating the label.
     */
    public static JPanel createAvatarCircle(JLabel initialsLabel) {
        Color avatarBg = new Color(0, 122, 255);
        JPanel circle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 80;
                int cx = getWidth() / 2;
                g2.setColor(avatarBg);
                g2.fillOval(cx - size / 2, 4, size, size);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 30));
                FontMetrics fm = g2.getFontMetrics();
                String text = initialsLabel.getText();
                if (text == null) text = "";
                g2.drawString(text,
                    cx - fm.stringWidth(text) / 2,
                    4 + (size + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        circle.setOpaque(false);
        circle.setPreferredSize(new Dimension(0, 92));
        circle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        circle.setAlignmentX(Component.LEFT_ALIGNMENT);
        return circle;
    }

    /**
     * An action row inside a card: title + subtitle stacked on the left,
     * a button on the right. Used for "Edit Profile", "Change Password", etc.
     */
    public static JPanel createActionRow(String title, String subtitle, JButton btn) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_ROW, AppTheme.PADDING_CARD,
            AppTheme.PADDING_ROW, AppTheme.PADDING_CARD
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(AppTheme.FONT_SMALL);
        titleLbl.setForeground(AppTheme.TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(AppTheme.FONT_CAPTION);
        subLbl.setForeground(AppTheme.TEXT_MUTED);

        textBlock.add(titleLbl);
        textBlock.add(Box.createRigidArea(new Dimension(0, 2)));
        textBlock.add(subLbl);

        row.add(textBlock, BorderLayout.CENTER);
        row.add(btn, BorderLayout.EAST);
        return row;
    }

    public static JScrollPane createAppleScrollPane(JComponent content) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(AppTheme.BG_CONTENT);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
    
        JScrollBar vsb = scroll.getVerticalScrollBar();
        vsb.setOpaque(false);
        vsb.setPreferredSize(new Dimension(AppTheme.SCROLL_BAR_WIDTH, Integer.MAX_VALUE));
        vsb.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            private boolean hover = false;
    
            @Override protected void configureScrollBarColors() {}
    
            @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
    
            private JButton zeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                btn.setMinimumSize(new Dimension(0, 0));
                btn.setMaximumSize(new Dimension(0, 0));
                return btn;
            }
    
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                // transparent track for cleaner Apple look
            }
    
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r == null || r.width <= 0 || r.height <= 0) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? AppTheme.SCROLL_THUMB_HOVER : AppTheme.SCROLL_THUMB);
    
                int pad = 1;
                g2.fillRoundRect(
                    r.x + pad, r.y + pad,
                    r.width - pad * 2, r.height - pad * 2,
                    AppTheme.SCROLL_THUMB_RADIUS, AppTheme.SCROLL_THUMB_RADIUS
                );
                g2.dispose();
            }
    
            @Override
            protected void setThumbBounds(int x, int y, int width, int height) {
                super.setThumbBounds(x, y, width, height);
                scrollbar.repaint();
            }
    
            @Override
            protected TrackListener createTrackListener() {
                return new TrackListener() {
                    @Override public void mouseEntered(MouseEvent e) { hover = true; scrollbar.repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hover = false; scrollbar.repaint(); }
                    @Override public void mouseMoved(MouseEvent e)   { scrollbar.repaint(); }
                };
            }
        });
    
        return scroll;
    }
    
    // ─── Combo box ────────────────────────────────────────────────────────────
    
    /**
     * Rounded combo box styled to match createTextField.
     * Border is grey at rest and turns blue (AppTheme.PRIMARY) on focus.
     * The popup list uses the same font and Apple-style selection highlight.
     *
     * Usage:
     *   JComboBox<String> combo = UIComponent.createComboBox(
     *       new String[]{"Option A", "Option B", "Option C"});
     */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items) {
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean focused = isFocusOwner()
                        || (getEditor() != null && getEditor().getEditorComponent().isFocusOwner());
                g2.setColor(focused ? AppTheme.BORDER_FOCUS : AppTheme.BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3,
                        AppTheme.RADIUS_FIELD * 2, AppTheme.RADIUS_FIELD * 2);
                g2.dispose();
            }
 
            @Override
            protected void paintComponent(Graphics g) {
                // paint rounded background
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(AppTheme.BG_FIELD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(),
                        AppTheme.RADIUS_FIELD * 2, AppTheme.RADIUS_FIELD * 2);
                g2.dispose();
                super.paintComponent(g);
            }
        };
 
        combo.setFont(AppTheme.FONT_SMALL);
        combo.setForeground(AppTheme.TEXT_PRIMARY);
        combo.setBackground(AppTheme.BG_FIELD);
        combo.setPreferredSize(AppTheme.FIELD_SIZE);
        combo.setMaximumSize(AppTheme.FIELD_SIZE);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setFocusable(true);
        combo.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
 
        // ── Custom UI: hide the default button, draw our own chevron ──────────
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
 
            @Override
            protected JButton createArrowButton() {
                JButton arrow = new JButton() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        // draw a blue up/down chevron (↕) centred in the button
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(AppTheme.PRIMARY);
                        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 
                        int cx = getWidth() / 2;
                        int cy = getHeight() / 2;
 
                        // up caret
                        int[] upX = {cx - 4, cx, cx + 4};
                        int[] upY = {cy - 1, cy - 4, cy - 1};
                        g2.drawPolyline(upX, upY, 3);
 
                        // down caret
                        int[] dnX = {cx - 4, cx, cx + 4};
                        int[] dnY = {cy + 1, cy + 4, cy + 1};
                        g2.drawPolyline(dnX, dnY, 3);
 
                        g2.dispose();
                    }
                };
                arrow.setOpaque(false);
                arrow.setContentAreaFilled(false);
                arrow.setBorderPainted(false);
                arrow.setFocusPainted(false);
                arrow.setPreferredSize(new Dimension(32, AppTheme.FIELD_SIZE.height));
                arrow.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return arrow;
            }
 
            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                // repaint border when focus moves in/out of the combo or its editor
                comboBox.addFocusListener(new java.awt.event.FocusAdapter() {
                    @Override public void focusGained(java.awt.event.FocusEvent e) { comboBox.repaint(); }
                    @Override public void focusLost(java.awt.event.FocusEvent e)   { comboBox.repaint(); }
                });
            }
        });
 
        // ── Popup list styling ────────────────────────────────────────────────
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
 
                JLabel lbl = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                lbl.setFont(AppTheme.FONT_SMALL);
                lbl.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
 
                if (isSelected) {
                    lbl.setBackground(AppTheme.STATUS_INPROGRESS_BG); // light blue
                    lbl.setForeground(AppTheme.TEXT_PRIMARY);
                } else {
                    lbl.setBackground(AppTheme.BG_CARD);
                    lbl.setForeground(AppTheme.TEXT_PRIMARY);
                }
                return lbl;
            }
        });
 
        // Style the popup panel itself
        Object popup = combo.getUI().getAccessibleChild(combo, 0);
        if (popup instanceof javax.swing.plaf.basic.BasicComboPopup) {
            javax.swing.plaf.basic.BasicComboPopup bp =
                    (javax.swing.plaf.basic.BasicComboPopup) popup;
            bp.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(AppTheme.BORDER_DEFAULT, 1),
                    BorderFactory.createEmptyBorder(4, 0, 4, 0)
            ));
            bp.getList().setBackground(AppTheme.BG_CARD);
            bp.getList().setSelectionBackground(AppTheme.STATUS_INPROGRESS_BG);
            bp.getList().setSelectionForeground(AppTheme.TEXT_PRIMARY);
        }
 
        return combo;
    }
    
    public static JTextArea createTextArea(String placeholder, int rows) {
        JTextArea area = new JTextArea(placeholder, rows, 0) {
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean focused = isFocusOwner();
                g2.setColor(focused ? AppTheme.BORDER_FOCUS : AppTheme.BORDER_DEFAULT);
                g2.setStroke(new BasicStroke(focused ? 2f : 1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3,
                        AppTheme.RADIUS_FIELD * 2, AppTheme.RADIUS_FIELD * 2);
                g2.dispose();
            }
        };
 
        area.setFont(AppTheme.FONT_SMALL);
        area.setForeground(AppTheme.TEXT_MUTED);
        area.setBackground(AppTheme.BG_FIELD);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setOpaque(true);
        area.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
 
        area.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (area.getText().equals(placeholder)) {
                    area.setText("");
                    area.setForeground(AppTheme.TEXT_PRIMARY);
                }
                area.repaint();
            }
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (area.getText().trim().isEmpty()) {
                    area.setText(placeholder);
                    area.setForeground(AppTheme.TEXT_MUTED);
                }
                area.repaint();
            }
        });
 
        return area;
    }

    // ─── Date Picker ──────────────────────────────────────────────────────────

    /**
     * Creates a date picker field. Clicking opens a calendar popup.
     *
     * Usage:
     *   DatePickerField picker = UIComponent.createDatePicker();
     *   DatePickerField picker = UIComponent.createDatePicker(LocalDate.of(2026, 1, 1));
     *
     *   picker.getText()                          // "dd/MM/yyyy" string
     *   picker.getDate()                          // LocalDate
     *   picker.setDate(LocalDate.now())           // set programmatically
     *   picker.addDateChangeListener(d -> ...)    // listen for changes
     */
    public static DatePickerField createDatePicker() {
        return new DatePickerField(LocalDate.now());
    }

    public static DatePickerField createDatePicker(LocalDate initialDate) {
        return new DatePickerField(initialDate);
    }

    // ─── DatePickerField ──────────────────────────────────────────────────────
 
    public static class DatePickerField extends JPanel {
 
        public interface DateChangeListener {
            void onDateChanged(LocalDate date);
        }
 
        private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
 
        private LocalDate selectedDate;
        private final JTextField textField;
        private DateChangeListener changeListener;
 
        public DatePickerField(LocalDate initialDate) {
            this.selectedDate = initialDate;
            setLayout(new BorderLayout(0, 0));
            setOpaque(false);
 
            // Plain text field — no icon, just click to open
            textField = createTextField("Select date...");
            textField.setText(selectedDate != null ? selectedDate.format(FMT) : "");
            textField.setEditable(false);
            textField.setCursor(new Cursor(Cursor.HAND_CURSOR));
            textField.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { openPopup(); }
            });
 
            add(textField, BorderLayout.CENTER);
        }
 
        // ── Public API ────────────────────────────────────────────────────────
 
        /** Returns the selected date as dd/MM/yyyy, or empty string if none */
        public String getText() { return textField.getText(); }
 
        /** Returns the selected date as a LocalDate, or null if none */
        public LocalDate getDate() { return selectedDate; }
 
        /** Sets the date and updates the field; pass null to clear */
        public void setDate(LocalDate date) {
            this.selectedDate = date;
            textField.setText(date != null ? date.format(FMT) : "");
            if (changeListener != null) changeListener.onDateChanged(date);
        }
 
        /** Sets the date from a dd/MM/yyyy string; ignores invalid input */
        public void setText(String ddmmyyyy) {
            try { setDate(LocalDate.parse(ddmmyyyy.trim(), FMT)); }
            catch (Exception ignored) {}
        }
 
        /** Register a listener called whenever the user picks a date */
        public void addDateChangeListener(DateChangeListener listener) {
            this.changeListener = listener;
        }
 
        // ── Popup ─────────────────────────────────────────────────────────────
 
        private void openPopup() {
            Window owner = SwingUtilities.getWindowAncestor(this);
            JDialog popup = new JDialog(owner, Dialog.ModalityType.MODELESS);
            popup.setUndecorated(true);
            popup.setBackground(new Color(0, 0, 0, 0));

            // Remember date before opening so we can revert on outside-click dismiss
            LocalDate dateBeforeOpen = selectedDate;
            // Flag: true once user picks a date — prevents windowLostFocus from reverting
            boolean[] datePicked = {false};

            CalendarPanel cal = new CalendarPanel(
                selectedDate != null ? selectedDate : LocalDate.now(), picked -> {
                datePicked[0] = true;
                setDate(picked);
                popup.dispose();
            });

            // Revert only if the user dismissed without picking (clicked outside)
            popup.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
                @Override public void windowGainedFocus(java.awt.event.WindowEvent e) {}
                @Override public void windowLostFocus(java.awt.event.WindowEvent e) {
                    if (!datePicked[0]) {
                        selectedDate = dateBeforeOpen;
                        textField.setText(dateBeforeOpen != null ? dateBeforeOpen.format(FMT) : "");
                    }
                    popup.dispose();
                }
            });

            popup.setContentPane(cal);
            popup.pack();
 
            try {
                Point loc = getLocationOnScreen();
                popup.setLocation(loc.x, loc.y + getHeight() + 4);
            } catch (Exception e) {
                popup.setLocationRelativeTo(this);
            }
            popup.setVisible(true);
        }
    }

    // ─── CalendarPanel ────────────────────────────────────────────────────────

    private static class CalendarPanel extends JPanel {

        interface Callback { void onSelected(LocalDate date); }

        private LocalDate viewMonth;
        private final LocalDate selectedDate;
        private final Callback callback;
        private JLabel monthLabel;
        private JPanel daysGrid;

        CalendarPanel(LocalDate initial, Callback callback) {
            this.selectedDate = initial;
            this.viewMonth    = initial.withDayOfMonth(1);
            this.callback     = callback;

            setLayout(new BorderLayout(0, 0));
            setOpaque(false); // transparent so rounded bg shows

            daysGrid = new JPanel(new GridLayout(0, 7, 6, 6));
            daysGrid.setOpaque(false);

            JPanel content = new JPanel(new BorderLayout(0, 8));
            content.setOpaque(false);
            content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
            content.add(buildNav(),      BorderLayout.NORTH);
            content.add(buildDayNames(), BorderLayout.CENTER);
            content.add(daysGrid,        BorderLayout.SOUTH);

            add(content, BorderLayout.CENTER);

            refreshDays();
        }

        // Rounded white card with shadow-like border
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(AppTheme.BG_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
            g2.setColor(AppTheme.BORDER_DEFAULT);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, AppTheme.RADIUS_CARD * 2, AppTheme.RADIUS_CARD * 2);
            g2.dispose();
        }

        private JPanel buildNav() {
            JPanel nav = new JPanel(new BorderLayout(0, 0));
            nav.setOpaque(false);
            nav.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

            JButton prev = navBtn("‹");
            JButton next = navBtn("›");

            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setFont(AppTheme.FONT_SMALL_BOLD);
            monthLabel.setForeground(AppTheme.TEXT_PRIMARY);

            prev.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { viewMonth = viewMonth.minusMonths(1); refreshDays(); }
            });
            next.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { viewMonth = viewMonth.plusMonths(1); refreshDays(); }
            });

            nav.add(prev,       BorderLayout.WEST);
            nav.add(monthLabel, BorderLayout.CENTER);
            nav.add(next,       BorderLayout.EAST);
            return nav;
        }

        private JPanel buildDayNames() {
            JPanel p = new JPanel(new GridLayout(1, 7, 6, 0));
            p.setOpaque(false);
            p.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
            for (String d : new String[]{"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"}) {
                JLabel lbl = new JLabel(d, SwingConstants.CENTER);
                lbl.setFont(AppTheme.FONT_CAPTION);
                lbl.setForeground(AppTheme.TEXT_SECONDARY);
                p.add(lbl);
            }
            return p;
        }

        private void refreshDays() {
            monthLabel.setText(viewMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
            daysGrid.removeAll();

            int firstDow    = viewMonth.getDayOfWeek().getValue() % 7; // Sun=0
            int daysInMonth = viewMonth.lengthOfMonth();

            for (int i = 0; i < firstDow; i++) {
                JLabel empty = new JLabel("");
                empty.setPreferredSize(new Dimension(32, 32));
                daysGrid.add(empty);
            }

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date     = viewMonth.withDayOfMonth(day);
                boolean isSelected = date.equals(selectedDate);
                boolean isToday    = date.equals(LocalDate.now());
                final int d = day;

                // Use JLabel for rendering — avoids JButton's internal clipping/truncation
                JLabel btn = new JLabel(String.valueOf(d), SwingConstants.CENTER) {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                        if (isSelected) {
                            g2.setColor(AppTheme.PRIMARY);
                            g2.fillOval(0, 0, getWidth(), getHeight());
                        } else if (isToday) {
                            g2.setColor(AppTheme.PRIMARY);
                            g2.setStroke(new BasicStroke(1.5f));
                            g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
                        }
                        g2.dispose();
                        super.paintComponent(g);
                    }
                };

                btn.setFont(AppTheme.FONT_SMALL);
                btn.setForeground(isSelected ? AppTheme.TEXT_ON_PRIMARY
                    : isToday    ? AppTheme.PRIMARY
                    :              AppTheme.TEXT_PRIMARY);
                btn.setOpaque(false);
                btn.setPreferredSize(new Dimension(32, 32));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                // Hover effect
                btn.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        if (!isSelected) btn.setForeground(AppTheme.PRIMARY);
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        btn.setForeground(isSelected ? AppTheme.TEXT_ON_PRIMARY
                            : isToday ? AppTheme.PRIMARY : AppTheme.TEXT_PRIMARY);
                    }
                    @Override public void mouseClicked(MouseEvent e) {
                        callback.onSelected(date);
                    }
                });

                daysGrid.add(btn);
            }

            daysGrid.revalidate();
            daysGrid.repaint();
            revalidate();
            repaint();
        }

        private JButton navBtn(String text) {
            JButton btn = new JButton("<html>" + text + "</html>");
            btn.setFont(AppTheme.FONT_BODY_BOLD);
            btn.setForeground(AppTheme.PRIMARY);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setOpaque(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(32, 28));
            btn.setMargin(new Insets(0, 0, 0, 0));
            return btn;
        }
    }

}