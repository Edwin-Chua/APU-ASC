package views.manager;

import models.dao.UserDAO;
import models.entities.User;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import java.awt.*;

public class ManagerProfilePage extends JPanel {

    private static final String CARD_VIEW = "VIEW";
    private static final String CARD_EDIT_PROFILE = "EDIT_PROFILE";
    private static final String CARD_CHANGE_PASSWORD = "CHANGE_PASSWORD";

    private final MainFrame frame;
    private final UserDAO userDAO = new UserDAO();

    private User currentUser;

    // Header/identity
    private final JLabel initialsLabel = new JLabel("");
    private JPanel avatarCircle;
    private JLabel headerName;
    private JLabel headerSub;

    // View mode labels
    private JLabel lblUserId;
    private JLabel lblUsername;
    private JLabel lblRole;
    private JLabel lblCreatedDate;

    // Edit profile fields
    private JTextField editUsername;

    // Change password fields
    private JPasswordField cpCurrent;
    private JPasswordField cpNew;
    private JPasswordField cpConfirm;

    private final CardLayout profileCards = new CardLayout();
    private JPanel cPanel;

    public ManagerProfilePage(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_CONTENT);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildScrollBody(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BG_HEADER);
        header.setPreferredSize(new Dimension(0, AppTheme.HEADER_HEIGHT));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_HEADER),
            BorderFactory.createEmptyBorder(0, AppTheme.PADDING_PAGE, 0, AppTheme.PADDING_PAGE)
        ));
        header.add(UIComponent.createPageTitle("Profile"), BorderLayout.WEST);
        return header;
    }

    private JScrollPane buildScrollBody() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BG_CONTENT);
        content.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, 80, AppTheme.PADDING_PAGE, 80
        ));

        avatarCircle = UIComponent.createAvatarCircle(initialsLabel);
        content.add(avatarCircle);
        content.add(UIComponent.verticalGap(12));

        headerName = new JLabel("-", SwingConstants.CENTER);
        headerName.setFont(AppTheme.FONT_HEADING_2);
        headerName.setForeground(AppTheme.TEXT_PRIMARY);
        headerName.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        headerSub = new JLabel("-", SwingConstants.CENTER);
        headerSub.setFont(AppTheme.FONT_CAPTION);
        headerSub.setForeground(AppTheme.TEXT_MUTED);
        headerSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerSub.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        content.add(headerName);
        content.add(UIComponent.verticalGap(4));
        content.add(headerSub);
        content.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        cPanel = new JPanel(profileCards);
        cPanel.setOpaque(false);
        cPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        cPanel.add(buildViewMode(), CARD_VIEW);
        cPanel.add(buildEditProfileMode(), CARD_EDIT_PROFILE);
        cPanel.add(buildChangePasswordMode(), CARD_CHANGE_PASSWORD);

        content.add(cPanel);

        // Use your shared Apple-like scrollbar. If not added yet, replace with new JScrollPane(content).
        return UIComponent.createAppleScrollPane(content);
    }

    private JPanel buildViewMode() {
        JPanel viewMode = new JPanel();
        viewMode.setLayout(new BoxLayout(viewMode, BoxLayout.Y_AXIS));
        viewMode.setOpaque(false);

        viewMode.add(UIComponent.createSectionLabel("ACCOUNT INFORMATION"));

        JPanel infoCard = UIComponent.createCard();

        lblUserId = new JLabel("-");
        infoCard.add(UIComponent.createInfoRow("User ID", lblUserId));
        infoCard.add(UIComponent.createRowDivider());

        lblUsername = new JLabel("-");
        infoCard.add(UIComponent.createInfoRow("Username", lblUsername));
        infoCard.add(UIComponent.createRowDivider());

        lblRole = new JLabel("-");
        infoCard.add(UIComponent.createInfoRow("Role", lblRole));
        infoCard.add(UIComponent.createRowDivider());

        lblCreatedDate = new JLabel("-");
        infoCard.add(UIComponent.createInfoRow("Member Since", lblCreatedDate));

        viewMode.add(infoCard);
        viewMode.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        viewMode.add(UIComponent.createSectionLabel("ACCOUNT SETTINGS"));

        JButton btnEdit = UIComponent.createLinkButton("Edit");
        JButton btnChangePass = UIComponent.createLinkButton("Change");

        JPanel actCard = UIComponent.createCard();
        actCard.add(UIComponent.createActionRow("Edit Profile", "Update your username", btnEdit));
        actCard.add(UIComponent.createRowDivider());
        actCard.add(UIComponent.createActionRow("Change Password", "Update your account password", btnChangePass));

        viewMode.add(actCard);

        btnEdit.addActionListener(e -> {
            if (currentUser == null) {
                return;
            }
            editUsername.setText(currentUser.getUsername());
            profileCards.show(cPanel, CARD_EDIT_PROFILE);
        });

        btnChangePass.addActionListener(e -> {
            cpCurrent.setText("");
            cpNew.setText("");
            cpConfirm.setText("");
            profileCards.show(cPanel, CARD_CHANGE_PASSWORD);
        });

        return viewMode;
    }

    private JPanel buildEditProfileMode() {
        JPanel editMode = new JPanel();
        editMode.setLayout(new BoxLayout(editMode, BoxLayout.Y_AXIS));
        editMode.setOpaque(false);

        JPanel editTop = new JPanel(new BorderLayout());
        editTop.setOpaque(false);
        editTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        editTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        editTop.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.GAP_ITEM, 0));

        JLabel editTitle = new JLabel("Edit Profile");
        editTitle.setFont(AppTheme.FONT_HEADING_3);
        editTitle.setForeground(AppTheme.TEXT_PRIMARY);

        JButton btnCancel = UIComponent.createLinkButton("Cancel");
        editTop.add(editTitle, BorderLayout.WEST);
        editTop.add(btnCancel, BorderLayout.EAST);
        editMode.add(editTop);

        editMode.add(UIComponent.createSectionLabel("ACCOUNT INFORMATION"));

        JPanel editCard = UIComponent.createCard();

        JLabel editIdVal = new JLabel("-");
        editIdVal.setFont(AppTheme.FONT_SMALL);
        editIdVal.setForeground(AppTheme.TEXT_MUTED);
        editIdVal.setHorizontalAlignment(SwingConstants.RIGHT);
        editCard.add(UIComponent.createInfoRow("User ID", editIdVal));
        editCard.add(UIComponent.createRowDivider());

        editUsername = UIComponent.createTextField("New username");
        editUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        editCard.add(UIComponent.createInfoRow("Username", editUsername));

        editMode.add(editCard);
        editMode.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        JButton btnSave = UIComponent.createPrimaryButton("Save Changes");
        btnSave.setPreferredSize(new Dimension(180, 44));
        btnSave.setMaximumSize(new Dimension(180, 44));
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        editMode.add(btnSave);

        btnCancel.addActionListener(e -> profileCards.show(cPanel, CARD_VIEW));

        btnSave.addActionListener(e -> {
            if (currentUser == null) {
                return;
            }

            String newUsername = editUsername.getText().trim();
            if (newUsername.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Username cannot be empty.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (newUsername.contains("|")) {
                JOptionPane.showMessageDialog(frame, "Username cannot contain '|'.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(frame,
                "Update your username to \"" + newUsername + "\"?",
                "Confirm Update", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            if (userDAO.updateStaffLikeUsername("Manager", currentUser.getUserId(), newUsername)) {
                JOptionPane.showMessageDialog(frame, "Profile updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

                // Keep local user model in sync
                currentUser = new User(
                    currentUser.getUserId(),
                    newUsername,
                    currentUser.getPassword(),
                    currentUser.getContactNum(),
                    currentUser.getEmail(),
                    currentUser.getRole(),
                    currentUser.getStatus(),
                    currentUser.getCreatedDate()
                );
                loadUser(currentUser);

                editIdVal.setText(currentUser.getUserId());
                profileCards.show(cPanel, CARD_VIEW);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to update profile.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Keep User ID shown correctly when entering edit panel
        editMode.addHierarchyListener(e -> {
            if (currentUser != null) {
                editIdVal.setText(currentUser.getUserId());
            }
        });

        return editMode;
    }

    private JPanel buildChangePasswordMode() {
        JPanel cpMode = new JPanel();
        cpMode.setLayout(new BoxLayout(cpMode, BoxLayout.Y_AXIS));
        cpMode.setOpaque(false);

        JPanel cpTop = new JPanel(new BorderLayout());
        cpTop.setOpaque(false);
        cpTop.setAlignmentX(Component.LEFT_ALIGNMENT);
        cpTop.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cpTop.setBorder(BorderFactory.createEmptyBorder(0, 0, AppTheme.GAP_ITEM, 0));

        JLabel cpTitle = new JLabel("Change Password");
        cpTitle.setFont(AppTheme.FONT_HEADING_3);
        cpTitle.setForeground(AppTheme.TEXT_PRIMARY);

        JButton btnCancel = UIComponent.createLinkButton("Cancel");
        cpTop.add(cpTitle, BorderLayout.WEST);
        cpTop.add(btnCancel, BorderLayout.EAST);
        cpMode.add(cpTop);

        cpMode.add(UIComponent.createSectionLabel("SECURITY"));

        JPanel cpCard = UIComponent.createCard();

        cpCurrent = UIComponent.createPasswordField();
        cpCurrent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        cpCard.add(UIComponent.createInfoRow("Current Password", cpCurrent));
        cpCard.add(UIComponent.createRowDivider());

        cpNew = UIComponent.createPasswordField();
        cpNew.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        cpCard.add(UIComponent.createInfoRow("New Password", cpNew));
        cpCard.add(UIComponent.createRowDivider());

        cpConfirm = UIComponent.createPasswordField();
        cpConfirm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        cpCard.add(UIComponent.createInfoRow("Confirm Password", cpConfirm));

        cpMode.add(cpCard);
        cpMode.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        JButton btnSavePassword = UIComponent.createPrimaryButton("Save Password");
        btnSavePassword.setPreferredSize(new Dimension(180, 44));
        btnSavePassword.setMaximumSize(new Dimension(180, 44));
        btnSavePassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        cpMode.add(btnSavePassword);

        btnCancel.addActionListener(e -> profileCards.show(cPanel, CARD_VIEW));

        btnSavePassword.addActionListener(e -> {
            if (currentUser == null) {
                return;
            }

            String op = new String(cpCurrent.getPassword()).trim();
            String np = new String(cpNew.getPassword()).trim();
            String cf = new String(cpConfirm.getPassword()).trim();

            if (!op.equals(currentUser.getPassword())) {
                JOptionPane.showMessageDialog(frame, "Current password is incorrect.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (np.length() < 8 || np.length() > 16) {
                JOptionPane.showMessageDialog(frame, "New password must be 8-16 characters.",
                    "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!np.equals(cf)) {
                JOptionPane.showMessageDialog(frame, "Passwords do not match.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (np.contains("|")) {
                JOptionPane.showMessageDialog(frame, "Password cannot contain '|'.",
                    "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (userDAO.updateStaffLikePassword("Manager", currentUser.getUserId(), np)) {
                JOptionPane.showMessageDialog(frame, "Password changed successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

                // Keep local model updated so next validation uses latest password
                currentUser = new User(
                    currentUser.getUserId(),
                    currentUser.getUsername(),
                    np,
                    currentUser.getContactNum(),
                    currentUser.getEmail(),
                    currentUser.getRole(),
                    currentUser.getStatus(),
                    currentUser.getCreatedDate()
                );
                loadUser(currentUser);

                cpCurrent.setText("");
                cpNew.setText("");
                cpConfirm.setText("");
                profileCards.show(cPanel, CARD_VIEW);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to change password.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return cpMode;
    }

    // Called by ManagerDashboard.refreshUser()
    public void loadUser(User user) {
        this.currentUser = user;

        String name = user.getUsername();
        String initials = (name != null && !name.isEmpty())
            ? String.valueOf(name.charAt(0)).toUpperCase()
            : "?";
        initialsLabel.setText(initials);
        if (avatarCircle != null) {
            avatarCircle.repaint();
        }

        if (lblUserId != null) {
            lblUserId.setText(user.getUserId());
        }
        if (lblUsername != null) {
            lblUsername.setText(user.getUsername());
        }
        if (lblRole != null) {
            lblRole.setText(user.getRole());
        }
        if (lblCreatedDate != null) {
            lblCreatedDate.setText(user.getCreatedDate());
        }

        if (headerName != null) {
            headerName.setText(user.getUsername());
        }
        if (headerSub != null) {
            String date = (user.getCreatedDate() == null || user.getCreatedDate().isBlank())
                ? ""
                : " \u2022 Member since " + user.getCreatedDate();
            headerSub.setText(user.getRole() + date);
        }
    }
}