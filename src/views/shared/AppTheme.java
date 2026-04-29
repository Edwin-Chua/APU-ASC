package views.shared;

import java.awt.*;

public final class AppTheme {

    private AppTheme() {}

    // ─── Brand / Accent ──────────────────────────────────────────────────────
    public static final Color PRIMARY          = new Color(0, 122, 255);   // Apple accent blue
    public static final Color PRIMARY_HOVER    = new Color(0, 105, 217);
    public static final Color PRIMARY_LIGHT    = new Color(0, 122, 255, 20); // tinted bg on hover

    // ─── Semantic colors ─────────────────────────────────────────────────────
    public static final Color SUCCESS          = new Color(40, 167, 80);
    public static final Color WARNING          = new Color(180, 120, 0);
    public static final Color DANGER           = new Color(255, 59, 48);   // Apple red
    public static final Color DANGER_HOVER     = new Color(220, 40, 30);

    // ─── Background colors ───────────────────────────────────────────────────
    public static final Color BG_PAGE          = new Color(242, 242, 247); // Apple systemGroupedBackground
    public static final Color BG_CONTENT       = new Color(242, 242, 247);
    public static final Color BG_CARD          = Color.WHITE;
    public static final Color BG_HEADER        = Color.WHITE;
    public static final Color BG_FIELD         = new Color(250, 250, 252);

    // ─── Sidebar colors ──────────────────────────────────────────────────────
    public static final Color SIDEBAR_BG       = new Color(242, 242, 247); // light Apple sidebar
    public static final Color SIDEBAR_ACTIVE_BG  = new Color(232, 240, 255);  // opaque light blue
    public static final Color SIDEBAR_ACTIVE_INDICATOR = PRIMARY;
    public static final Color SIDEBAR_HOVER_BG   = new Color(235, 235, 240);  // opaque light grey
    public static final Color SIDEBAR_DIVIDER  = new Color(220, 220, 225);

    // ─── Text colors ─────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY     = new Color(28, 28, 30);    // Apple label
    public static final Color TEXT_SECONDARY   = new Color(134, 134, 139); // Apple secondaryLabel
    public static final Color TEXT_MUTED       = new Color(174, 174, 178); // Apple tertiaryLabel
    public static final Color TEXT_DARK        = new Color(28, 28, 30);
    public static final Color TEXT_ON_PRIMARY  = Color.WHITE;
    public static final Color TEXT_DANGER      = DANGER;
    public static final Color TEXT_SUCCESS     = SUCCESS;
    public static final Color TEXT_WARNING     = WARNING;
    public static final Color TEXT_LINK        = PRIMARY;

    // ─── Border / separator colors ───────────────────────────────────────────
    public static final Color BORDER_DEFAULT   = new Color(210, 210, 215);
    public static final Color BORDER_FOCUS     = PRIMARY;
    public static final Color BORDER_CARD      = new Color(230, 230, 235);
    public static final Color BORDER_HEADER    = new Color(230, 230, 235);
    public static final Color SEPARATOR        = new Color(230, 230, 235);

    // ─── Shadow ──────────────────────────────────────────────────────────────
    public static final Color SHADOW           = new Color(0, 0, 0, 18);

    // ─── Status badge colors ─────────────────────────────────────────────────
    public static final Color STATUS_COMPLETED_BG   = new Color(209, 244, 223);
    public static final Color STATUS_COMPLETED_FG   = new Color(22, 120, 60);
    public static final Color STATUS_PENDING_BG     = new Color(255, 243, 205);
    public static final Color STATUS_PENDING_FG     = new Color(140, 90, 0);
    public static final Color STATUS_CANCELLED_BG   = new Color(255, 218, 215);
    public static final Color STATUS_CANCELLED_FG   = new Color(180, 30, 20);
    public static final Color STATUS_INPROGRESS_BG  = new Color(209, 231, 255);
    public static final Color STATUS_INPROGRESS_FG  = new Color(0, 80, 180);

    // ─── Fonts ───────────────────────────────────────────────────────────────
    // SansSerif is a generic family — Java maps it to the cleanest available font
    public static final Font FONT_TITLE        = new Font("SansSerif", Font.BOLD,  28);
    public static final Font FONT_HEADING_1    = new Font("SansSerif", Font.BOLD,  24);
    public static final Font FONT_HEADING_2    = new Font("SansSerif", Font.BOLD,  20);
    public static final Font FONT_HEADING_3    = new Font("SansSerif", Font.BOLD,  17);
    public static final Font FONT_BODY         = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD    = new Font("SansSerif", Font.BOLD,  14);
    public static final Font FONT_SMALL        = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL_BOLD   = new Font("SansSerif", Font.BOLD,  13);
    public static final Font FONT_CAPTION      = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_LABEL        = new Font("SansSerif", Font.BOLD,  11);
    public static final Font FONT_SIDEBAR_ITEM = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SIDEBAR_BOLD = new Font("SansSerif", Font.BOLD,  13);
    public static final Font FONT_SIDEBAR_SECTION = new Font("SansSerif", Font.BOLD, 10);

    // ─── Corner radius ───────────────────────────────────────────────────────
    public static final int RADIUS_CARD        = 16;
    public static final int RADIUS_BUTTON      = 10;
    public static final int RADIUS_FIELD       = 10;
    public static final int RADIUS_BADGE       = 20;

    // ─── Dimensions ──────────────────────────────────────────────────────────
    public static final Dimension FIELD_SIZE   = new Dimension(380, 38);
    public static final Dimension BUTTON_SIZE  = new Dimension(380, 40);
    public static final Dimension BUTTON_SM    = new Dimension(140, 34);
    public static final int SIDEBAR_WIDTH      = 220;
    public static final int HEADER_HEIGHT      = 56;
    public static final int STATUS_BAR_HEIGHT  = 30;
    public static final int ROW_HEIGHT_TABLE   = 38;

    // ─── Spacing / padding ───────────────────────────────────────────────────
    public static final int PADDING_PAGE          = 32;   // outer page padding (top/left/right/bottom)
    public static final int PADDING_CARD          = 20;   // inner card content padding
    public static final int PADDING_ROW           = 13;   // info row top/bottom padding
    public static final int GAP_SECTION           = 24;   // vertical gap between sections
    public static final int GAP_ITEM              = 12;   // vertical gap between list items

    // ─── Component-specific constants ────────────────────────────────────────
    public static final int INFO_LABEL_WIDTH      = 140;  // fixed width for label column in info rows
    public static final int NAV_INDICATOR_WIDTH   = 4;    // left border thickness for active nav item

    // ─── Scrollbar (Apple-like) ───────────────────────────────────────────────
    public static final Color SCROLL_TRACK        = new Color(0, 0, 0, 0);      // transparent
    public static final Color SCROLL_THUMB        = new Color(60, 60, 67, 70);  // subtle gray
    public static final Color SCROLL_THUMB_HOVER  = new Color(60, 60, 67, 110); // darker on hover
    public static final int   SCROLL_BAR_WIDTH    = 8;
    public static final int   SCROLL_THUMB_RADIUS = 8;
}