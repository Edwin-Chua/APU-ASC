package views.manager;

import views.MainFrame;
import views.shared.AppTheme;
import views.shared.UIComponent;

import javax.swing.*;
import java.awt.*;

public class MonthlyReportsPage extends JPanel {

    public MonthlyReportsPage(MainFrame frame) {
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

        JLabel title = UIComponent.createPageTitle("Monthly Reports");
        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(AppTheme.BG_CONTENT);
        body.setBorder(BorderFactory.createEmptyBorder(
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE,
            AppTheme.PADDING_PAGE, AppTheme.PADDING_PAGE
        ));

        JLabel placeholder = new JLabel("Monthly reports coming soon.", SwingConstants.CENTER);
        placeholder.setFont(AppTheme.FONT_BODY);
        placeholder.setForeground(AppTheme.TEXT_MUTED);
        body.add(placeholder, BorderLayout.CENTER);
        return body;
    }
}