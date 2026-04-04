package views.manager;

import models.entities.User;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import java.awt.*;

public class ManagerProfilePage extends JPanel {

    private JLabel lblUserId;
    private JLabel lblUsername;
    private JLabel lblRole;
    private JLabel lblCreatedDate;

    public ManagerProfilePage(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_CONTENT);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(),   BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BG_HEADER);
        header.setPreferredSize(new Dimension(0, AppTheme.HEADER_HEIGHT));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_HEADER),
            BorderFactory.createEmptyBorder(0, AppTheme.PADDING_PAGE, 0, AppTheme.PADDING_PAGE)
        ));

        JLabel title = UIComponent.createPageTitle("Profile");
        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(AppTheme.BG_CONTENT);

        JPanel card = buildProfileCard();
        body.add(card);
        return body;
    }

    private JPanel buildProfileCard() {
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
        card.setPreferredSize(new Dimension(480, 320));

        // ── Account info section ──────────────────────────────────────────────
        JLabel sectionInfo = UIComponent.createSectionLabel("Account Information");
        sectionInfo.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_CARD, AppTheme.PADDING_CARD, 8, AppTheme.PADDING_CARD));
        sectionInfo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(sectionInfo);

        lblUserId = new JLabel("-");
        card.add(UIComponent.createInfoRow("User ID", lblUserId));
        card.add(UIComponent.createRowDivider());

        lblUsername = new JLabel("-");
        card.add(UIComponent.createInfoRow("Username", lblUsername));
        card.add(UIComponent.createRowDivider());

        lblRole = new JLabel("-");
        card.add(UIComponent.createInfoRow("Role", lblRole));
        card.add(UIComponent.createRowDivider());

        lblCreatedDate = new JLabel("-");
        card.add(UIComponent.createInfoRow("Member Since", lblCreatedDate));

        card.add(Box.createRigidArea(new Dimension(0, AppTheme.PADDING_CARD)));

        return card;
    }

    /** Called by ManagerDashboard.refreshUser() whenever the manager logs in. */
    public void loadUser(User user) {
        lblUserId.setText(user.getUserId());
        lblUsername.setText(user.getUsername());
        lblRole.setText(user.getRole());
        lblCreatedDate.setText(user.getCreatedDate());
    }
}