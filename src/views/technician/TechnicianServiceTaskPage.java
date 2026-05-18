package views.technician;

import models.dao.AppointmentDAO;
import models.entities.Appointment;
import models.entities.User;
import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TechnicianServiceTaskPage extends JPanel {

    private final MainFrame frame;
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private User currentUser;

    private JComboBox<String> appointmentCombo;
    private JTextArea feedbackArea;
    private JLabel lblAppointmentInfo;

    public TechnicianServiceTaskPage(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(AppTheme.BG_CONTENT);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AppTheme.BG_HEADER);
        header.setPreferredSize(new Dimension(0, AppTheme.HEADER_HEIGHT));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, AppTheme.BORDER_HEADER),
            BorderFactory.createEmptyBorder(0, AppTheme.PADDING_PAGE, 0, AppTheme.PADDING_PAGE)
        ));

        header.add(UIComponent.createPageTitle("Service Task"), BorderLayout.WEST);
        return header;
    }

    private JScrollPane buildBody() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppTheme.BG_CONTENT);
        content.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, 80, AppTheme.PADDING_PAGE, 80
        ));

        JLabel subtitle = new JLabel("Update assigned appointment status and leave service feedback.");
        subtitle.setFont(AppTheme.FONT_CAPTION);
        subtitle.setForeground(AppTheme.TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(subtitle);
        content.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        content.add(UIComponent.createSectionLabel("UPDATE APPOINTMENT STATUS"));

        JPanel card = UIComponent.createCard();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360)); 
        appointmentCombo = UIComponent.createComboBox(new String[0]);
        appointmentCombo.setPreferredSize(new Dimension(520, 44));
        appointmentCombo.setMaximumSize(new Dimension(520, 44));
        appointmentCombo.addActionListener(e -> updateSelectedAppointmentInfo());

        card.add(UIComponent.createInfoRow("Ongoing Appointment", appointmentCombo));
        card.add(UIComponent.createRowDivider());

        lblAppointmentInfo = new JLabel("Select an appointment to view details.");
        lblAppointmentInfo.setFont(AppTheme.FONT_SMALL);
        lblAppointmentInfo.setForeground(AppTheme.TEXT_PRIMARY);
        card.add(UIComponent.createInfoRow("Appointment Details", lblAppointmentInfo));
        card.add(UIComponent.createRowDivider());

        feedbackArea = UIComponent.createTextArea("Enter service notes for customer...", 8);
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setMargin(new Insets(12, 12, 12, 12));
        feedbackArea.setFont(AppTheme.FONT_SMALL);

        JScrollPane feedbackScroll = new JScrollPane(feedbackArea);
        feedbackScroll.setPreferredSize(new Dimension(0, 200));
        feedbackScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        feedbackScroll.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER_CARD, 1));
        feedbackScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(UIComponent.createInfoRow("Service Feedback", feedbackScroll));
        card.add(Box.createVerticalStrut(12));

        content.add(card);
        content.add(UIComponent.verticalGap(AppTheme.GAP_SECTION));

        JButton btnComplete = UIComponent.createPrimaryButton("Mark as Completed");
        btnComplete.setPreferredSize(new Dimension(200, 44));
        btnComplete.setMaximumSize(new Dimension(200, 44));
        btnComplete.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnComplete.addActionListener(e -> markAppointmentCompleted());

        content.add(btnComplete);

        return UIComponent.createAppleScrollPane(content);
    }

    public void loadUser(User user) {
        this.currentUser = user;
        loadOngoingAppointments();
    }

    private void loadOngoingAppointments() {
        appointmentCombo.removeAllItems();
        appointmentCombo.addItem("-- Select Appointment --");

        if (currentUser == null) return;

        List<Appointment> appointments =
            appointmentDAO.getAppointmentsByTechnicianId(currentUser.getUserId());

        for (Appointment a : appointments) {
            if ("Approved".equalsIgnoreCase(a.getStatus())) {
                appointmentCombo.addItem(
                    a.getAppointmentId() + " — " +
                    a.getAppointmentDate() + " " +
                    a.getTimeSlot() + " — " +
                    a.getVehicleBrand() + " " +
                    a.getVehicleModel()
                );
            }
        }
    }

    private void updateSelectedAppointmentInfo() {
        if (appointmentCombo == null || lblAppointmentInfo == null) return;

        String selected = (String) appointmentCombo.getSelectedItem();

        if (selected == null || selected.startsWith("--")) {
            lblAppointmentInfo.setText("Select an appointment to view details.");
            return;
        }

        String appointmentId = selected.split(" — ")[0].trim();
        Appointment a = appointmentDAO.getAppointmentById(appointmentId);
        if (a == null) {
            lblAppointmentInfo.setText("Appointment not found.");
            return;
        }

        lblAppointmentInfo.setText(
            "<html>" +
            "Customer: " + a.getCustomerId() + "<br>" +
            "Vehicle: " + a.getVehicleBrand() + " " + a.getVehicleModel() + "<br>" +
            "Plate: " + a.getVehiclePlate() + "<br>" +
            "Service Item: " + a.getServiceItemId() +
            "</html>"
        );
    }

    private void markAppointmentCompleted() {
        String selected = (String) appointmentCombo.getSelectedItem();

        if (selected == null || selected.startsWith("--")) {
            JOptionPane.showMessageDialog(frame,
                "Please select an appointment.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String feedback = feedbackArea.getText().trim();

        if (feedback.isEmpty() || feedback.equals("Enter service notes for customer...")) {
            JOptionPane.showMessageDialog(frame,
                "Please enter service feedback.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (feedback.contains("|")) {
            JOptionPane.showMessageDialog(frame,
                "Feedback cannot contain '|'.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String appointmentId = selected.split(" — ")[0].trim();

        int confirm = JOptionPane.showConfirmDialog(frame,
            "Mark appointment " + appointmentId + " as completed?",
            "Confirm Completion",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = appointmentDAO.completeAppointmentWithFeedback(appointmentId, feedback);

        if (success) {
            JOptionPane.showMessageDialog(frame,
                "Appointment completed successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            feedbackArea.setText("");
            loadOngoingAppointments();
            lblAppointmentInfo.setText("Select an appointment to view details.");

        } else {
            JOptionPane.showMessageDialog(frame,
                "Failed to update appointment.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}